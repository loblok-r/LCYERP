package cn.loblok.common.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "attendance_summary", schema = "erp_test", catalog = "")
@Data
public class AttendanceSummary {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private long id;
    @Basic
    @Column(name = "company_id")
    private long companyId;
    @Basic
    @Column(name = "employee_id")
    private long employeeId;
    @Basic
    @Column(name = "payroll_month")
    private String payrollMonth;
    @Basic
    @Column(name = "working_days")
    private int workingDays;
    @Basic
    @Column(name = "actual_days")
    private int actualDays;
    @Basic
    @Column(name = "overtime_hours")
    private BigDecimal overtimeHours;
    @Basic
    @Column(name = "leave_days")
    private BigDecimal leaveDays;

}