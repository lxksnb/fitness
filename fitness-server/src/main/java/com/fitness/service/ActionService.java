package com.fitness.service;

import com.fitness.common.ResultCode;
import com.fitness.dto.ActionDTO;
import com.fitness.entity.ActionLibrary;
import com.fitness.entity.ActionMuscleTarget;
import com.fitness.entity.UserActionRecord;
import com.fitness.exception.BusinessException;
import com.fitness.mapper.ActionLibraryMapper;
import com.fitness.mapper.ActionMuscleTargetMapper;
import com.fitness.mapper.UserActionRecordMapper;
import com.fitness.utils.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 训练动作服务
 * 处理动作库的增删改查和用户训练记录
 */
@Service
public class ActionService {

    private final ActionLibraryMapper actionMapper;
    private final ActionMuscleTargetMapper muscleTargetMapper;
    private final UserActionRecordMapper recordMapper;

    public ActionService(ActionLibraryMapper actionMapper, ActionMuscleTargetMapper muscleTargetMapper, UserActionRecordMapper recordMapper) {
        this.actionMapper = actionMapper;
        this.muscleTargetMapper = muscleTargetMapper;
        this.recordMapper = recordMapper;
    }

    /**
     * 搜索动作库
     * @param keyword 动作名称关键字
     * @param muscleCode 肌群过滤条件
     * @return 匹配的动作列表
     */
    public List<ActionLibrary> search(String keyword, String muscleCode, String scope) {
        Long userId = SecurityUtils.getCurrentUserId();
        String normalizedScope = normalizeScope(scope);
        List<ActionLibrary> actions = actionMapper.searchByName(keyword, userId, muscleCode, normalizedScope);
        fillMuscles(actions);
        return actions;
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
        replaceMuscles(action.getId(), dto);
        fillMuscles(Collections.singletonList(action));
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
        replaceMuscles(id, dto);
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
        action.setSuitableFor(joinLegacySuitableFor(dto));
        action.setImageUrls(dto.getImageUrls());
        action.setVideoUrl(dto.getVideoUrl());
    }

    public void fillMuscles(List<ActionLibrary> actions) {
        if (actions == null || actions.isEmpty()) return;
        List<Long> actionIds = actions.stream().map(ActionLibrary::getId).collect(Collectors.toList());
        List<ActionMuscleTarget> targets = muscleTargetMapper.selectByActionIds(actionIds);
        Map<Long, List<ActionMuscleTarget>> grouped = targets.stream()
                .collect(Collectors.groupingBy(ActionMuscleTarget::getActionId));
        for (ActionLibrary action : actions) {
            List<ActionMuscleTarget> actionTargets = grouped.getOrDefault(action.getId(), Collections.emptyList());
            action.setPrimaryMuscles(actionTargets.stream()
                    .filter(target -> "PRIMARY".equals(target.getTargetRole()))
                    .map(ActionMuscleTarget::getMuscleCode)
                    .collect(Collectors.toList()));
            action.setSecondaryMuscles(actionTargets.stream()
                    .filter(target -> "SECONDARY".equals(target.getTargetRole()))
                    .map(ActionMuscleTarget::getMuscleCode)
                    .collect(Collectors.toList()));

            if ((action.getPrimaryMuscles() == null || action.getPrimaryMuscles().isEmpty())
                    && action.getSuitableFor() != null && !action.getSuitableFor().trim().isEmpty()) {
                action.setPrimaryMuscles(java.util.Arrays.asList(action.getSuitableFor().split(",")));
            }
        }
    }

    public void replaceMuscles(Long actionId, ActionDTO dto) {
        muscleTargetMapper.deleteByActionId(actionId);
        insertMuscles(actionId, normalizeCodes(dto.getPrimaryMuscles()), "PRIMARY");
        Set<String> primarySet = new LinkedHashSet<>(normalizeCodes(dto.getPrimaryMuscles()));
        List<String> secondary = normalizeCodes(dto.getSecondaryMuscles()).stream()
                .filter(code -> !primarySet.contains(code))
                .collect(Collectors.toList());
        insertMuscles(actionId, secondary, "SECONDARY");
    }

    private void insertMuscles(Long actionId, List<String> muscleCodes, String role) {
        for (int i = 0; i < muscleCodes.size(); i++) {
            ActionMuscleTarget target = new ActionMuscleTarget();
            target.setActionId(actionId);
            target.setMuscleCode(muscleCodes.get(i));
            target.setTargetRole(role);
            target.setSortOrder(i + 1);
            muscleTargetMapper.insert(target);
        }
    }

    private String joinLegacySuitableFor(ActionDTO dto) {
        List<String> codes = new ArrayList<>();
        codes.addAll(normalizeCodes(dto.getPrimaryMuscles()));
        codes.addAll(normalizeCodes(dto.getSecondaryMuscles()));
        if (codes.isEmpty()) {
            codes.addAll(normalizeCodes(dto.getSuitableFor()));
        }
        return codes.isEmpty() ? null : String.join(",", new LinkedHashSet<>(codes));
    }

    private List<String> normalizeCodes(List<String> codes) {
        if (codes == null) return Collections.emptyList();
        return codes.stream()
                .filter(code -> code != null && !code.trim().isEmpty())
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
    }

    private String normalizeScope(String scope) {
        if (scope == null || scope.trim().isEmpty()) return null;
        String value = scope.trim().toUpperCase();
        if ("SYSTEM".equals(value) || "USER".equals(value)) {
            return value;
        }
        throw new BusinessException(ResultCode.BAD_REQUEST, "动作范围参数不正确");
    }
}
