package com.fitness.entity;

import lombok.Data;
import java.util.Date;

@Data
public class ActionLibrary {
    private Long id;
    private String scope;
    private Long userId;
    private String actionName;
    private String description;
    private String suitableFor;
    private String imageUrls;
    private String videoUrl;
    private String status;
    private Date createdAt;
    private Date updatedAt;
}
