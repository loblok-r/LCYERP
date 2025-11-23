package cn.loblok.erpcore.task;

import cn.loblok.common.Enum.PayrollStatus;
import cn.loblok.erpcore.entity.PayrollDetail;
import cn.loblok.erpcore.service.Impl.PayrollDetailServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 「模拟」银行异步回调的行为
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReconciliationTask {

    private final PayrollDetailServiceImpl payrollDetailServiceImpl;

    // 每 40 秒模拟一次
//    @Scheduled(fixedDelay = 40_000)
    @Scheduled(cron = "${payroll.reconcile.cron}")
    public void reconcileSentRecords() {
        log.info("🔄 开始对账任务（模拟银行结果查询）...");

        long lastId = 0;
        int batchSize = 100;
        int processed = 0;
        final int MAX_PROCESS = 50_000;

        while (processed < MAX_PROCESS) {

            List<PayrollDetail> batch = payrollDetailServiceImpl.findSentRecordsBatch(lastId, batchSize);
            if (batch.isEmpty()) break;

            for (PayrollDetail detail : batch) {
                // 模拟调用银行查询接口（真实场景应调用 bankClient.query(detail.getBizId())）
                PayrollStatus finalStatus = Math.random() < 0.95 ? PayrollStatus.SUCCESS : PayrollStatus.SUCCESS;
                payrollDetailServiceImpl.transitionStatus(detail.getId(), finalStatus);
                log.info("📨 模拟银行结果: {} -> {}", detail.getBizId(), finalStatus);

                lastId = detail.getId();
                processed++;
            }
        }

        log.info("✅ 对账任务结束，共处理 {} 条 SENT 记录", processed);
    }
}