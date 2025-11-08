package cn.loblok.lcyerp01.dto;

import cn.loblok.lcyerp01.entity.BudgetRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BudgetRecordRepository extends JpaRepository<BudgetRecord, Long> {
    // 我们将在这里定义查询方法
    // Spring Data JPA 会自动为你实现这个方法
    List<BudgetRecord> findByDeptIdAndYearAndMonthBetweenAndStatus(
            Integer deptId,
            Integer year,
            Byte startMonth,
            Byte endMonth,
            Byte status
    );

    // 你还可以定义其他类似的方法，比如：
    // List<BudgetRecord> findByProjectCode(String projectCode);
    // Page<BudgetRecord> findByDeptId(Integer deptId, Pageable pageable);
}