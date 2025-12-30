package cn.loblok.erpcore.dao;

import cn.loblok.erpcore.entity.CompanyBankConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
/**
 * @author Loblok
 * @date 2023/9/23
 */
public interface CompanyBankConfigRepository extends JpaRepository<CompanyBankConfig,Long> {
    @Query("SELECT c.bankCode FROM CompanyBankConfig c WHERE c.companyId = :companyId")
    Optional<String> findBankCodeByCompanyId(@Param("companyId") Long companyId);
}
