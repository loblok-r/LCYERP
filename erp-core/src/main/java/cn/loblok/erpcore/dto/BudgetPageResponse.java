package cn.loblok.erpcore.dto;

import cn.loblok.erpcore.entity.BudgetRecord;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预算分页响应
 */
@Data
public class BudgetPageResponse {

    private List<BudgetRecord> data;
    private boolean hasMore;
    private Cursor nextCursor;

    @Data
    public static class Cursor {
        private LocalDateTime createTime;
        private Long id;
        // getters/setters
    }
    // getters/setters
}