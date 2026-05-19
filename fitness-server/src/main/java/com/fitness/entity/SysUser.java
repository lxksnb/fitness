package com.fitness.entity;

import lombok.Data;
import java.util.Date;

/**
 * 系统用户实体
 * 存储用户账号、密码、角色等基础认证信息
 */
@Data
public class SysUser {
    /** 主键 ID */
    private Long id;
    /** 用户名 */
    private String username;
    /** 加密后的密码 */
    private String password;
    /** 昵称 */
    private String nickname;
    /** 邮箱 */
    private String email;
    /** 头像 URL */
    private String avatar;
    /** 角色（USER / ADMIN） */
    private String role;
    /** 状态（ACTIVE / DISABLED） */
    private String status;
    /** 创建时间 */
    private Date createdAt;
    /** 更新时间 */
    private Date updatedAt;
}
