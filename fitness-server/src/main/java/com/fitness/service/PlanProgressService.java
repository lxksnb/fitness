package com.fitness.service;

import com.fitness.common.ResultCode;
import com.fitness.entity.FitnessPlan;
import com.fitness.entity.PlanTrainingDay;
import com.fitness.entity.TrainingRecord;
import com.fitness.exception.BusinessException;
import com.fitness.mapper.FitnessPlanMapper;
import com.fitness.mapper.PlanTrainingDayMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 计划执行进度服务
 * 以用户实际完成或主动跳过作为计划推进依据，避免按自然日轮换导致计划错位。
 */
@Service
public class PlanProgressService {

    private final FitnessPlanMapper planMapper;
    private final PlanTrainingDayMapper dayMapper;

    public PlanProgressService(FitnessPlanMapper planMapper, PlanTrainingDayMapper dayMapper) {
        this.planMapper = planMapper;
        this.dayMapper = dayMapper;
    }

    /**
     * 获取激活计划当前待执行训练日。
     */
    public PlanTrainingDay getCurrentDay(FitnessPlan plan) {
        if (plan == null || plan.getId() == null) {
            return null;
        }
        List<PlanTrainingDay> days = sortedDays(plan.getId());
        if (days.isEmpty()) {
            return null;
        }
        Integer currentOrder = plan.getCurrentDayOrder();
        for (PlanTrainingDay day : days) {
            if (Objects.equals(day.getDayOrder(), currentOrder)) {
                return day;
            }
        }
        return days.get(0);
    }

    /**
     * 训练记录命中当前待执行训练日时，推进计划到下一日。
     */
    public void advanceAfterTraining(Long userId, TrainingRecord record) {
        if (record == null || record.getPlanId() == null || record.getTrainingDayId() == null) {
            return;
        }
        FitnessPlan activePlan = getActivePlan(userId);
        if (activePlan == null || !Objects.equals(activePlan.getId(), record.getPlanId())) {
            return;
        }
        PlanTrainingDay currentDay = getCurrentDay(activePlan);
        if (currentDay == null || !Objects.equals(currentDay.getId(), record.getTrainingDayId())) {
            return;
        }
        advanceToNextDay(userId, activePlan);
    }

    /**
     * 用户主动跳过当前训练日时，推进到下一日。
     */
    public PlanTrainingDay skipCurrentDay(Long userId, Long planId) {
        FitnessPlan activePlan = getActivePlan(userId);
        if (activePlan == null || !Objects.equals(activePlan.getId(), planId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "当前激活计划不存在");
        }
        return advanceToNextDay(userId, activePlan);
    }

    private FitnessPlan getActivePlan(Long userId) {
        List<FitnessPlan> activePlans = planMapper.selectActiveByUser(userId);
        return activePlans != null && !activePlans.isEmpty() ? activePlans.get(0) : null;
    }

    private PlanTrainingDay advanceToNextDay(Long userId, FitnessPlan plan) {
        List<PlanTrainingDay> days = sortedDays(plan.getId());
        if (days.isEmpty()) {
            throw new BusinessException(ResultCode.NOT_FOUND, "计划训练日不存在");
        }
        PlanTrainingDay currentDay = getCurrentDay(plan);
        int currentIndex = Math.max(days.indexOf(currentDay), 0);
        PlanTrainingDay nextDay = days.get((currentIndex + 1) % days.size());
        planMapper.updateCurrentDayOrder(userId, plan.getId(), nextDay.getDayOrder());
        return nextDay;
    }

    private List<PlanTrainingDay> sortedDays(Long planId) {
        List<PlanTrainingDay> days = dayMapper.selectByPlanId(planId);
        if (days == null) {
            return Collections.emptyList();
        }
        days.sort(Comparator.comparing(PlanTrainingDay::getDayOrder));
        return days;
    }
}
