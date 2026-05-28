package com.fitness.mapper;

import com.fitness.entity.FitnessPlan;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface FitnessPlanMapper {
    FitnessPlan selectById(Long id);
    List<FitnessPlan> selectByUserId(Long userId);
    List<FitnessPlan> selectActiveByUser(Long userId);
    int insert(FitnessPlan plan);
    int updateById(FitnessPlan plan);
    int deleteById(Long id);
    int deactivateByUser(Long userId);
    int activateById(Long id);
    int updateCurrentDayOrder(@org.apache.ibatis.annotations.Param("userId") Long userId,
                              @org.apache.ibatis.annotations.Param("planId") Long planId,
                              @org.apache.ibatis.annotations.Param("currentDayOrder") Integer currentDayOrder);
}
