package com.fitness.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.sql.Time;
import java.util.Date;
import java.util.List;

/**
 * 训练记录请求DTO
 */
public class TrainingRecordDTO {
    /** 训练日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date recordDate;
    /** 关联计划ID(可选) */
    private Long planId;
    /** 关联训练日ID(可选) */
    private Long trainingDayId;
    /** 训练类型 */
    private String trainingType;
    /** 开始时间 */
    private Time startTime;
    /** 结束时间 */
    private Time endTime;
    /** 训练时长(分钟) */
    private Integer durationMinutes;
    /** 备注 */
    private String note;
    /** 动作明细列表 */
    private List<DetailItem> details;

    // getters and setters
    public Date getRecordDate() { return recordDate; }
    public void setRecordDate(Date d) { this.recordDate = d; }
    public Long getPlanId() { return planId; }
    public void setPlanId(Long id) { this.planId = id; }
    public Long getTrainingDayId() { return trainingDayId; }
    public void setTrainingDayId(Long id) { this.trainingDayId = id; }
    public String getTrainingType() { return trainingType; }
    public void setTrainingType(String s) { this.trainingType = s; }
    public Time getStartTime() { return startTime; }
    public void setStartTime(Time d) { this.startTime = d; }
    public Time getEndTime() { return endTime; }
    public void setEndTime(Time d) { this.endTime = d; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer i) { this.durationMinutes = i; }
    public String getNote() { return note; }
    public void setNote(String s) { this.note = s; }
    public List<DetailItem> getDetails() { return details; }
    public void setDetails(List<DetailItem> l) { this.details = l; }

    /**
     * 动作明细项
     */
    public static class DetailItem {
        /** 动作ID */
        private Long actionId;
        /** 动作名称 */
        private String actionName;
        /** 实际组数 */
        private Integer sets;
        /** 使用重量(kg) */
        private Double weightKg;
        /** 排序 */
        private Integer sortOrder;

        public Long getActionId() { return actionId; }
        public void setActionId(Long id) { this.actionId = id; }
        public String getActionName() { return actionName; }
        public void setActionName(String s) { this.actionName = s; }
        public Integer getSets() { return sets; }
        public void setSets(Integer i) { this.sets = i; }
        public Double getWeightKg() { return weightKg; }
        public void setWeightKg(Double d) { this.weightKg = d; }
        public Integer getSortOrder() { return sortOrder; }
        public void setSortOrder(Integer i) { this.sortOrder = i; }
    }
}
