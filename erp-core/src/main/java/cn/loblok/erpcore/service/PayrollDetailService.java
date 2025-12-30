package cn.loblok.erpcore.service;

import cn.loblok.common.Enum.PayrollStatus;
import cn.loblok.common.entity.PayrollDetail;

import java.util.List;

public interface PayrollDetailService {


    /**
     * 单个处理
     */
    void processSingle(PayrollDetail detail);

    /**
     * 批量处理
     */
    void transitionStatus(long id, PayrollStatus newStatus) ;

    /**
     * 批量处理
     */
    List<PayrollDetail> findBatchByStatus(PayrollStatus payrollStatus,long lastId, int limit);

    /**
     * 判断是否处理过
     */
    boolean isProcessed(String bizId);

}
