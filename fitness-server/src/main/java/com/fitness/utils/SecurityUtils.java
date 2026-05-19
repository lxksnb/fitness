package com.fitness.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全工具类
 * 提供从 Spring Security 上下文中获取当前登录用户信息的方法
 */
public class SecurityUtils {

    /**
     * 获取当前登录用户的 ID
     * 从 SecurityContextHolder 中提取认证信息中保存的用户主键
     *
     * @return 当前用户 ID
     * @throws RuntimeException 如果用户未登录或认证信息无效
     */
    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long) {
            return (Long) auth.getPrincipal();
        }
        throw new RuntimeException("用户未登录");
    }
}
