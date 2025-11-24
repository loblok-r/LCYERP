package cn.loblok.common.dao;

import cn.loblok.common.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company,Long> {
    Optional<Company> findByIdAndCompanyId(String employeeId, long companyId);
}
