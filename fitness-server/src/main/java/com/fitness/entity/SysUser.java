package com.fitness.entity;

import lombok.Data;
import java.util.Date;

@Data
public class SysUser {
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String email;
    private String avatar;
    private String role;
    private String status;
    private Date createdAt;
    private Date updatedAt;
}
