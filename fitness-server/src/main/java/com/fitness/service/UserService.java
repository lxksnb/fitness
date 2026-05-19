package com.fitness.service;

import com.fitness.dto.UserProfileDTO;
import com.fitness.entity.SysUser;
import com.fitness.entity.UserProfile;
import com.fitness.mapper.SysUserMapper;
import com.fitness.mapper.UserProfileMapper;
import com.fitness.utils.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    private final SysUserMapper userMapper;
    private final UserProfileMapper profileMapper;

    public UserService(SysUserMapper userMapper, UserProfileMapper profileMapper) {
        this.userMapper = userMapper;
        this.profileMapper = profileMapper;
    }

    public Map<String, Object> getProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        SysUser user = userMapper.selectById(userId);
        UserProfile profile = profileMapper.selectByUserId(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("username", user.getUsername());
        result.put("nickname", user.getNickname());
        result.put("email", user.getEmail());
        result.put("avatar", user.getAvatar());
        result.put("role", user.getRole());
        if (profile != null) {
            result.put("gender", profile.getGender());
            result.put("birthday", profile.getBirthday());
            result.put("heightCm", profile.getHeightCm());
            result.put("targetType", profile.getTargetType());
            result.put("targetWeightKg", profile.getTargetWeightKg());
            result.put("targetDate", profile.getTargetDate());
            result.put("weeklyChangeRate", profile.getWeeklyChangeRate());
        }
        return result;
    }

    @Transactional
    public void updateProfile(UserProfileDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        // Update sys_user
        SysUser user = new SysUser();
        user.setId(userId);
        user.setNickname(dto.getNickname());
        user.setEmail(dto.getEmail());
        userMapper.updateById(user);
        // Update or insert user_profile
        UserProfile profile = profileMapper.selectByUserId(userId);
        if (profile == null) {
            profile = new UserProfile();
            profile.setUserId(userId);
            fillProfile(profile, dto);
            profileMapper.insert(profile);
        } else {
            fillProfile(profile, dto);
            profileMapper.updateByUserId(profile);
        }
    }

    private void fillProfile(UserProfile profile, UserProfileDTO dto) {
        profile.setGender(dto.getGender());
        profile.setBirthday(dto.getBirthday());
        profile.setHeightCm(dto.getHeightCm());
        profile.setTargetType(dto.getTargetType());
        profile.setTargetWeightKg(dto.getTargetWeightKg());
        profile.setTargetDate(dto.getTargetDate());
        profile.setWeeklyChangeRate(dto.getWeeklyChangeRate());
    }
}
