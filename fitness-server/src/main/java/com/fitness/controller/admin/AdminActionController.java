package com.fitness.controller.admin;

import com.fitness.common.Result;
import com.fitness.dto.ActionDTO;
import com.fitness.entity.ActionLibrary;
import com.fitness.mapper.ActionLibraryMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员动作库管理控制器
 * 管理员可管理系统公共动作库(SCOPE=SYSTEM)
 */
@RestController
@RequestMapping("/api/v1/admin/actions")
public class AdminActionController {

    private final ActionLibraryMapper actionMapper;

    /**
     * 构造函数 - 注入动作库Mapper
     */
    public AdminActionController(ActionLibraryMapper actionMapper) {
        this.actionMapper = actionMapper;
    }

    /**
     * 查询所有系统动作列表
     * 返回SCOPE=SYSTEM且STATUS=ACTIVE的动作
     */
    @GetMapping
    public Result<List<ActionLibrary>> list() {
        return Result.ok(actionMapper.selectSystemActions());
    }

    /**
     * 新增系统动作
     * 创建SCOPE=SYSTEM的动作记录
     */
    @PostMapping
    public Result<?> create(@RequestBody ActionDTO dto) {
        ActionLibrary action = new ActionLibrary();
        action.setScope("SYSTEM");
        action.setActionName(dto.getActionName());
        action.setDescription(dto.getDescription());
        action.setSuitableFor(dto.getSuitableFor());
        action.setImageUrls(dto.getImageUrls());
        action.setVideoUrl(dto.getVideoUrl());
        action.setStatus("ACTIVE");
        actionMapper.insert(action);
        return Result.ok();
    }

    /**
     * 更新系统动作信息
     */
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody ActionDTO dto) {
        ActionLibrary action = actionMapper.selectById(id);
        if (action != null) {
            action.setActionName(dto.getActionName());
            action.setDescription(dto.getDescription());
            action.setSuitableFor(dto.getSuitableFor());
            action.setImageUrls(dto.getImageUrls());
            action.setVideoUrl(dto.getVideoUrl());
            actionMapper.updateById(action);
        }
        return Result.ok();
    }

    /**
     * 删除系统动作(软删除)
     * 将状态设为DELETED而非物理删除
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        ActionLibrary action = actionMapper.selectById(id);
        if (action != null) {
            action.setStatus("DELETED");
            actionMapper.updateById(action);
        }
        return Result.ok();
    }
}
