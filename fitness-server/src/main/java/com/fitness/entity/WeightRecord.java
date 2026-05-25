package com.fitness.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

/**
 * 体重记录实体
 * 存储用户每次体重测量的数据
 */
@Data
public class WeightRecord {
    /** 主键 ID */
    private Long id;
    /** 用户 ID */
    private Long userId;
    /** 记录日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Shanghai")
    private Date recordDate;
    /** 体重（千克） */
    private Double weightKg;
    /** BMI 指数 */
    private Double bmi;
    /** 备注 */
    private String note;
    /** 创建时间 */
    private Date createdAt;
    /** 更新时间 */
    private Date updatedAt;
}
