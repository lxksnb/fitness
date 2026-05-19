package com.fitness.entity;

import lombok.Data;
import java.util.Date;

@Data
public class TrainingRecord {
    private Long id;
    private Long userId;
    private Date recordDate;
    private Long planId;
    private Long trainingDayId;
    private String trainingType;
    private Date startTime;
    private Date endTime;
    private Integer durationMinutes;
    private String note;
    private Date createdAt;
    private Date updatedAt;
}
