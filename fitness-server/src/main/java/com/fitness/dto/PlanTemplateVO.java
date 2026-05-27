package com.fitness.dto;

import java.util.Date;

/**
 * 计划模板视图 VO
 * 管理后台模板列表展示用，仅包含基本信息不含子表数据
 */
public class PlanTemplateVO {
    /** 模板 ID */
    private Long id;
    /** 模板名称 */
    private String templateName;
    /** 模板描述 */
    private String description;
    /** 计划类型（TRAINING / DIET / HYBRID） */
    private String planType;
    /** 训练分化类型 */
    private String splitType;
    /** 难度级别（BEGINNER / INTERMEDIATE / ADVANCED） */
    private String difficulty;
    /** 创建时间 */
    private Date createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTemplateName() { return templateName; }
    public void setTemplateName(String s) { this.templateName = s; }
    public String getDescription() { return description; }
    public void setDescription(String s) { this.description = s; }
    public String getPlanType() { return planType; }
    public void setPlanType(String s) { this.planType = s; }
    public String getSplitType() { return splitType; }
    public void setSplitType(String s) { this.splitType = s; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String s) { this.difficulty = s; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date d) { this.createdAt = d; }
}
