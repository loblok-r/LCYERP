package cn.loblok.erpcore.service;

public interface FinanceRiskService {

    boolean isFundsSufficientForPayroll(Long companyId, String payrollMonth);

}
