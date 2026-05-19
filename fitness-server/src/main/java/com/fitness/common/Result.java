package com.fitness.common;

public class Result<T> {
    private int code;
    private String message;
    private T data;

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.code = ResultCode.SUCCESS.getCode();
        r.message = ResultCode.SUCCESS.getMessage();
        r.data = data;
        return r;
    }

    public static <T> Result<T> ok() { return ok(null); }

    public static <T> Result<T> fail(ResultCode code) {
        Result<T> r = new Result<>();
        r.code = code.getCode();
        r.message = code.getMessage();
        return r;
    }

    public static <T> Result<T> fail(ResultCode code, String message) {
        Result<T> r = new Result<>();
        r.code = code.getCode();
        r.message = message;
        return r;
    }
}
