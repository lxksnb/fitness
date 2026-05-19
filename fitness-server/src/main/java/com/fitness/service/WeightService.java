package com.fitness.service;

import com.fitness.dto.WeightRecordDTO;
import com.fitness.dto.WeightTrendVO;
import com.fitness.entity.UserProfile;
import com.fitness.entity.WeightRecord;
import com.fitness.mapper.UserProfileMapper;
import com.fitness.mapper.WeightRecordMapper;
import com.fitness.utils.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 体重服务
 * 处理体重记录的增删改查和趋势分析
 */
@Service
public class WeightService {

    private final WeightRecordMapper weightMapper;
    private final UserProfileMapper profileMapper;

    public WeightService(WeightRecordMapper weightMapper, UserProfileMapper profileMapper) {
        this.weightMapper = weightMapper;
        this.profileMapper = profileMapper;
    }

    /**
     * 查询指定日期范围内的体重记录
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 体重记录列表
     */
    public List<WeightRecord> list(Date startDate, Date endDate) {
        Long userId = SecurityUtils.getCurrentUserId();
        return weightMapper.selectByUserAndDateRange(userId, startDate, endDate);
    }

    /**
     * 保存体重记录（新建或更新）
     * 如果当天已有记录则更新，否则新增；同时自动计算 BMI
     *
     * @param dto 体重记录 DTO
     */
    @Transactional
    public void save(WeightRecordDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        WeightRecord existing = weightMapper.selectByUserAndDate(userId, dto.getRecordDate());
        if (existing != null) {
            // 更新已有记录
            existing.setWeightKg(dto.getWeightKg());
            existing.setNote(dto.getNote());
            existing.setBmi(calcBmi(userId, dto.getWeightKg()));
            weightMapper.updateById(existing);
        } else {
            // 创建新记录
            WeightRecord record = new WeightRecord();
            record.setUserId(userId);
            record.setRecordDate(dto.getRecordDate());
            record.setWeightKg(dto.getWeightKg());
            record.setNote(dto.getNote());
            record.setBmi(calcBmi(userId, dto.getWeightKg()));
            weightMapper.insert(record);
        }
    }

    /**
     * 获取体重趋势数据
     * @param days 查询最近的天数
     * @return 体重趋势数据点列表（日期、体重、BMI）
     */
    public List<WeightTrendVO> trend(int days) {
        Long userId = SecurityUtils.getCurrentUserId();
        long now = System.currentTimeMillis();
        Date endDate = new Date(now);
        Date startDate = new Date(now - (long) days * 24 * 3600 * 1000);
        List<WeightRecord> records = weightMapper.selectByUserAndDateRange(userId, startDate, endDate);
        List<WeightTrendVO> result = new ArrayList<>();
        for (WeightRecord r : records) {
            result.add(new WeightTrendVO(r.getRecordDate(), r.getWeightKg(), r.getBmi()));
        }
        return result;
    }

    /**
     * 计算 BMI 指数
     * BMI = 体重(kg) / 身高(m)^2
     *
     * @param userId 用户 ID
     * @param weightKg 体重（千克）
     * @return BMI 值，保留一位小数；如果无法获取身高则返回 null
     */
    private Double calcBmi(Long userId, Double weightKg) {
        UserProfile profile = profileMapper.selectByUserId(userId);
        if (profile != null && profile.getHeightCm() != null && profile.getHeightCm() > 0) {
            double heightM = profile.getHeightCm() / 100.0;
            double bmi = weightKg / (heightM * heightM);
            return Math.round(bmi * 10.0) / 10.0; // 保留一位小数
        }
        return null;
    }
}
