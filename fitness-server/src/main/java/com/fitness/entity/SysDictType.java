package com.fitness.entity;

import lombok.Data;
import java.util.Date;

@Data
public class SysDictType {
    private Long id;
    private String dictCode;
    private String dictName;
    private String status;
    private Date createdAt;
    private Date updatedAt;
}
