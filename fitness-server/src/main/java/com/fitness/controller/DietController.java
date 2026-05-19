package com.fitness.controller;

import com.fitness.common.Result;
import com.fitness.dto.DailySummaryVO;
import com.fitness.dto.DietRecordDTO;
import com.fitness.entity.DietRecord;
import com.fitness.service.DietService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/diet")
public class DietController {

    private final DietService dietService;

    public DietController(DietService dietService) {
        this.dietService = dietService;
    }

    @GetMapping
    public Result<List<DietRecord>> listByDate(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        return Result.ok(dietService.listByDate(date));
    }

    @PostMapping
    public Result<DietRecord> create(@RequestBody DietRecordDTO dto) {
        return Result.ok(dietService.create(dto));
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody DietRecordDTO dto) {
        dietService.update(id, dto);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        dietService.delete(id);
        return Result.ok();
    }

    @GetMapping("/daily-summary")
    public Result<DailySummaryVO> dailySummary(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        return Result.ok(dietService.dailySummary(date));
    }
}
