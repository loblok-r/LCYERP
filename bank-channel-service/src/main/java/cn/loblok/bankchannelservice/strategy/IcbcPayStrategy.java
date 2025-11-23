package cn.loblok.bankchannelservice.strategy;

import cn.loblok.common.dto.PayRequest;
import cn.loblok.common.dto.PayResult;
import org.springframework.stereotype.Service;

@Service
public class IcbcPayStrategy implements PayStrategy {
    @Override
    public PayResult send(PayRequest request) {
        // 模拟 5% 失败率
        if (Math.random() < 0.05) {
            return new PayResult("9999", "ICBC: 卡号无效");
        }
        return new PayResult("0000", "交易成功");
    }
}