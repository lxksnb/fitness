package com.fitness.entity;

import lombok.Data;
import java.util.Date;

/**
 * 食物营养成分实体
 * 存储食物每份的营养素含量和卡路里
 */
@Data
public class FoodNutrition {
    /** 主键 ID */
    private Long id;
    /** 关联食物 ID */
    private Long foodId;
    /** 单位类型（PER_100G / PER_SERVING） */
    private String unitType;
    /** 每份重量（克） */
    private Double servingWeightG;
    /** 碳水含量（克） */
    private Double carbGrams;
    /** 蛋白质含量（克） */
    private Double proteinGrams;
    /** 脂肪含量（克） */
    private Double fatGrams;
    /** 卡路里 */
    private Double calories;
    /** 图片 URL */
    private String imageUrl;
    /** 创建时间 */
    private Date createdAt;
    /** 更新时间 */
    private Date updatedAt;
}
