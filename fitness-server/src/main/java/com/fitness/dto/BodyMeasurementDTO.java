package com.fitness.dto;

import java.util.Date;

/**
 * 身体围度记录请求DTO
 */
public class BodyMeasurementDTO {
    /** 记录日期 */
    private Date recordDate;
    /** 胸围(cm) */
    private Double chestCm;
    /** 腰围(cm) */
    private Double waistCm;
    /** 左臂围(cm) */
    private Double leftArmCm;
    /** 右臂围(cm) */
    private Double rightArmCm;
    /** 左大腿围(cm) */
    private Double leftThighCm;
    /** 右大腿围(cm) */
    private Double rightThighCm;
    /** 臀围(cm) */
    private Double hipCm;
    /** 颈围(cm) */
    private Double neckCm;
    /** 备注 */
    private String note;

    public Date getRecordDate() { return recordDate; }
    public void setRecordDate(Date d) { this.recordDate = d; }
    public Double getChestCm() { return chestCm; }
    public void setChestCm(Double d) { this.chestCm = d; }
    public Double getWaistCm() { return waistCm; }
    public void setWaistCm(Double d) { this.waistCm = d; }
    public Double getLeftArmCm() { return leftArmCm; }
    public void setLeftArmCm(Double d) { this.leftArmCm = d; }
    public Double getRightArmCm() { return rightArmCm; }
    public void setRightArmCm(Double d) { this.rightArmCm = d; }
    public Double getLeftThighCm() { return leftThighCm; }
    public void setLeftThighCm(Double d) { this.leftThighCm = d; }
    public Double getRightThighCm() { return rightThighCm; }
    public void setRightThighCm(Double d) { this.rightThighCm = d; }
    public Double getHipCm() { return hipCm; }
    public void setHipCm(Double d) { this.hipCm = d; }
    public Double getNeckCm() { return neckCm; }
    public void setNeckCm(Double d) { this.neckCm = d; }
    public String getNote() { return note; }
    public void setNote(String s) { this.note = s; }
}
