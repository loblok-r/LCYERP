package cn.loblok.erpcore.manager;

import cn.loblok.erpcore.entity.AssetDetail;
import cn.loblok.erpcore.task.AssetValidationTask;
import cn.loblok.erpcore.task.EquipmentValidationTask;
import cn.loblok.erpcore.task.FinanceValidationTask;
import cn.loblok.erpcore.task.PurchaseValidationTask;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ✅ 关键点：
 *
 * CountDownLatch 等待所有任务完成；
 * AtomicBoolean 共享状态，任一失败即标记整体失败；
 * 超时控制（10s），避免卡死。
 */
@Service
public class AssetValidationCoordinator {

    private final List<AssetValidationTask> validationTasks;
    private final ExecutorService validatorExecutor;

    public AssetValidationCoordinator(
            FinanceValidationTask f,
            PurchaseValidationTask p,
            EquipmentValidationTask e) {
        this.validationTasks = Arrays.asList(f, p, e);
        this.validatorExecutor = Executors.newFixedThreadPool(validationTasks.size());
    }

    public boolean runValidations(List<AssetDetail> assets) {
        CountDownLatch latch = new CountDownLatch(validationTasks.size());
        AtomicBoolean allPassed = new AtomicBoolean(true); // 全局成功标志

        for (AssetValidationTask task : validationTasks) {
            validatorExecutor.submit(() -> {
                try {
                    if (!task.validate(assets)) {
                        System.out.println("❌ [" + task.getSourceName() + "] 校验失败");
                        allPassed.set(false);
                    } else {
                        System.out.println("✅ [" + task.getSourceName() + "] 校验通过");
                    }
                } catch (Exception ex) {
                    System.err.println("⚠️ [" + task.getSourceName() + "] 校验异常: " + ex.getMessage());
                    allPassed.set(false);
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await(10, TimeUnit.SECONDS); // 最多等 10 秒
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        return allPassed.get();
    }
}