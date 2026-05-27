package com.fitness.controller.admin;

import com.fitness.common.Result;
import com.fitness.dto.PlanCreateDTO;
import com.fitness.dto.PlanTemplateUpdateDTO;
import com.fitness.dto.PlanTemplateVO;
import com.fitness.service.PlanTemplateService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员计划模板管理控制器
 * 管理员可管理系统级别的训练计划模板，供用户浏览和导入
 */
@RestController
@RequestMapping("/api/v1/admin/templates")
public class AdminTemplateController {

    private final PlanTemplateService templateService;

    public AdminTemplateController(PlanTemplateService templateService) {
        this.templateService = templateService;
    }

    /**
     * 获取所有模板列表
     */
    @GetMapping
    public Result<List<PlanTemplateVO>> list() {
        return Result.ok(templateService.list());
    }

    /**
     * 创建计划模板（含训练日、动作、餐次配置）
     * 管理员创建系统级别的模板供用户浏览和导入
     */
    @PostMapping
    public Result<?> create(@RequestBody PlanCreateDTO dto) {
        templateService.create(dto);
        return Result.ok();
    }

    /**
     * 更新模板基本信息
     * 管理员编辑模板的名称、描述、计划类型、分化方式、难度
     */
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody PlanTemplateUpdateDTO dto) {
        templateService.update(id, dto);
        return Result.ok();
    }

    /**
     * 删除模板（软删除）
     * 将模板状态设为 DELETED
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        templateService.delete(id);
        return Result.ok();
    }
}
