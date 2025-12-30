package cn.loblok.erpcore.config;


import cn.loblok.erpcore.event.MessageConfirmEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置
 */
@Configuration
@Slf4j
public class RabbitConfig {


    @Bean
    public RabbitTemplate rabbitTemplate(CachingConnectionFactory connectionFactory, ApplicationEventPublisher eventPublisher) {

        //启用 Publisher Confirm（Spring Boot 3.x 方式）
        connectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);


        RabbitTemplate template = new RabbitTemplate(connectionFactory);

        //  Confirm 回调
        template.setConfirmCallback((correlationData, ack, cause) -> {
            String bizId = (correlationData != null && correlationData.getId() != null)
                    ? correlationData.getId()
                    : "unknown";
            eventPublisher.publishEvent(new MessageConfirmEvent(bizId, ack, cause));
        });


        //  Return 回调（处理 routingKey 无效等情况）
        template.setReturnsCallback(returned -> {
            log.warn("消息无法路由: exchange={}, routingKey={}, replyText={}",
                    returned.getExchange(),
                    returned.getRoutingKey(),
                    returned.getReplyText());
        });

        // 设置 mandatory = true，ReturnCallback 才会触发
        template.setMandatory(true);

        return template;
    }
}