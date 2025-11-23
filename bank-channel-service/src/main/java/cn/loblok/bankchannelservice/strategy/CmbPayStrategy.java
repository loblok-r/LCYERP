package cn.loblok.bankchannelservice.strategy;

import cn.loblok.common.dto.PayRequest;
import cn.loblok.common.dto.PayResult;
import org.springframework.stereotype.Service;

@Service
public class CmbPayStrategy implements PayStrategy {
    @Override
    public PayResult send(PayRequest request) {
        if (Math.random() < 0.05) {
            return new PayResult("FAIL", "CMB: 余额不足");
        }
        return new PayResult("SUC", "代付成功");
    }
}