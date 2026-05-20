package com.fitness.service;

import com.fitness.common.ResultCode;
import com.fitness.dto.PlanCreateDTO;
import com.fitness.dto.PlanVO;
import com.fitness.entity.*;
import com.fitness.exception.BusinessException;
import com.fitness.mapper.*;
import com.fitness.utils.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 健身计划服务
 * 处理健身计划的创建、编辑、删除、激活以及详情查看
 */
@Service
public class PlanService {

    private final FitnessPlanMapper planMapper;
    private final PlanTrainingDayMapper dayMapper;
    private final PlanTrainingActionMapper actionMapper;
    private final PlanMealConfigMapper mealConfigMapper;
    private final PlanMealFoodMapper mealFoodMapper;

    public PlanService(FitnessPlanMapper planMapper, PlanTrainingDayMapper dayMapper,
                       PlanTrainingActionMapper actionMapper, PlanMealConfigMapper mealConfigMapper,
                       PlanMealFoodMapper mealFoodMapper) {
        this.planMapper = planMapper;
        this.dayMapper = dayMapper;
        this.actionMapper = actionMapper;
        this.mealConfigMapper = mealConfigMapper;
        this.mealFoodMapper = mealFoodMapper;
    }

    /**
     * 获取当前用户的所有计划列表
     * @return 计划实体列表
     */
    public List<FitnessPlan> list() {
        return planMapper.selectByUserId(SecurityUtils.getCurrentUserId());
    }

    /**
     * 获取计划详情（含训练日和餐食配置）
     * @param id 计划 ID
     * @return 计划 VO（包含完整的训练日和餐食配置树）
     */
    public PlanVO getById(Long id) {
        FitnessPlan plan = planMapper.selectById(id);
        if (plan == null || !plan.getUserId().equals(SecurityUtils.getCurrentUserId())) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return buildVO(plan);
    }

    /**
     * 创建完整计划（含训练日、动作、餐食配置、食物）
     * @param dto 计划创建 DTO
     * @return 创建后的计划 VO
     */
    @Transactional
    public PlanVO create(PlanCreateDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        // 创建计划主记录
        FitnessPlan plan = new FitnessPlan();
        plan.setUserId(userId);
        plan.setPlanName(dto.getPlanName());
        plan.setPlanType(dto.getPlanType());
        plan.setSplitType(dto.getSplitType());
        plan.setIsActive(0);
        planMapper.insert(plan);

        // 创建训练日及其动作
        if (dto.getTrainingDays() != null) {
            int order = 1;
            for (PlanCreateDTO.TrainingDayItem dayItem : dto.getTrainingDays()) {
                PlanTrainingDay day = new PlanTrainingDay();
                day.setPlanId(plan.getId());
                day.setDayOrder(order++);
                day.setDayType(dayItem.getDayType());
                day.setTrainingType(dayItem.getTrainingType());
                day.setCarbMultiplier(dayItem.getCarbMultiplier() != null ? dayItem.getCarbMultiplier() : 0);
                day.setProteinMultiplier(dayItem.getProteinMultiplier() != null ? dayItem.getProteinMultiplier() : 0);
                day.setFatMultiplier(dayItem.getFatMultiplier() != null ? dayItem.getFatMultiplier() : 0);
                dayMapper.insert(day);

                // 插入训练日关联的动作
                if (dayItem.getActions() != null) {
                    int aOrder = 0;
                    for (PlanCreateDTO.ActionItem a : dayItem.getActions()) {
                        PlanTrainingAction action = new PlanTrainingAction();
                        action.setTrainingDayId(day.getId());
                        action.setActionId(a.getActionId());
                        action.setActionName(a.getActionName());
                        action.setMinSets(a.getMinSets() != null ? a.getMinSets() : 3);
                        action.setMaxSets(a.getMaxSets() != null ? a.getMaxSets() : 5);
                        action.setRestMinutes(a.getRestMinutes() != null ? a.getRestMinutes() : 2);
                        action.setSortOrder(a.getSortOrder() != null ? a.getSortOrder() : aOrder++);
                        actionMapper.insert(action);
                    }
                }
            }
        }

        // 创建餐食配置及食物
        if (dto.getMealConfigs() != null) {
            for (PlanCreateDTO.MealConfigItem mcItem : dto.getMealConfigs()) {
                PlanMealConfig mc = new PlanMealConfig();
                mc.setPlanId(plan.getId());
                mc.setDayType(mcItem.getDayType());
                mc.setMealType(mcItem.getMealType());
                mc.setCarbRatio(mcItem.getCarbRatio() != null ? mcItem.getCarbRatio() : 0);
                mc.setProteinRatio(mcItem.getProteinRatio() != null ? mcItem.getProteinRatio() : 0);
                mc.setFatRatio(mcItem.getFatRatio() != null ? mcItem.getFatRatio() : 0);
                mc.setSortOrder(mcItem.getSortOrder() != null ? mcItem.getSortOrder() : 0);
                mealConfigMapper.insert(mc);

                // 插入餐食关联的食物
                if (mcItem.getFoods() != null) {
                    int fOrder = 0;
                    for (PlanCreateDTO.MealFoodItem f : mcItem.getFoods()) {
                        PlanMealFood mf = new PlanMealFood();
                        mf.setMealConfigId(mc.getId());
                        mf.setFoodId(f.getFoodId());
                        mf.setFoodName(f.getFoodName());
                        mf.setSuggestedAmountG(f.getSuggestedAmountG() != null ? f.getSuggestedAmountG() : 100);
                        mf.setSortOrder(f.getSortOrder() != null ? f.getSortOrder() : fOrder++);
                        mealFoodMapper.insert(mf);
                    }
                }
            }
        }

        return getById(plan.getId());
    }

