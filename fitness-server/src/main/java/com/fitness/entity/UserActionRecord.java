package com.fitness.entity;

import lombok.Data;
import java.util.Date;

@Data
public class UserActionRecord {
    private Long id;
    private Long userId;
    private Long actionId;
    private Date recordDate;
    private Double weightKg;
    private Integer maxReps;
    private String note;
    private Date createdAt;
}
