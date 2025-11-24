package cn.loblok.erpcore.task;

import cn.loblok.common.entity.PayrollDetail;
import cn.loblok.erpcore.service.Impl.AlertService;
import cn.loblok.erpcore.service.Impl.PayrollDetailServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 负责“执行 + 补偿”
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PayrollDispatchTask {

    private final PayrollDetailServiceImpl payrollDetailServiceImpl;
    private final RedissonClient redissonClient;
    private final AlertService alertService;
    private static final String LOCK_KEY = "lock:payroll_dispatch";
    //private static final int LOCK_EXPIRE_SECONDS = 300; // 5分钟

    // 本地测试：每 20 秒执行一次（生产环境改为 cron）
//    @Scheduled(fixedDelay = 20_000)
    @Scheduled(cron = "${payroll.dispatch.cron}")
    public void dispatchPendingPayrolls() {
        //获取锁对象
        RLock lock = redissonClient.getLock(LOCK_KEY);

        try{
            // 尝试立即获取锁，如果已被占用，立刻返回 false,第二个参数其实是 leaseTime（租期），不是超时时间！
            // 实际等价于 tryLock(0, -1, SECONDS)
            boolean locked = lock.tryLock(0, TimeUnit.SECONDS);
            if(!locked){
                log.warn("🔒 另一个实例正在执行发薪任务，本次跳过");
                return;
            }
            log.info("🔑 成功获取分布式锁，开始执行发薪任务 [task={}]...", System.currentTimeMillis());
            doDispatch();

        }catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("❌ 获取锁时被中断", e);
        } catch (Exception e) {
            log.error("💥 发薪任务执行异常", e);
            alertService.notifyOps("【严重】自动发薪任务崩溃，请立即处理！");
        } finally {
            // Redisson 会自动释放锁（即使没手动 unlock，看门狗也会清理）
            if (lock.isHeldByCurrentThread()) {
                lock.unlock(); // 显式释放更清晰
            }
        }
    }

    private void doDispatch() {
        log.info("🔍 开始自动发薪任务...");
        long lastId = 0;
        int batchSize = 100;
        int processed = 0;
        final int MAX_PROCESS = 50_000; // 防止单次任务过载

        while (processed < MAX_PROCESS) {
            List<PayrollDetail> batch = payrollDetailServiceImpl.findPendingBatch(lastId, batchSize);
            if (batch.isEmpty()) break;

            for (PayrollDetail detail : batch) {
                payrollDetailServiceImpl.processSingle(detail);
                lastId = detail.getId();
                processed++;
            }
        }

        log.info("✅ 自动发薪任务结束，共处理 {} 条记录", processed);
        if (processed >= MAX_PROCESS) {
            alertService.notifyOps("⚠️ 发薪任务达到单次处理上限，可能有积压");
        }
    }
}