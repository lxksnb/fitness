package com.fitness.dto;

import java.util.Date;
import java.util.List;

/**
 * 计划视图 VO
 * 用于前端展示健身计划的完整信息，包含训练日、动作、餐食配置等
 */
public class PlanVO {
    /** 计划 ID */
    private Long id;
    /** 计划名称 */
    private String planName;
    /** 计划类型 */
    private String planType;
    /** 训练分化类型 */
    private String splitType;
    /** 是否启用 */
    private Integer isActive;
    /** 创建时间 */
    private Date createdAt;
    /** 训练日列表 */
    private List<TrainingDayVO> trainingDays;
    /** 餐食配置列表 */
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

    /**
     * 训练日视图内部类
     */
    public static class TrainingDayVO {
        /** ID */
        private Long id;
        /** 训练日序号 */
        private Integer dayOrder;
        /** 训练日类型 */
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

    /**
     * 动作视图内部类
     */
    public static class ActionVO {
        /** ID */
        private Long id;
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

    /**
     * 餐食配置视图内部类
     */
    public static class MealConfigVO {
        /** ID */
        private Long id;
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

    /**
     * 膳食食物视图内部类
     */
    public static class MealFoodVO {
        /** ID */
        private Long id;
        /** 食物库 ID */
        private Long foodId;
        /** 食物名称 */
        private String foodName;
        /** 建议摄入量（克） */
        private Integer suggestedAmountG;
        /** 排序序号 */
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
