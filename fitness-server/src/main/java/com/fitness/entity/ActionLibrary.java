package com.fitness.entity;

import lombok.Data;
import java.util.Date;
import java.util.List;

/**
 * 动作库实体
 * 存储系统预设和用户自定义的训练动作信息
 */
@Data
public class ActionLibrary {
    /** 主键 ID */
    private Long id;
    /** 作用域（SYSTEM / USER） */
    private String scope;
    /** 用户 ID（用户自定义动作时关联） */
    private Long userId;
    /** 动作名称 */
    private String actionName;
    /** 动作描述 */
    private String description;
    /** 适用人群 */
    private String suitableFor;
    /** 主要刺激肌群编码 */
    private List<String> primaryMuscles;
    /** 辅助刺激肌群编码 */
    private List<String> secondaryMuscles;
    /** 图片 URL 列表（JSON 数组） */
    private String imageUrls;
    /** 视频 URL */
    private String videoUrl;
    /** 状态 */
    private String status;
    /** 创建时间 */
    private Date createdAt;
    /** 更新时间 */
    private Date updatedAt;
}
