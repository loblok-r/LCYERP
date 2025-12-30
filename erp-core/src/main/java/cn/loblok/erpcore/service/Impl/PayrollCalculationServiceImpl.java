package cn.loblok.erpcore.service.Impl;

import cn.loblok.common.Enum.PayrollStatus;
import cn.loblok.common.dao.*;
import cn.loblok.common.entity.*;
import cn.loblok.erpcore.service.PayrollCalculationService;
import cn.loblok.erpcore.util.CaculateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
/**
 * 薪资计算实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PayrollCalculationServiceImpl implements PayrollCalculationService {

    private final PayrollDetailRepository payrollDetailRepo;
    private final EmployeeRepository employeeRepo;
    private final AttendanceSummaryRepository attendanceRepo;
    private final SpecialDeductionRepository deductionRepo;
    private final SocialSecurityConfigRepository ssConfigRepo;
    private final CompanyRepository companyRepo;

    // 每30秒扫描一次待计算任务
    @Scheduled(fixedDelay = 30_000)
    @Override
    public void calculatePendingPayrolls() {
        // 获取所有 PENDING_CALCULATION 记录（可分页，本地简单处理）
        List<PayrollDetail> pendingList = payrollDetailRepo
                .findByStatus(PayrollStatus.PENDING_CALCULATION);

        for (PayrollDetail detail : pendingList) {
            try {
                // 尝试抢占：DRAFT/PENDING_CALCULATION → CALCULATING
                if (payrollDetailRepo.updateStatusIfMatch(String.valueOf(detail.getId()), PayrollStatus.PENDING_CALCULATION, PayrollStatus.CALCULATING) == 0) {
                    continue; // 已被其他实例处理
                }

                doCalculate(detail);
                log.info("✅ 薪资计算成功: bizId={}", detail.getBizId());

            } catch (Exception e) {
                log.error("❌ 薪资计算失败: bizId={}", detail.getBizId(), e);
                // 标记为失败（可后续人工干预）
                payrollDetailRepo.updateStatusIfMatch(String.valueOf(detail.getId()), PayrollStatus.CALCULATING, PayrollStatus.CALCULATION_FAILED);
            }
        }
    }

    @Override
    public void doCalculate(PayrollDetail detail) {

        // 1. 加载关联数据
        Employee emp = employeeRepo.findByIdAndCompanyId(
                detail.getEmployeeId(), detail.getCompanyId()
        ).orElseThrow(() -> new RuntimeException("员工不存在")); //对应的员工

        AttendanceSummary att = attendanceRepo.findByEmployeeIdAndPayrollMonth(
                detail.getEmployeeId(), detail.getPayrollMonth()
        ).orElseGet(AttendanceSummary::new); // 默认0  加班，打卡情况

        SpecialDeduction ded = deductionRepo.findByEmployeeIdAndPayrollMonth(
                detail.getEmployeeId(), detail.getPayrollMonth()
        ).orElse(new SpecialDeduction()); //特殊申报 子女教育等

        Company company = companyRepo.findById(emp.getCompanyId())
                .orElseThrow(() -> new RuntimeException("未配置公司")); //对应的公司

        SocialSecurityConfig config = ssConfigRepo.findById(company.getSocialSecurityCity())
                .orElseThrow(() -> new RuntimeException("未配置社保城市: " + company.getSocialSecurityCity()));

        // 2. 计算应发工资（Gross Pay）
        BigDecimal base = emp.getBaseSalary();
        BigDecimal overtimePay = att.getOvertimeHours().multiply(BigDecimal.valueOf(50)); // 50元/小时
        BigDecimal leaveDeduction = att.getLeaveDays().multiply(
                base.divide(BigDecimal.valueOf(21.75), 2, RoundingMode.HALF_UP) // 日薪
        );
        BigDecimal gross = base.add(overtimePay).subtract(leaveDeduction);
        if (gross.compareTo(BigDecimal.ZERO) < 0) gross = BigDecimal.ZERO;

        // 3. 计算社保 & 公积金（个人部分）
        // 社保基数 = min(max(gross, 社保下限), 社保上限)
        BigDecimal ssBase = CaculateUtil.clamp(gross, config.getSocialSecurityBaseMin(), config.getSocialSecurityBaseMax());
        BigDecimal pension = ssBase.multiply(config.getPensionRatioEmployee());
        BigDecimal medical = ssBase.multiply(config.getMedicalRatioEmployee());
        BigDecimal unemployment = ssBase.multiply(config.getUnemploymentRatioEmployee());
        BigDecimal socialSecurity = pension.add(medical).add(unemployment);

        // 公积金基数 = min(max(gross, 公积金下限), 公积金上限)
        BigDecimal hfBase = CaculateUtil.clamp(gross, config.getHousingFundBaseMin(), config.getHousingFundBaseMax());
        BigDecimal housingFund = hfBase.multiply(emp.getHousingFundRatioEmployee());

        // 4. 计算个税
        BigDecimal taxableIncome = gross
                .subtract(socialSecurity)
                .subtract(housingFund)
                .subtract(BigDecimal.valueOf(5000)) // 起征点
                .subtract(ded.getTotalAmount());    // 专项附加扣除

        BigDecimal incomeTax = BigDecimal.ZERO;
        if (taxableIncome.compareTo(BigDecimal.ZERO) > 0) {
            incomeTax = CaculateUtil.calculateProgressiveTax(taxableIncome);
        }

        // 5. 实发工资（Net Pay = Amount）
        BigDecimal netPay = gross
                .subtract(socialSecurity)
                .subtract(housingFund)
                .subtract(incomeTax);

        // 6. 填充结果
        detail.setGrossPay(CaculateUtil.round(gross));
        detail.setSocialSecurityEmployee(CaculateUtil.round(socialSecurity));
        detail.setHousingFundEmployee(CaculateUtil.round(housingFund));
        detail.setIncomeTax(CaculateUtil.round(incomeTax));
        detail.setAmount(CaculateUtil.round(netPay)); // ←←← 关键：amount = netPay

        // 7. 更新状态
        detail.setStatus(PayrollStatus.SCHEDULED);
        payrollDetailRepo.save(detail);

    }
}