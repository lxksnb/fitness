package com.fitness.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

/**
 * 饮食记录实体
 * 存储用户每日各餐的饮食摄入数据
 */
@Data
public class DietRecord {
    /** 主键 ID */
    private Long id;
    /** 用户 ID */
    private Long userId;
    /** 记录日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Shanghai")
    private Date recordDate;
    /** 餐次类型 */
    private String mealType;
    /** 食物名称 */
    private String foodName;
    /** 图片 URL */
    private String imageUrl;
    /** 碳水摄入（克） */
    private Double carbGrams;
    /** 蛋白质摄入（克） */
    private Double proteinGrams;
    /** 脂肪摄入（克） */
    private Double fatGrams;
    /** 卡路里摄入 */
    private Double calories;
    /** 记录时间 */
    private Date recordedAt;
    /** 创建时间 */
    private Date createdAt;
    /** 更新时间 */
    private Date updatedAt;
}
