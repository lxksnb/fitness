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

    public List<DietRecord> listByDate(Date date) {
        return dietMapper.selectByUserAndDate(SecurityUtils.getCurrentUserId(), date);
    }

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
        record.setCalories(calcCalories(record.getCarbGrams(), record.getProteinGrams(), record.getFatGrams()));
        dietMapper.updateById(record);
    }

    @Transactional
    public void delete(Long id) {
        DietRecord record = dietMapper.selectById(id);
        if (record == null || !record.getUserId().equals(SecurityUtils.getCurrentUserId())) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        dietMapper.deleteById(id);
    }

    public DailySummaryVO dailySummary(Date date) {
        Long userId = SecurityUtils.getCurrentUserId();
        DailySummaryVO vo = new DailySummaryVO();

        // Actual intake
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

        // Target from active plan
        List<FitnessPlan> activePlans = planMapper.selectActiveByUser(userId);
        FitnessPlan activePlan = (activePlans != null && !activePlans.isEmpty()) ? activePlans.get(0) : null;
        if (activePlan != null) {
            List<PlanTrainingDay> days = dayMapper.selectByPlanId(activePlan.getId());
            WeightRecord latestWeight = weightMapper.selectLatestByUser(userId);
            double weight = latestWeight != null ? latestWeight.getWeightKg() : 65.0;

            // Try to find today's training day based on plan rotation
            // Simplified: use the first training day's multipliers
            if (!days.isEmpty()) {
                PlanTrainingDay today = days.get(0); // Default to first day
                vo.setTargetCarb(weight * today.getCarbMultiplier());
                vo.setTargetProtein(weight * today.getProteinMultiplier());
                vo.setTargetFat(weight * today.getFatMultiplier());
            }
        }

        if (vo.getTargetCarb() == null) {
            vo.setTargetCarb(200.0); vo.setTargetProtein(100.0);
            vo.setTargetFat(50.0);
        }
        vo.setTargetCalories(calcCalories(vo.getTargetCarb(), vo.getTargetProtein(), vo.getTargetFat()));

        return vo;
    }

    private double calcCalories(double carb, double protein, double fat) {
        return carb * 4 + protein * 4 + fat * 9;
    }

    private Double toDouble(Object obj) {
        if (obj == null) return 0.0;
        return ((Number) obj).doubleValue();
    }
}
