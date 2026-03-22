package com.javareview.common.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应包装类
 * <p>
 * 所有接口统一返回此对象，前端/调用方只需按固定结构解析。
 * 使用泛型 T 承载不同业务的返回数据。
 *
 * @author java-review
 */
@Data
public class Result<T> implements Serializable {

    private int code;
    private String message;
    private T data;

    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(200, "success", data);
    }

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }

    public static <T> Result<T> fail(ErrorCode errorCode) {
        return new Result<>(errorCode.getCode(), errorCode.getMessage(), null);
    }
}
