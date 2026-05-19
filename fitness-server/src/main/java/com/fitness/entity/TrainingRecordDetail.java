package com.fitness.entity;

import lombok.Data;
import java.util.Date;

/**
 * 训练记录详情实体
 * 训练记录中每个具体动作的组数、重量等明细数据
 */
@Data
public class TrainingRecordDetail {
    /** 主键 ID */
    private Long id;
    /** 关联训练记录 ID */
    private Long trainingRecordId;
    /** 关联动作 ID */
    private Long actionId;
    /** 动作名称 */
    private String actionName;
    /** 完成组数 */
    private Integer sets;
    /** 使用重量（千克） */
    private Double weightKg;
    /** 排序序号 */
    private Integer sortOrder;
    /** 创建时间 */
    private Date createdAt;
}
