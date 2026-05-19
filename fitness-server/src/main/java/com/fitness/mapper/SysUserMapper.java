package com.fitness.mapper;

import com.fitness.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface SysUserMapper {
    SysUser selectById(Long id);
    SysUser selectByUsername(String username);
    int insert(SysUser user);
    int updateById(SysUser user);
    int countByUsername(String username);
    /** 查询所有用户列表 */
    List<SysUser> selectAll();
    /** 更新用户状态(启用/禁用) */
    int updateStatus(SysUser user);
}
