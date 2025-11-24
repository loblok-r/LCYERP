package cn.loblok.bankchannelservice.util;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
@Slf4j
public class RabbitUtil {
    // 安全 ACK 工具方法
    public static void safeAck(Channel channel, long deliveryTag) {
        try {
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            log.error("ACK 失败, deliveryTag={}", deliveryTag, e);
            // 通常无法恢复，但至少不 crash
        }
    }

    // 安全 NACK 工具方法
    public static void safeNack(Channel channel, long deliveryTag, boolean requeue) {
        try {
            channel.basicNack(deliveryTag, false, requeue);
        } catch (IOException e) {
            log.error("NACK 失败, deliveryTag={}, requeue={}", deliveryTag, requeue, e);
        }
    }
}