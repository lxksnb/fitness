package com.fitness.entity;

import lombok.Data;
import java.util.Date;

@Data
public class SysDictData {
    private Long id;
    private Long dictTypeId;
    private String dictLabel;
    private String dictValue;
    private Integer sort;
    private String status;
    private Date createdAt;
    private Date updatedAt;
}
