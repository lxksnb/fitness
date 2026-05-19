package com.fitness.entity;

import lombok.Data;
import java.util.Date;

@Data
public class PlanMealConfig {
    private Long id;
    private Long planId;
    private String dayType;
    private String mealType;
    private Double carbRatio;
    private Double proteinRatio;
    private Double fatRatio;
    private Integer sortOrder;
    private Date createdAt;
}
