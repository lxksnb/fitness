package com.fitness.service;

import com.fitness.dto.DashboardVO;
import com.fitness.dto.WeightTrendVO;
import com.fitness.entity.*;
import com.fitness.mapper.*;
import com.fitness.utils.SecurityUtils;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 首页看板服务
 * <p>
 * 聚合体重、饮食、饮水、训练、计划目标等多维度数据，
 * 为首页看板提供一站式数据查询。
 */
@Service
public class DashboardService {

    private final WeightRecordMapper weightMapper;
    private final DietRecordMapper dietMapper;
    private final WaterRecordMapper waterMapper;
    private final FitnessPlanMapper planMapper;
    private final PlanTrainingDayMapper dayMapper;
    private final TrainingRecordMapper trainMapper;
    private final WeightService weightService;

    public DashboardService(WeightRecordMapper weightMapper,
                            DietRecordMapper dietMapper,
                            WaterRecordMapper waterMapper,
                            FitnessPlanMapper planMapper,
                            PlanTrainingDayMapper dayMapper,
                            TrainingRecordMapper trainMapper,
                            WeightService weightService) {
        this.weightMapper = weightMapper;
        this.dietMapper = dietMapper;
        this.waterMapper = waterMapper;
        this.planMapper = planMapper;
        this.dayMapper = dayMapper;
        this.trainMapper = trainMapper;
        this.weightService = weightService;
    }

    /**
     * 获取首页看板聚合数据
     * <p>
     * 聚合当前用户的今日体重、昨日体重、饮食汇总、
     * 饮水总量、计划目标、训练记录、连续打卡天数及体重趋势。
     *
     * @return 首页看板数据VO
     */
    public DashboardVO getDashboard() {
        Long userId = SecurityUtils.getCurrentUserId();
        Date today = getToday();
        Date yesterday = getYesterday();

        DashboardVO vo = new DashboardVO();

        // ---- 1. 体重数据 ----
        WeightRecord todayWeight = weightMapper.selectByUserAndDate(userId, today);
        WeightRecord yesterdayWeight = weightMapper.selectByUserAndDate(userId, yesterday);

        if (todayWeight != null) {
            vo.setTodayWeight(todayWeight.getWeightKg());
            if (yesterdayWeight != null) {
                vo.setYesterdayWeight(yesterdayWeight.getWeightKg());
                vo.setWeightChange(round(todayWeight.getWeightKg() - yesterdayWeight.getWeightKg()));
            }
        }

        // ---- 2. 饮水数据 ----
        Integer waterTotal = waterMapper.selectTodayTotal(userId, today);
        vo.setWaterTotal(waterTotal != null ? waterTotal : 0);
        vo.setWaterTarget(2000);

        // ---- 3. 饮食汇总 ----
        Map<String, Object> dietSummary = dietMapper.selectDailySummary(userId, today);
        if (dietSummary != null) {
            vo.setActualCarb(toDouble(dietSummary.get("carbGrams")));
            vo.setActualProtein(toDouble(dietSummary.get("proteinGrams")));
            vo.setActualFat(toDouble(dietSummary.get("fatGrams")));
            vo.setActualCalories(toDouble(dietSummary.get("calories")));
        } else {
            vo.setActualCarb(0.0);
            vo.setActualProtein(0.0);
            vo.setActualFat(0.0);
            vo.setActualCalories(0.0);
        }

        // ---- 4. 计划目标 (基于活跃计划 + 体重 + 营养乘数) ----
        List<FitnessPlan> activePlans = planMapper.selectActiveByUser(userId);
        FitnessPlan activePlan = (activePlans != null && !activePlans.isEmpty()) ? activePlans.get(0) : null;
        if (activePlan != null && todayWeight != null) {
            List<PlanTrainingDay> days = dayMapper.selectByPlanId(activePlan.getId());
            if (days != null && !days.isEmpty()) {
                // 简化: 取第一个训练日的营养乘数作为目标
                PlanTrainingDay day = days.get(0);
                double w = todayWeight.getWeightKg();
                double carbMult = day.getCarbMultiplier() != null ? day.getCarbMultiplier() : 0;
                double proteinMult = day.getProteinMultiplier() != null ? day.getProteinMultiplier() : 0;
                double fatMult = day.getFatMultiplier() != null ? day.getFatMultiplier() : 0;
                vo.setTargetCarb(w * carbMult);
                vo.setTargetProtein(w * proteinMult);
                vo.setTargetFat(w * fatMult);
                vo.setTargetCalories(calcCalories(
                        vo.getTargetCarb(), vo.getTargetProtein(), vo.getTargetFat()));
            }
        }

        // ---- 5. 今日训练 ----
        List<TrainingRecord> todayTraining = trainMapper.selectByUserAndDate(userId, today);
        if (todayTraining != null && !todayTraining.isEmpty()) {
            vo.setTodayTrainingType(todayTraining.get(0).getTrainingType());
            vo.setTodayTraining(todayTraining.get(0));
        }

        // ---- 6. 今日饮食记录 ----
        List<DietRecord> todayDietRecords = dietMapper.selectByUserAndDate(userId, today);
        if (todayDietRecords != null && !todayDietRecords.isEmpty()) {
            vo.setTodayDietRecords(todayDietRecords);
        }

        // ---- 7. 连续打卡天数 ----
        vo.setStreakDays(calcStreak(userId));

        // ---- 8. 体重趋势(近30天) ----
        try {
            List<WeightTrendVO> trend = weightService.trend(30);
            vo.setWeightTrend(trend);
        } catch (Exception e) {
            // 趋势数据加载失败不影响其他数据展示
            vo.setWeightTrend(Collections.emptyList());
        }

        return vo;
    }

