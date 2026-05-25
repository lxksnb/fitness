package com.fitness.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

/**
 * 身体照片实体
 * 存储用户上传的身体对比照片
 */
@Data
public class BodyPhoto {
    /** 主键 ID */
    private Long id;
    /** 用户 ID */
    private Long userId;
    /** 拍照日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Shanghai")
    private Date photoDate;
    /** 照片类型（FRONT / SIDE / BACK） */
    private String photoType;
    /** 图片 URL */
    private String imageUrl;
    /** 备注 */
    private String note;
    /** 创建时间 */
    private Date createdAt;
    /** 更新时间 */
    private Date updatedAt;
}
