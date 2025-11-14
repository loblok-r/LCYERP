package cn.loblok.lcyerp01.task;

import cn.loblok.lcyerp01.entity.AssetDetail;

import java.util.List;

public interface AssetValidationTask {
    String getSourceName(); // 如 "财务系统"
    boolean validate(List<AssetDetail> assets); // 返回是否通过
}