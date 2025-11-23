package cn.loblok.erpcore.task;

import cn.loblok.erpcore.entity.AssetDetail;

import java.util.List;

public interface AssetValidationTask {
    String getSourceName(); // 如 "财务系统"
    boolean validate(List<AssetDetail> assets); // 返回是否通过
}