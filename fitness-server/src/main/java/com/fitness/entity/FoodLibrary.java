package com.fitness.entity;

import lombok.Data;
import java.util.Date;

@Data
public class FoodLibrary {
    private Long id;
    private String scope;
    private Long userId;
    private String foodName;
    private String imageUrl;
    private String status;
    private Long createdBy;
    private Date createdAt;
    private Date updatedAt;
}
