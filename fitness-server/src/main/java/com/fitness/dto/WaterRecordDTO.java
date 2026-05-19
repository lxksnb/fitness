package com.fitness.dto;

import java.util.Date;

/**
 * 饮水记录请求DTO
 */
public class WaterRecordDTO {
    /** 记录日期 */
    private Date recordDate;
    /** 饮水量(ml) */
    private Integer amountMl;

    public Date getRecordDate() { return recordDate; }
    public void setRecordDate(Date d) { this.recordDate = d; }
    public Integer getAmountMl() { return amountMl; }
    public void setAmountMl(Integer i) { this.amountMl = i; }
}
