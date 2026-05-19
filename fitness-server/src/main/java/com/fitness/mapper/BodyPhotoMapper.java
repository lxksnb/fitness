package com.fitness.mapper;

import com.fitness.entity.BodyPhoto;
import org.apache.ibatis.annotations.Mapper;
import java.util.Date;
import java.util.List;

@Mapper
public interface BodyPhotoMapper {
    BodyPhoto selectByUserAndDate(@org.apache.ibatis.annotations.Param("userId") Long userId, @org.apache.ibatis.annotations.Param("photoDate") Date photoDate);
    List<BodyPhoto> selectByUserAndDateRange(@org.apache.ibatis.annotations.Param("userId") Long userId, @org.apache.ibatis.annotations.Param("startDate") Date startDate, @org.apache.ibatis.annotations.Param("endDate") Date endDate);
    BodyPhoto selectById(Long id);
    int insert(BodyPhoto photo);
    int updateById(BodyPhoto photo);
    int deleteById(Long id);
}
