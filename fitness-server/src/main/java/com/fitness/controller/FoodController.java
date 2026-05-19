package com.fitness.controller;

import com.fitness.common.Result;
import com.fitness.dto.FoodCreateDTO;
import com.fitness.dto.FoodVO;
import com.fitness.service.FoodService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/foods")
public class FoodController {

    private final FoodService foodService;

    public FoodController(FoodService foodService) {
        this.foodService = foodService;
    }

    @GetMapping
    public Result<List<FoodVO>> search(@RequestParam(required = false) String keyword) {
        return Result.ok(foodService.search(keyword));
    }

    @GetMapping("/{id}")
    public Result<FoodVO> getById(@PathVariable Long id) {
        return Result.ok(foodService.getById(id));
    }

    @PostMapping
    public Result<FoodVO> create(@RequestBody FoodCreateDTO dto) {
        return Result.ok(foodService.create(dto));
    }

    @PutMapping("/{id}")
    public Result<FoodVO> update(@PathVariable Long id, @RequestBody FoodCreateDTO dto) {
        return Result.ok(foodService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        foodService.delete(id);
        return Result.ok();
    }
}
