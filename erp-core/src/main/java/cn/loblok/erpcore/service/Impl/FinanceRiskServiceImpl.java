package cn.loblok.erpcore.service.Impl;

import cn.loblok.common.Enum.PayrollStatus;
import cn.loblok.common.dao.PayrollDetailRepository;
import cn.loblok.erpcore.service.FinanceRiskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
public class FinanceRiskServiceImpl implements FinanceRiskService {

    private final PayrollDetailRepository payrollDetailRepo;
    // 可对接真实财务系统，此处模拟

    public boolean isFundsSufficientForPayroll(Long companyId, String payrollMonth) {
        // 1. 计算本月应发工资总额
        BigDecimal totalPayroll = payrollDetailRepo
                .findTotalAmountByStatusAndMonth(PayrollStatus.SCHEDULED, payrollMonth, companyId)
                .orElse(BigDecimal.ZERO);

        if (totalPayroll.compareTo(BigDecimal.ZERO) == 0) {
            return true; // 无薪资记录，跳过
        }

        // 2. 【模拟】调用财务系统检查余额（实际应为 FeignClient 或 MQ）
        BigDecimal availableBalance = getAvailableBalanceFromFinanceSystem(companyId);

        boolean sufficient = availableBalance.compareTo(totalPayroll) >= 0;

        log.info("💰 薪资资金校验 | 公司: {} | 应发: {} | 余额: {} | 充足: {}",
                companyId, totalPayroll, availableBalance, sufficient);

        return sufficient;
    }

    // 模拟：实际应对接 ERP 财务模块或银行接口
    private BigDecimal getAvailableBalanceFromFinanceSystem(Long companyId) {
        // 面试时可以说："这里会调用财务系统的 /api/account/balance 接口"
        return new BigDecimal("3000000.00"); // 假设余额 300 万
    }
}