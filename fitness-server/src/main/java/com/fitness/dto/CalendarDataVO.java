package com.fitness.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

/**
 * 训练打卡日历数据
 */
public class CalendarDataVO {
    /** 训练日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Shanghai")
    private Date recordDate;
    /** 训练类型 */
    private String trainingType;

    public CalendarDataVO() {}

    public CalendarDataVO(Date recordDate, String trainingType) {
        this.recordDate = recordDate;
        this.trainingType = trainingType;
    }

    public Date getRecordDate() { return recordDate; }
    public void setRecordDate(Date d) { this.recordDate = d; }
    public String getTrainingType() { return trainingType; }
    public void setTrainingType(String s) { this.trainingType = s; }
}
