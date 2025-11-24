package cn.loblok.common.dao;

import cn.loblok.common.entity.AttendanceSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttendanceSummaryRepository extends JpaRepository<AttendanceSummary,Long> {
    Optional<AttendanceSummary> findByEmployeeIdAndPayrollMonth(String employeeId, String payrollMonth);
}
