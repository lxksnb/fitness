package com.fitness.entity;

import lombok.Data;
import java.util.Date;

/**
 * 动作刺激肌群关系
 */
@Data
public class ActionMuscleTarget {
    private Long id;
    private Long actionId;
    private String muscleCode;
    private String targetRole;
    private Integer sortOrder;
    private Date createdAt;
}
