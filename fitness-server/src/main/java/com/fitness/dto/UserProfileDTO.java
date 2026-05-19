package com.fitness.dto;

import java.util.Date;

/**
 * 用户资料 DTO
 * 用于更新用户个人资料时的数据传输
 */
public class UserProfileDTO {
    /** 性别 */
    private String gender;
    /** 出生日期 */
    private Date birthday;
    /** 身高（厘米） */
    private Double heightCm;
    /** 目标类型 */
    private String targetType;
    /** 目标体重（千克） */
    private Double targetWeightKg;
    /** 目标达成日期 */
    private Date targetDate;
    /** 每周预期变化率 */
    private Double weeklyChangeRate;
    /** 昵称 */
    private String nickname;
    /** 邮箱 */
    private String email;

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public Date getBirthday() { return birthday; }
    public void setBirthday(Date birthday) { this.birthday = birthday; }
    public Double getHeightCm() { return heightCm; }
    public void setHeightCm(Double heightCm) { this.heightCm = heightCm; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public Double getTargetWeightKg() { return targetWeightKg; }
    public void setTargetWeightKg(Double targetWeightKg) { this.targetWeightKg = targetWeightKg; }
    public Date getTargetDate() { return targetDate; }
    public void setTargetDate(Date targetDate) { this.targetDate = targetDate; }
    public Double getWeeklyChangeRate() { return weeklyChangeRate; }
    public void setWeeklyChangeRate(Double weeklyChangeRate) { this.weeklyChangeRate = weeklyChangeRate; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
