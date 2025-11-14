package cn.loblok.lcyerp01.task;

import cn.loblok.lcyerp01.entity.AssetDetail;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class FinanceValidationTask implements AssetValidationTask {
    @Override
    public String getSourceName() { return "财务系统"; }
    @Override
    public boolean validate(List<AssetDetail> assets) {
        return assets.stream().allMatch(a ->
                a.getOriginalValue().compareTo(BigDecimal.ZERO) > 0 &&
                        (a.getDepreciationMethod() == 1 || a.getDepreciationMethod() == 2)
        );
    }
}

// 类似实现 PurchaseValidationTask、EquipmentValidationTask