    /**
     * 更新计划（删除旧子记录后重建）
     * @param id 计划 ID
     * @param dto 计划创建 DTO
     * @return 更新后的计划 VO
     */
    @Transactional
    public PlanVO update(Long id, PlanCreateDTO dto) {
        FitnessPlan plan = planMapper.selectById(id);
        if (plan == null || !plan.getUserId().equals(SecurityUtils.getCurrentUserId())) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        plan.setPlanName(dto.getPlanName());
        plan.setPlanType(dto.getPlanType());
        plan.setSplitType(dto.getSplitType());
        planMapper.updateById(plan);

        // 删除旧子记录并重建（数据库级联删除动作和食物）
        dayMapper.deleteByPlanId(id);
        mealConfigMapper.deleteByPlanId(id);

        recreateChildren(id, dto);

        return getById(id);
    }

    /**
     * 删除计划
     * @param id 计划 ID
     */
    @Transactional
    public void delete(Long id) {
        FitnessPlan plan = planMapper.selectById(id);
        if (plan == null || !plan.getUserId().equals(SecurityUtils.getCurrentUserId())) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        // 先删除子表数据，防止孤儿记录
        mealConfigMapper.deleteByPlanId(id);
        dayMapper.deleteByPlanId(id);
        // 再删除主表
        planMapper.deleteById(id);
    }

