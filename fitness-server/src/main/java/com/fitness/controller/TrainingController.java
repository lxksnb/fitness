package com.fitness.controller;

import com.fitness.common.Result;
import com.fitness.dto.CalendarDataVO;
import com.fitness.dto.TrainingRecordDTO;
import com.fitness.entity.TrainingRecord;
import com.fitness.service.TrainingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 训练记录控制器
 */
@RestController
@RequestMapping("/api/v1/training")
public class TrainingController {

    private final TrainingService trainingService;

    public TrainingController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    /**
     * 按日期范围查询训练记录列表
     */
    @GetMapping
    public Result<List<TrainingRecord>> list(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        return Result.ok(trainingService.list(startDate, endDate));
    }

    /**
     * 查询训练记录详情(含动作明细)
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(@PathVariable Long id) {
        return Result.ok(trainingService.getById(id));
    }

    /**
     * 创建训练记录
     */
    @PostMapping
    public Result<TrainingRecord> create(@RequestBody TrainingRecordDTO dto) {
        return Result.ok(trainingService.create(dto));
    }

    /**
     * 更新训练记录
     */
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody TrainingRecordDTO dto) {
        trainingService.update(id, dto);
        return Result.ok();
    }

    /**
     * 删除训练记录
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        trainingService.delete(id);
        return Result.ok();
    }

    /**
     * 获取训练打卡日历数据
     */
    @GetMapping("/calendar")
    public Result<List<CalendarDataVO>> calendar(
            @RequestParam int year, @RequestParam int month) {
        return Result.ok(trainingService.calendar(year, month));
    }
}
