package com.fitness.dto;

/**
 * 注册请求 DTO
 * 封装用户注册时提交的账号信息
 */
public class RegisterDTO {
    /** 用户名 */
    private String username;
    /** 密码 */
    private String password;
    /** 昵称 */
    private String nickname;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
}
