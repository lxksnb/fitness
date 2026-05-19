package com.fitness.mapper;

import com.fitness.entity.BodyPhoto;
import org.apache.ibatis.annotations.Mapper;
import java.util.Date;

@Mapper
public interface BodyPhotoMapper {
    BodyPhoto selectByUserAndDate(@org.apache.ibatis.annotations.Param("userId") Long userId, @org.apache.ibatis.annotations.Param("photoDate") Date photoDate);
    int insert(BodyPhoto photo);
    int updateById(BodyPhoto photo);
    int deleteById(Long id);
}
