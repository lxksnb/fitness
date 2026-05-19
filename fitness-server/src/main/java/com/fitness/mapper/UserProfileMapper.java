package com.fitness.mapper;

import com.fitness.entity.UserProfile;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserProfileMapper {
    UserProfile selectByUserId(Long userId);
    int insert(UserProfile profile);
    int updateByUserId(UserProfile profile);
}
