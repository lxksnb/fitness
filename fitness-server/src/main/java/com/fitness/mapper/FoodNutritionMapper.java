package com.fitness.mapper;

import com.fitness.entity.FoodNutrition;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface FoodNutritionMapper {
    List<FoodNutrition> selectByFoodId(Long foodId);
    int insert(FoodNutrition nutrition);
    int updateById(FoodNutrition nutrition);
    int deleteByFoodId(Long foodId);
    int batchInsert(List<FoodNutrition> list);
}
