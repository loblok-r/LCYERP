package cn.loblok.bankchannelservice.service;

import cn.loblok.bankchannelservice.config.BankConfig;
import cn.loblok.common.dto.PayRequest;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;

public interface GenericBankPayConsumer {

    // 通用处理逻辑
    void processPay(PayRequest request, Channel channel, Message message, BankConfig config) ;

}
