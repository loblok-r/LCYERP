package cn.loblok.erpcore.service;

import cn.loblok.common.entity.PayrollDetail;

public interface PayrollCalculationService {


    // 每30秒扫描一次待计算任务
    void calculatePendingPayrolls();


    // 计算单个薪资单
    void doCalculate(PayrollDetail detail);
}
