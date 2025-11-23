package cn.loblok.bankchannelservice.config;

import cn.loblok.bankchannelservice.strategy.CmbPayStrategy;
import cn.loblok.bankchannelservice.strategy.PayStrategy;
import org.springframework.stereotype.Component;

@Component
public class CmbBankConfig implements BankConfig{
    @Override
    public String getQueueName() {
        return "cmb.pay.queue";
    }

    @Override
    public String getBankCode() {
        return "CMB";
    }

    @Override
    public int getConcurrency() {
        return 2;
    }

    @Override
    public PayStrategy getStrategy() {
        return new CmbPayStrategy();
    }

    @Override
    public boolean isSuccess(String code) {
        if("CARD_INVALID".equals(code) || "ACCOUNT_CLOSED".equals(code)){
            return false;
        }
        return true;
    }
}