package com.fitness.dto;

/**
 * 登录请求 DTO
 * 封装用户登录时提交的凭证信息
 */
public class LoginDTO {
    /** 用户名 */
    private String username;
    /** 密码 */
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
