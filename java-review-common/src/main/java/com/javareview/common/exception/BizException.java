package com.javareview.common.exception;

import com.javareview.common.result.ErrorCode;
import lombok.Getter;

/**
 * 业务异常
 * <p>
 * 当业务校验不通过时抛出，由 GlobalExceptionHandler 统一捕获并返回标准错误响应。
 * 禁止在 Service 层直接 catch 后吞掉异常——要么处理，要么抛出。
 *
 * @author java-review
 */
@Getter
public class BizException extends RuntimeException {

    private final ErrorCode errorCode;

    public BizException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BizException(ErrorCode errorCode, String detail) {
        super(detail);
        this.errorCode = errorCode;
    }
}
