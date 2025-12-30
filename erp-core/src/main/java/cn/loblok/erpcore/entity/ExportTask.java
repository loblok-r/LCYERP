package cn.loblok.erpcore.entity;

import cn.loblok.common.Enum.ExportStatus;
import lombok.Data;

import java.time.LocalDateTime;

//导出任务实体
@Data
public class ExportTask {
    /**
     * 任务ID
     */
    private String taskId;
    /**
     * 任务状态，默认为待处理
     */
    private ExportStatus status = ExportStatus.PENDING;
    /**
     * 错误信息
     */
    private String errorMessage;
    /**
     * 输出文件路径
     */
    private String outputFile; // 最终文件路径
    /**
     * 总行数
     */
    private long totalRows;
    /**
     * 创建时间，默认为当前时间
     */
    private LocalDateTime createTime = LocalDateTime.now();
}