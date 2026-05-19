package com.fitness.entity;

import lombok.Data;
import java.util.Date;

@Data
public class BodyMeasurement {
    private Long id;
    private Long userId;
    private Date recordDate;
    private Double chestCm;
    private Double waistCm;
    private Double leftArmCm;
    private Double rightArmCm;
    private Double leftThighCm;
    private Double rightThighCm;
    private Double hipCm;
    private Double neckCm;
    private String note;
    private Date createdAt;
    private Date updatedAt;
}
