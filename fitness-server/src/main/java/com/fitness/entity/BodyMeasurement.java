package com.fitness.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

/**
 * 身体围度测量实体
 * 记录用户身体各部位的围度数据
 */
@Data
public class BodyMeasurement {
    /** 主键 ID */
    private Long id;
    /** 用户 ID */
    private Long userId;
    /** 记录日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Shanghai")
    private Date recordDate;
    /** 胸围（厘米） */
    private Double chestCm;
    /** 腰围（厘米） */
    private Double waistCm;
    /** 左臂围（厘米） */
    private Double leftArmCm;
    /** 右臂围（厘米） */
    private Double rightArmCm;
    /** 左大腿围（厘米） */
    private Double leftThighCm;
    /** 右大腿围（厘米） */
    private Double rightThighCm;
    /** 臀围（厘米） */
    private Double hipCm;
    /** 颈围（厘米） */
    private Double neckCm;
    /** 备注 */
    private String note;
    /** 创建时间 */
    private Date createdAt;
    /** 更新时间 */
    private Date updatedAt;
}
