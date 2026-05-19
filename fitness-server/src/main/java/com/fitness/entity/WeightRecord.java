package com.fitness.entity;

import lombok.Data;
import java.util.Date;

@Data
public class WeightRecord {
    private Long id;
    private Long userId;
    private Date recordDate;
    private Double weightKg;
    private Double bmi;
    private String note;
    private Date createdAt;
    private Date updatedAt;
}
