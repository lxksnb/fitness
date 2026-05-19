package com.fitness.mapper;

import com.fitness.entity.ActionLibrary;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface ActionLibraryMapper {
    List<ActionLibrary> searchByName(@org.apache.ibatis.annotations.Param("keyword") String keyword);
    List<ActionLibrary> selectByScopeAndUser(@org.apache.ibatis.annotations.Param("scope") String scope, @org.apache.ibatis.annotations.Param("userId") Long userId);
    ActionLibrary selectById(Long id);
    int insert(ActionLibrary action);
    int updateById(ActionLibrary action);
    int deleteById(Long id);
}
