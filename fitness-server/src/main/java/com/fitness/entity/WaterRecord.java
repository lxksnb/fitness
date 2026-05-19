package com.fitness.entity;

import lombok.Data;
import java.util.Date;

@Data
public class WaterRecord {
    private Long id;
    private Long userId;
    private Date recordDate;
    private Integer amountMl;
    private Date recordedAt;
    private Date createdAt;
}
