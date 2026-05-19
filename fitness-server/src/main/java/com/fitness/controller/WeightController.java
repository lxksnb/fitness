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

@RestController
@RequestMapping("/api/v1/weight")
public class WeightController {

    private final WeightService weightService;

    public WeightController(WeightService weightService) {
        this.weightService = weightService;
    }

    @GetMapping
    public Result<List<WeightRecord>> list(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        return Result.ok(weightService.list(startDate, endDate));
    }

    @PostMapping
    public Result<?> save(@RequestBody WeightRecordDTO dto) {
        weightService.save(dto);
        return Result.ok();
    }

    @GetMapping("/trend")
    public Result<List<WeightTrendVO>> trend(@RequestParam(defaultValue = "30") int days) {
        return Result.ok(weightService.trend(days));
    }
}
