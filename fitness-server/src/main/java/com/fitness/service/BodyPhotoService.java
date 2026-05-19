package com.fitness.service;

import com.fitness.common.ResultCode;
import com.fitness.dto.BodyPhotoDTO;
import com.fitness.entity.BodyPhoto;
import com.fitness.exception.BusinessException;
import com.fitness.mapper.BodyPhotoMapper;
import com.fitness.utils.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 身体照片记录服务
 */
@Service
public class BodyPhotoService {

    private final BodyPhotoMapper mapper;

    public BodyPhotoService(BodyPhotoMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 按日期范围查询当前用户的身体照片记录
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 照片记录列表
     */
    public List<BodyPhoto> list(Date startDate, Date endDate) {
        return mapper.selectByUserAndDateRange(SecurityUtils.getCurrentUserId(), startDate, endDate);
    }

    /**
     * 新增身体照片记录
     *
     * @param dto 照片记录请求数据
     */
    @Transactional
    public void save(BodyPhotoDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        BodyPhoto photo = new BodyPhoto();
        photo.setUserId(userId);
        photo.setPhotoDate(dto.getPhotoDate());
        photo.setPhotoType(dto.getPhotoType());
        photo.setImageUrl(dto.getImageUrl());
        photo.setNote(dto.getNote());
        mapper.insert(photo);
    }

    /**
     * 删除身体照片记录（仅允许删除自己的记录）
     *
     * @param id 照片记录ID
     */
    @Transactional
    public void delete(Long id) {
        BodyPhoto photo = mapper.selectById(id);
        if (photo == null || !photo.getUserId().equals(SecurityUtils.getCurrentUserId())) {
            throw new BusinessException(ResultCode.NOT_FOUND, "照片记录不存在");
        }
        mapper.deleteById(id);
    }
}
