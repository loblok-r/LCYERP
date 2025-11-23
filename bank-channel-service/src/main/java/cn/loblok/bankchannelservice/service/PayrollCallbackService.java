package cn.loblok.bankchannelservice.service;

import cn.loblok.common.Enum.PayrollStatus;

public interface PayrollCallbackService {

    /**
     * 更新某笔薪资明细的最终状态（SUCCESS / FAILED）
     */
    void updateStatus(String bizId, PayrollStatus status);

    /**
     * 幂等检查：判断该 bizId 是否已被处理过（防止重复消费）
     */
    boolean isProcessed(String bizId);

}
