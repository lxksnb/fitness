package com.fitness.mapper;

import com.fitness.entity.ActionMuscleTarget;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ActionMuscleTargetMapper {
    List<ActionMuscleTarget> selectByActionIds(@Param("actionIds") List<Long> actionIds);

    void deleteByActionId(@Param("actionId") Long actionId);

    int insert(ActionMuscleTarget target);
}
