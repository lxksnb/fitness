package com.fitness.controller.admin;

import com.fitness.common.Result;
import com.fitness.dto.PlanCreateDTO;
import com.fitness.entity.*;
import com.fitness.mapper.*;
import com.fitness.utils.SecurityUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员计划模板管理控制器
 * 管理员可管理系统级别的训练计划模板
 */
@RestController
@RequestMapping("/api/v1/admin/templates")
public class AdminTemplateController {

    private final PlanTemplateMapper templateMapper;
    private final PlanTemplateDayMapper templateDayMapper;
    private final PlanTemplateActionMapper templateActionMapper;
    private final PlanTemplateMealConfigMapper templateMealConfigMapper;
    private final PlanTemplateMealFoodMapper templateMealFoodMapper;

    /**
     * 构造函数 - 注入模板相关的所有Mapper
     */
    public AdminTemplateController(PlanTemplateMapper templateMapper,
                                   PlanTemplateDayMapper templateDayMapper,
                                   PlanTemplateActionMapper templateActionMapper,
                                   PlanTemplateMealConfigMapper templateMealConfigMapper,
                                   PlanTemplateMealFoodMapper templateMealFoodMapper) {
        this.templateMapper = templateMapper;
        this.templateDayMapper = templateDayMapper;
        this.templateActionMapper = templateActionMapper;
        this.templateMealConfigMapper = templateMealConfigMapper;
        this.templateMealFoodMapper = templateMealFoodMapper;
    }

    /**
     * 获取所有模板列表
     */
    @GetMapping
    public Result<List<PlanTemplate>> list() {
        return Result.ok(templateMapper.selectAll());
    }

    /**
     * 创建计划模板(含训练日、动作、餐次配置)
     * 管理员创建系统级别的模板供用户浏览和导入
     */
    @PostMapping
    @Transactional
    public Result<?> create(@RequestBody PlanCreateDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();

        // 创建模板主记录
        PlanTemplate template = new PlanTemplate();
        template.setTemplateName(dto.getTemplateName());
        template.setPlanType(dto.getPlanType());
        template.setSplitType(dto.getSplitType());
        template.setDifficulty(dto.getDifficulty());
        template.setCreatedBy(userId);
        template.setStatus("ACTIVE");
        templateMapper.insert(template);

        // 创建训练日及其动作
        if (dto.getTrainingDays() != null) {
            int order = 1;
            for (PlanCreateDTO.TrainingDayItem dayItem : dto.getTrainingDays()) {
                PlanTemplateDay day = new PlanTemplateDay();
                day.setTemplateId(template.getId());
                day.setDayOrder(order++);
                day.setDayType(dayItem.getDayType());
                day.setTrainingType(dayItem.getTrainingType());
                day.setCarbMultiplier(dayItem.getCarbMultiplier() != null ? dayItem.getCarbMultiplier() : 0);
                day.setProteinMultiplier(dayItem.getProteinMultiplier() != null ? dayItem.getProteinMultiplier() : 0);
                day.setFatMultiplier(dayItem.getFatMultiplier() != null ? dayItem.getFatMultiplier() : 0);
                templateDayMapper.insert(day);

                if (dayItem.getActions() != null) {
                    int aOrder = 0;
                    for (PlanCreateDTO.ActionItem a : dayItem.getActions()) {
                        PlanTemplateAction action = new PlanTemplateAction();
                        action.setTemplateDayId(day.getId());
                        action.setActionId(a.getActionId());
                        action.setActionName(a.getActionName());
                        action.setMinSets(a.getMinSets() != null ? a.getMinSets() : 3);
                        action.setMaxSets(a.getMaxSets() != null ? a.getMaxSets() : 5);
                        action.setRestMinutes(a.getRestMinutes() != null ? a.getRestMinutes() : 2);
                        action.setSortOrder(a.getSortOrder() != null ? a.getSortOrder() : aOrder++);
                        templateActionMapper.insert(action);
                    }
                }
            }
        }

        // 创建餐次配置及其食物
        if (dto.getMealConfigs() != null) {
            for (PlanCreateDTO.MealConfigItem mcItem : dto.getMealConfigs()) {
                PlanTemplateMealConfig mc = new PlanTemplateMealConfig();
                mc.setTemplateId(template.getId());
                mc.setDayType(mcItem.getDayType());
                mc.setMealType(mcItem.getMealType());
                mc.setCarbRatio(mcItem.getCarbRatio() != null ? mcItem.getCarbRatio() : 0);
                mc.setProteinRatio(mcItem.getProteinRatio() != null ? mcItem.getProteinRatio() : 0);
                mc.setFatRatio(mcItem.getFatRatio() != null ? mcItem.getFatRatio() : 0);
                mc.setSortOrder(mcItem.getSortOrder() != null ? mcItem.getSortOrder() : 0);
                templateMealConfigMapper.insert(mc);

                if (mcItem.getFoods() != null) {
                    int fOrder = 0;
                    for (PlanCreateDTO.MealFoodItem f : mcItem.getFoods()) {
                        PlanTemplateMealFood mf = new PlanTemplateMealFood();
                        mf.setMealConfigId(mc.getId());
                        mf.setFoodId(f.getFoodId());
                        mf.setFoodName(f.getFoodName());
                        mf.setSuggestedAmountG(f.getSuggestedAmountG() != null ? f.getSuggestedAmountG() : 100);
                        mf.setSortOrder(f.getSortOrder() != null ? f.getSortOrder() : fOrder++);
                        templateMealFoodMapper.insert(mf);
                    }
                }
            }
        }

        return Result.ok();
    }

    /**
     * 删除模板(软删除)
     * 将模板状态设为DELETED
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        PlanTemplate template = templateMapper.selectById(id);
        if (template != null) {
            template.setStatus("DELETED");
            templateMapper.updateById(template);
        }
        return Result.ok();
    }
}
