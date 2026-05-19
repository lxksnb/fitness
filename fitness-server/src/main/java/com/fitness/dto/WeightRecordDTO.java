package com.fitness.dto;

import java.util.Date;

public class WeightRecordDTO {
    private Date recordDate;
    private Double weightKg;
    private String note;

    public Date getRecordDate() { return recordDate; }
    public void setRecordDate(Date recordDate) { this.recordDate = recordDate; }
    public Double getWeightKg() { return weightKg; }
    public void setWeightKg(Double weightKg) { this.weightKg = weightKg; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
