package cn.loblok.common.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

@Entity
@Data
public class Employee {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private long id;
    @Basic
    @Column(name = "company_id")
    private long companyId;
    @Basic
    @Column(name = "employee_code")
    private String employeeCode;
    @Basic
    @Column(name = "name")
    private String name;
    @Basic
    @Column(name = "id_card")
    private String idCard;
    @Basic
    @Column(name = "bank_card")
    private String bankCard;
    @Basic
    @Column(name = "bank_name")
    private String bankName;
    @Basic
    @Column(name = "bank_code")
    private String bankCode;
    @Basic
    @Column(name = "join_date")
    private Date joinDate;
    @Basic
    @Column(name = "department")
    private String department;
    @Basic
    @Column(name = "position")
    private String position;
    @Basic
    @Column(name = "base_salary")
    private BigDecimal baseSalary;
    @Basic
    @Column(name = "social_security_type")
    private byte socialSecurityType;
    @Basic
    @Column(name = "housing_fund_ratio_employee")
    private BigDecimal housingFundRatioEmployee;
    @Basic
    @Column(name = "housing_fund_ratio_company")
    private BigDecimal housingFundRatioCompany;
    @Basic
    @Column(name = "status")
    private byte status;
    @Basic
    @Column(name = "created_at")
    private Timestamp createdAt;


}