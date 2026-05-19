package com.fitness.dto;

public class ActionDTO {
    private String actionName;
    private String description;
    private String suitableFor;
    private String imageUrls;
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
