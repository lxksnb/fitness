package com.fitness.controller;

import com.fitness.common.Result;
import com.fitness.dto.UserProfileDTO;
import com.fitness.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户控制器
 * 处理当前登录用户的个人信息查询与更新
 */
@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 获取当前用户的个人资料
     * @return 包含用户基本信息和健身资料的 Map
     */
    @GetMapping("/profile")
    public Result<Map<String, Object>> getProfile() {
        return Result.ok(userService.getProfile());
    }

    /**
     * 更新当前用户的个人资料
     * @param dto 用户资料 DTO
     * @return 更新成功响应
     */
    @PutMapping("/profile")
    public Result<?> updateProfile(@RequestBody UserProfileDTO dto) {
        userService.updateProfile(dto);
        return Result.ok();
    }
}
