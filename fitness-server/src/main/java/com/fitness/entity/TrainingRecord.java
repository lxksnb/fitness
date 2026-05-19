package com.fitness.entity;

import lombok.Data;
import java.util.Date;

/**
 * 训练记录实体
 * 存储用户每次训练的概要信息（时间、时长、类型等）
 */
@Data
public class TrainingRecord {
    /** 主键 ID */
    private Long id;
    /** 用户 ID */
    private Long userId;
    /** 记录日期 */
    private Date recordDate;
    /** 关联计划 ID */
    private Long planId;
    /** 关联训练日 ID */
    private Long trainingDayId;
    /** 训练类型 */
    private String trainingType;
    /** 开始时间 */
    private Date startTime;
    /** 结束时间 */
    private Date endTime;
    /** 训练时长（分钟） */
    private Integer durationMinutes;
    /** 备注 */
    private String note;
    /** 创建时间 */
    private Date createdAt;
    /** 更新时间 */
    private Date updatedAt;
}
