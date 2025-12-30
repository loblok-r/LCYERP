package cn.loblok.erpcore.task;

import cn.loblok.erpcore.service.Impl.FinanceRiskServiceImpl;
import cn.loblok.erpcore.service.Impl.PayrollDetailServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.YearMonth;

/**
 *
 * 按 HR 配置的 cron 触发，只负责“开启发薪流程”
 *
 */

@Component
@RequiredArgsConstructor
@Slf4j
public class MonthlyPayrollTriggerTask {

    private final PayrollDetailServiceImpl payrollService;

    private final FinanceRiskServiceImpl financeRiskService;

    //以每月5号5：00发薪为例子
    @Scheduled(cron = "${payroll.trigger.cron}") // e.g., "0 0 5 5 * ?"
    public void triggerMonthlyPayroll() {
        String payrollMonth = getMonth();
        Long companyId = getCurrentCompanyId(); // 从上下文获取

        try {
            // 🔒【关键风控点】发薪前资金校验
            if (!financeRiskService.isFundsSufficientForPayroll(companyId, getMonth())) {
                String errorMsg = String.format(
                        "发薪风控拦截：公司 %d 在 %s 月薪资发放资金不足",
                        companyId, payrollMonth
                );
                log.error(errorMsg);
                //alertService.sendCriticalAlert(errorMsg); // 企业微信/邮件通知 CFO
                return; // ⛔ 不激活发薪！
            }

            //资金充足，正常激活
            int count = payrollService.markThisMonthAsPending(payrollMonth, companyId);
            if (count > 0) {
                log.info("已激活 {} 条 {} 月薪资记录进入发薪队列", count, payrollMonth);
            }

        } catch (Exception e) {
            log.error("发薪触发异常", e);
           // alertService.sendCriticalAlert("发薪调度失败，请立即处理！");
        }
    }

    private String getMonth(){
        return YearMonth.now().minusMonths(1).toString(); // 发上个月工资
    }

    private Long getCurrentCompanyId() {
        // Todo 从 SecurityContext / TenantContext 获取
        return 1L; // 简化示例
    }
    //触发薪资计算
    @Scheduled(cron = "0 0 2 1 * ?") // 每月1号凌晨2点
    public void triggerMonthlyCalculation() {
        String payrollMonth = getMonth();
        Long companyId = getCurrentCompanyId(); // 从上下文获取
        // 将本月所有 DRAFT → PENDING_CALCULATION
        payrollService.markThisMonthForCalculation(payrollMonth, companyId);
    }
}