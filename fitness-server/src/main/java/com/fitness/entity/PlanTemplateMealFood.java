package com.fitness.entity;

import lombok.Data;
import java.util.Date;

/**
 * 计划模板餐食食物实体
 * 计划模板中每餐配置包含的具体食物
 */
@Data
public class PlanTemplateMealFood {
    /** 主键 ID */
    private Long id;
    /** 关联餐食配置 ID */
    private Long mealConfigId;
    /** 关联食物库 ID */
    private Long foodId;
    /** 食物名称 */
    private String foodName;
    /** 建议摄入量（克） */
    private Integer suggestedAmountG;
    /** 排序序号 */
    private Integer sortOrder;
    /** 创建时间 */
    private Date createdAt;
}
