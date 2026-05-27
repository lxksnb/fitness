package com.fitness.service;

import com.fitness.common.ResultCode;
import com.fitness.dto.CalendarDataVO;
import com.fitness.dto.TrainingRecordDTO;
import com.fitness.entity.PlanTrainingAction;
import com.fitness.entity.TrainingRecord;
import com.fitness.entity.TrainingRecordDetail;
import com.fitness.exception.BusinessException;
import com.fitness.mapper.PlanTrainingActionMapper;
import com.fitness.mapper.TrainingRecordDetailMapper;
import com.fitness.mapper.TrainingRecordMapper;
import com.fitness.utils.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 训练记录服务
 */
@Service
public class TrainingService {

    private final TrainingRecordMapper recordMapper;
    private final TrainingRecordDetailMapper detailMapper;
    private final PlanTrainingActionMapper planActionMapper;

    public TrainingService(TrainingRecordMapper recordMapper,
                           TrainingRecordDetailMapper detailMapper,
                           PlanTrainingActionMapper planActionMapper) {
        this.recordMapper = recordMapper;
        this.detailMapper = detailMapper;
        this.planActionMapper = planActionMapper;
    }

    /**
     * 按日期范围查询训练记录
     */
    public List<TrainingRecord> list(Date startDate, Date endDate) {
        return recordMapper.selectByUserAndDateRange(
                SecurityUtils.getCurrentUserId(), startDate, endDate);
    }

    /**
     * 查询单条训练记录(含动作明细)
     */
    public Map<String, Object> getById(Long id) {
        TrainingRecord record = recordMapper.selectById(id);
        if (record == null || !record.getUserId().equals(SecurityUtils.getCurrentUserId())) {
            throw new BusinessException(ResultCode.NOT_FOUND, "训练记录不存在");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("record", record);
        result.put("details", detailMapper.selectByTrainingRecordId(id));
        return result;
    }

    /**
     * 创建训练记录，支持从计划导入动作列表
     */
    @Transactional
    public TrainingRecord create(TrainingRecordDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();

        // 创建训练记录主表
        TrainingRecord record = new TrainingRecord();
        record.setUserId(userId);
        record.setRecordDate(dto.getRecordDate());
        record.setPlanId(dto.getPlanId());
        record.setTrainingDayId(dto.getTrainingDayId());
        record.setTrainingType(dto.getTrainingType());
        record.setStartTime(dto.getStartTime());
        record.setEndTime(dto.getEndTime());
        record.setDurationMinutes(dto.getDurationMinutes());
        record.setNote(dto.getNote());
        recordMapper.insert(record);

        // 处理动作明细
        List<TrainingRecordDetail> details = buildDetails(record.getId(), dto);
        if (!details.isEmpty()) {
            detailMapper.batchInsert(details);
        }

        return record;
    }

    /**
     * 更新训练记录
     */
    @Transactional
    public void update(Long id, TrainingRecordDTO dto) {
        TrainingRecord record = recordMapper.selectById(id);
        if (record == null || !record.getUserId().equals(SecurityUtils.getCurrentUserId())) {
            throw new BusinessException(ResultCode.NOT_FOUND, "训练记录不存在");
        }

        record.setRecordDate(dto.getRecordDate());
        record.setPlanId(dto.getPlanId());
        record.setTrainingDayId(dto.getTrainingDayId());
        record.setTrainingType(dto.getTrainingType());
        record.setStartTime(dto.getStartTime());
        record.setEndTime(dto.getEndTime());
        record.setDurationMinutes(dto.getDurationMinutes());
        record.setNote(dto.getNote());
        recordMapper.updateById(record);

        // 删除旧明细，重建新明细
        detailMapper.deleteByRecordId(id);
        List<TrainingRecordDetail> details = buildDetails(id, dto);
        if (!details.isEmpty()) {
            detailMapper.batchInsert(details);
        }
    }

    /**
     * 删除训练记录
     */
    @Transactional
    public void delete(Long id) {
        TrainingRecord record = recordMapper.selectById(id);
        if (record == null || !record.getUserId().equals(SecurityUtils.getCurrentUserId())) {
            throw new BusinessException(ResultCode.NOT_FOUND, "训练记录不存在");
        }
        // 先删除动作明细，防止孤儿记录
        detailMapper.deleteByRecordId(id);
        recordMapper.deleteById(id);
    }

    /**
     * 获取训练打卡日历数据
     */
    public List<CalendarDataVO> calendar(int year, int month) {
        List<TrainingRecord> records = recordMapper.selectCalendarData(
                SecurityUtils.getCurrentUserId(), year, month);
        List<CalendarDataVO> result = new ArrayList<>();
        for (TrainingRecord r : records) {
            result.add(new CalendarDataVO(r.getRecordDate(), r.getTrainingType()));
        }
        return result;
    }

    /**
     * 构建动作明细列表：优先从计划导入，否则使用用户输入
     */
    private List<TrainingRecordDetail> buildDetails(Long recordId, TrainingRecordDTO dto) {
        List<TrainingRecordDetail> details = new ArrayList<>();

        // 合并用户手动输入的动作明细(如果有)
        if (dto.getDetails() != null) {
            int order = 0;
            for (TrainingRecordDTO.DetailItem item : dto.getDetails()) {
                TrainingRecordDetail detail = new TrainingRecordDetail();
                detail.setTrainingRecordId(recordId);
                detail.setActionId(item.getActionId());
                detail.setActionName(item.getActionName());
                detail.setSets(item.getSets() != null ? item.getSets() : 0);
                detail.setWeightKg(item.getWeightKg() != null ? item.getWeightKg() : 0.0);
                detail.setSortOrder(item.getSortOrder() != null ? item.getSortOrder() : order++);
                details.add(detail);
            }
        }

        // 如果前端只提交了计划训练日而未提交动作明细，则从计划训练日导入动作列表。
        if (details.isEmpty() && dto.getTrainingDayId() != null) {
            List<PlanTrainingAction> planActions = planActionMapper.selectByDayId(dto.getTrainingDayId());
            int order = 0;
            for (PlanTrainingAction pa : planActions) {
                TrainingRecordDetail detail = new TrainingRecordDetail();
                detail.setTrainingRecordId(recordId);
                detail.setActionId(pa.getActionId());
                detail.setActionName(pa.getActionName());
                detail.setSets(pa.getMinSets());
                detail.setWeightKg(0.0);
                detail.setSortOrder(order++);
                details.add(detail);
            }
        }

        return details;
    }
}
