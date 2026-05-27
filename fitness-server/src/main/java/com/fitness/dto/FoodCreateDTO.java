package com.fitness.dto;

import java.util.List;

/**
 * 食物创建 DTO
 * 用于创建食物及其营养成分信息
 */
public class FoodCreateDTO {
    /** 食物名称 */
    private String foodName;
    /** Food category dict value */
    private String categoryType;
    /** 图片 URL */
    private String imageUrl;
    /** 营养成分列表 */
    private List<NutritionItem> nutritions;

    public String getFoodName() { return foodName; }
    public void setFoodName(String s) { this.foodName = s; }
    public String getCategoryType() { return categoryType; }
    public void setCategoryType(String s) { this.categoryType = s; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String s) { this.imageUrl = s; }
    public List<NutritionItem> getNutritions() { return nutritions; }
    public void setNutritions(List<NutritionItem> l) { this.nutritions = l; }

    /**
     * 营养成分内部类
     * 描述食物的单个营养成分条目（每份/每100克的营养素含量）
     */
    public static class NutritionItem {
        /** 单位类型（PER_100G / PER_SERVING） */
        private String unitType;
        /** 每份重量（克） */
        private Double servingWeightG;
        /** Edible weight in grams */
        private Double edibleWeightG;
        /** 碳水含量（克） */
        private Double carbGrams;
        /** 蛋白质含量（克） */
        private Double proteinGrams;
        /** 脂肪含量（克） */
        private Double fatGrams;
        /** 图片 URL */
        private String imageUrl;

        public String getUnitType() { return unitType; }
        public void setUnitType(String s) { this.unitType = s; }
        public Double getServingWeightG() { return servingWeightG; }
        public void setServingWeightG(Double d) { this.servingWeightG = d; }
        public Double getEdibleWeightG() { return edibleWeightG; }
        public void setEdibleWeightG(Double d) { this.edibleWeightG = d; }
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
