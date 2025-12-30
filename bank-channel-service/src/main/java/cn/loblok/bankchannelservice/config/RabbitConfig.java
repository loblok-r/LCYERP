package cn.loblok.bankchannelservice.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE_NAME = "salary.pay.exchange";
    public static final String ICBC_QUEUE = "icbc.pay.queue";
    public static final String CMB_QUEUE = "cmb.pay.queue";
    public static final String CCB_QUEUE = "ccb.pay.queue";


    @Bean
    public Exchange payExchange() {
        return new DirectExchange(EXCHANGE_NAME, true, false);
    }



    // ====== 工商银行队列 + DLQ ======
    @Bean
    public Queue icbcPayQueue() {
        return QueueBuilder.durable(ICBC_QUEUE)
                .withArgument("x-dead-letter-exchange", "icbc.dlq.exchange")
                .withArgument("x-dead-letter-routing-key", "icbc.pay.dlq")
                .build();
    }
    //QueueBuilder.durable(...) 等价于 new Queue(name, true, ...)
    //→ durable = true
    @Bean
    public Binding icbcBinding() {
        return BindingBuilder.bind(icbcPayQueue())
                .to(payExchange())
                .with("icbc")// routing key 必须与 erp-core 发送时一致
                .noargs(); // 结束绑定配置
    }

    // --- ICBC 死信队列 ---
    @Bean
    public Queue icbcDlq() {
        return QueueBuilder.durable("icbc.pay.dlq").build();
    }

    // --- ICBC 死信交换机 ---
    @Bean
    public DirectExchange icbcDlx() {
        return new DirectExchange("icbc.dlq.exchange", true, false);
    }

    // --- ICBC DLQ 绑定 ---
    @Bean
    public Binding icbcDlqBinding() {
        return BindingBuilder.bind(icbcDlq())
                .to(icbcDlx())
                .with("icbc.pay.dlq");
    }

    // ====== 招商银行队列 + DLQ ======
    @Bean
    public Queue cmbPayQueue() {
        return QueueBuilder.durable(CMB_QUEUE)
                .withArgument("x-dead-letter-exchange", "cmb.dlq.exchange")
                .withArgument("x-dead-letter-routing-key", "cmb.pay.dlq")
                .build();
    }

    @Bean
    public Binding cmbBinding() {
        return BindingBuilder.bind(cmbPayQueue())
                .to(payExchange())
                .with("cmb")// routing key 必须与 erp-core 发送时一致
                .noargs(); // 结束绑定配置
    }

    @Bean
    public Queue cmbDlq() {
        return QueueBuilder.durable("cmb.pay.dlq").build();
    }

    @Bean
    public DirectExchange cmbDlx() {
        return new DirectExchange("cmb.dlq.exchange");
    }

    @Bean
    public Binding cmbDlqBinding() {
        return BindingBuilder.bind(cmbDlq())
                .to(cmbDlx())
                .with("cmb.pay.dlq");
    }

    // ==============================
    // 4. 建设银行（CCB）相关资源
    // ==============================

    @Bean
    public Queue ccbPayQueue() {
        return QueueBuilder.durable(CCB_QUEUE)
                .withArgument("x-dead-letter-exchange", "ccb.dlq.exchange")
                .withArgument("x-dead-letter-routing-key", "ccb.pay.dlq")
                .build();
    }

    @Bean
    public Binding ccbBinding() {
        return BindingBuilder.bind(ccbPayQueue())
                .to(payExchange())
                .with("ccb")// routing key 必须与 erp-core 发送时一致
                .noargs(); // 结束绑定配置
    }

    // --- CCB 死信交换机 ---
    @Bean
    public DirectExchange ccbDlx() {
        return new DirectExchange("ccb.dlq.exchange", true, false);
    }

    // --- CCB 死信队列 ---
    @Bean
    public Queue ccbDlq() {
        return QueueBuilder.durable("ccb.pay.dlq").build();
    }

    // --- CCB DLQ 绑定 ---
    @Bean
    public Binding ccbDlqBinding() {
        return BindingBuilder.bind(ccbDlq())
                .to(ccbDlx())
                .with("ccb.pay.dlq");
    }

}