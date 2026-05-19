package com.fitness.mapper;

import com.fitness.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysUserMapper {
    SysUser selectById(Long id);
    SysUser selectByUsername(String username);
    int insert(SysUser user);
    int updateById(SysUser user);
    int countByUsername(String username);
}
