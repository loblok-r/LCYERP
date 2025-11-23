package cn.loblok.common.dto;

import lombok.Data;

@Data
public class PayResult {
    private String code;      // "0000"=成功，其他=失败
    private String message;
    // 构造函数、getters
    public PayResult(String code, String message) {
        this.code = code;
        this.message = message;
    }
}