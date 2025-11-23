package cn.loblok.erpcore.dao;

import cn.loblok.erpcore.entity.PayrollDetail;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface PayrollDetailRepository extends JpaRepository<PayrollDetail,Long> {
    @Modifying
    @Transactional
    @Query("UPDATE PayrollDetail p SET p.status = :status, p.retryCount = :retryCount, p.updateTime = CURRENT_TIMESTAMP WHERE p.id = :id")
    void updateStatus(
            @Param("id") Long id,
            @Param("status") String status,
            @Param("retryCount") Integer retryCount
    );

//    Page<PayrollDetail> findByStatus(String status, Pageable pageable);
    //统一改为游标分页
    @Query("SELECT p FROM PayrollDetail p WHERE p.status = 'PENDING' AND p.id > :lastId ORDER BY p.id ASC")
    List<PayrollDetail> findPendingBatch(@Param("lastId") long lastId, Pageable pageable);


    // PayrollDetailRepository.java
    @Query("SELECT p FROM PayrollDetail p WHERE p.status = 'SENT' AND p.id > :lastId ORDER BY p.id ASC")
    List<PayrollDetail> findSentRecordsBatch(@Param("lastId") long lastId, Pageable pageable);

}
