package com.rcd.movierecommender.backend.exception;

public enum ErrorCode {
    BAD_REQUEST(40000, "请求参数错误"),
    VALIDATION_ERROR(40001, "请求参数校验失败"),
    MISSING_PARAMETER(40002, "缺少必填参数"),
    METHOD_NOT_ALLOWED(40500, "请求方法不被支持"),
    DATABASE_ERROR(50010, "数据库访问异常"),
    RECOMMENDATION_ENGINE_ERROR(50020, "推荐算法执行失败"),
    INTERNAL_ERROR(50000, "服务器内部错误");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
