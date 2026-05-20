package com.fitness.mapper;

import com.fitness.entity.SysDictType;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface SysDictTypeMapper {
    SysDictType selectByCode(String dictCode);
    List<SysDictType> selectAll();
    int insert(SysDictType dictType);
    int updateById(SysDictType dictType);
    int deleteById(Long id);
}
