package cn.loblok.bankchannelservice.strategy;


import cn.loblok.common.dto.PayRequest;
import cn.loblok.common.dto.PayResult;

public interface PayStrategy {
    PayResult send(PayRequest request);
}