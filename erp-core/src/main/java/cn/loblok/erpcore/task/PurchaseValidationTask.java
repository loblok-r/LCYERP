package cn.loblok.erpcore.task;

import cn.loblok.erpcore.entity.AssetDetail;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class PurchaseValidationTask implements AssetValidationTask {

    @Override
    public String getSourceName() {
        return "采购系统";
    }

    @Override
    public boolean validate(List<AssetDetail> assets) {
        if (assets == null || assets.isEmpty()) {
            return true; // 空数据视为通过（或根据业务决定是否失败）
        }

        LocalDate now = LocalDate.now();

        return assets.stream().allMatch(asset -> {
            // 1. 采购日期不能为 null，且不能晚于今天
            if (asset.getPurchaseDate() == null || asset.getPurchaseDate().isAfter(now)) {
                return false;
            }

            // 2. 供应商名称不能为空（允许空字符串？根据业务，这里要求非空且非纯空白）
            if (asset.getSupplierName() == null || asset.getSupplierName().trim().isEmpty()) {
                return false;
            }

            return true;
        });
    }
}