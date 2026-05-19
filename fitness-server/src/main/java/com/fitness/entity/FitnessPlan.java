package com.fitness.entity;

import lombok.Data;
import java.util.Date;

/**
 * 健身计划实体
 * 存储用户创建的个性化健身/饮食计划
 */
@Data
public class FitnessPlan {
    /** 主键 ID */
    private Long id;
    /** 用户 ID */
    private Long userId;
    /** 计划名称 */
    private String planName;
    /** 计划类型（TRAINING / DIET / HYBRID） */
    private String planType;
    /** 训练分化类型（PUSH_PULL_LEGS / UPPER_LOWER / FULL_BODY 等） */
    private String splitType;
    /** 是否启用（1=启用 0=停用） */
    private Integer isActive;
    /** 创建时间 */
    private Date createdAt;
    /** 更新时间 */
    private Date updatedAt;
}
