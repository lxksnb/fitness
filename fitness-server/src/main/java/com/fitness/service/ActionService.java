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

@Service
public class ActionService {

    private final ActionLibraryMapper actionMapper;
    private final UserActionRecordMapper recordMapper;

    public ActionService(ActionLibraryMapper actionMapper, UserActionRecordMapper recordMapper) {
        this.actionMapper = actionMapper;
        this.recordMapper = recordMapper;
    }

    public List<ActionLibrary> search(String keyword, String suitableFor) {
        Long userId = SecurityUtils.getCurrentUserId();
        return actionMapper.searchByName(keyword, userId, suitableFor);
    }

    @Transactional
    public ActionLibrary create(ActionDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        ActionLibrary action = new ActionLibrary();
        action.setScope("USER");
        action.setUserId(userId);
        fillAction(action, dto);
        actionMapper.insert(action);
        return action;
    }

    @Transactional
    public void update(Long id, ActionDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        ActionLibrary action = actionMapper.selectById(id);
        if (action == null) throw new BusinessException(ResultCode.NOT_FOUND, "动作不存在");
        if (!userId.equals(action.getUserId())) throw new BusinessException(ResultCode.FORBIDDEN, "不能修改他人动作");
        fillAction(action, dto);
        actionMapper.updateById(action);
    }

    @Transactional
    public void delete(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        ActionLibrary action = actionMapper.selectById(id);
        if (action == null) throw new BusinessException(ResultCode.NOT_FOUND);
        if (!userId.equals(action.getUserId())) throw new BusinessException(ResultCode.FORBIDDEN);
        action.setStatus("DELETED");
        actionMapper.updateById(action);
    }

    public List<UserActionRecord> getRecords(Long actionId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return recordMapper.selectByUserIdAndAction(userId, actionId);
    }

    private void fillAction(ActionLibrary action, ActionDTO dto) {
        action.setActionName(dto.getActionName());
        action.setDescription(dto.getDescription());
        action.setSuitableFor(dto.getSuitableFor());
        action.setImageUrls(dto.getImageUrls());
        action.setVideoUrl(dto.getVideoUrl());
    }
}
