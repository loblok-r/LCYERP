package cn.loblok.common.dao;

import cn.loblok.common.Enum.PayrollStatus;
import cn.loblok.common.entity.PayrollDetail;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


public interface PayrollDetailRepository extends JpaRepository<PayrollDetail,Long> {
    @Modifying
    @Transactional
    @Query("UPDATE PayrollDetail p SET p.status = :status, p.retryCount = :retryCount, p.updateTime = CURRENT_TIMESTAMP WHERE p.id = :id")
    void updateStatus(
            @Param("id") Long id,
            @Param("status") PayrollStatus status,
            @Param("retryCount") Integer retryCount
    );

    //    Page<PayrollDetail> findByStatus(String status, Pageable pageable);
    //统一改为游标分页
    @Query("SELECT p FROM PayrollDetail p WHERE p.status = 'PENDING' AND p.id > :lastId ORDER BY p.id ASC")
    List<PayrollDetail> findPendingBatch(@Param("lastId") long lastId, Pageable pageable);


    // PayrollDetailRepository.java
    @Query("SELECT p FROM PayrollDetail p WHERE p.status = 'SENT' AND p.id > :lastId ORDER BY p.id ASC")
    List<PayrollDetail> findSentRecordsBatch(@Param("lastId") long lastId, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE PayrollDetail p SET p.status = :newStatus, p.updateTime = CURRENT_TIMESTAMP " +
            "WHERE p.bizId = :bizId AND p.status = :expectedStatus")
    int updateStatusIfMatch(
            @Param("bizId") String bizId,
            @Param("expectedStatus") PayrollStatus expectedStatus,   // ← 枚举
            @Param("newStatus") PayrollStatus newStatus
    );

    @Modifying
    @Transactional
    @Query("UPDATE PayrollDetail p " +
            "SET p.status = :newStatus, p.updateTime = CURRENT_TIMESTAMP " +
            "WHERE p.bizId = :bizId " +
            "AND p.status NOT IN :finalStatuses")
    int updateStatusIfNotFinal(@Param("bizId") String bizId,
                               @Param("newStatus") PayrollStatus newStatus,
                               @Param("finalStatuses") List<PayrollStatus> finalStatuses);

    @Modifying
    @Transactional
    @Query("""
    UPDATE PayrollDetail p 
    SET p.status = :newStatus, p.updateTime = CURRENT_TIMESTAMP 
    WHERE p.status = :expectedStatus
      AND p.payrollMonth = :payrollMonth
      AND p.companyId = :companyId
""")
    int updateStatusToPending(@Param("expectedStatus") PayrollStatus expectedStatus,
                              @Param("newStatus") PayrollStatus newStatus,
                              @Param("payrollMonth") String payrollMonth,
                              @Param("companyId") Long companyId
    );

    List<PayrollDetail> findByStatus(PayrollStatus payrollStatus);

    Optional<BigDecimal> findTotalAmountByStatusAndMonth(PayrollStatus payrollStatus, String payrollMonth, Long companyId);

}
