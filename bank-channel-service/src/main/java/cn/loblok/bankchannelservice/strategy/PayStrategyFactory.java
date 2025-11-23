package cn.loblok.bankchannelservice.strategy;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PayStrategyFactory {
    private final Map<String, PayStrategy> strategies = new HashMap<>();

    public PayStrategyFactory(
            IcbcPayStrategy icbc,
            CmbPayStrategy cmb,
            CcbPayStrategy ccb
    ) {
        strategies.put("ICBC", icbc);
        strategies.put("CMB", cmb);
        strategies.put("CCB", ccb);
    }

    public PayStrategy getStrategy(String bankCode) {
        return strategies.get(bankCode);
    }
}