package com.fitness.mapper;

import com.fitness.entity.DietRecord;
import org.apache.ibatis.annotations.Mapper;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface DietRecordMapper {
    DietRecord selectById(Long id);
    List<DietRecord> selectByUserAndDate(@org.apache.ibatis.annotations.Param("userId") Long userId, @org.apache.ibatis.annotations.Param("recordDate") Date recordDate);
    Map<String, Object> selectDailySummary(@org.apache.ibatis.annotations.Param("userId") Long userId, @org.apache.ibatis.annotations.Param("recordDate") Date recordDate);
    int insert(DietRecord record);
    int updateById(DietRecord record);
    int deleteById(Long id);
}
