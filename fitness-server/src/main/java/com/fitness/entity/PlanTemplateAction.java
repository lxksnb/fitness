package com.fitness.entity;

import lombok.Data;
import java.util.Date;

@Data
public class PlanTemplateAction {
    private Long id;
    private Long templateDayId;
    private Long actionId;
    private String actionName;
    private Integer minSets;
    private Integer maxSets;
    private Integer restMinutes;
    private Integer sortOrder;
    private Date createdAt;
}
