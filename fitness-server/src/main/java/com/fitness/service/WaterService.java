package com.fitness.service;

import com.fitness.dto.WaterRecordDTO;
import com.fitness.entity.WaterRecord;
import com.fitness.mapper.WaterRecordMapper;
import com.fitness.utils.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 饮水记录服务
 */
@Service
public class WaterService {

    private final WaterRecordMapper mapper;

    public WaterService(WaterRecordMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 记录饮水
     *
     * @param dto 饮水记录请求数据
     */
    @Transactional
    public void save(WaterRecordDTO dto) {
        WaterRecord record = new WaterRecord();
        record.setUserId(SecurityUtils.getCurrentUserId());
        record.setRecordDate(dto.getRecordDate());
        record.setAmountMl(dto.getAmountMl());
        record.setRecordedAt(new Date());
        mapper.insert(record);
    }

    /**
     * 获取当日饮水总量及目标
     *
     * @param date 查询日期
     * @return 包含totalMl(已饮水量)和targetMl(目标饮水量)的Map
     */
    public Map<String, Object> todayTotal(Date date) {
        Long userId = SecurityUtils.getCurrentUserId();
        Integer total = mapper.selectTodayTotal(userId, date);
        Map<String, Object> result = new HashMap<>();
        result.put("totalMl", total != null ? total : 0);
        result.put("targetMl", 2000); // 默认每日饮水目标2000ml
        return result;
    }

    /**
     * 查询某日饮水记录列表
     *
     * @param date 查询日期
     * @return 饮水记录列表
     */
    public List<WaterRecord> list(Date date) {
        return mapper.selectByUserAndDate(SecurityUtils.getCurrentUserId(), date);
    }
}
