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

@Service
public class WeightService {

    private final WeightRecordMapper weightMapper;
    private final UserProfileMapper profileMapper;

    public WeightService(WeightRecordMapper weightMapper, UserProfileMapper profileMapper) {
        this.weightMapper = weightMapper;
        this.profileMapper = profileMapper;
    }

    public List<WeightRecord> list(Date startDate, Date endDate) {
        Long userId = SecurityUtils.getCurrentUserId();
        return weightMapper.selectByUserAndDateRange(userId, startDate, endDate);
    }

    @Transactional
    public void save(WeightRecordDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        WeightRecord existing = weightMapper.selectByUserAndDate(userId, dto.getRecordDate());
        if (existing != null) {
            existing.setWeightKg(dto.getWeightKg());
            existing.setNote(dto.getNote());
            existing.setBmi(calcBmi(userId, dto.getWeightKg()));
            weightMapper.updateById(existing);
        } else {
            WeightRecord record = new WeightRecord();
            record.setUserId(userId);
            record.setRecordDate(dto.getRecordDate());
            record.setWeightKg(dto.getWeightKg());
            record.setNote(dto.getNote());
            record.setBmi(calcBmi(userId, dto.getWeightKg()));
            weightMapper.insert(record);
        }
    }

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

    private Double calcBmi(Long userId, Double weightKg) {
        UserProfile profile = profileMapper.selectByUserId(userId);
        if (profile != null && profile.getHeightCm() != null && profile.getHeightCm() > 0) {
            double heightM = profile.getHeightCm() / 100.0;
            double bmi = weightKg / (heightM * heightM);
            return Math.round(bmi * 10.0) / 10.0;
        }
        return null;
    }
}
