package cn.loblok.erpcore.service.Impl;

import cn.loblok.common.entity.PayrollDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 告警服务
 */
@Slf4j
@Service
public class AlertService {

    /**
     * 模拟发送告警（本地测试用日志代替邮件/短信）
     */
    public void notifyFinance(PayrollDetail detail) {
        String message = String.format(
                "🚨【发薪失败告警】bizId=%s, employeeId=%s, bankCard=%s, amount=%s, retryCount=%d",
                detail.getBizId(),
                detail.getEmployeeId(),
                maskBankCard(detail.getBankCard()),
                detail.getAmount(),
                detail.getRetryCount()
        );
        log.error(message);
        // TODO: 实际项目中可接入企业微信、钉钉、邮件、Sentry 等
    }

    /**
     * 脱敏银行卡号（如 6225********1234）
     */
    private String maskBankCard(String card) {
        if (card == null || card.length() < 8) {
            return "****";
        }
        int len = card.length();
        return card.substring(0, 4) + "********" + card.substring(len - 4);
    }

    /**
     * 模拟发送告警（本地测试用日志代替邮件/短信）
     */
    public void notifyOps(String message) {
        log.error(message);
    }
}