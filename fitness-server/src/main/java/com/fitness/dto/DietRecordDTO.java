package com.fitness.dto;

import java.util.Date;

public class DietRecordDTO {
    private Date recordDate;
    private String mealType;
    private String foodName;
    private String imageUrl;
    private Double carbGrams;
    private Double proteinGrams;
    private Double fatGrams;
    private Date recordedAt;

    // getters and setters for all fields
    public Date getRecordDate() { return recordDate; }
    public void setRecordDate(Date d) { this.recordDate = d; }
    public String getMealType() { return mealType; }
    public void setMealType(String s) { this.mealType = s; }
    public String getFoodName() { return foodName; }
    public void setFoodName(String s) { this.foodName = s; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String s) { this.imageUrl = s; }
    public Double getCarbGrams() { return carbGrams; }
    public void setCarbGrams(Double d) { this.carbGrams = d; }
    public Double getProteinGrams() { return proteinGrams; }
    public void setProteinGrams(Double d) { this.proteinGrams = d; }
    public Double getFatGrams() { return fatGrams; }
    public void setFatGrams(Double d) { this.fatGrams = d; }
    public Date getRecordedAt() { return recordedAt; }
    public void setRecordedAt(Date d) { this.recordedAt = d; }
}
