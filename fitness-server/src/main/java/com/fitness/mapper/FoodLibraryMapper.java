package com.fitness.mapper;

import com.fitness.entity.FoodLibrary;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface FoodLibraryMapper {
    List<FoodLibrary> searchByName(@org.apache.ibatis.annotations.Param("keyword") String keyword, @org.apache.ibatis.annotations.Param("userId") Long userId, @org.apache.ibatis.annotations.Param("scope") String scope, @org.apache.ibatis.annotations.Param("categoryType") String categoryType);
    List<FoodLibrary> selectByScopeAndUser(@org.apache.ibatis.annotations.Param("scope") String scope, @org.apache.ibatis.annotations.Param("userId") Long userId);
    FoodLibrary selectById(Long id);
    int insert(FoodLibrary food);
    int updateById(FoodLibrary food);
    int deleteById(Long id);
    /** 查询系统级别的食物(SCOPE=SYSTEM, STATUS=ACTIVE) */
    List<FoodLibrary> selectSystemFoods(@org.apache.ibatis.annotations.Param("keyword") String keyword, @org.apache.ibatis.annotations.Param("categoryType") String categoryType);
}
