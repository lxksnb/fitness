package com.fitness.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类
 * 负责生成、解析和验证 JWT Token，支持访问令牌和刷新令牌两种类型
 */
@Component
public class JwtUtils {

    /** JWT 签名密钥 */
    private final SecretKey key;
    /** 访问令牌过期时间（毫秒） */
    private final long accessExpiration;
    /** 刷新令牌过期时间（毫秒） */
    private final long refreshExpiration;

    /**
     * 构造函数，从配置文件注入 JWT 相关参数
     * @param secret JWT 签名密钥
     * @param accessExpiration 访问令牌过期时间
     * @param refreshExpiration 刷新令牌过期时间
     */
    public JwtUtils(@Value("${jwt.secret}") String secret,
                    @Value("${jwt.access-token-expiration}") long accessExpiration,
                    @Value("${jwt.refresh-token-expiration}") long refreshExpiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    /**
     * 创建访问令牌
     * @param userId 用户 ID
     * @param username 用户名
     * @return JWT 访问令牌字符串
     */
    public String createAccessToken(Long userId, String username) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("username", username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessExpiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 创建刷新令牌
     * @param userId 用户 ID
     * @return JWT 刷新令牌字符串
     */
    public String createRefreshToken(Long userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 从令牌中解析用户 ID
     * @param token JWT 令牌
     * @return 用户 ID
     */
    public Long getUserId(String token) {
        return Long.parseLong(Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject());
    }

    /**
     * 验证令牌是否有效
     * @param token JWT 令牌
     * @return true 表示令牌有效，false 表示无效或已过期
     */
    public boolean validate(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
