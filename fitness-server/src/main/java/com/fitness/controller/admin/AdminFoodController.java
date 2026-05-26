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
    public Result<List<FoodLibrary>> list(@RequestParam(required = false) String keyword) {
        return Result.ok(foodMapper.selectSystemFoods(keyword));
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
        food.setScope("SYSTEM");
        food.setImageUrl(dto.getImageUrl());
        food.setStatus("ACTIVE");
        foodMapper.insert(food);

        // 创建关联的营养信息记录
        if (dto.getNutritions() != null) {
            for (FoodCreateDTO.NutritionItem item : dto.getNutritions()) {
                FoodNutrition n = new FoodNutrition();
                n.setFoodId(food.getId());
                n.setUnitType(item.getUnitType());
                n.setServingWeightG(item.getServingWeightG());
                n.setCarbGrams(item.getCarbGrams());
                n.setProteinGrams(item.getProteinGrams());
                n.setFatGrams(item.getFatGrams());
                // 根据宏量营养素计算卡路里: 碳水*4 + 蛋白质*4 + 脂肪*9
                n.setCalories(item.getCarbGrams() * 4 + item.getProteinGrams() * 4 + item.getFatGrams() * 9);
                n.setImageUrl(item.getImageUrl());
                nutritionMapper.insert(n);
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
            food.setImageUrl(dto.getImageUrl());
            foodMapper.updateById(food);

            // 删除旧的营养信息，再插入新的
            nutritionMapper.deleteByFoodId(id);
            if (dto.getNutritions() != null) {
                for (FoodCreateDTO.NutritionItem item : dto.getNutritions()) {
                    FoodNutrition n = new FoodNutrition();
                    n.setFoodId(id);
                    n.setUnitType(item.getUnitType());
                    n.setServingWeightG(item.getServingWeightG());
                    n.setCarbGrams(item.getCarbGrams());
                    n.setProteinGrams(item.getProteinGrams());
                    n.setFatGrams(item.getFatGrams());
                    // 根据宏量营养素计算卡路里
                    n.setCalories(item.getCarbGrams() * 4 + item.getProteinGrams() * 4 + item.getFatGrams() * 9);
                    n.setImageUrl(item.getImageUrl());
                    nutritionMapper.insert(n);
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
}
