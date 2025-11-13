package cn.loblok.lcyerp01.service;

import cn.loblok.lcyerp01.dao.BudgetRecordRepository;
import cn.loblok.lcyerp01.dto.BudgetPageResponse;
import cn.loblok.lcyerp01.entity.BudgetRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BudgetQueryService {

    @Autowired
    private BudgetRecordRepository repository;

    // 模拟一个常见的复杂查询：按部门、年份、月份范围、状态查询
    public List<BudgetRecord> queryBudgets(Integer deptId, Integer year, Byte startMonth, Byte endMonth, Byte status) {
        return repository.findByDeptIdAndYearAndMonthBetweenAndStatus(deptId, year, startMonth, endMonth, status);
    }

    public Page<BudgetRecord> getDeepPage(Integer deptId, Integer year, Byte status, Integer page, Integer size) {
        // 注意：PageRequest 的 page 是从 0 开始的！
        // 要跳过 100,000 条 → page = 100000 / 20 = 5000（第 5001 页）

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createTime", "id")
        );

        return repository.findByDeptIdAndYearAndStatus(deptId,year,status,pageable);
    }

    public BudgetPageResponse getPageByCursor(Integer deptId, Integer year, Byte status, LocalDateTime cursorTime, Long cursorId, Integer size) {
        List<BudgetRecord> records;
        if (cursorTime == null || cursorId == null) {
            // 第一页
            records = repository.findFirstPage(deptId, year, status, size);
        } else {
            // 后续页
            records = repository.findNextPageByCursor(deptId, year, status, cursorTime, cursorId, size);
        }
        boolean hasMore = records.size() == size;
        BudgetPageResponse.Cursor nextCursor = null;

        if (!records.isEmpty()) {
            BudgetRecord last = records.get(records.size() - 1);
            nextCursor = new BudgetPageResponse.Cursor();
            nextCursor.setCreateTime(last.getCreateTime());
            nextCursor.setId(last.getId());
        }
        BudgetPageResponse budgetPageResponse = new BudgetPageResponse();
        budgetPageResponse.setData(records);
        budgetPageResponse.setHasMore(hasMore);
        budgetPageResponse.setNextCursor(nextCursor);

        return budgetPageResponse;
    }
}

