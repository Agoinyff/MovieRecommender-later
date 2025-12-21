package com.rcd.movierecommender.backend.exception;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 业务异常：用于承载统一的错误码与细节，便于全局异常处理器输出一致的响应格式。
 */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final List<String> details;

    public BusinessException(ErrorCode errorCode, String message) {
        this(errorCode, message, null, null);
    }

    public BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        this(errorCode, message, cause, null);
    }

    public BusinessException(ErrorCode errorCode, String message, Throwable cause, List<String> details) {
        super(message, cause);
        this.errorCode = errorCode;
        this.details = details == null ? Collections.emptyList() : new ArrayList<>(details);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public List<String> getDetails() {
        return details;
    }
}
