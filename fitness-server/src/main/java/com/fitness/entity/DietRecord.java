package com.fitness.entity;

import lombok.Data;
import java.util.Date;

@Data
public class DietRecord {
    private Long id;
    private Long userId;
    private Date recordDate;
    private String mealType;
    private String foodName;
    private String imageUrl;
    private Double carbGrams;
    private Double proteinGrams;
    private Double fatGrams;
    private Double calories;
    private Date recordedAt;
    private Date createdAt;
    private Date updatedAt;
}
