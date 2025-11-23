package cn.loblok.bankchannelservice.config;

import cn.loblok.bankchannelservice.strategy.CcbPayStrategy;
import cn.loblok.bankchannelservice.strategy.PayStrategy;
import org.springframework.stereotype.Component;

@Component
public class CcbBankConfig implements BankConfig{
    @Override
    public String getQueueName() {
        return "ccb.pay.queue";
    }

    @Override
    public String getBankCode() {
        return "CCB";
    }

    @Override
    public int getConcurrency() {
        return 2;
    }

    @Override
    public PayStrategy getStrategy() {
        return new CcbPayStrategy();
    }

    @Override
    public boolean isSuccess(String code) {
        if(code.startsWith("E10")){//如E1001 = 账户冻结（永久）
            return false;
        }
        return true;
    }
}