package com.fitness.controller;

import com.fitness.common.Result;
import com.fitness.dto.WeightRecordDTO;
import com.fitness.dto.WeightTrendVO;
import com.fitness.entity.WeightRecord;
import com.fitness.service.WeightService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 体重控制器
 * 处理体重记录的增删改查和趋势分析
 */
@RestController
@RequestMapping("/api/v1/weight")
public class WeightController {

    private final WeightService weightService;

    public WeightController(WeightService weightService) {
        this.weightService = weightService;
    }

    /**
     * 查询指定日期范围内的体重记录
     * @param startDate 开始日期（yyyy-MM-dd）
     * @param endDate 结束日期（yyyy-MM-dd）
     * @return 体重记录列表
     */
    @GetMapping
    public Result<List<WeightRecord>> list(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        return Result.ok(weightService.list(startDate, endDate));
    }

    /**
     * 保存体重记录（新建或更新当天记录）
     * @param dto 体重记录 DTO
     * @return 保存成功响应
     */
    @PostMapping
    public Result<?> save(@RequestBody WeightRecordDTO dto) {
        weightService.save(dto);
        return Result.ok();
    }

    /**
     * 获取体重趋势数据
     * @param days 查询最近的天数，默认 30 天
     * @return 体重趋势数据点列表
     */
    @GetMapping("/trend")
    public Result<List<WeightTrendVO>> trend(@RequestParam(defaultValue = "30") int days) {
        return Result.ok(weightService.trend(days));
    }
}
