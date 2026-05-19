package com.fitness.entity;

import lombok.Data;
import java.util.Date;

@Data
public class PlanTemplateMealFood {
    private Long id;
    private Long mealConfigId;
    private Long foodId;
    private String foodName;
    private Integer suggestedAmountG;
    private Integer sortOrder;
    private Date createdAt;
}
