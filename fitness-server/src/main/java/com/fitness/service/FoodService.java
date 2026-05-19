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

@Service
public class FoodService {

    private final FoodLibraryMapper foodMapper;
    private final FoodNutritionMapper nutritionMapper;

    public FoodService(FoodLibraryMapper foodMapper, FoodNutritionMapper nutritionMapper) {
        this.foodMapper = foodMapper;
        this.nutritionMapper = nutritionMapper;
    }

    public List<FoodVO> search(String keyword) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }
        List<FoodLibrary> foods = foodMapper.searchByName(keyword.trim(), userId);
        return toVOList(foods);
    }

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

        if (dto.getNutritions() != null) {
            for (FoodCreateDTO.NutritionItem item : dto.getNutritions()) {
                FoodNutrition n = buildNutrition(food.getId(), item);
                nutritionMapper.insert(n);
            }
        }
        return getById(food.getId());
    }

    @Transactional
    public FoodVO update(Long id, FoodCreateDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        FoodLibrary food = foodMapper.selectById(id);
        if (food == null) throw new BusinessException(ResultCode.NOT_FOUND, "食物不存在");
        if (!userId.equals(food.getUserId())) throw new BusinessException(ResultCode.FORBIDDEN, "不能修改他人食物");
        food.setFoodName(dto.getFoodName());
        food.setImageUrl(dto.getImageUrl());
        foodMapper.updateById(food);
        nutritionMapper.deleteByFoodId(id);
        if (dto.getNutritions() != null) {
            for (FoodCreateDTO.NutritionItem item : dto.getNutritions()) {
                FoodNutrition n = buildNutrition(id, item);
                nutritionMapper.insert(n);
            }
        }
        return getById(id);
    }

    @Transactional
    public void delete(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        FoodLibrary food = foodMapper.selectById(id);
        if (food == null) throw new BusinessException(ResultCode.NOT_FOUND);
        if (!userId.equals(food.getUserId())) throw new BusinessException(ResultCode.FORBIDDEN);
        food.setStatus("DELETED");
        foodMapper.updateById(food);
    }

    public FoodVO getById(Long id) {
        FoodLibrary food = foodMapper.selectById(id);
        if (food == null) return null;
        List<FoodNutrition> nutritions = nutritionMapper.selectByFoodId(id);
        return toVO(food, nutritions);
    }

    private FoodNutrition buildNutrition(Long foodId, FoodCreateDTO.NutritionItem item) {
        FoodNutrition n = new FoodNutrition();
        n.setFoodId(foodId);
        n.setUnitType(item.getUnitType());
        n.setServingWeightG(item.getServingWeightG());
        n.setCarbGrams(item.getCarbGrams());
        n.setProteinGrams(item.getProteinGrams());
        n.setFatGrams(item.getFatGrams());
        double carb = item.getCarbGrams() != null ? item.getCarbGrams() : 0;
        double protein = item.getProteinGrams() != null ? item.getProteinGrams() : 0;
        double fat = item.getFatGrams() != null ? item.getFatGrams() : 0;
        n.setCalories(carb * 4 + protein * 4 + fat * 9);
        n.setImageUrl(item.getImageUrl());
        return n;
    }

    private List<FoodVO> toVOList(List<FoodLibrary> foods) {
        List<FoodVO> result = new ArrayList<>();
        for (FoodLibrary f : foods) {
            List<FoodNutrition> nutritions = nutritionMapper.selectByFoodId(f.getId());
            result.add(toVO(f, nutritions));
        }
        return result;
    }

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
}
