package com.fitness.service;

import com.fitness.common.ResultCode;
import com.fitness.dto.FoodCreateDTO;
import com.fitness.dto.FoodVO;
import com.fitness.entity.FoodLibrary;
import com.fitness.entity.FoodNutrition;
import com.fitness.exception.BusinessException;
import com.fitness.mapper.FoodLibraryMapper;
import com.fitness.mapper.FoodNutritionMapper;
import com.fitness.utils.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 食物服务
 * 处理食物库的增删改查，包括食物基本信息和营养成分管理
 */
@Service
public class FoodService {

    private final FoodLibraryMapper foodMapper;
    private final FoodNutritionMapper nutritionMapper;

    public FoodService(FoodLibraryMapper foodMapper, FoodNutritionMapper nutritionMapper) {
        this.foodMapper = foodMapper;
        this.nutritionMapper = nutritionMapper;
    }

    /**
     * 按关键字搜索食物
     * @param keyword 食物名称关键字
     * @return 食物 VO 列表，包含营养成分信息
     */
    public List<FoodVO> search(String keyword, String scope) {
        Long userId = SecurityUtils.getCurrentUserId();
        String normalizedScope = normalizeScope(scope);
        // keyword可为null, SQL中通过<if>动态标签处理空值查询全部
        List<FoodLibrary> foods = foodMapper.searchByName(keyword, userId, normalizedScope);
        return toVOList(foods);
    }

    /**
     * 创建食物及其营养成分
     * @param dto 食物创建 DTO
     * @return 创建后的食物 VO
     */
    @Transactional
    public FoodVO create(FoodCreateDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        FoodLibrary food = new FoodLibrary();
        food.setScope("USER");
        food.setUserId(userId);
        food.setFoodName(dto.getFoodName());
        food.setImageUrl(dto.getImageUrl());
        food.setStatus("ACTIVE");
        foodMapper.insert(food);

        // 批量插入营养成分记录
        if (dto.getNutritions() != null) {
            for (FoodCreateDTO.NutritionItem item : dto.getNutritions()) {
                FoodNutrition n = buildNutrition(food.getId(), item);
                nutritionMapper.insert(n);
            }
        }
        return getById(food.getId());
    }

    /**
     * 更新食物信息和营养成分
     * 采用先删后增的策略更新营养成分
     *
     * @param id 食物 ID
     * @param dto 食物创建 DTO
     * @return 更新后的食物 VO
     */
    @Transactional
    public FoodVO update(Long id, FoodCreateDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        FoodLibrary food = foodMapper.selectById(id);
        if (food == null) throw new BusinessException(ResultCode.NOT_FOUND, "食物不存在");
        if (!userId.equals(food.getUserId())) throw new BusinessException(ResultCode.FORBIDDEN, "不能修改他人食物");
        food.setFoodName(dto.getFoodName());
        food.setImageUrl(dto.getImageUrl());
        foodMapper.updateById(food);
        // 先删除旧营养成分，再重新插入
        nutritionMapper.deleteByFoodId(id);
        if (dto.getNutritions() != null) {
            for (FoodCreateDTO.NutritionItem item : dto.getNutritions()) {
                FoodNutrition n = buildNutrition(id, item);
                nutritionMapper.insert(n);
            }
        }
        return getById(id);
    }

    /**
     * 删除食物（软删除）
     * @param id 食物 ID
     */
    @Transactional
    public void delete(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        FoodLibrary food = foodMapper.selectById(id);
        if (food == null) throw new BusinessException(ResultCode.NOT_FOUND);
        if (!userId.equals(food.getUserId())) throw new BusinessException(ResultCode.FORBIDDEN);
        food.setStatus("DELETED");
        foodMapper.updateById(food);
    }

    /**
     * 根据 ID 获取食物详情（含营养成分）
     * @param id 食物 ID
     * @return 食物 VO，包含营养成分列表
     */
    public FoodVO getById(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        FoodLibrary food = foodMapper.selectById(id);
        if (food == null) return null;
        if (!"SYSTEM".equals(food.getScope()) && !userId.equals(food.getUserId())) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        List<FoodNutrition> nutritions = nutritionMapper.selectByFoodId(id);
        return toVO(food, nutritions);
    }

    /**
     * 构建 FoodNutrition 实体并计算卡路里
     * 热量公式：碳水*4 + 蛋白质*4 + 脂肪*9
     */
    private FoodNutrition buildNutrition(Long foodId, FoodCreateDTO.NutritionItem item) {
        FoodNutrition n = new FoodNutrition();
        n.setFoodId(foodId);
        n.setUnitType(item.getUnitType());
        n.setServingWeightG(item.getServingWeightG());
        n.setCarbGrams(item.getCarbGrams());
        n.setProteinGrams(item.getProteinGrams());
        n.setFatGrams(item.getFatGrams());
        // 根据宏量营养素计算卡路里
        double carb = item.getCarbGrams() != null ? item.getCarbGrams() : 0;
        double protein = item.getProteinGrams() != null ? item.getProteinGrams() : 0;
        double fat = item.getFatGrams() != null ? item.getFatGrams() : 0;
        n.setCalories(carb * 4 + protein * 4 + fat * 9);
        n.setImageUrl(item.getImageUrl());
        return n;
    }

    /** 批量转换 FoodLibrary 列表为 FoodVO 列表 */
    private List<FoodVO> toVOList(List<FoodLibrary> foods) {
        List<FoodVO> result = new ArrayList<>();
        for (FoodLibrary f : foods) {
            List<FoodNutrition> nutritions = nutritionMapper.selectByFoodId(f.getId());
            result.add(toVO(f, nutritions));
        }
        return result;
    }

    /** 将 FoodLibrary 和营养成分列表转换为 FoodVO */
    private FoodVO toVO(FoodLibrary food, List<FoodNutrition> nutritions) {
        FoodVO vo = new FoodVO();
        vo.setId(food.getId());
        vo.setScope(food.getScope());
        vo.setFoodName(food.getFoodName());
        vo.setImageUrl(food.getImageUrl());
        vo.setStatus(food.getStatus());
        vo.setCreatedAt(food.getCreatedAt());
        List<FoodVO.NutritionVO> nvos = new ArrayList<>();
        for (FoodNutrition n : nutritions) {
            FoodVO.NutritionVO nvo = new FoodVO.NutritionVO();
            nvo.setId(n.getId());
            nvo.setUnitType(n.getUnitType());
            nvo.setServingWeightG(n.getServingWeightG());
            nvo.setCarbGrams(n.getCarbGrams());
            nvo.setProteinGrams(n.getProteinGrams());
            nvo.setFatGrams(n.getFatGrams());
            nvo.setCalories(n.getCalories());
            nvo.setImageUrl(n.getImageUrl());
            nvos.add(nvo);
        }
        vo.setNutritions(nvos);
        return vo;
    }

    private String normalizeScope(String scope) {
        if (scope == null || scope.trim().isEmpty()) return null;
        String value = scope.trim().toUpperCase();
        if ("SYSTEM".equals(value) || "USER".equals(value)) {
            return value;
        }
        throw new BusinessException(ResultCode.BAD_REQUEST, "食物范围参数不正确");
    }
}