    /**
     * 计算连续打卡天数
     * <p>
     * 从今天开始向前倒推，统计有训练记录的连续天数。
     * 如果今天没有记录，则从昨天开始检查。
     *
     * @param userId 用户ID
     * @return 连续打卡天数
     */
    private int calcStreak(Long userId) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        int streak = 0;
        // 如果今天还没有训练记录，从昨天开始统计
        Date today = cal.getTime();
        List<TrainingRecord> todayRecords = trainMapper.selectByUserAndDate(userId, today);
        if (todayRecords == null || todayRecords.isEmpty()) {
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }

        // 向前倒推，统计连续有训练记录的天数，最多统计365天
        for (int i = 0; i < 365; i++) {
            Date checkDate = cal.getTime();
            List<TrainingRecord> records = trainMapper.selectByUserAndDate(userId, checkDate);
            if (records != null && !records.isEmpty()) {
                streak++;
                cal.add(Calendar.DAY_OF_MONTH, -1);
            } else {
                break;
            }
        }

        return streak;
    }

    /**
     * 计算食物热量
     * <p>
     * 根据三大宏量营养素计算总热量:
     * 碳水 4 kcal/g, 蛋白质 4 kcal/g, 脂肪 9 kcal/g
     *
     * @param carb    碳水克数
     * @param protein 蛋白质克数
     * @param fat     脂肪克数
     * @return 总热量(kcal)
     */
    private double calcCalories(double carb, double protein, double fat) {
        return carb * 4 + protein * 4 + fat * 9;
    }

    /**
     * 四舍五入保留一位小数
     *
     * @param value 原始值
     * @return 四舍五入后的值
     */
    private Double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    /**
     * Object 转 Double
     * <p>
     * 处理 MyBatis 返回的 Map 中的数值类型转换
     *
     * @param obj Map中取出的值
     * @return double值，null则返回0.0
     */
    private Double toDouble(Object obj) {
        if (obj == null) return 0.0;
        return ((Number) obj).doubleValue();
    }

    /**
     * 获取今日日期(零点)
     *
     * @return 今日零点Date
     */
    private Date getToday() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * 获取昨日日期(零点)
     *
     * @return 昨日零点Date
     */
    private Date getYesterday() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        return cal.getTime();
    }
}
