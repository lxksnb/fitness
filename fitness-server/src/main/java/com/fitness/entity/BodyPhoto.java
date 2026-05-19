package com.fitness.entity;

import lombok.Data;
import java.util.Date;

@Data
public class BodyPhoto {
    private Long id;
    private Long userId;
    private Date photoDate;
    private String photoType;
    private String imageUrl;
    private String note;
    private Date createdAt;
    private Date updatedAt;
}
