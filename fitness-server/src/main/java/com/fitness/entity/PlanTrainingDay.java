package com.fitness.entity;

import lombok.Data;
import java.util.Date;

@Data
public class PlanTrainingDay {
    private Long id;
    private Long planId;
    private Integer dayOrder;
    private String dayType;
    private String trainingType;
    private Double carbMultiplier;
    private Double proteinMultiplier;
    private Double fatMultiplier;
    private Date createdAt;
}
