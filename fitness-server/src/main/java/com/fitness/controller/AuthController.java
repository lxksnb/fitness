package com.fitness.controller;

import com.fitness.common.Result;
import com.fitness.dto.LoginDTO;
import com.fitness.dto.RegisterDTO;
import com.fitness.dto.TokenResponse;
import com.fitness.service.AuthService;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 处理用户注册、登录和令牌刷新请求
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 用户注册
     * @param dto 注册请求参数
     * @return 注册成功响应（无数据体）
     */
    @PostMapping("/register")
    public Result<?> register(@RequestBody RegisterDTO dto) {
        authService.register(dto);
        return Result.ok();
    }

    /**
     * 用户登录
     * @param dto 登录请求参数
     * @return 包含访问令牌和用户信息的响应
     */
    @PostMapping("/login")
    public Result<TokenResponse> login(@RequestBody LoginDTO dto) {
        return Result.ok(authService.login(dto));
    }

    /**
     * 刷新令牌
     * @param refreshToken 刷新令牌
     * @return 新的令牌响应
     */
    @PostMapping("/refresh")
    public Result<TokenResponse> refresh(@RequestParam String refreshToken) {
        return Result.ok(authService.refresh(refreshToken));
    }
}
