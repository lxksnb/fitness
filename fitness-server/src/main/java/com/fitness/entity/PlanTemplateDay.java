package com.fitness.entity;

import lombok.Data;
import java.util.Date;

/**
 * 计划模板训练日实体
 * 计划模板中的每日训练安排
 */
@Data
public class PlanTemplateDay {
    /** 主键 ID */
    private Long id;
    /** 关联模板 ID */
    private Long templateId;
    /** 训练日序号 */
    private Integer dayOrder;
    /** 训练日类型（TRAINING / REST） */
    private String dayType;
    /** 训练类型 */
    private String trainingType;
    /** 碳水倍率 */
    private Double carbMultiplier;
    /** 蛋白质倍率 */
    private Double proteinMultiplier;
    /** 脂肪倍率 */
    private Double fatMultiplier;
    /** 创建时间 */
    private Date createdAt;
}
