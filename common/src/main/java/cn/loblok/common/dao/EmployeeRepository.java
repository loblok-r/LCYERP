package cn.loblok.common.dao;

import cn.loblok.common.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee,Long> {
    Optional<Employee> findByIdAndCompanyId(String employeeId, long companyId);
}
