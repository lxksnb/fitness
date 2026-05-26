package com.fitness.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 训练动作 DTO
 * 用于创建或更新训练动作信息
 */
public class ActionDTO {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    /** 动作名称 */
    private String actionName;
    /** 动作描述 */
    private String description;
    /** 适用部位列表(前端checkbox多选, 逗号分隔存入DB) */
    private java.util.List<String> suitableFor;
    /** 主要刺激肌群 */
    private java.util.List<String> primaryMuscles;
    /** 辅助刺激肌群 */
    private java.util.List<String> secondaryMuscles;
    /** 图片 URL 列表 */
    private String imageUrls;
    /** 视频 URL */
    private String videoUrl;

    public String getActionName() { return actionName; }
    public void setActionName(String name) { this.actionName = name; }
    public String getDescription() { return description; }
    public void setDescription(String desc) { this.description = desc; }
    public java.util.List<String> getSuitableFor() { return suitableFor; }
    public void setSuitableFor(java.util.List<String> sf) { this.suitableFor = sf; }
    public java.util.List<String> getPrimaryMuscles() { return primaryMuscles; }
    public void setPrimaryMuscles(java.util.List<String> primaryMuscles) { this.primaryMuscles = primaryMuscles; }
    public java.util.List<String> getSecondaryMuscles() { return secondaryMuscles; }
    public void setSecondaryMuscles(java.util.List<String> secondaryMuscles) { this.secondaryMuscles = secondaryMuscles; }
    public String getImageUrls() { return imageUrls; }
    @JsonSetter("imageUrls")
    public void setImageUrls(Object urls) {
        if (urls == null || urls instanceof String) {
            this.imageUrls = (String) urls;
            return;
        }
        try {
            this.imageUrls = OBJECT_MAPPER.writeValueAsString(urls);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid imageUrls", e);
        }
    }
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String url) { this.videoUrl = url; }
}
