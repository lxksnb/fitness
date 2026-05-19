package com.fitness.dto;

import java.util.List;

public class FoodCreateDTO {
    private String foodName;
    private String imageUrl;
    private List<NutritionItem> nutritions;

    public String getFoodName() { return foodName; }
    public void setFoodName(String s) { this.foodName = s; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String s) { this.imageUrl = s; }
    public List<NutritionItem> getNutritions() { return nutritions; }
    public void setNutritions(List<NutritionItem> l) { this.nutritions = l; }

    public static class NutritionItem {
        private String unitType;
        private Double servingWeightG;
        private Double carbGrams;
        private Double proteinGrams;
        private Double fatGrams;
        private String imageUrl;

        public String getUnitType() { return unitType; }
        public void setUnitType(String s) { this.unitType = s; }
        public Double getServingWeightG() { return servingWeightG; }
        public void setServingWeightG(Double d) { this.servingWeightG = d; }
        public Double getCarbGrams() { return carbGrams; }
        public void setCarbGrams(Double d) { this.carbGrams = d; }
        public Double getProteinGrams() { return proteinGrams; }
        public void setProteinGrams(Double d) { this.proteinGrams = d; }
        public Double getFatGrams() { return fatGrams; }
        public void setFatGrams(Double d) { this.fatGrams = d; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String s) { this.imageUrl = s; }
    }
}
