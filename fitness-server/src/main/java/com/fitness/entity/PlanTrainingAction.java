package com.fitness.entity;

import lombok.Data;
import java.util.Date;

@Data
public class PlanTrainingAction {
    private Long id;
    private Long trainingDayId;
    private Long actionId;
    private String actionName;
    private Integer minSets;
    private Integer maxSets;
    private Integer restMinutes;
    private Integer sortOrder;
    private Date createdAt;
}
