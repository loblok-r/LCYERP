package cn.loblok.bankchannelservice.strategy;

import cn.loblok.common.dto.PayRequest;
import cn.loblok.common.dto.PayResult;
import org.springframework.stereotype.Service;

@Service
public class CcbPayStrategy implements PayStrategy {
    @Override
    public PayResult send(PayRequest request) {
        if (Math.random() < 0.05) {
            return new PayResult("E1001", "CCB: 账户冻结");
        }
        return new PayResult("000000", "处理成功");
    }
}