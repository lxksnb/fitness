package com.fitness.controller.admin;

import com.fitness.common.Result;
import com.fitness.common.ResultCode;
import com.fitness.dto.ActionDTO;
import com.fitness.entity.ActionLibrary;
import com.fitness.exception.BusinessException;
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
    public Result<List<ActionLibrary>> list(@RequestParam(required = false) String keyword) {
        return Result.ok(actionMapper.selectSystemActions(keyword));
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
        // 前端checkbox多选传来数组, 拼成逗号分隔字符串存入数据库
        action.setSuitableFor(joinList(dto.getSuitableFor()));
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
            if (!"SYSTEM".equals(action.getScope())) {
                throw new BusinessException(ResultCode.NOT_FOUND);
            }
            action.setActionName(dto.getActionName());
            action.setDescription(dto.getDescription());
            action.setSuitableFor(joinList(dto.getSuitableFor()));
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
            if (!"SYSTEM".equals(action.getScope())) {
                throw new BusinessException(ResultCode.NOT_FOUND);
            }
            action.setStatus("DELETED");
            actionMapper.updateById(action);
        }
        return Result.ok();
    }

    /**
     * 将前端checkbox多选数组转为逗号分隔字符串存入数据库
     * @param list 前端传来的部位列表
     * @return 逗号分隔的字符串, 如 "CHEST,BACK,LEGS"
     */
    private String joinList(java.util.List<String> list) {
        if (list != null && !list.isEmpty()) {
            return String.join(",", list);
        }
        return null;
    }
}
