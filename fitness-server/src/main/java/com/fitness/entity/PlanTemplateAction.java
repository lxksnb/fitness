package com.fitness.entity;

import lombok.Data;
import java.util.Date;

/**
 * 计划模板动作实体
 * 计划模板中每个训练日包含的具体训练动作
 */
@Data
public class PlanTemplateAction {
    /** 主键 ID */
    private Long id;
    /** 关联模板训练日 ID */
    private Long templateDayId;
    /** 关联动作库 ID */
    private Long actionId;
    /** 动作名称 */
    private String actionName;
    /** 最少组数 */
    private Integer minSets;
    /** 最多组数 */
    private Integer maxSets;
    /** 组间休息（分钟） */
    private Integer restMinutes;
    /** 排序序号 */
    private Integer sortOrder;
    /** 创建时间 */
    private Date createdAt;
}
