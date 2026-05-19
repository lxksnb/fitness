package com.fitness.mapper;

import com.fitness.entity.TrainingRecord;
import org.apache.ibatis.annotations.Mapper;
import java.util.Date;
import java.util.List;

@Mapper
public interface TrainingRecordMapper {
    TrainingRecord selectById(Long id);
    List<TrainingRecord> selectByUserAndDate(@org.apache.ibatis.annotations.Param("userId") Long userId, @org.apache.ibatis.annotations.Param("recordDate") Date recordDate);
    List<TrainingRecord> selectByUserAndDateRange(@org.apache.ibatis.annotations.Param("userId") Long userId, @org.apache.ibatis.annotations.Param("startDate") Date startDate, @org.apache.ibatis.annotations.Param("endDate") Date endDate);
    List<TrainingRecord> selectCalendarData(@org.apache.ibatis.annotations.Param("userId") Long userId, @org.apache.ibatis.annotations.Param("year") Integer year, @org.apache.ibatis.annotations.Param("month") Integer month);
    int insert(TrainingRecord record);
    int updateById(TrainingRecord record);
    int deleteById(Long id);
}
