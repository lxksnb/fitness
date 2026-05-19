package com.fitness.service;

import com.fitness.common.ResultCode;
import com.fitness.dto.DailySummaryVO;
import com.fitness.dto.DietRecordDTO;
import com.fitness.entity.*;
import com.fitness.exception.BusinessException;
import com.fitness.mapper.*;
import com.fitness.utils.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 饮食记录服务
 * 处理每日饮食记录的增删改查，并提供每日营养摄入汇总分析
 */
@Service
public class DietService {

    private final DietRecordMapper dietMapper;
    private final FitnessPlanMapper planMapper;
    private final PlanTrainingDayMapper dayMapper;
    private final WeightRecordMapper weightMapper;

    public DietService(DietRecordMapper dietMapper, FitnessPlanMapper planMapper,
                       PlanTrainingDayMapper dayMapper, WeightRecordMapper weightMapper) {
        this.dietMapper = dietMapper;
        this.planMapper = planMapper;
        this.dayMapper = dayMapper;
        this.weightMapper = weightMapper;
    }

    /**
     * 获取指定日期的饮食记录列表
     * @param date 查询日期
     * @return 饮食记录列表
     */
    public List<DietRecord> listByDate(Date date) {
        return dietMapper.selectByUserAndDate(SecurityUtils.getCurrentUserId(), date);
    }

    /**
     * 创建饮食记录
     * 自动根据宏量营养素计算卡路里
     *
     * @param dto 饮食记录 DTO
     * @return 创建的饮食记录
     */
    @Transactional
    public DietRecord create(DietRecordDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        DietRecord record = new DietRecord();
        record.setUserId(userId);
        record.setRecordDate(dto.getRecordDate());
        record.setMealType(dto.getMealType());
        record.setFoodName(dto.getFoodName());
        record.setImageUrl(dto.getImageUrl());
        record.setCarbGrams(dto.getCarbGrams() != null ? dto.getCarbGrams() : 0);
        record.setProteinGrams(dto.getProteinGrams() != null ? dto.getProteinGrams() : 0);
        record.setFatGrams(dto.getFatGrams() != null ? dto.getFatGrams() : 0);
        record.setCalories(calcCalories(record.getCarbGrams(), record.getProteinGrams(), record.getFatGrams()));
        record.setRecordedAt(dto.getRecordedAt() != null ? dto.getRecordedAt() : new Date());
        dietMapper.insert(record);
        return record;
    }

    /**
     * 更新饮食记录
     * @param id 记录 ID
     * @param dto 饮食记录 DTO
     */
    @Transactional
    public void update(Long id, DietRecordDTO dto) {
        DietRecord record = dietMapper.selectById(id);
        if (record == null || !record.getUserId().equals(SecurityUtils.getCurrentUserId())) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        record.setMealType(dto.getMealType());
        record.setFoodName(dto.getFoodName());
        record.setImageUrl(dto.getImageUrl());
        record.setCarbGrams(dto.getCarbGrams() != null ? dto.getCarbGrams() : 0);
        record.setProteinGrams(dto.getProteinGrams() != null ? dto.getProteinGrams() : 0);
        record.setFatGrams(dto.getFatGrams() != null ? dto.getFatGrams() : 0);
        // 根据更新的宏量营养素重新计算卡路里
        record.setCalories(calcCalories(record.getCarbGrams(), record.getProteinGrams(), record.getFatGrams()));
        dietMapper.updateById(record);
    }

    /**
     * 删除饮食记录
     * @param id 记录 ID
     */
    @Transactional
    public void delete(Long id) {
        DietRecord record = dietMapper.selectById(id);
        if (record == null || !record.getUserId().equals(SecurityUtils.getCurrentUserId())) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        dietMapper.deleteById(id);
    }

    /**
     * 获取每日饮食摄入汇总
     * 对比实际摄入与计划目标（如用户已激活计划）
     *
     * @param date 查询日期
     * @return 每日汇总 VO，包含实际摄入和计划目标
     */
    public DailySummaryVO dailySummary(Date date) {
        Long userId = SecurityUtils.getCurrentUserId();
        DailySummaryVO vo = new DailySummaryVO();

        // 获取当日实际摄入汇总
        Map<String, Object> summary = dietMapper.selectDailySummary(userId, date);
        if (summary != null) {
            vo.setActualCarb(toDouble(summary.get("carbGrams")));
            vo.setActualProtein(toDouble(summary.get("proteinGrams")));
            vo.setActualFat(toDouble(summary.get("fatGrams")));
            vo.setActualCalories(toDouble(summary.get("calories")));
        } else {
            vo.setActualCarb(0.0); vo.setActualProtein(0.0);
            vo.setActualFat(0.0); vo.setActualCalories(0.0);
        }

        // 从激活的计划中获取目标摄入量
        List<FitnessPlan> activePlans = planMapper.selectActiveByUser(userId);
        FitnessPlan activePlan = (activePlans != null && !activePlans.isEmpty()) ? activePlans.get(0) : null;
        if (activePlan != null) {
            List<PlanTrainingDay> days = dayMapper.selectByPlanId(activePlan.getId());
            WeightRecord latestWeight = weightMapper.selectLatestByUser(userId);
            double weight = latestWeight != null ? latestWeight.getWeightKg() : 65.0;

            // 尝试根据计划轮换匹配当日训练日
            // 简化处理：使用第一个训练日的营养素倍率
            if (!days.isEmpty()) {
                PlanTrainingDay today = days.get(0); // 默认取第一个训练日
                vo.setTargetCarb(weight * today.getCarbMultiplier());
                vo.setTargetProtein(weight * today.getProteinMultiplier());
                vo.setTargetFat(weight * today.getFatMultiplier());
            }
        }

        // 如果没有目标值，使用默认推荐值
        if (vo.getTargetCarb() == null) {
            vo.setTargetCarb(200.0); vo.setTargetProtein(100.0);
            vo.setTargetFat(50.0);
        }
        vo.setTargetCalories(calcCalories(vo.getTargetCarb(), vo.getTargetProtein(), vo.getTargetFat()));

        return vo;
    }

    /**
     * 根据宏量营养素计算卡路里
     * 公式：碳水*4 + 蛋白质*4 + 脂肪*9
     */
    private double calcCalories(double carb, double protein, double fat) {
        return carb * 4 + protein * 4 + fat * 9;
    }

    /** 安全地将 Object 转换为 Double */
    private Double toDouble(Object obj) {
        if (obj == null) return 0.0;
        return ((Number) obj).doubleValue();
    }
}
