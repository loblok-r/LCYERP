package cn.loblok.erpcore.entity;

import cn.loblok.common.Enum.ExportStatus;
import lombok.Data;

import java.time.LocalDateTime;

//导出任务实体
@Data
public class ExportTask {
    private String taskId;
    private ExportStatus status = ExportStatus.PENDING;
    private String errorMessage;
    private String outputFile; // 最终文件路径
    private long totalRows;
    private LocalDateTime createTime = LocalDateTime.now();
}