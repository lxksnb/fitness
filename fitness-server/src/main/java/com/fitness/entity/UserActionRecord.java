package com.fitness.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

/**
 * 用户动作记录实体
 * 记录用户每次训练中具体动作的表现数据（重量、次数等）
 */
@Data
public class UserActionRecord {
    /** 主键 ID */
    private Long id;
    /** 用户 ID */
    private Long userId;
    /** 关联动作 ID */
    private Long actionId;
    /** 记录日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Shanghai")
    private Date recordDate;
    /** 使用重量（千克） */
    private Double weightKg;
    /** 最大重复次数 */
    private Integer maxReps;
    /** 备注 */
    private String note;
    /** 创建时间 */
    private Date createdAt;
}
