package com.fitness.entity;

import lombok.Data;
import java.util.Date;

@Data
public class FitnessPlan {
    private Long id;
    private Long userId;
    private String planName;
    private String planType;
    private String splitType;
    private Integer isActive;
    private Date createdAt;
    private Date updatedAt;
}
