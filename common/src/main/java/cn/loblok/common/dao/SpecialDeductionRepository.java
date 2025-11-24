package cn.loblok.common.dao;

import cn.loblok.common.entity.SpecialDeduction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpecialDeductionRepository extends JpaRepository<SpecialDeduction,Long> {
    Optional<SpecialDeduction> findByEmployeeIdAndPayrollMonth(String employeeId, String payrollMonth);
}
