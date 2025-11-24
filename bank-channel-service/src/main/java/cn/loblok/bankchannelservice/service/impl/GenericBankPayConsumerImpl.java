package cn.loblok.bankchannelservice.service.impl;

import cn.loblok.bankchannelservice.config.BankConfig;
import cn.loblok.bankchannelservice.config.CcbBankConfig;
import cn.loblok.bankchannelservice.config.CmbBankConfig;
import cn.loblok.bankchannelservice.config.IcbcBankConfig;
import cn.loblok.bankchannelservice.service.GenericBankPayConsumer;
import cn.loblok.bankchannelservice.util.RabbitUtil;
import cn.loblok.common.Enum.PayrollStatus;
import cn.loblok.common.dao.PayrollDetailRepository;
import cn.loblok.common.dto.PayRequest;
import cn.loblok.common.dto.PayResult;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GenericBankPayConsumerImpl implements GenericBankPayConsumer {

    @Autowired
    private PayrollCallbackServiceImpl callbackService;
    // 注入配置（用于 SpEL 表达式）
    @Autowired
    private IcbcBankConfig icbcBankConfig;
    @Autowired private CmbBankConfig cmbBankConfig;
    @Autowired private CcbBankConfig ccbBankConfig;
    @Autowired private PayrollDetailRepository payrollDetailRepository;


    // 工行消费者
    @RabbitListener(
            queues = "#{@icbcBankConfig.getQueueName()}",
            concurrency = "#{@icbcBankConfig.getConcurrency()}",
            ackMode = "MANUAL"
    )
    public void handleIcbc(PayRequest request, Channel channel, Message message) {
        processPay(request, channel, message, icbcBankConfig);
    }

    // 招行消费者
    @RabbitListener(
            queues = "#{@cmbBankConfig.getQueueName()}",
            concurrency = "#{@cmbBankConfig.getConcurrency()}",
            ackMode = "MANUAL"
    )
    public void handleCmb(PayRequest request, Channel channel, Message message) {
        processPay(request, channel, message, cmbBankConfig);
    }

    // ccb消费者
    @RabbitListener(
            queues = "#{@ccbBankConfig.getQueueName()}",
            concurrency = "#{@ccbBankConfig.getConcurrency()}",
            ackMode = "MANUAL"
    )
    public void handleCcb(PayRequest request, Channel channel, Message message) {
        processPay(request, channel, message, ccbBankConfig);
    }


    @Override
    public void processPay(PayRequest request, Channel channel, Message message, BankConfig config) {

        String bizId = request.getBizId();
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
            // 使用 JPQL / MyBatis 更新：仅当状态为 SUBMITTED_TO_MQ 时才更新为 PROCESSING
            int updated = payrollDetailRepository.updateStatusIfMatch(
                    bizId,
                    PayrollStatus.SUBMITTED_TO_MQ,
                    PayrollStatus.PROCESSING
            );

            if (updated == 0) {
                // 说明状态已变更（可能是重复消息，或已被其他消费者处理）
                log.info("bizId={} 状态不可变，跳过", bizId);
                channel.basicAck(deliveryTag, false);
                return;
            }

            // === 关键：在这里做有限次、带退避的重试 ===
            int maxRetries = 3;
            for (int attempt = 0; attempt <= maxRetries; attempt++) {
                try {
                    PayResult result = config.getStrategy().send(request);
                    if (config.isSuccess(result.getCode())) {
                        // 成功：ACK
                        callbackService.updateStatus(bizId, PayrollStatus.SUCCESS);
                        channel.basicAck(deliveryTag, false);
                        return;
                    } else if (isPermanentError(config.getBankCode(), result)) {
                        // 永久失败：拒绝消息 → 进入 DLQ
                       // payrollDetailRepository.updateStatus(Long.parseLong(bizId), PayrollStatus.FAILED, 1);
                        log.warn("永久性错误，拒绝消息进入 DLQ, bizId={}", request.getBizId());
                        channel.basicNack(deliveryTag, false, false); // requeue = false
                        return;
                    }
                    // 否则视为临时错误，继续重试
                } catch (Exception e) {
                    log.warn("bizId={}, 第{}次尝试失败: {}", bizId, attempt + 1, e.getMessage());
                }
                // 最后一次不 sleep
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep((long) Math.pow(2, attempt) * 1000); // 1s, 2s, 4s...
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
            // 重试耗尽，仍失败 → 永久失败 or 进 DLQ
            //payrollDetailRepository.updateStatus(Long.parseLong(bizId), PayrollStatus.FAILED, 1);
            channel.basicNack(deliveryTag, false, false);
        }catch (Exception e) {
            // 兜底：如果前面任何地方出错（包括 updateStatus），也要确保 ACK/NACK
            log.error("处理消息异常，deliveryTag={}, bizId={}", deliveryTag, bizId, e);
            RabbitUtil.safeNack(channel, deliveryTag, false); // 进 DLQ
        }
    }


    /**
     * 判断是否为永久性错误（不可重试）
     */
    private boolean isPermanentError(String bankCode, PayResult result) {
        if (result == null) {
            // 网络异常、超时等，视为临时错误
            return false;
        }
        String code = result.getCode();
        return switch (bankCode) {
            case "ICBC" -> !code.equals("0000") && !code.equals("9999"); // 假设 9999 是临时错误（实际需根据银行文档）
            case "CMB"  -> code.equals("CARD_INVALID") || code.equals("ACCOUNT_CLOSED");
            case "CCB"  -> code.startsWith("E10"); // 如 E1001=账户冻结（永久）
            default -> true; // 未知银行视为永久错误
        };
    }

}