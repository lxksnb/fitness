package com.fitness.mapper;

import com.fitness.entity.PlanTrainingDay;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface PlanTrainingDayMapper {
    List<PlanTrainingDay> selectByPlanId(Long planId);
    int insert(PlanTrainingDay day);
    int updateById(PlanTrainingDay day);
    int deleteByPlanId(Long planId);
    int batchInsert(List<PlanTrainingDay> list);
}
