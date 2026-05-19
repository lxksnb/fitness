package com.fitness.mapper;

import com.fitness.entity.FitnessPlan;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface FitnessPlanMapper {
    List<FitnessPlan> selectByUserId(Long userId);
    List<FitnessPlan> selectActiveByUser(Long userId);
    int insert(FitnessPlan plan);
    int updateById(FitnessPlan plan);
    int deleteById(Long id);
    int deactivateByUser(Long userId);
}
