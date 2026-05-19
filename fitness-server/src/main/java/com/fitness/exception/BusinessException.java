package com.fitness.exception;

import com.fitness.common.ResultCode;

/**
 * 业务异常类
 * 用于在业务逻辑中抛出可预见的异常，被全局异常处理器捕获后返回统一格式的错误响应
 */
public class BusinessException extends RuntimeException {
    /** 业务错误状态码 */
    private final ResultCode code;

    /**
     * 使用状态码构造业务异常
     * @param code 业务错误状态码
     */
    public BusinessException(ResultCode code) {
        super(code.getMessage());
        this.code = code;
    }

    /**
     * 使用状态码和自定义消息构造业务异常
     * @param code 业务错误状态码
     * @param message 自定义错误消息
     */
    public BusinessException(ResultCode code, String message) {
        super(message);
        this.code = code;
    }

    /** 获取错误状态码 */
    public ResultCode getCode() { return code; }
}
