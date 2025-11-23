package cn.loblok.bankchannelservice.config;

import cn.loblok.bankchannelservice.strategy.PayStrategy;

public interface BankConfig {
    String getQueueName();      // 队列名
    String getBankCode();       // 银行编码
    int getConcurrency();       // 并发数
    PayStrategy getStrategy();  // 对应策略
    boolean isSuccess(String code); // 成功码判断
}