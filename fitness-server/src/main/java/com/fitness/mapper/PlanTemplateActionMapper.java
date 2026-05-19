package com.fitness.mapper;

import com.fitness.entity.PlanTemplateAction;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface PlanTemplateActionMapper {
    List<PlanTemplateAction> selectByTemplateDayId(Long templateDayId);
    int insert(PlanTemplateAction action);
    int batchInsert(List<PlanTemplateAction> list);
    int deleteByTemplateDayId(Long templateDayId);
}
