package cn.loblok.erpcore.entity;

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
    private Long id; // 主键ID

    private Integer deptId; // 部门ID
    private String projectCode; // 项目编码
    private Integer year; // 年份
    private Byte month; // 月份
    private BigDecimal amount; // 金额
    private Byte status; // 状态

    @Column(name = "version", length = 20)
    private String version;

    @Column(name = "currency", length = 3)
    private String currency;

    @CreatedDate
    private LocalDateTime createTime;

    @LastModifiedDate
    private LocalDateTime updateTime;
}