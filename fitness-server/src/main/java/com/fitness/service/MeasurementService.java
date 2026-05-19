package com.fitness.service;

import com.fitness.dto.BodyMeasurementDTO;
import com.fitness.entity.BodyMeasurement;
import com.fitness.mapper.BodyMeasurementMapper;
import com.fitness.utils.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 身体围度记录服务
 */
@Service
public class MeasurementService {

    private final BodyMeasurementMapper mapper;

    public MeasurementService(BodyMeasurementMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 按日期范围查询当前用户的围度记录
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 围度记录列表
     */
    public List<BodyMeasurement> list(Date startDate, Date endDate) {
        return mapper.selectByUserAndDateRange(SecurityUtils.getCurrentUserId(), startDate, endDate);
    }

    /**
     * 新增身体围度记录
     *
     * @param dto 围度记录请求数据
     */
    @Transactional
    public void save(BodyMeasurementDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        BodyMeasurement m = new BodyMeasurement();
        m.setUserId(userId);
        m.setRecordDate(dto.getRecordDate());
        m.setChestCm(dto.getChestCm());
        m.setWaistCm(dto.getWaistCm());
        m.setLeftArmCm(dto.getLeftArmCm());
        m.setRightArmCm(dto.getRightArmCm());
        m.setLeftThighCm(dto.getLeftThighCm());
        m.setRightThighCm(dto.getRightThighCm());
        m.setHipCm(dto.getHipCm());
        m.setNeckCm(dto.getNeckCm());
        m.setNote(dto.getNote());
        mapper.insert(m);
    }
}
