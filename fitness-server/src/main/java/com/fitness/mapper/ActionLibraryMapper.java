package com.fitness.mapper;

import com.fitness.entity.ActionLibrary;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface ActionLibraryMapper {
    List<ActionLibrary> searchByName(@org.apache.ibatis.annotations.Param("keyword") String keyword, @org.apache.ibatis.annotations.Param("userId") Long userId, @org.apache.ibatis.annotations.Param("muscleCode") String muscleCode);
    List<ActionLibrary> selectByScopeAndUser(@org.apache.ibatis.annotations.Param("scope") String scope, @org.apache.ibatis.annotations.Param("userId") Long userId);
    ActionLibrary selectById(Long id);
    int insert(ActionLibrary action);
    int updateById(ActionLibrary action);
    int deleteById(Long id);
    /** 查询系统级别的动作(SCOPE=SYSTEM, STATUS=ACTIVE) */
    List<ActionLibrary> selectSystemActions(@org.apache.ibatis.annotations.Param("keyword") String keyword);
}
