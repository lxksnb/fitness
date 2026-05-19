package com.fitness.controller;

import com.fitness.common.Result;
import com.fitness.dto.WaterRecordDTO;
import com.fitness.entity.WaterRecord;
import com.fitness.service.WaterService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 饮水记录接口
 */
@RestController
@RequestMapping("/api/v1/water")
public class WaterController {

    private final WaterService waterService;

    public WaterController(WaterService waterService) {
        this.waterService = waterService;
    }

    /**
     * 获取当日饮水总量
     *
     * @param date 查询日期
     * @return 包含totalMl和targetMl的Map
     */
    @GetMapping("/today")
    public Result<Map<String, Object>> todayTotal(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        return Result.ok(waterService.todayTotal(date));
    }

    /**
     * 查询某日饮水记录列表
     *
     * @param date 查询日期
     * @return 饮水记录列表
     */
    @GetMapping
    public Result<List<WaterRecord>> list(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        return Result.ok(waterService.list(date));
    }

    /**
     * 新增饮水记录
     *
     * @param dto 饮水记录请求数据
     * @return 操作结果
     */
    @PostMapping
    public Result<?> save(@RequestBody WaterRecordDTO dto) {
        waterService.save(dto);
        return Result.ok();
    }
}
