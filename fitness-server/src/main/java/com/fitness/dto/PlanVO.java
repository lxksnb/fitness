package com.fitness.dto;

import java.util.Date;
import java.util.List;

public class PlanVO {
    private Long id;
    private String planName;
    private String planType;
    private String splitType;
    private Integer isActive;
    private Date createdAt;
    private List<TrainingDayVO> trainingDays;
    private List<MealConfigVO> mealConfigs;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPlanName() { return planName; }
    public void setPlanName(String s) { this.planName = s; }
    public String getPlanType() { return planType; }
    public void setPlanType(String s) { this.planType = s; }
    public String getSplitType() { return splitType; }
    public void setSplitType(String s) { this.splitType = s; }
    public Integer getIsActive() { return isActive; }
    public void setIsActive(Integer i) { this.isActive = i; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date d) { this.createdAt = d; }
    public List<TrainingDayVO> getTrainingDays() { return trainingDays; }
    public void setTrainingDays(List<TrainingDayVO> l) { this.trainingDays = l; }
    public List<MealConfigVO> getMealConfigs() { return mealConfigs; }
    public void setMealConfigs(List<MealConfigVO> l) { this.mealConfigs = l; }

    public static class TrainingDayVO {
        private Long id;
        private Integer dayOrder;
        private String dayType;
        private String trainingType;
        private Double carbMultiplier;
        private Double proteinMultiplier;
        private Double fatMultiplier;
        private List<ActionVO> actions;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Integer getDayOrder() { return dayOrder; }
        public void setDayOrder(Integer i) { this.dayOrder = i; }
        public String getDayType() { return dayType; }
        public void setDayType(String s) { this.dayType = s; }
        public String getTrainingType() { return trainingType; }
        public void setTrainingType(String s) { this.trainingType = s; }
        public Double getCarbMultiplier() { return carbMultiplier; }
        public void setCarbMultiplier(Double d) { this.carbMultiplier = d; }
        public Double getProteinMultiplier() { return proteinMultiplier; }
        public void setProteinMultiplier(Double d) { this.proteinMultiplier = d; }
        public Double getFatMultiplier() { return fatMultiplier; }
        public void setFatMultiplier(Double d) { this.fatMultiplier = d; }
        public List<ActionVO> getActions() { return actions; }
        public void setActions(List<ActionVO> l) { this.actions = l; }
    }

    public static class ActionVO {
        private Long id;
        private Long actionId;
        private String actionName;
        private Integer minSets;
        private Integer maxSets;
        private Integer restMinutes;
        private Integer sortOrder;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
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

    public static class MealConfigVO {
        private Long id;
        private String dayType;
        private String mealType;
        private Double carbRatio;
        private Double proteinRatio;
        private Double fatRatio;
        private Integer sortOrder;
        private List<MealFoodVO> foods;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
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
        public List<MealFoodVO> getFoods() { return foods; }
        public void setFoods(List<MealFoodVO> l) { this.foods = l; }
    }

    public static class MealFoodVO {
        private Long id;
        private Long foodId;
        private String foodName;
        private Integer suggestedAmountG;
        private Integer sortOrder;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
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
