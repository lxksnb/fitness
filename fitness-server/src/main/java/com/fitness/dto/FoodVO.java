package com.fitness.dto;

import java.util.Date;
import java.util.List;

public class FoodVO {
    private Long id;
    private String scope;
    private String foodName;
    private String imageUrl;
    private String status;
    private Date createdAt;
    private List<NutritionVO> nutritions;

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getScope() { return scope; } public void setScope(String s) { this.scope = s; }
    public String getFoodName() { return foodName; } public void setFoodName(String s) { this.foodName = s; }
    public String getImageUrl() { return imageUrl; } public void setImageUrl(String s) { this.imageUrl = s; }
    public String getStatus() { return status; } public void setStatus(String s) { this.status = s; }
    public Date getCreatedAt() { return createdAt; } public void setCreatedAt(Date d) { this.createdAt = d; }
    public List<NutritionVO> getNutritions() { return nutritions; } public void setNutritions(List<NutritionVO> l) { this.nutritions = l; }

    public static class NutritionVO {
        private Long id;
        private String unitType;
        private Double servingWeightG;
        private Double carbGrams;
        private Double proteinGrams;
        private Double fatGrams;
        private Double calories;
        private String imageUrl;

        public Long getId() { return id; } public void setId(Long id) { this.id = id; }
        public String getUnitType() { return unitType; } public void setUnitType(String s) { this.unitType = s; }
        public Double getServingWeightG() { return servingWeightG; } public void setServingWeightG(Double d) { this.servingWeightG = d; }
        public Double getCarbGrams() { return carbGrams; } public void setCarbGrams(Double d) { this.carbGrams = d; }
        public Double getProteinGrams() { return proteinGrams; } public void setProteinGrams(Double d) { this.proteinGrams = d; }
        public Double getFatGrams() { return fatGrams; } public void setFatGrams(Double d) { this.fatGrams = d; }
        public Double getCalories() { return calories; } public void setCalories(Double d) { this.calories = d; }
        public String getImageUrl() { return imageUrl; } public void setImageUrl(String s) { this.imageUrl = s; }
    }
}
