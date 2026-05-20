package com.fitness.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

/**
 * 体重记录 DTO
 * 用于创建或更新体重记录
 */
public class WeightRecordDTO {
    /** 记录日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date recordDate;
    /** 体重（千克） */
    private Double weightKg;
    /** 备注 */
    private String note;

    public Date getRecordDate() { return recordDate; }
    public void setRecordDate(Date recordDate) { this.recordDate = recordDate; }
    public Double getWeightKg() { return weightKg; }
    public void setWeightKg(Double weightKg) { this.weightKg = weightKg; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
