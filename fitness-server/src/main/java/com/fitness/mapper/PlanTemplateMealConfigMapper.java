package com.fitness.mapper;

import com.fitness.entity.PlanTemplateMealConfig;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface PlanTemplateMealConfigMapper {
    List<PlanTemplateMealConfig> selectByTemplateId(Long templateId);
    int insert(PlanTemplateMealConfig config);
    int batchInsert(List<PlanTemplateMealConfig> list);
    int deleteByTemplateId(Long templateId);
}
