package com.fitness.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置类
 * 允许前端跨域访问，配置允许的域名、HTTP 方法、请求头以及是否允许携带凭证
 */
@Configuration
public class CorsConfig {

    /**
     * 创建跨域过滤器 Bean
     * 允许所有来源的跨域请求，支持所有 HTTP 方法和请求头，允许携带 Cookie/Token
     * @return CorsFilter 实例
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");   // 允许所有域名跨域
        config.addAllowedMethod("*");          // 允许所有 HTTP 方法
        config.addAllowedHeader("*");          // 允许所有请求头
        config.setAllowCredentials(true);      // 允许携带认证凭证
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
