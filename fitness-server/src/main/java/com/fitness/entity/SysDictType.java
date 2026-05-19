package com.fitness.entity;

import lombok.Data;
import java.util.Date;

/**
 * 系统字典类型实体
 * 定义字典分类，如性别、运动类型、饮食类型等
 */
@Data
public class SysDictType {
    /** 主键 ID */
    private Long id;
    /** 字典编码（唯一标识） */
    private String dictCode;
    /** 字典名称 */
    private String dictName;
    /** 状态 */
    private String status;
    /** 创建时间 */
    private Date createdAt;
    /** 更新时间 */
    private Date updatedAt;
}
