package cn.loblok.common.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Entity
@Data
public class Company {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private long id;
    @Basic
    @Column(name = "name")
    private String name;
    @Basic
    @Column(name = "social_security_city")
    private String socialSecurityCity;
    @Basic
    @Column(name = "housing_fund_city")
    private String housingFundCity;
    @Basic
    @Column(name = "tax_bureau_code")
    private String taxBureauCode;
    @Basic
    @Column(name = "created_at")
    private Timestamp createdAt;


}