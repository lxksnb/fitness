package com.fitness.exception;

import com.fitness.common.ResultCode;

public class BusinessException extends RuntimeException {
    private final ResultCode code;

    public BusinessException(ResultCode code) {
        super(code.getMessage());
        this.code = code;
    }

    public BusinessException(ResultCode code, String message) {
        super(message);
        this.code = code;
    }

    public ResultCode getCode() { return code; }
}
