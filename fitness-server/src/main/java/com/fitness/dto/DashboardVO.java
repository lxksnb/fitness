package com.fitness.dto;

import java.util.List;

/**
 * 首页看板数据VO
 * <p>
 * 聚合用户今日体重、饮食、饮水、训练、计划目标
 * 以及体重趋势、连续打卡等综合数据
 */
public class DashboardVO {

    /** 今日体重(kg) */
    private Double todayWeight;

    /** 昨日体重(kg) */
    private Double yesterdayWeight;

    /** 体重变化(kg)，今日 - 昨日 */
    private Double weightChange;

    /** 目标热量(kcal) */
    private Double targetCalories;

    /** 实际摄入热量(kcal) */
    private Double actualCalories;

    /** 目标碳水(g) */
    private Double targetCarb;

    /** 实际摄入碳水(g) */
    private Double actualCarb;

    /** 目标蛋白质(g) */
    private Double targetProtein;

    /** 实际摄入蛋白质(g) */
    private Double actualProtein;

    /** 目标脂肪(g) */
    private Double targetFat;

    /** 实际摄入脂肪(g) */
    private Double actualFat;

    /** 今日饮水总量(ml) */
    private Integer waterTotal;

    /** 饮水目标(ml) */
    private Integer waterTarget;

    /** 连续打卡天数 */
    private Integer streakDays;

    /** 今日训练类型 */
    private String todayTrainingType;

    /** 体重趋势数据(近30天) */
    private List<WeightTrendVO> weightTrend;

    /** 今日饮食记录列表 */
    private List<?> todayDietRecords;

    /** 今日训练记录 */
    private Object todayTraining;

    // ==================== Getters / Setters ====================

    public Double getTodayWeight() {
        return todayWeight;
    }

    public void setTodayWeight(Double todayWeight) {
        this.todayWeight = todayWeight;
    }

    public Double getYesterdayWeight() {
        return yesterdayWeight;
    }

    public void setYesterdayWeight(Double yesterdayWeight) {
        this.yesterdayWeight = yesterdayWeight;
    }

    public Double getWeightChange() {
        return weightChange;
    }

    public void setWeightChange(Double weightChange) {
        this.weightChange = weightChange;
    }

    public Double getTargetCalories() {
        return targetCalories;
    }

    public void setTargetCalories(Double targetCalories) {
        this.targetCalories = targetCalories;
    }

    public Double getActualCalories() {
        return actualCalories;
    }

    public void setActualCalories(Double actualCalories) {
        this.actualCalories = actualCalories;
    }

    public Double getTargetCarb() {
        return targetCarb;
    }

    public void setTargetCarb(Double targetCarb) {
        this.targetCarb = targetCarb;
    }

    public Double getActualCarb() {
        return actualCarb;
    }

    public void setActualCarb(Double actualCarb) {
        this.actualCarb = actualCarb;
    }

    public Double getTargetProtein() {
        return targetProtein;
    }

    public void setTargetProtein(Double targetProtein) {
        this.targetProtein = targetProtein;
    }

    public Double getActualProtein() {
        return actualProtein;
    }

    public void setActualProtein(Double actualProtein) {
        this.actualProtein = actualProtein;
    }

    public Double getTargetFat() {
        return targetFat;
    }

    public void setTargetFat(Double targetFat) {
        this.targetFat = targetFat;
    }

    public Double getActualFat() {
        return actualFat;
    }

    public void setActualFat(Double actualFat) {
        this.actualFat = actualFat;
    }

    public Integer getWaterTotal() {
        return waterTotal;
    }

    public void setWaterTotal(Integer waterTotal) {
        this.waterTotal = waterTotal;
    }

    public Integer getWaterTarget() {
        return waterTarget;
    }

    public void setWaterTarget(Integer waterTarget) {
        this.waterTarget = waterTarget;
    }

    public Integer getStreakDays() {
        return streakDays;
    }

    public void setStreakDays(Integer streakDays) {
        this.streakDays = streakDays;
    }

    public String getTodayTrainingType() {
        return todayTrainingType;
    }

    public void setTodayTrainingType(String todayTrainingType) {
        this.todayTrainingType = todayTrainingType;
    }

    public List<WeightTrendVO> getWeightTrend() {
        return weightTrend;
    }

    public void setWeightTrend(List<WeightTrendVO> weightTrend) {
        this.weightTrend = weightTrend;
    }

    public List<?> getTodayDietRecords() {
        return todayDietRecords;
    }

    public void setTodayDietRecords(List<?> todayDietRecords) {
        this.todayDietRecords = todayDietRecords;
    }

    public Object getTodayTraining() {
        return todayTraining;
    }

    public void setTodayTraining(Object todayTraining) {
        this.todayTraining = todayTraining;
    }
}
