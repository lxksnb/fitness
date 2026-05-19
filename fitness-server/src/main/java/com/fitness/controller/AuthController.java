package com.fitness.controller;

import com.fitness.common.Result;
import com.fitness.dto.LoginDTO;
import com.fitness.dto.RegisterDTO;
import com.fitness.dto.TokenResponse;
import com.fitness.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public Result<?> register(@RequestBody RegisterDTO dto) {
        authService.register(dto);
        return Result.ok();
    }

    @PostMapping("/login")
    public Result<TokenResponse> login(@RequestBody LoginDTO dto) {
        return Result.ok(authService.login(dto));
    }

    @PostMapping("/refresh")
    public Result<TokenResponse> refresh(@RequestParam String refreshToken) {
        return Result.ok(authService.refresh(refreshToken));
    }
}
