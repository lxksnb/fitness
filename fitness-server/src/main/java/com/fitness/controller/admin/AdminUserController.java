package com.fitness.controller.admin;

import com.fitness.common.Result;
import com.fitness.entity.SysUser;
import com.fitness.mapper.SysUserMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员用户管理控制器
 * 管理员可查看用户列表、启用/禁用用户账号
 */
@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final SysUserMapper userMapper;

    /**
     * 构造函数 - 注入系统用户Mapper
     */
    public AdminUserController(SysUserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 获取所有用户列表
     */
    @GetMapping
    public Result<List<SysUser>> list() {
        List<SysUser> users = userMapper.selectAll();
        // 清除密码字段，防止泄露
        for (SysUser user : users) {
            user.setPassword(null);
        }
        return Result.ok(users);
    }

    /**
     * 启用或禁用用户
     * @param id 用户ID
     * @param status 新状态(ACTIVE/DISABLED)
     */
    @PutMapping("/{id}/status")
    public Result<?> toggleStatus(@PathVariable Long id, @RequestParam String status) {
        SysUser user = userMapper.selectById(id);
        if (user != null) {
            user.setStatus(status);
            userMapper.updateStatus(user);
        }
        return Result.ok();
    }
}
