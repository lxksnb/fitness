package com.fitness.mapper;

import com.fitness.entity.BodyMeasurement;
import org.apache.ibatis.annotations.Mapper;
import java.util.Date;
import java.util.List;

@Mapper
public interface BodyMeasurementMapper {
    BodyMeasurement selectByUserAndDate(@org.apache.ibatis.annotations.Param("userId") Long userId, @org.apache.ibatis.annotations.Param("recordDate") Date recordDate);
    List<BodyMeasurement> selectByUserAndDateRange(@org.apache.ibatis.annotations.Param("userId") Long userId, @org.apache.ibatis.annotations.Param("startDate") Date startDate, @org.apache.ibatis.annotations.Param("endDate") Date endDate);
    int insert(BodyMeasurement measurement);
    int updateById(BodyMeasurement measurement);
}
