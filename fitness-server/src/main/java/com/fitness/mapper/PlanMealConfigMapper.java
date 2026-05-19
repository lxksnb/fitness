package com.fitness.mapper;

import com.fitness.entity.PlanMealConfig;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface PlanMealConfigMapper {
    List<PlanMealConfig> selectByPlanId(Long planId);
    List<PlanMealConfig> selectByPlanIdAndDayType(@org.apache.ibatis.annotations.Param("planId") Long planId, @org.apache.ibatis.annotations.Param("dayType") String dayType);
    int insert(PlanMealConfig config);
    int updateById(PlanMealConfig config);
    int deleteByPlanId(Long planId);
    int batchInsert(List<PlanMealConfig> list);
}
