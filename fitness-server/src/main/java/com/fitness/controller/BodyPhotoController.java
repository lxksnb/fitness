package com.fitness.controller;

import com.fitness.common.Result;
import com.fitness.dto.BodyPhotoDTO;
import com.fitness.entity.BodyPhoto;
import com.fitness.service.BodyPhotoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 身体照片记录接口
 */
@RestController
@RequestMapping("/api/v1/body-photo")
public class BodyPhotoController {

    private final BodyPhotoService bodyPhotoService;

    public BodyPhotoController(BodyPhotoService bodyPhotoService) {
        this.bodyPhotoService = bodyPhotoService;
    }

    /**
     * 查询身体照片记录列表
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 照片记录列表
     */
    @GetMapping
    public Result<List<BodyPhoto>> list(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        return Result.ok(bodyPhotoService.list(startDate, endDate));
    }

    /**
     * 新增身体照片记录
     *
     * @param dto 照片记录请求数据
     * @return 操作结果
     */
    @PostMapping
    public Result<?> save(@RequestBody BodyPhotoDTO dto) {
        bodyPhotoService.save(dto);
        return Result.ok();
    }

    /**
     * 删除身体照片记录
     *
     * @param id 照片记录ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        bodyPhotoService.delete(id);
        return Result.ok();
    }
}
