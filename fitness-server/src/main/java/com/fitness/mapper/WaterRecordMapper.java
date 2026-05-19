package com.fitness.mapper;

import com.fitness.entity.WaterRecord;
import org.apache.ibatis.annotations.Mapper;
import java.util.Date;

@Mapper
public interface WaterRecordMapper {
    WaterRecord selectByUserAndDate(@org.apache.ibatis.annotations.Param("userId") Long userId, @org.apache.ibatis.annotations.Param("recordDate") Date recordDate);
    Integer selectTodayTotal(@org.apache.ibatis.annotations.Param("userId") Long userId, @org.apache.ibatis.annotations.Param("recordDate") Date recordDate);
    int insert(WaterRecord record);
    int deleteById(Long id);
}
