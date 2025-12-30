package cn.loblok.erpcore.service;

public interface FinanceRiskService {

    /**
     * 薪资资金充足校验
     *
     * @param companyId 公司 ID
     * @param payrollMonth 薪资月份
     * @return 是否充足
     */
    boolean isFundsSufficientForPayroll(Long companyId, String payrollMonth);

}
