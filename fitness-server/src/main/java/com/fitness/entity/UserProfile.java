package com.fitness.entity;

import lombok.Data;
import java.util.Date;

@Data
public class UserProfile {
    private Long id;
    private Long userId;
    private String gender;
    private Date birthday;
    private Double heightCm;
    private String targetType;
    private Double targetWeightKg;
    private Date targetDate;
    private Double weeklyChangeRate;
    private Date createdAt;
    private Date updatedAt;
}
