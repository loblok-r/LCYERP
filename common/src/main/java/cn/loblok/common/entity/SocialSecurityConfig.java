package cn.loblok.common.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Data
@Table(name = "social_security_config", schema = "erp_test", catalog = "")
public class SocialSecurityConfig {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "city")
    private String city;
    @Basic
    @Column(name = "pension_ratio_employee")
    private BigDecimal pensionRatioEmployee;
    @Basic
    @Column(name = "pension_ratio_company")
    private BigDecimal pensionRatioCompany;
    @Basic
    @Column(name = "medical_ratio_employee")
    private BigDecimal medicalRatioEmployee;
    @Basic
    @Column(name = "medical_ratio_company")
    private BigDecimal medicalRatioCompany;
    @Basic
    @Column(name = "unemployment_ratio_employee")
    private BigDecimal unemploymentRatioEmployee;
    @Basic
    @Column(name = "unemployment_ratio_company")
    private BigDecimal unemploymentRatioCompany;
    @Basic
    @Column(name = "maternity_ratio_company")
    private BigDecimal maternityRatioCompany;
    @Basic
    @Column(name = "work_injury_ratio_company")
    private BigDecimal workInjuryRatioCompany;
    @Basic
    @Column(name = "housing_fund_base_min")
    private BigDecimal housingFundBaseMin;
    @Basic
    @Column(name = "housing_fund_base_max")
    private BigDecimal housingFundBaseMax;
    @Basic
    @Column(name = "social_security_base_min")
    private BigDecimal socialSecurityBaseMin;
    @Basic
    @Column(name = "social_security_base_max")
    private BigDecimal socialSecurityBaseMax;


}