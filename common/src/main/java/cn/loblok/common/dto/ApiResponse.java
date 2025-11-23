package cn.loblok.common.dto;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private T data;
    private String message;
    private boolean success;

    private ApiResponse(boolean success, T data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
    }

    // 成功：带数据
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> success(T data,String message) {
        return new ApiResponse<>(true, data, message);
    }

    // 成功：无数据（如 void 操作）
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(true, null, null);
    }

    // 错误：适用于任何泛型 T（data 为 null）
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null, message);
    }
}