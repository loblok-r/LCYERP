package cn.loblok.bankchannelservice.service.impl;

import cn.loblok.bankchannelservice.config.BankConfig;
import cn.loblok.bankchannelservice.config.CcbBankConfig;
import cn.loblok.bankchannelservice.config.CmbBankConfig;
import cn.loblok.bankchannelservice.config.IcbcBankConfig;
import cn.loblok.bankchannelservice.service.GenericBankPayConsumer;
import cn.loblok.common.Enum.PayrollStatus;
import cn.loblok.common.dto.PayRequest;
import cn.loblok.common.dto.PayResult;
import cn.loblok.common.exception.TemporaryBankException;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

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
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            if (callbackService.isProcessed(request.getBizId())) {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                return;
            }

            PayResult result = config.getStrategy().send(request);

            if(config.isSuccess(result.getCode())){
                // 成功：ACK
                callbackService.updateStatus(request.getBizId(), PayrollStatus.SUCCESS);
                channel.basicAck(deliveryTag, false);

            }else if(isPermanentError(config.getBankCode(), result)){
                // 永久失败：拒绝消息 → 进入 DLQ
                log.warn("永久性错误，拒绝消息进入 DLQ, bizId={}", request.getBizId());
                channel.basicNack(deliveryTag, false, false); // requeue = false

            }else{
                // 临时失败：如何处理？
                // 方案A：NACK 重试（RabbitMQ 自动重试）
                // 方案B：发到重试队列（更可控）
//                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true); // requeue=true
//                return;
                // 临时失败：抛异常 → 触发 Spring Retry 机制
                throw new TemporaryBankException("银行临时错误: " + result.getCode());
            }


        } catch (TemporaryBankException e) {
            // 临时异常：重试（由 Spring Retry 控制，或手动 NACK requeue=true）
            log.warn("临时错误，将重试, bizId={}", request.getBizId());
            try {
                channel.basicNack(deliveryTag, false, true); // requeue = true
            } catch (IOException ex) {
                log.error("NACK(requeue=true) 失败", ex);
            }
        } catch (Exception e) {
            // 其他未知异常：视为临时错误，重试
            log.error("未知异常，将重试, bizId={}", request.getBizId(), e);
            try {
                channel.basicNack(deliveryTag, false, true);
            } catch (IOException ex) {
                log.error("NACK(requeue=true) 失败", ex);
            }
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