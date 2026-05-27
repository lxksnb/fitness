package com.fitness.dto;

/**
 * 计划模板更新 DTO
 * 管理员编辑模板基本信息时使用的请求体
 */
public class PlanTemplateUpdateDTO {
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
}
