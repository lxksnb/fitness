package com.fitness.dto;

/**
 * 训练动作 DTO
 * 用于创建或更新训练动作信息
 */
public class ActionDTO {
    /** 动作名称 */
    private String actionName;
    /** 动作描述 */
    private String description;
    /** 适用人群 */
    private String suitableFor;
    /** 图片 URL 列表 */
    private String imageUrls;
    /** 视频 URL */
    private String videoUrl;

    public String getActionName() { return actionName; }
    public void setActionName(String name) { this.actionName = name; }
    public String getDescription() { return description; }
    public void setDescription(String desc) { this.description = desc; }
    public String getSuitableFor() { return suitableFor; }
    public void setSuitableFor(String sf) { this.suitableFor = sf; }
    public String getImageUrls() { return imageUrls; }
    public void setImageUrls(String urls) { this.imageUrls = urls; }
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String url) { this.videoUrl = url; }
}
