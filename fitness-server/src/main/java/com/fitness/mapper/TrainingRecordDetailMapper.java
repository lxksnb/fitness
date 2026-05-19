package com.fitness.mapper;

import com.fitness.entity.TrainingRecordDetail;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface TrainingRecordDetailMapper {
    List<TrainingRecordDetail> selectByTrainingRecordId(Long trainingRecordId);
    int insert(TrainingRecordDetail detail);
    int updateById(TrainingRecordDetail detail);
    int deleteByRecordId(Long trainingRecordId);
    int batchInsert(List<TrainingRecordDetail> list);
}
