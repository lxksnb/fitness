package com.fitness.entity;

import lombok.Data;
import java.util.Date;

/**
 * 食物库实体
 * 存储系统预设和用户自定义的食物信息
 */
@Data
public class FoodLibrary {
    /** 主键 ID */
    private Long id;
    /** 作用域（SYSTEM / USER） */
    private String scope;
    /** 用户 ID（用户自定义食物时关联） */
    private Long userId;
    /** 食物名称 */
    private String foodName;
    /** Food category dict value */
    private String categoryType;
    /** 图片 URL */
    private String imageUrl;
    /** 状态 */
    private String status;
    /** 创建人 ID */
    private Long createdBy;
    /** 创建时间 */
    private Date createdAt;
    /** 更新时间 */
    private Date updatedAt;
}
