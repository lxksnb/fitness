package com.fitness.mapper;

import com.fitness.entity.PlanTemplateMealFood;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface PlanTemplateMealFoodMapper {
    List<PlanTemplateMealFood> selectByMealConfigId(Long mealConfigId);
    int insert(PlanTemplateMealFood food);
    int batchInsert(List<PlanTemplateMealFood> list);
    int deleteByMealConfigId(Long mealConfigId);
}
