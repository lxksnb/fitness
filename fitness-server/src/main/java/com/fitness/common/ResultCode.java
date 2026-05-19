package com.fitness.common;

/**
 * 统一响应状态码枚举
 * 定义系统中所有接口返回的标准状态码和对应的默认消息
 */
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或token已过期"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "数据冲突"),
    INTERNAL_ERROR(500, "系统内部错误");

    /** HTTP 状态码 */
    private final int code;
    /** 默认响应消息 */
    private final String message;

    /**
     * 构造函数
     * @param code HTTP 状态码
     * @param message 默认响应消息
     */
    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /** 获取 HTTP 状态码 */
    public int getCode() { return code; }
    /** 获取默认响应消息 */
    public String getMessage() { return message; }
}
