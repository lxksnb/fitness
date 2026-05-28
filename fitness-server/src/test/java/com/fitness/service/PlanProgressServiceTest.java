package com.fitness.service;

import com.fitness.entity.FitnessPlan;
import com.fitness.entity.PlanTrainingDay;
import com.fitness.entity.TrainingRecord;
import com.fitness.mapper.FitnessPlanMapper;
import com.fitness.mapper.PlanTrainingDayMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanProgressServiceTest {

    @Mock
    private FitnessPlanMapper planMapper;
    @Mock
    private PlanTrainingDayMapper dayMapper;
    @InjectMocks
    private PlanProgressService planProgressService;

    @Test
    void advanceAfterTrainingMovesOnlyWhenRecordMatchesCurrentDay() {
        FitnessPlan plan = activePlan(10L, 1L, 2);
        PlanTrainingDay day1 = planDay(101L, 1);
        PlanTrainingDay day2 = planDay(102L, 2);
        PlanTrainingDay day3 = planDay(103L, 3);
        TrainingRecord record = new TrainingRecord();
        record.setPlanId(10L);
        record.setTrainingDayId(102L);

        when(planMapper.selectActiveByUser(1L)).thenReturn(Collections.singletonList(plan));
        when(dayMapper.selectByPlanId(10L)).thenReturn(Arrays.asList(day1, day2, day3));

        planProgressService.advanceAfterTraining(1L, record);

        verify(planMapper).updateCurrentDayOrder(1L, 10L, 3);
    }

    @Test
    void advanceAfterTrainingDoesNotMoveWhenRecordIsNotCurrentDay() {
        FitnessPlan plan = activePlan(10L, 1L, 2);
        PlanTrainingDay day1 = planDay(101L, 1);
        PlanTrainingDay day2 = planDay(102L, 2);
        TrainingRecord record = new TrainingRecord();
        record.setPlanId(10L);
        record.setTrainingDayId(101L);

        when(planMapper.selectActiveByUser(1L)).thenReturn(Collections.singletonList(plan));
        when(dayMapper.selectByPlanId(10L)).thenReturn(Arrays.asList(day1, day2));

        planProgressService.advanceAfterTraining(1L, record);

        verify(planMapper, never()).updateCurrentDayOrder(1L, 10L, 1);
        verify(planMapper, never()).updateCurrentDayOrder(1L, 10L, 2);
    }

    @Test
    void skipCurrentDayWrapsToFirstDayAfterLastDay() {
        FitnessPlan plan = activePlan(10L, 1L, 3);
        when(planMapper.selectActiveByUser(1L)).thenReturn(Collections.singletonList(plan));
        when(dayMapper.selectByPlanId(10L)).thenReturn(Arrays.asList(
            planDay(101L, 1),
            planDay(102L, 2),
            planDay(103L, 3)
        ));

        PlanTrainingDay nextDay = planProgressService.skipCurrentDay(1L, 10L);

        assertEquals(1, nextDay.getDayOrder());
        verify(planMapper).updateCurrentDayOrder(1L, 10L, 1);
    }

    private FitnessPlan activePlan(Long planId, Long userId, Integer currentDayOrder) {
        FitnessPlan plan = new FitnessPlan();
        plan.setId(planId);
        plan.setUserId(userId);
        plan.setIsActive(1);
        plan.setCurrentDayOrder(currentDayOrder);
        return plan;
    }

    private PlanTrainingDay planDay(Long id, Integer dayOrder) {
        PlanTrainingDay day = new PlanTrainingDay();
        day.setId(id);
        day.setPlanId(10L);
        day.setDayOrder(dayOrder);
        day.setDayType("TRAINING");
        day.setTrainingType("CHEST");
        return day;
    }
}
