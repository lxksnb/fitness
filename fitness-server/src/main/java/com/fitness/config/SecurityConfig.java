package com.fitness.config;

import com.fitness.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 安全配置类
 * 配置无状态 JWT 认证模式、接口权限规则和密码加密器
 */
@Configuration
public class SecurityConfig {

    /** JWT 认证过滤器 */
    private final JwtAuthenticationFilter jwtFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    /**
     * 密码编码器 Bean，使用 BCrypt 加密
     * @return BCryptPasswordEncoder 实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 安全过滤链配置
     * - 禁用 CSRF（前后端分离，通过 Token 保证安全）
     * - 无状态会话管理
     * - 配置接口权限：认证接口和字典查询公开，管理接口仅 ADMIN 角色可访问，其余需认证
     * - 在用户名密码认证过滤器之前添加 JWT 过滤器
     *
     * @param http HttpSecurity 安全配置对象
     * @return 安全过滤链
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // CSRF 已通过 Token 机制防护，此处禁用
        http.csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
            .antMatchers("/api/v1/auth/**").permitAll()           // 认证接口公开
            .antMatchers(HttpMethod.GET, "/uploads/**").permitAll()
            .antMatchers(HttpMethod.GET, "/api/v1/dict/**").permitAll() // 字典查询公开
            .antMatchers("/api/v1/admin/**").hasAuthority("ADMIN") // 管理接口仅管理员
            .anyRequest().authenticated()                         // 其余接口需要认证
            .and()
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
