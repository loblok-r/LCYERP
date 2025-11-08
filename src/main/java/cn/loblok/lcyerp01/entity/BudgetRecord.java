package cn.loblok.lcyerp01.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "budget_records")
@Data // Lombok注解
public class BudgetRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer deptId;
    private String projectCode;
    private Integer year;
    private Byte month;
    private BigDecimal amount;
    private Byte status;

    @Column(name = "version", length = 20)
    private String version;

    @Column(name = "currency", length = 3)
    private String currency;

    @CreatedDate
    private LocalDateTime createTime;

    @LastModifiedDate
    private LocalDateTime updateTime;
}