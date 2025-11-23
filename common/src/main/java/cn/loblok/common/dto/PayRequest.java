package cn.loblok.common.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PayRequest {
    private String bizId;
    private String employeeId;
    private String bankCard;
    private BigDecimal amount;
    // getters/setters
}