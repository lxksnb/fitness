package com.fitness.mapper;

import com.fitness.entity.SysDictData;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface SysDictDataMapper {
    List<SysDictData> selectByDictCode(String dictCode);
    List<SysDictData> selectByTypeId(Long dictTypeId);
    int insert(SysDictData dictData);
    int updateById(SysDictData dictData);
    int deleteById(Long id);
}
