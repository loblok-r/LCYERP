package cn.loblok.erpcore.task;

import cn.loblok.erpcore.entity.AssetDetail;

import java.util.List;

/**
 * 资产数据源的资产数据验证任务
 */
public interface AssetValidationTask {
    String getSourceName(); // 如 "财务系统"
    boolean validate(List<AssetDetail> assets); // 返回是否通过
}