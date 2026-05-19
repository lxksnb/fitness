package com.fitness.entity;

import lombok.Data;
import java.util.Date;

@Data
public class PlanTemplate {
    private Long id;
    private String templateName;
    private String description;
    private String planType;
    private String splitType;
    private String difficulty;
    private Long createdBy;
    private String status;
    private Date createdAt;
    private Date updatedAt;
}
