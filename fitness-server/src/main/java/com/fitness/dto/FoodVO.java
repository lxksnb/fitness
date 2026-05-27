package com.fitness.dto;

import java.util.Date;
import java.util.List;

/**
 * 食物视图 VO
 * 用于前端展示食物的完整信息及营养成分
 */
public class FoodVO {
    /** 食物 ID */
    private Long id;
    /** 作用域（SYSTEM / USER） */
    private String scope;
    /** 食物名称 */
    private String foodName;
    /** Food category dict value */
    private String categoryType;
    /** 图片 URL */
    private String imageUrl;
    /** 状态 */
    private String status;
    /** 创建时间 */
    private Date createdAt;
    /** 营养成分列表 */
    private List<NutritionVO> nutritions;

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getScope() { return scope; } public void setScope(String s) { this.scope = s; }
    public String getFoodName() { return foodName; } public void setFoodName(String s) { this.foodName = s; }
    public String getCategoryType() { return categoryType; } public void setCategoryType(String s) { this.categoryType = s; }
    public String getImageUrl() { return imageUrl; } public void setImageUrl(String s) { this.imageUrl = s; }
    public String getStatus() { return status; } public void setStatus(String s) { this.status = s; }
    public Date getCreatedAt() { return createdAt; } public void setCreatedAt(Date d) { this.createdAt = d; }
    public List<NutritionVO> getNutritions() { return nutritions; } public void setNutritions(List<NutritionVO> l) { this.nutritions = l; }

    /**
     * 营养成分视图内部类
     */
    public static class NutritionVO {
        /** 营养成分 ID */
        private Long id;
        /** 单位类型 */
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
        /** 卡路里 */
        private Double calories;
        /** 图片 URL */
        private String imageUrl;

        public Long getId() { return id; } public void setId(Long id) { this.id = id; }
        public String getUnitType() { return unitType; } public void setUnitType(String s) { this.unitType = s; }
        public Double getServingWeightG() { return servingWeightG; } public void setServingWeightG(Double d) { this.servingWeightG = d; }
        public Double getEdibleWeightG() { return edibleWeightG; } public void setEdibleWeightG(Double d) { this.edibleWeightG = d; }
        public Double getCarbGrams() { return carbGrams; } public void setCarbGrams(Double d) { this.carbGrams = d; }
        public Double getProteinGrams() { return proteinGrams; } public void setProteinGrams(Double d) { this.proteinGrams = d; }
        public Double getFatGrams() { return fatGrams; } public void setFatGrams(Double d) { this.fatGrams = d; }
        public Double getCalories() { return calories; } public void setCalories(Double d) { this.calories = d; }
        public String getImageUrl() { return imageUrl; } public void setImageUrl(String s) { this.imageUrl = s; }
    }
}
