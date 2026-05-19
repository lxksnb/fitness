package com.fitness.service;

import com.fitness.common.ResultCode;
import com.fitness.dto.ActionDTO;
import com.fitness.entity.ActionLibrary;
import com.fitness.entity.UserActionRecord;
import com.fitness.exception.BusinessException;
import com.fitness.mapper.ActionLibraryMapper;
import com.fitness.mapper.UserActionRecordMapper;
import com.fitness.utils.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 训练动作服务
 * 处理动作库的增删改查和用户训练记录
 */
@Service
public class ActionService {

    private final ActionLibraryMapper actionMapper;
    private final UserActionRecordMapper recordMapper;

    public ActionService(ActionLibraryMapper actionMapper, UserActionRecordMapper recordMapper) {
        this.actionMapper = actionMapper;
        this.recordMapper = recordMapper;
    }

    /**
     * 搜索动作库
     * @param keyword 动作名称关键字
     * @param suitableFor 适用人群过滤条件
     * @return 匹配的动作列表
     */
    public List<ActionLibrary> search(String keyword, String suitableFor) {
        Long userId = SecurityUtils.getCurrentUserId();
        return actionMapper.searchByName(keyword, userId, suitableFor);
    }

    /**
     * 创建个人自定义动作
     * @param dto 动作信息
     * @return 创建后的动作对象
     */
    @Transactional
    public ActionLibrary create(ActionDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        ActionLibrary action = new ActionLibrary();
        action.setScope("USER"); // 用户自定义动作
        action.setUserId(userId);
        fillAction(action, dto);
        actionMapper.insert(action);
        return action;
    }

    /**
     * 更新动作信息（仅限自己的动作）
     * @param id 动作 ID
     * @param dto 更新的动作信息
     * @throws BusinessException 动作不存在或不是自己的动作时抛出
     */
    @Transactional
    public void update(Long id, ActionDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        ActionLibrary action = actionMapper.selectById(id);
        if (action == null) throw new BusinessException(ResultCode.NOT_FOUND, "动作不存在");
        if (!userId.equals(action.getUserId())) throw new BusinessException(ResultCode.FORBIDDEN, "不能修改他人动作");
        fillAction(action, dto);
        actionMapper.updateById(action);
    }

    /**
     * 删除动作（软删除，将状态设为 DELETED）
     * @param id 动作 ID
     * @throws BusinessException 动作不存在或不是自己的动作时抛出
     */
    @Transactional
    public void delete(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        ActionLibrary action = actionMapper.selectById(id);
        if (action == null) throw new BusinessException(ResultCode.NOT_FOUND);
        if (!userId.equals(action.getUserId())) throw new BusinessException(ResultCode.FORBIDDEN);
        action.setStatus("DELETED"); // 软删除
        actionMapper.updateById(action);
    }

    /**
     * 获取指定动作的用户训练记录
     * @param actionId 动作 ID
     * @return 训练记录列表
     */
    public List<UserActionRecord> getRecords(Long actionId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return recordMapper.selectByUserIdAndAction(userId, actionId);
    }

    /**
     * 填充动作实体字段
     */
    private void fillAction(ActionLibrary action, ActionDTO dto) {
        action.setActionName(dto.getActionName());
        action.setDescription(dto.getDescription());
        action.setSuitableFor(dto.getSuitableFor());
        action.setImageUrls(dto.getImageUrls());
        action.setVideoUrl(dto.getVideoUrl());
    }
}