    /**
     * 激活计划（先将用户所有计划设为未激活，再激活指定计划）
     * @param id 计划 ID
     */
    @Transactional
    public void activate(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        FitnessPlan plan = planMapper.selectById(id);
        if (plan == null || !plan.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        // 先将该用户所有计划设为未激活
        planMapper.deactivateByUser(userId);
        plan.setIsActive(1);
        planMapper.updateById(plan);
    }

    /**
     * 重建计划的子记录（训练日和餐食配置）
     */
    private void recreateChildren(Long planId, PlanCreateDTO dto) {
        if (dto.getTrainingDays() != null) {
            int order = 1;
            for (PlanCreateDTO.TrainingDayItem dayItem : dto.getTrainingDays()) {
                PlanTrainingDay day = new PlanTrainingDay();
                day.setPlanId(planId);
                day.setDayOrder(order++);
                day.setDayType(dayItem.getDayType());
                day.setTrainingType(dayItem.getTrainingType());
                day.setCarbMultiplier(dayItem.getCarbMultiplier() != null ? dayItem.getCarbMultiplier() : 0);
                day.setProteinMultiplier(dayItem.getProteinMultiplier() != null ? dayItem.getProteinMultiplier() : 0);
                day.setFatMultiplier(dayItem.getFatMultiplier() != null ? dayItem.getFatMultiplier() : 0);
                dayMapper.insert(day);
                if (dayItem.getActions() != null) {
                    int aOrder = 0;
                    for (PlanCreateDTO.ActionItem a : dayItem.getActions()) {
                        PlanTrainingAction action = new PlanTrainingAction();
                        action.setTrainingDayId(day.getId());
                        action.setActionId(a.getActionId());
                        action.setActionName(a.getActionName());
                        action.setMinSets(a.getMinSets() != null ? a.getMinSets() : 3);
                        action.setMaxSets(a.getMaxSets() != null ? a.getMaxSets() : 5);
                        action.setRestMinutes(a.getRestMinutes() != null ? a.getRestMinutes() : 2);
                        action.setSortOrder(a.getSortOrder() != null ? a.getSortOrder() : aOrder++);
                        actionMapper.insert(action);
                    }
                }
            }
        }
        if (dto.getMealConfigs() != null) {
            for (PlanCreateDTO.MealConfigItem mcItem : dto.getMealConfigs()) {
                PlanMealConfig mc = new PlanMealConfig();
                mc.setPlanId(planId);
                mc.setDayType(mcItem.getDayType());
                mc.setMealType(mcItem.getMealType());
                mc.setCarbRatio(mcItem.getCarbRatio() != null ? mcItem.getCarbRatio() : 0);
                mc.setProteinRatio(mcItem.getProteinRatio() != null ? mcItem.getProteinRatio() : 0);
                mc.setFatRatio(mcItem.getFatRatio() != null ? mcItem.getFatRatio() : 0);
                mc.setSortOrder(mcItem.getSortOrder() != null ? mcItem.getSortOrder() : 0);
                mealConfigMapper.insert(mc);
                if (mcItem.getFoods() != null) {
                    int fOrder = 0;
                    for (PlanCreateDTO.MealFoodItem f : mcItem.getFoods()) {
                        PlanMealFood mf = new PlanMealFood();
                        mf.setMealConfigId(mc.getId());
                        mf.setFoodId(f.getFoodId());
                        mf.setFoodName(f.getFoodName());
                        mf.setSuggestedAmountG(f.getSuggestedAmountG() != null ? f.getSuggestedAmountG() : 100);
                        mf.setSortOrder(f.getSortOrder() != null ? f.getSortOrder() : fOrder++);
                        mealFoodMapper.insert(mf);
                    }
                }
            }
        }
    }

    /**
     * 构建 PlanVO 完整对象树
     * 从数据库加载计划的训练日、动作、餐食配置和食物，组装为 VO 树
     */
    private PlanVO buildVO(FitnessPlan plan) {
        PlanVO vo = new PlanVO();
        vo.setId(plan.getId());
        vo.setPlanName(plan.getPlanName());
        vo.setPlanType(plan.getPlanType());
        vo.setSplitType(plan.getSplitType());
        vo.setIsActive(plan.getIsActive());
        vo.setCreatedAt(plan.getCreatedAt());

        // 组装训练日及动作
        List<PlanTrainingDay> days = dayMapper.selectByPlanId(plan.getId());
        List<PlanVO.TrainingDayVO> dayVOs = new ArrayList<>();
        for (PlanTrainingDay day : days) {
            PlanVO.TrainingDayVO dvo = new PlanVO.TrainingDayVO();
            dvo.setId(day.getId());
            dvo.setDayOrder(day.getDayOrder());
            dvo.setDayType(day.getDayType());
            dvo.setTrainingType(day.getTrainingType());
            dvo.setCarbMultiplier(day.getCarbMultiplier());
            dvo.setProteinMultiplier(day.getProteinMultiplier());
            dvo.setFatMultiplier(day.getFatMultiplier());

            List<PlanTrainingAction> actions = actionMapper.selectByDayId(day.getId());
            List<PlanVO.ActionVO> actionVOs = new ArrayList<>();
            for (PlanTrainingAction a : actions) {
                PlanVO.ActionVO avo = new PlanVO.ActionVO();
                avo.setId(a.getId());
                avo.setActionId(a.getActionId());
                avo.setActionName(a.getActionName());
                avo.setMinSets(a.getMinSets());
                avo.setMaxSets(a.getMaxSets());
                avo.setRestMinutes(a.getRestMinutes());
                avo.setSortOrder(a.getSortOrder());
                actionVOs.add(avo);
            }
            dvo.setActions(actionVOs);
            dayVOs.add(dvo);
        }
        vo.setTrainingDays(dayVOs);

        // 组装餐食配置及食物
        List<PlanMealConfig> mealConfigs = mealConfigMapper.selectByPlanId(plan.getId());
        List<PlanVO.MealConfigVO> mcVOs = new ArrayList<>();
        for (PlanMealConfig mc : mealConfigs) {
            PlanVO.MealConfigVO mcvo = new PlanVO.MealConfigVO();
            mcvo.setId(mc.getId());
            mcvo.setDayType(mc.getDayType());
            mcvo.setMealType(mc.getMealType());
            mcvo.setCarbRatio(mc.getCarbRatio());
            mcvo.setProteinRatio(mc.getProteinRatio());
            mcvo.setFatRatio(mc.getFatRatio());
            mcvo.setSortOrder(mc.getSortOrder());

            List<PlanMealFood> foods = mealFoodMapper.selectByMealConfigId(mc.getId());
            List<PlanVO.MealFoodVO> foodVOs = new ArrayList<>();
            for (PlanMealFood f : foods) {
                PlanVO.MealFoodVO fvo = new PlanVO.MealFoodVO();
                fvo.setId(f.getId());
                fvo.setFoodId(f.getFoodId());
                fvo.setFoodName(f.getFoodName());
                fvo.setSuggestedAmountG(f.getSuggestedAmountG());
                fvo.setSortOrder(f.getSortOrder());
                foodVOs.add(fvo);
            }
            mcvo.setFoods(foodVOs);
            mcVOs.add(mcvo);
        }
        vo.setMealConfigs(mcVOs);

        return vo;
    }
}
