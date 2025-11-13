package cn.loblok.lcyerp01.dao;

import cn.loblok.lcyerp01.entity.BudgetRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
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

    // 标准方法名查询，Spring Data JPA 自动生成 SQL
    Page<BudgetRecord> findByDeptIdAndYearAndStatus(
            Integer deptId,
            Integer year,
            Byte status,
            Pageable pageable
    );

    // Repository 新增方法
    @Query(value = """
    SELECT * FROM budget_records
    WHERE dept_id = :deptId AND year = :year AND status = :status
      AND (create_time < :cursorTime OR (create_time = :cursorTime AND id < :cursorId))
    ORDER BY create_time DESC, id DESC
    LIMIT :size
    """, nativeQuery = true)
    List<BudgetRecord> findNextPageByCursor(
            @Param("deptId") Integer deptId,
            @Param("year") Integer year,
            @Param("status") Byte status,
            @Param("cursorTime") LocalDateTime cursorTime,
            @Param("cursorId") Long cursorId,
            @Param("size") int size
    );
    // 第一页：没有游标时使用
    @Query(value = """
        SELECT * FROM budget_records
        WHERE dept_id = :deptId 
          AND year = :year 
          AND status = :status
        ORDER BY create_time DESC, id DESC
        LIMIT :size
        """, nativeQuery = true)
    List<BudgetRecord> findFirstPage(
            @Param("deptId") Integer deptId,
            @Param("year") Integer year,
            @Param("status") Byte status,
            @Param("size") int size
    );
}