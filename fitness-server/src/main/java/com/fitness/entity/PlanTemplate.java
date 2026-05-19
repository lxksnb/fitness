package com.fitness.entity;

import lombok.Data;
import java.util.Date;

/**
 * 计划模板实体
 * 系统预设或管理员创建的健身计划模板，供用户快速套用
 */
@Data
public class PlanTemplate {
    /** 主键 ID */
    private Long id;
    /** 模板名称 */
    private String templateName;
    /** 模板描述 */
    private String description;
    /** 计划类型（TRAINING / DIET / HYBRID） */
    private String planType;
    /** 训练分化类型 */
    private String splitType;
    /** 难度级别（BEGINNER / INTERMEDIATE / ADVANCED） */
    private String difficulty;
    /** 创建人 ID */
    private Long createdBy;
    /** 状态 */
    private String status;
    /** 创建时间 */
    private Date createdAt;
    /** 更新时间 */
    private Date updatedAt;
}
