package com.fitness.dto;

public class DailySummaryVO {
    private Double targetCarb;
    private Double targetProtein;
    private Double targetFat;
    private Double targetCalories;
    private Double actualCarb;
    private Double actualProtein;
    private Double actualFat;
    private Double actualCalories;

    // getters and setters for ALL fields
    public Double getTargetCarb() { return targetCarb; }
    public void setTargetCarb(Double d) { this.targetCarb = d; }
    public Double getTargetProtein() { return targetProtein; }
    public void setTargetProtein(Double d) { this.targetProtein = d; }
    public Double getTargetFat() { return targetFat; }
    public void setTargetFat(Double d) { this.targetFat = d; }
    public Double getTargetCalories() { return targetCalories; }
    public void setTargetCalories(Double d) { this.targetCalories = d; }
    public Double getActualCarb() { return actualCarb; }
    public void setActualCarb(Double d) { this.actualCarb = d; }
    public Double getActualProtein() { return actualProtein; }
    public void setActualProtein(Double d) { this.actualProtein = d; }
    public Double getActualFat() { return actualFat; }
    public void setActualFat(Double d) { this.actualFat = d; }
    public Double getActualCalories() { return actualCalories; }
    public void setActualCalories(Double d) { this.actualCalories = d; }
}
