package cn.loblok.lcyerp01.dto;

public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> res = new ApiResponse<>();
        res.code = 200;
        res.message = "success";
        res.data = data;
        return res;
    }

    public static ApiResponse<String> error(String message) {
        ApiResponse<String> res = new ApiResponse<>();
        res.code = 500;
        res.message = message;
        return res;
    }

    // getter/setter...
}