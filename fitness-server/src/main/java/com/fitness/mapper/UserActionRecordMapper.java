package com.fitness.mapper;

import com.fitness.entity.UserActionRecord;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface UserActionRecordMapper {
    List<UserActionRecord> selectByUserIdAndAction(@org.apache.ibatis.annotations.Param("userId") Long userId, @org.apache.ibatis.annotations.Param("actionId") Long actionId);
    int insert(UserActionRecord record);
    List<UserActionRecord> selectByUserIdAndActionId(@org.apache.ibatis.annotations.Param("userId") Long userId, @org.apache.ibatis.annotations.Param("actionId") Long actionId);
}
