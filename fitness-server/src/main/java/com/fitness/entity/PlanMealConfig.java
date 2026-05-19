package com.fitness.entity;

import lombok.Data;
import java.util.Date;

/**
 * 计划餐食配置实体
 * 计划中按训练日和餐次类型分配的营养素比例
 */
@Data
public class PlanMealConfig {
    /** 主键 ID */
    private Long id;
    /** 关联计划 ID */
    private Long planId;
    /** 训练日类型（TRAINING / REST） */
    private String dayType;
    /** 餐次类型（BREAKFAST / LUNCH / DINNER / SNACK） */
    private String mealType;
    /** 碳水比例 */
    private Double carbRatio;
    /** 蛋白质比例 */
    private Double proteinRatio;
    /** 脂肪比例 */
    private Double fatRatio;
    /** 排序序号 */
    private Integer sortOrder;
    /** 创建时间 */
    private Date createdAt;
}
