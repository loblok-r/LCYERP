package cn.loblok.erpcore.task;

import cn.loblok.erpcore.entity.AssetDetail;
import org.springframework.stereotype.Component;

import java.util.List;
/**
 * 设备资产信息校验任务
 */
@Component
public class EquipmentValidationTask implements AssetValidationTask {

    @Override
    public String getSourceName() {
        return "设备系统";
    }

    @Override
    public boolean validate(List<AssetDetail> assets) {
        if (assets == null || assets.isEmpty()) {
            return true;
        }

        return assets.stream().allMatch(asset -> {
            // 1. 状态必须在合法范围内：0-在用, 1-闲置, 2-维修中, 3-报废
            int status = asset.getStatus();
            if (status < 0 || status > 3) {
                return false;
            }

            // 2. 如果状态是“在用”（0），则必须有保管人（custodian 非空）
            if (status == 0) {
                if (asset.getCustodian() == null || asset.getCustodian().trim().isEmpty()) {
                    return false;
                }
            }

            // 3. （可选）位置字段建议非空，但非强制？这里按“建议有”处理，不做强校验
            // 如果业务要求强校验，可取消注释以下逻辑：
            // if (asset.getLocation() == null || asset.getLocation().trim().isEmpty()) {
            //     return false;
            // }

            return true;
        });
    }
}