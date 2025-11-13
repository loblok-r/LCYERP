package cn.loblok.lcyerp01.dto;

import cn.loblok.lcyerp01.entity.BudgetRecord;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

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