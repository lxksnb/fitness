package com.fitness.entity;

import lombok.Data;
import java.util.Date;

@Data
public class FoodNutrition {
    private Long id;
    private Long foodId;
    private String unitType;
    private Double servingWeightG;
    private Double carbGrams;
    private Double proteinGrams;
    private Double fatGrams;
    private Double calories;
    private String imageUrl;
    private Date createdAt;
    private Date updatedAt;
}
