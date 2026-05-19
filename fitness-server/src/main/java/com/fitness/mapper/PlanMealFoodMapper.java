package com.fitness.mapper;

import com.fitness.entity.PlanMealFood;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface PlanMealFoodMapper {
    List<PlanMealFood> selectByMealConfigId(Long mealConfigId);
    int insert(PlanMealFood food);
    int updateById(PlanMealFood food);
    int deleteByMealConfigId(Long mealConfigId);
    int batchInsert(List<PlanMealFood> list);
}
