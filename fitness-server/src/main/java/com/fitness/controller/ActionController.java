package com.fitness.controller;

import com.fitness.common.Result;
import com.fitness.dto.ActionDTO;
import com.fitness.entity.ActionLibrary;
import com.fitness.entity.UserActionRecord;
import com.fitness.service.ActionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 训练动作控制器
 * 处理动作库的增删改查和用户训练记录查询
 */
@RestController
@RequestMapping("/api/v1/actions")
public class ActionController {

    private final ActionService actionService;

    public ActionController(ActionService actionService) {
        this.actionService = actionService;
    }

    /**
     * 搜索动作库
     * @param keyword 动作名称关键字
     * @param muscleCode 肌群过滤条件
     * @return 匹配的动作列表
     */
    @GetMapping
    public Result<List<ActionLibrary>> search(@RequestParam(required = false) String keyword,
                                               @RequestParam(required = false) String suitableFor,
                                               @RequestParam(required = false) String muscleCode) {
        String filterMuscle = muscleCode != null ? muscleCode : suitableFor;
        return Result.ok(actionService.search(keyword, filterMuscle));
    }

    /**
     * 创建个人自定义动作
     * @param dto 动作信息
     * @return 创建后的动作对象
     */
    @PostMapping
    public Result<ActionLibrary> create(@RequestBody ActionDTO dto) {
        return Result.ok(actionService.create(dto));
    }

    /**
     * 更新动作信息
     * @param id 动作 ID
     * @param dto 更新的动作信息
     * @return 更新成功响应
     */
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody ActionDTO dto) {
        actionService.update(id, dto);
        return Result.ok();
    }

    /**
     * 删除动作（软删除）
     * @param id 动作 ID
     * @return 删除成功响应
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        actionService.delete(id);
        return Result.ok();
    }

    /**
     * 获取指定动作的用户训练记录
     * @param id 动作 ID
     * @return 训练记录列表
     */
    @GetMapping("/{id}/records")
    public Result<List<UserActionRecord>> getRecords(@PathVariable Long id) {
        return Result.ok(actionService.getRecords(id));
    }
}
