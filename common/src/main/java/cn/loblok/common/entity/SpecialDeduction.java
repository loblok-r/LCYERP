package cn.loblok.common.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "special_deduction", schema = "erp_test", catalog = "")
@Data
public class SpecialDeduction {
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
    @Column(name = "children_education")
    private Integer childrenEducation;
    @Basic
    @Column(name = "continuing_education")
    private Integer continuingEducation;
    @Basic
    @Column(name = "housing_loan_interest")
    private Byte housingLoanInterest;
    @Basic
    @Column(name = "housing_rent")
    private Byte housingRent;
    @Basic
    @Column(name = "supporting_elderly")
    private Byte supportingElderly;
    @Basic
    @Column(name = "total_amount")
    private BigDecimal totalAmount;

}