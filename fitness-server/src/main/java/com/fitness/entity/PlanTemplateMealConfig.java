package com.fitness.entity;

import lombok.Data;
import java.util.Date;

@Data
public class PlanTemplateMealConfig {
    private Long id;
    private Long templateId;
    private String dayType;
    private String mealType;
    private Double carbRatio;
    private Double proteinRatio;
    private Double fatRatio;
    private Integer sortOrder;
    private Date createdAt;
}
