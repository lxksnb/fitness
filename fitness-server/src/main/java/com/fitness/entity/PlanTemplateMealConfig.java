package com.fitness.entity;

import lombok.Data;
import java.util.Date;

/**
 * 计划模板餐食配置实体
 * 计划模板中按训练日和餐次类型的营养素分配
 */
@Data
public class PlanTemplateMealConfig {
    /** 主键 ID */
    private Long id;
    /** 关联模板 ID */
    private Long templateId;
    /** 训练日类型 */
    private String dayType;
    /** 餐次类型 */
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
