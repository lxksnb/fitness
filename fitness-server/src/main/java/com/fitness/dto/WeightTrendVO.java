package com.fitness.dto;

import java.util.Date;

/**
 * 体重趋势 VO
 * 用于前端展示体重变化趋势图表的数据点
 */
public class WeightTrendVO {
    /** 记录日期 */
    private Date date;
    /** 体重（千克） */
    private Double weight;
    /** BMI 指数 */
    private Double bmi;

    public WeightTrendVO() {}
    public WeightTrendVO(Date date, Double weight, Double bmi) {
        this.date = date; this.weight = weight; this.bmi = bmi;
    }
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }
    public Double getBmi() { return bmi; }
    public void setBmi(Double bmi) { this.bmi = bmi; }
}
