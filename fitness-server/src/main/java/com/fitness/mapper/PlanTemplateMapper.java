package com.fitness.mapper;

import com.fitness.entity.PlanTemplate;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface PlanTemplateMapper {
    List<PlanTemplate> selectAll();
    PlanTemplate selectById(Long id);
    int insert(PlanTemplate template);
    int updateById(PlanTemplate template);
    int deleteById(Long id);
}
