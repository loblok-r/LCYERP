package cn.loblok.common.entity;

import cn.loblok.common.Enum.PayrollStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "payroll_detail", schema = "erp_test", catalog = "")
@Data
public class PayrollDetail {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private long id;
    @Basic
    @Column(name = "biz_id")
    private String bizId;
    @Basic
    @Column(name = "company_id")
    private long companyId;
    @Basic
    @Column(name = "employee_id")
    private String employeeId;
    @Basic
    @Column(name = "payroll_month")
    private String  payrollMonth; //发薪月份



    @Basic
    @Column(name = "gross_pay")
    private BigDecimal grossPay = BigDecimal.ZERO;//应发工资（税前）
    @Basic
    @Column(name = "social_security_employee")
    private BigDecimal socialSecurityEmployee = BigDecimal.ZERO;//社保个人缴纳
    @Basic
    @Column(name = "housing_fund_employee")
    private BigDecimal housingFundEmployee = BigDecimal.ZERO;//公积金个人缴纳
    @Basic
    @Column(name = "income_tax")
    private BigDecimal incomeTax = BigDecimal.ZERO;//个人所得税
    @Basic
    @Column(name = "amount")
    private BigDecimal amount;



    @Basic
    @Enumerated(EnumType.STRING) //
    @Column(name = "status")
    private PayrollStatus status; // 类型改为枚举！

    @Basic
    @Column(name = "bank_card")
    private String bankCard;
    @Basic
    @Column(name = "bank_code")
    private String bankCode;
    @Basic
    @Column(name = "retry_count")
    private Integer retryCount;
    @Basic
    @Column(name = "create_time")
    private Timestamp createTime;
    @Basic
    @Column(name = "update_time")
    private Timestamp updateTime;

}