package com.fitness.controller;

import com.fitness.common.Result;
import com.fitness.dto.PlanCreateDTO;
import com.fitness.dto.PlanVO;
import com.fitness.entity.*;
import com.fitness.mapper.*;
import com.fitness.service.PlanService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 计划模板控制器
 * 用户可浏览系统模板列表并一键导入为自己的训练计划
 */
@RestController
@RequestMapping("/api/v1/plan-templates")
public class PlanTemplateController {

    private final PlanTemplateMapper templateMapper;
    private final PlanTemplateDayMapper templateDayMapper;
    private final PlanTemplateActionMapper templateActionMapper;
    private final PlanTemplateMealConfigMapper templateMealConfigMapper;
    private final PlanTemplateMealFoodMapper templateMealFoodMapper;
    private final PlanService planService;

    /**
     * 构造函数 - 注入模板相关Mapper和计划服务
     */
    public PlanTemplateController(PlanTemplateMapper templateMapper,
                                   PlanTemplateDayMapper templateDayMapper,
                                   PlanTemplateActionMapper templateActionMapper,
                                   PlanTemplateMealConfigMapper templateMealConfigMapper,
                                   PlanTemplateMealFoodMapper templateMealFoodMapper,
                                   PlanService planService) {
        this.templateMapper = templateMapper;
        this.templateDayMapper = templateDayMapper;
        this.templateActionMapper = templateActionMapper;
        this.templateMealConfigMapper = templateMealConfigMapper;
        this.templateMealFoodMapper = templateMealFoodMapper;
        this.planService = planService;
    }

    /**
     * 浏览所有可用模板列表
     */
    @GetMapping
    public Result<List<PlanTemplate>> list() {
        return Result.ok(templateMapper.selectAll());
    }

    /**
     * 获取单个模板详情(含训练日和餐次配置)
     */
    @GetMapping("/{id}")
    public Result<PlanCreateDTO> getById(@PathVariable Long id) {
        PlanTemplate template = templateMapper.selectById(id);
        if (template == null) {
            return Result.ok(null);
        }
        return Result.ok(buildTemplateDTO(template));
    }

    /**
     * 一键导入模板为个人计划
     * 将系统模板复制为用户自己的训练计划
     */
    @PostMapping("/{id}/import")
    @Transactional
    public Result<PlanVO> importTemplate(@PathVariable Long id) {
        PlanTemplate template = templateMapper.selectById(id);
        if (template == null) {
            return Result.ok(null);
        }
        // 将模板转换为PlanCreateDTO，然后通过PlanService创建用户计划
        PlanCreateDTO dto = buildTemplateDTO(template);
        return Result.ok(planService.create(dto));
    }

    /**
     * 将模板实体转换为PlanCreateDTO
     * 包含训练日、动作、餐次配置的完整数据
     */
    private PlanCreateDTO buildTemplateDTO(PlanTemplate template) {
        PlanCreateDTO dto = new PlanCreateDTO();
        dto.setTemplateName(template.getTemplateName());
        dto.setPlanType(template.getPlanType());
        dto.setSplitType(template.getSplitType());
        dto.setDifficulty(template.getDifficulty());

        // 转换训练日
        List<PlanTemplateDay> days = templateDayMapper.selectByTemplateId(template.getId());
        if (days != null && !days.isEmpty()) {
            List<PlanCreateDTO.TrainingDayItem> dayItems = new ArrayList<>();
            for (PlanTemplateDay day : days) {
                PlanCreateDTO.TrainingDayItem dayItem = new PlanCreateDTO.TrainingDayItem();
                dayItem.setDayOrder(day.getDayOrder());
                dayItem.setDayType(day.getDayType());
                dayItem.setTrainingType(day.getTrainingType());
                dayItem.setCarbMultiplier(day.getCarbMultiplier());
                dayItem.setProteinMultiplier(day.getProteinMultiplier());
                dayItem.setFatMultiplier(day.getFatMultiplier());

                // 转换动作
                List<PlanTemplateAction> actions = templateActionMapper.selectByTemplateDayId(day.getId());
                if (actions != null && !actions.isEmpty()) {
                    List<PlanCreateDTO.ActionItem> actionItems = new ArrayList<>();
                    for (PlanTemplateAction a : actions) {
                        PlanCreateDTO.ActionItem actionItem = new PlanCreateDTO.ActionItem();
                        actionItem.setActionId(a.getActionId());
                        actionItem.setActionName(a.getActionName());
                        actionItem.setMinSets(a.getMinSets());
                        actionItem.setMaxSets(a.getMaxSets());
                        actionItem.setRestMinutes(a.getRestMinutes());
                        actionItem.setSortOrder(a.getSortOrder());
                        actionItems.add(actionItem);
                    }
                    dayItem.setActions(actionItems);
                }
                dayItems.add(dayItem);
            }
            dto.setTrainingDays(dayItems);
        }

        // 转换餐次配置
        List<PlanTemplateMealConfig> mealConfigs = templateMealConfigMapper.selectByTemplateId(template.getId());
        if (mealConfigs != null && !mealConfigs.isEmpty()) {
            List<PlanCreateDTO.MealConfigItem> mcItems = new ArrayList<>();
            for (PlanTemplateMealConfig mc : mealConfigs) {
                PlanCreateDTO.MealConfigItem mcItem = new PlanCreateDTO.MealConfigItem();
                mcItem.setDayType(mc.getDayType());
                mcItem.setMealType(mc.getMealType());
                mcItem.setCarbRatio(mc.getCarbRatio());
                mcItem.setProteinRatio(mc.getProteinRatio());
                mcItem.setFatRatio(mc.getFatRatio());
                mcItem.setSortOrder(mc.getSortOrder());

                // 转换食物
                List<PlanTemplateMealFood> foods = templateMealFoodMapper.selectByMealConfigId(mc.getId());
                if (foods != null && !foods.isEmpty()) {
                    List<PlanCreateDTO.MealFoodItem> foodItems = new ArrayList<>();
                    for (PlanTemplateMealFood f : foods) {
                        PlanCreateDTO.MealFoodItem foodItem = new PlanCreateDTO.MealFoodItem();
                        foodItem.setFoodId(f.getFoodId());
                        foodItem.setFoodName(f.getFoodName());
                        foodItem.setSuggestedAmountG(f.getSuggestedAmountG());
                        foodItem.setSortOrder(f.getSortOrder());
                        foodItems.add(foodItem);
                    }
                    mcItem.setFoods(foodItems);
                }
                mcItems.add(mcItem);
            }
            dto.setMealConfigs(mcItems);
        }

        return dto;
    }
}
