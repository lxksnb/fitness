package com.fitness.controller;

import com.fitness.common.Result;
import com.fitness.dto.FoodCreateDTO;
import com.fitness.dto.FoodVO;
import com.fitness.service.FoodService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 食物控制器
 * 处理食物库的增删改查，包括食物基本信息和营养成分管理
 */
@RestController
@RequestMapping("/api/v1/foods")
public class FoodController {

    private final FoodService foodService;

    public FoodController(FoodService foodService) {
        this.foodService = foodService;
    }

    /**
     * 搜索食物
     * @param keyword 食物名称关键字
     * @return 食物 VO 列表（含营养成分）
     */
    @GetMapping
    public Result<List<FoodVO>> search(@RequestParam(required = false) String keyword) {
        return Result.ok(foodService.search(keyword));
    }

    /**
     * 根据 ID 获取食物详情
     * @param id 食物 ID
     * @return 食物 VO（含营养成分列表）
     */
    @GetMapping("/{id}")
    public Result<FoodVO> getById(@PathVariable Long id) {
        return Result.ok(foodService.getById(id));
    }

    /**
     * 创建食物及其营养成分
     * @param dto 食物创建 DTO
     * @return 创建后的食物 VO
     */
    @PostMapping
    public Result<FoodVO> create(@RequestBody FoodCreateDTO dto) {
        return Result.ok(foodService.create(dto));
    }

    /**
     * 更新食物信息和营养成分
     * @param id 食物 ID
     * @param dto 食物创建 DTO
     * @return 更新后的食物 VO
     */
    @PutMapping("/{id}")
    public Result<FoodVO> update(@PathVariable Long id, @RequestBody FoodCreateDTO dto) {
        return Result.ok(foodService.update(id, dto));
    }

    /**
     * 删除食物（软删除）
     * @param id 食物 ID
     * @return 删除成功响应
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        foodService.delete(id);
        return Result.ok();
    }
}
