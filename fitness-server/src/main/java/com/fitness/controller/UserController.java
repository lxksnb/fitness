package com.fitness.controller;

import com.fitness.common.Result;
import com.fitness.dto.UserProfileDTO;
import com.fitness.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public Result<Map<String, Object>> getProfile() {
        return Result.ok(userService.getProfile());
    }

    @PutMapping("/profile")
    public Result<?> updateProfile(@RequestBody UserProfileDTO dto) {
        userService.updateProfile(dto);
        return Result.ok();
    }
}
