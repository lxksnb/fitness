package com.fitness.mapper;

import com.fitness.entity.PlanTrainingAction;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface PlanTrainingActionMapper {
    List<PlanTrainingAction> selectByDayId(Long trainingDayId);
    int insert(PlanTrainingAction action);
    int updateById(PlanTrainingAction action);
    int deleteByDayId(Long trainingDayId);
    int batchInsert(List<PlanTrainingAction> list);
}
