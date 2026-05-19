package com.fitness.dto;

/**
 * 令牌响应 DTO
 * 登录成功后返回的认证令牌和用户基本信息
 */
public class TokenResponse {
    /** 访问令牌 */
    private String accessToken;
    /** 刷新令牌 */
    private String refreshToken;
    /** 令牌过期时间（秒） */
    private long expiresIn;
    /** 用户 ID */
    private Long userId;
    /** 昵称 */
    private String nickname;
    /** 角色 */
    private String role;

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
