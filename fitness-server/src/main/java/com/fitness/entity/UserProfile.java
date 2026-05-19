package com.fitness.entity;

import lombok.Data;
import java.util.Date;

/**
 * 用户个人资料实体
 * 存储用户的身体指标、健身目标等扩展信息
 */
@Data
public class UserProfile {
    /** 主键 ID */
    private Long id;
    /** 关联用户 ID */
    private Long userId;
    /** 性别 */
    private String gender;
    /** 出生日期 */
    private Date birthday;
    /** 身高（厘米） */
    private Double heightCm;
    /** 目标类型（LOSE_WEIGHT / GAIN_MUSCLE / MAINTAIN） */
    private String targetType;
    /** 目标体重（千克） */
    private Double targetWeightKg;
    /** 目标达成日期 */
    private Date targetDate;
    /** 每周预期变化率 */
    private Double weeklyChangeRate;
    /** 创建时间 */
    private Date createdAt;
    /** 更新时间 */
    private Date updatedAt;
}
