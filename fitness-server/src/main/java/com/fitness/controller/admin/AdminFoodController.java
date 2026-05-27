package com.fitness.controller.admin;

import com.fitness.common.Result;
import com.fitness.common.ResultCode;
import com.fitness.dto.FoodCreateDTO;
import com.fitness.entity.FoodLibrary;
import com.fitness.entity.FoodNutrition;
import com.fitness.exception.BusinessException;
import com.fitness.mapper.FoodLibraryMapper;
import com.fitness.mapper.FoodNutritionMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员食物库管理控制器
 * 管理员可管理系统公共食物库(SCOPE=SYSTEM)
 */
@RestController
@RequestMapping("/api/v1/admin/foods")
public class AdminFoodController {

    private final FoodLibraryMapper foodMapper;
    private final FoodNutritionMapper nutritionMapper;

    /**
     * 构造函数 - 注入食物库和营养信息Mapper
     */
    public AdminFoodController(FoodLibraryMapper foodMapper, FoodNutritionMapper nutritionMapper) {
        this.foodMapper = foodMapper;
        this.nutritionMapper = nutritionMapper;
    }

    /**
     * 查询所有系统食物列表
     * 返回SCOPE=SYSTEM且STATUS=ACTIVE的食物
     */
    @GetMapping
    public Result<List<FoodLibrary>> list(@RequestParam(required = false) String keyword, @RequestParam(required = false) String categoryType) {
        return Result.ok(foodMapper.selectSystemFoods(keyword, categoryType));
    }

    /**
     * 获取食物详情（含营养信息）
     * 编辑时用于回填表单
     */
    @GetMapping("/{id}")
    public Result<FoodCreateDTO> getById(@PathVariable Long id) {
        FoodLibrary food = foodMapper.selectById(id);
        if (food == null || !"SYSTEM".equals(food.getScope())) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        FoodCreateDTO dto = new FoodCreateDTO();
        dto.setFoodName(food.getFoodName());
        dto.setCategoryType(food.getCategoryType());
        dto.setImageUrl(food.getImageUrl());

        List<FoodNutrition> nutritions = nutritionMapper.selectByFoodId(id);
        if (nutritions != null && !nutritions.isEmpty()) {
            List<FoodCreateDTO.NutritionItem> items = new java.util.ArrayList<>();
            for (FoodNutrition n : nutritions) {
                FoodCreateDTO.NutritionItem item = new FoodCreateDTO.NutritionItem();
                item.setUnitType(n.getUnitType());
                item.setServingWeightG(n.getServingWeightG());
                item.setEdibleWeightG(n.getEdibleWeightG());
                item.setCarbGrams(n.getCarbGrams());
                item.setProteinGrams(n.getProteinGrams());
                item.setFatGrams(n.getFatGrams());
                item.setImageUrl(n.getImageUrl());
                items.add(item);
            }
            dto.setNutritions(items);
        }
        return Result.ok(dto);
    }

    /**
     * 新增系统食物(含营养信息)
     * 创建SCOPE=SYSTEM的食物及其关联的营养数据
     */
    @PostMapping
    public Result<?> create(@RequestBody FoodCreateDTO dto) {
        // 创建食物主记录
        FoodLibrary food = new FoodLibrary();
        food.setFoodName(dto.getFoodName());
        food.setCategoryType(dto.getCategoryType());
        food.setScope("SYSTEM");
        food.setImageUrl(dto.getImageUrl());
        food.setStatus("ACTIVE");
        foodMapper.insert(food);

        // 创建关联的营养信息记录
        if (dto.getNutritions() != null) {
            for (FoodCreateDTO.NutritionItem item : dto.getNutritions()) {
                nutritionMapper.insert(buildNutrition(food.getId(), item));
            }
        }
        return Result.ok();
    }

    /**
     * 更新系统食物及其营养信息
     * 先更新食物基本信息，再删除旧营养数据并批量插入新数据
     */
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody FoodCreateDTO dto) {
        FoodLibrary food = foodMapper.selectById(id);
        if (food != null) {
            if (!"SYSTEM".equals(food.getScope())) {
                throw new BusinessException(ResultCode.NOT_FOUND);
            }
            // 更新食物基本信息
            food.setFoodName(dto.getFoodName());
            food.setCategoryType(dto.getCategoryType());
            food.setImageUrl(dto.getImageUrl());
            foodMapper.updateById(food);

            // 删除旧的营养信息，再插入新的
            nutritionMapper.deleteByFoodId(id);
            if (dto.getNutritions() != null) {
                for (FoodCreateDTO.NutritionItem item : dto.getNutritions()) {
                    nutritionMapper.insert(buildNutrition(id, item));
                }
            }
        }
        return Result.ok();
    }

    /**
     * 删除系统食物(软删除)
     * 将状态设为DELETED而非物理删除
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        FoodLibrary food = foodMapper.selectById(id);
        if (food != null) {
            if (!"SYSTEM".equals(food.getScope())) {
                throw new BusinessException(ResultCode.NOT_FOUND);
            }
            food.setStatus("DELETED");
            foodMapper.updateById(food);
        }
        return Result.ok();
    }

    private FoodNutrition buildNutrition(Long foodId, FoodCreateDTO.NutritionItem item) {
        FoodNutrition n = new FoodNutrition();
        n.setFoodId(foodId);
        n.setUnitType(item.getUnitType());
        if ("PER_100G".equals(item.getUnitType())) {
            n.setServingWeightG(100.0);
            n.setEdibleWeightG(100.0);
        } else {
            n.setServingWeightG(item.getServingWeightG());
            n.setEdibleWeightG(item.getEdibleWeightG());
        }
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
}
