package cn.loblok.erpcore.service;

import cn.loblok.common.Enum.PayrollStatus;
import cn.loblok.common.entity.PayrollDetail;

import java.util.List;

public interface PayrollDetailService {


    void processSingle(PayrollDetail detail);


    void transitionStatus(long id, PayrollStatus newStatus) ;


    List<PayrollDetail> findPendingBatch(long lastId, int limit);

    List<PayrollDetail> findSentRecordsBatch(long lastId, int limit);

    boolean isProcessed(String bizId);

}
