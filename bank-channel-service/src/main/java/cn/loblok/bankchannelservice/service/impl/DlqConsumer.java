package cn.loblok.bankchannelservice.service.impl;

import cn.loblok.common.Enum.PayrollStatus;
import cn.loblok.common.dto.PayRequest;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class DlqConsumer {

    @Autowired
    private PayrollCallbackServiceImpl callbackService;

    @RabbitListener(queues = "icbc.pay.dlq", ackMode = "MANUAL")
    public void handleIcbcDlq(PayRequest request, Message message, Channel channel) {
        // 工行兜底逻辑
        handleDeadLetter(request, message, channel);
    }

    @RabbitListener(queues = "cmb.pay.dlq", ackMode = "MANUAL")
    public void handleCmbDlq(PayRequest request, Message message, Channel channel) {
        // 招行兜底逻辑
        handleDeadLetter(request, message, channel);
    }

    @RabbitListener(queues = "ccb.pay.dlq", ackMode = "MANUAL")
    public void handleCcbDlq(PayRequest request, Message message, Channel channel) {
        // 建行兜底逻辑
        handleDeadLetter(request, message, channel);
    }

    /**
     * 兜底处理最终失败的消息
     */
    private void handleDeadLetter(PayRequest request, Message message, Channel channel) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String bizId = request.getBizId();
        log.error("💀 消息进入死信队列，bizId={}", bizId);

        try {
            if (!callbackService.isProcessed(bizId)) {
                callbackService.updateStatus(bizId, PayrollStatus.FAILED);
                log.warn("✅ 已强制将 bizId={} 标记为 FAILED", bizId);
            }

            // TODO: 告警通知
            // alertService.notifyFinance(bizId, "薪资代发失败，请人工核查");

            // 手动 ACK：确认处理成功
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("处理 DLQ 消息失败，bizId={}", bizId, e);
            // DLQ 消费必须成功！可记录到数据库供后续补偿
            try {
                // 不 requeue（因为已在 DLQ），也不 ACK → 消息保留在队列中
                // 实际上，在 MANUAL 模式下，不调用 ACK/NACK 就会一直 pending
                // 更安全做法：记录后 ACK，避免队列无限增长
                channel.basicAck(deliveryTag, false); // 或根据策略决定
            } catch (IOException ioEx) {
                log.error("ACK DLQ 消息失败", ioEx);
            }
        }
    }
}