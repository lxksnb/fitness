package com.fitness.entity;

import lombok.Data;
import java.util.Date;

/**
 * 系统字典数据实体
 * 存储字典类型下的具体键值对数据
 */
@Data
public class SysDictData {
    /** 主键 ID */
    private Long id;
    /** 关联字典类型 ID */
    private Long dictTypeId;
    /** 字典标签（显示文本） */
    private String dictLabel;
    /** 字典值 */
    private String dictValue;
    /** 排序序号 */
    private Integer sort;
    /** 状态 */
    private String status;
    /** 创建时间 */
    private Date createdAt;
    /** 更新时间 */
    private Date updatedAt;
}
