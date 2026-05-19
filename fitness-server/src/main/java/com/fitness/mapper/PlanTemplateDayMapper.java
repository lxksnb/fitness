package com.fitness.mapper;

import com.fitness.entity.PlanTemplateDay;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface PlanTemplateDayMapper {
    List<PlanTemplateDay> selectByTemplateId(Long templateId);
    int insert(PlanTemplateDay day);
    int batchInsert(List<PlanTemplateDay> list);
    int deleteByTemplateId(Long templateId);
}
