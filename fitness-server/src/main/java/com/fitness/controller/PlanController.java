package com.fitness.controller;

import com.fitness.common.Result;
import com.fitness.dto.PlanCreateDTO;
import com.fitness.dto.PlanVO;
import com.fitness.entity.FitnessPlan;
import com.fitness.service.PlanService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/plans")
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @GetMapping
    public Result<List<FitnessPlan>> list() {
        return Result.ok(planService.list());
    }

    @GetMapping("/{id}")
    public Result<PlanVO> getById(@PathVariable Long id) {
        return Result.ok(planService.getById(id));
    }

    @PostMapping
    public Result<PlanVO> create(@RequestBody PlanCreateDTO dto) {
        return Result.ok(planService.create(dto));
    }

    @PutMapping("/{id}")
    public Result<PlanVO> update(@PathVariable Long id, @RequestBody PlanCreateDTO dto) {
        return Result.ok(planService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        planService.delete(id);
        return Result.ok();
    }

    @PutMapping("/{id}/activate")
    public Result<?> activate(@PathVariable Long id) {
        planService.activate(id);
        return Result.ok();
    }
}
