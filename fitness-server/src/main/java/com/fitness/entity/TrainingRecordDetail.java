package com.fitness.entity;

import lombok.Data;
import java.util.Date;

@Data
public class TrainingRecordDetail {
    private Long id;
    private Long trainingRecordId;
    private Long actionId;
    private String actionName;
    private Integer sets;
    private Double weightKg;
    private Integer sortOrder;
    private Date createdAt;
}
