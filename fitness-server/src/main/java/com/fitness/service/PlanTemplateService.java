package com.fitness.service;

import com.fitness.common.ResultCode;
import com.fitness.dto.PlanCreateDTO;
import com.fitness.dto.PlanTemplateUpdateDTO;
import com.fitness.dto.PlanTemplateVO;
import com.fitness.entity.*;
import com.fitness.exception.BusinessException;
import com.fitness.mapper.*;
import com.fitness.utils.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 计划模板服务
 * 管理员对系统级训练计划模板的增删改查，模板可供用户浏览和导入
 */
@Service
public class PlanTemplateService {

    private final PlanTemplateMapper templateMapper;
    private final PlanTemplateDayMapper templateDayMapper;
    private final PlanTemplateActionMapper templateActionMapper;
    private final PlanTemplateMealConfigMapper templateMealConfigMapper;
    private final PlanTemplateMealFoodMapper templateMealFoodMapper;

    public PlanTemplateService(PlanTemplateMapper templateMapper,
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
     * 获取所有活跃模板列表
     * @return 模板 VO 列表
     */
    public List<PlanTemplateVO> list() {
        List<PlanTemplate> templates = templateMapper.selectAll();
        List<PlanTemplateVO> vos = new ArrayList<>(templates.size());
        for (PlanTemplate t : templates) {
            vos.add(toVO(t));
        }
        return vos;
    }

    /**
     * 创建计划模板（含训练日、动作、餐次配置）
     * @param dto 计划创建 DTO
     */
    @Transactional
    public void create(PlanCreateDTO dto) {
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
    }

    /**
     * 更新模板基本信息
     * @param id  模板 ID
     * @param dto 更新 DTO（仅含基本信息字段）
     */
    @Transactional
    public void update(Long id, PlanTemplateUpdateDTO dto) {
        PlanTemplate template = templateMapper.selectById(id);
        if (template == null || !"ACTIVE".equals(template.getStatus())) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        template.setTemplateName(dto.getTemplateName());
        template.setDescription(dto.getDescription());
        template.setPlanType(dto.getPlanType());
        template.setSplitType(dto.getSplitType());
        template.setDifficulty(dto.getDifficulty());
        templateMapper.updateById(template);
    }

    /**
     * 删除模板（软删除）
     * 将模板状态设为 DELETED
     * @param id 模板 ID
     */
    @Transactional
    public void delete(Long id) {
        PlanTemplate template = templateMapper.selectById(id);
        if (template == null || !"ACTIVE".equals(template.getStatus())) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        template.setStatus("DELETED");
        templateMapper.updateById(template);
    }

    /**
     * 将实体转换为 VO
     */
    private PlanTemplateVO toVO(PlanTemplate t) {
        PlanTemplateVO vo = new PlanTemplateVO();
        vo.setId(t.getId());
        vo.setTemplateName(t.getTemplateName());
        vo.setDescription(t.getDescription());
        vo.setPlanType(t.getPlanType());
        vo.setSplitType(t.getSplitType());
        vo.setDifficulty(t.getDifficulty());
        vo.setCreatedAt(t.getCreatedAt());
        return vo;
    }
}
