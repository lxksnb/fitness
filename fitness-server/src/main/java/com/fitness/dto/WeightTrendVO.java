package com.fitness.dto;

import java.util.Date;

public class WeightTrendVO {
    private Date date;
    private Double weight;
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
