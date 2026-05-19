package com.fitness.common;

/**
 * 统一响应结果封装类
 * 用于统一格式化所有接口的返回数据，包含状态码、消息和数据体
 *
 * @param <T> 响应数据的泛型类型
 */
public class Result<T> {
    /** HTTP 状态码 */
    private int code;
    /** 响应消息 */
    private String message;
    /** 响应数据体 */
    private T data;

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    /**
     * 返回成功结果，包含数据
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功结果
     */
    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.code = ResultCode.SUCCESS.getCode();
        r.message = ResultCode.SUCCESS.getMessage();
        r.data = data;
        return r;
    }

    /** 返回成功结果，无数据体 */
    public static <T> Result<T> ok() { return ok(null); }

    /**
     * 返回失败结果，使用默认错误消息
     * @param code 错误状态码
     * @param <T> 数据类型
     * @return 失败结果
     */
    public static <T> Result<T> fail(ResultCode code) {
        Result<T> r = new Result<>();
        r.code = code.getCode();
        r.message = code.getMessage();
        return r;
    }

    /**
     * 返回失败结果，使用自定义错误消息
     * @param code 错误状态码
     * @param message 自定义错误消息
     * @param <T> 数据类型
     * @return 失败结果
     */
    public static <T> Result<T> fail(ResultCode code, String message) {
        Result<T> r = new Result<>();
        r.code = code.getCode();
        r.message = message;
        return r;
    }
}
