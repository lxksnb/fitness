package com.fitness.dto;

import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.List;

/**
 * 计划创建 DTO
 * 用于一次性创建完整的健身计划，包含训练日、动作、餐食配置和食物列表
 */
public class PlanCreateDTO {
    /** 计划名称 */
    private String templateName;
    /** 计划类型（TRAINING / DIET / HYBRID） */
    private String planType;
    /** 训练分化类型 */
    private String splitType;
    /** 难度级别（BEGINNER / INTERMEDIATE / ADVANCED） */
    private String difficulty;
    /** 训练日列表 */
    private List<TrainingDayItem> trainingDays;
    /** 餐食配置列表 */
    private List<MealConfigItem> mealConfigs;

    public String getTemplateName() { return templateName; }
    public void setTemplateName(String s) { this.templateName = s; }
    public String getPlanType() { return planType; }
    public void setPlanType(String s) { this.planType = s; }
    public String getSplitType() { return splitType; }
    public void setSplitType(String s) { this.splitType = s; }
    public List<TrainingDayItem> getTrainingDays() { return trainingDays; }
    public void setTrainingDays(List<TrainingDayItem> l) { this.trainingDays = l; }
    public List<MealConfigItem> getMealConfigs() { return mealConfigs; }
    public void setMealConfigs(List<MealConfigItem> l) { this.mealConfigs = l; }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    /**
     * 训练日内嵌项
     */
    public static class TrainingDayItem {
        /** 训练日序号 */
        private Integer dayOrder;
        /** 训练日类型（TRAINING / REST） */
        private String dayType;
        /** 训练类型 */
        private String trainingType;
        /** 碳水倍率 */
        private Double carbMultiplier;
        /** 蛋白质倍率 */
        private Double proteinMultiplier;
        /** 脂肪倍率 */
        private Double fatMultiplier;
        /** 动作列表 */
        private List<ActionItem> actions;

        public Integer getDayOrder() { return dayOrder; }
        public void setDayOrder(Integer i) { this.dayOrder = i; }
        public String getDayType() { return dayType; }
        public void setDayType(String s) { this.dayType = s; }
        public String getTrainingType() { return trainingType; }
        @JsonSetter("trainingType")
        public void setTrainingType(Object value) {
            if (value == null || value instanceof String) {
                this.trainingType = (String) value;
            } else if (value instanceof List) {
                List<?> list = (List<?>) value;
                this.trainingType = list.isEmpty() ? null : list.stream()
                    .map(String::valueOf)
                    .reduce((left, right) -> left + "," + right)
                    .orElse(null);
            } else {
                this.trainingType = String.valueOf(value);
            }
        }
        public Double getCarbMultiplier() { return carbMultiplier; }
        public void setCarbMultiplier(Double d) { this.carbMultiplier = d; }
        public Double getProteinMultiplier() { return proteinMultiplier; }
        public void setProteinMultiplier(Double d) { this.proteinMultiplier = d; }
        public Double getFatMultiplier() { return fatMultiplier; }
        public void setFatMultiplier(Double d) { this.fatMultiplier = d; }
        public List<ActionItem> getActions() { return actions; }
        public void setActions(List<ActionItem> l) { this.actions = l; }
    }

    /**
     * 训练动作内嵌项
     */
    public static class ActionItem {
        /** 动作库 ID */
        private Long actionId;
        /** 动作名称 */
        private String actionName;
        /** 最少组数 */
        private Integer minSets;
        /** 最多组数 */
        private Integer maxSets;
        /** 组间休息（分钟） */
        private Integer restMinutes;
        /** 排序序号 */
        private Integer sortOrder;

        public Long getActionId() { return actionId; }
        public void setActionId(Long id) { this.actionId = id; }
        public String getActionName() { return actionName; }
        public void setActionName(String s) { this.actionName = s; }
        public Integer getMinSets() { return minSets; }
        public void setMinSets(Integer i) { this.minSets = i; }
        public Integer getMaxSets() { return maxSets; }
        public void setMaxSets(Integer i) { this.maxSets = i; }
        public Integer getRestMinutes() { return restMinutes; }
        public void setRestMinutes(Integer i) { this.restMinutes = i; }
        public Integer getSortOrder() { return sortOrder; }
        public void setSortOrder(Integer i) { this.sortOrder = i; }
    }

    /**
     * 餐食配置内嵌项
     */
    public static class MealConfigItem {
        /** 训练日类型 */
        private String dayType;
        /** 餐次类型 */
        private String mealType;
        /** 碳水比例 */
        private Double carbRatio;
        /** 蛋白质比例 */
        private Double proteinRatio;
        /** 脂肪比例 */
        private Double fatRatio;
        /** 排序序号 */
        private Integer sortOrder;
        /** 食物列表 */
        private List<MealFoodItem> foods;

        public String getDayType() { return dayType; }
        public void setDayType(String s) { this.dayType = s; }
        public String getMealType() { return mealType; }
        public void setMealType(String s) { this.mealType = s; }
        public Double getCarbRatio() { return carbRatio; }
        public void setCarbRatio(Double d) { this.carbRatio = d; }
        public Double getProteinRatio() { return proteinRatio; }
        public void setProteinRatio(Double d) { this.proteinRatio = d; }
        public Double getFatRatio() { return fatRatio; }
        public void setFatRatio(Double d) { this.fatRatio = d; }
        public Integer getSortOrder() { return sortOrder; }
        public void setSortOrder(Integer i) { this.sortOrder = i; }
        public List<MealFoodItem> getFoods() { return foods; }
        public void setFoods(List<MealFoodItem> l) { this.foods = l; }
    }

    /**
     * 餐食食物内嵌项
     */
    public static class MealFoodItem {
        /** 食物库 ID */
        private Long foodId;
        /** 食物名称 */
        private String foodName;
        /** 建议摄入量（克） */
        private Integer suggestedAmountG;
        /** 排序序号 */
        private Integer sortOrder;

        public Long getFoodId() { return foodId; }
        public void setFoodId(Long id) { this.foodId = id; }
        public String getFoodName() { return foodName; }
        public void setFoodName(String s) { this.foodName = s; }
        public Integer getSuggestedAmountG() { return suggestedAmountG; }
        public void setSuggestedAmountG(Integer i) { this.suggestedAmountG = i; }
        public Integer getSortOrder() { return sortOrder; }
        public void setSortOrder(Integer i) { this.sortOrder = i; }
    }
}
