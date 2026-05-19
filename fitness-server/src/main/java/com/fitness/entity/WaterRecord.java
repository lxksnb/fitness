package com.fitness.entity;

import lombok.Data;
import java.util.Date;

/**
 * 饮水记录实体
 * 记录用户每日饮水量
 */
@Data
public class WaterRecord {
    /** 主键 ID */
    private Long id;
    /** 用户 ID */
    private Long userId;
    /** 记录日期 */
    private Date recordDate;
    /** 饮水量（毫升） */
    private Integer amountMl;
    /** 记录时间 */
    private Date recordedAt;
    /** 创建时间 */
    private Date createdAt;
}
