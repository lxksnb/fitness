package com.fitness.mapper;

import com.fitness.entity.WeightRecord;
import org.apache.ibatis.annotations.Mapper;
import java.util.Date;
import java.util.List;

@Mapper
public interface WeightRecordMapper {
    WeightRecord selectByUserAndDate(@org.apache.ibatis.annotations.Param("userId") Long userId, @org.apache.ibatis.annotations.Param("recordDate") Date recordDate);
    List<WeightRecord> selectByUserAndDateRange(@org.apache.ibatis.annotations.Param("userId") Long userId, @org.apache.ibatis.annotations.Param("startDate") Date startDate, @org.apache.ibatis.annotations.Param("endDate") Date endDate);
    WeightRecord selectLatestByUser(Long userId);
    int insert(WeightRecord record);
    int updateById(WeightRecord record);
}
