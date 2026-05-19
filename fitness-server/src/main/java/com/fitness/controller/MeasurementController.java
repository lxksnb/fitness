package com.fitness.controller;

import com.fitness.common.Result;
import com.fitness.dto.BodyMeasurementDTO;
import com.fitness.entity.BodyMeasurement;
import com.fitness.service.MeasurementService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 身体围度记录接口
 */
@RestController
@RequestMapping("/api/v1/measurement")
public class MeasurementController {

    private final MeasurementService measurementService;

    public MeasurementController(MeasurementService measurementService) {
        this.measurementService = measurementService;
    }

    /**
     * 查询围度记录列表
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 围度记录列表
     */
    @GetMapping
    public Result<List<BodyMeasurement>> list(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        return Result.ok(measurementService.list(startDate, endDate));
    }

    /**
     * 新增围度记录
     *
     * @param dto 围度记录请求数据
     * @return 操作结果
     */
    @PostMapping
    public Result<?> save(@RequestBody BodyMeasurementDTO dto) {
        measurementService.save(dto);
        return Result.ok();
    }
}
