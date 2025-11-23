package cn.loblok.bankchannelservice.config;

import cn.loblok.bankchannelservice.strategy.IcbcPayStrategy;
import cn.loblok.bankchannelservice.strategy.PayStrategy;
import org.springframework.stereotype.Component;

@Component
public class IcbcBankConfig implements BankConfig{
    @Override
    public String getQueueName() {
        return "icbc.pay.queue";
    }

    @Override
    public String getBankCode() {
        return "ICBC";
    }

    @Override
    public int getConcurrency() {
        return 2;
    }

    @Override
    public PayStrategy getStrategy() {
        return new IcbcPayStrategy();
    }

    @Override
    public boolean isSuccess(String code) {
        if("0000".equals(code) || code.equals("9999")){ //假设9999是临时错误
            return false;
        }
        //其他错误
        return true;
    }
}