package cn.loblok.lcyerp01.dao;

import cn.loblok.lcyerp01.entity.AssetDetail;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AssetDetailRepository extends JpaRepository<AssetDetail,Long> {
    @Query("SELECT a FROM AssetDetail a WHERE a.id > :lastId ORDER BY a.id ASC")
    List<AssetDetail> findNextBatch(@Param("lastId") Long lastId, Pageable pageable);

    @Query("SELECT MIN(a.id) FROM AssetDetail a")
    Long findMinId();

    @Query("SELECT MAX(a.id) FROM AssetDetail a")
    Long findMaxId();

    @Query(value = "SELECT * FROM asset_detail WHERE id > :lastId AND id <= :endId ORDER BY id ASC LIMIT :limit",
            nativeQuery = true)
    List<AssetDetail> findNextInSegmentNative(@Param("lastId") Long lastId,
                                              @Param("endId") Long endId,
                                              @Param("limit") int limit);
}
