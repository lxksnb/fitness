package com.fitness.dto;

import java.util.Date;

public class UserProfileDTO {
    private String gender;
    private Date birthday;
    private Double heightCm;
    private String targetType;
    private Double targetWeightKg;
    private Date targetDate;
    private Double weeklyChangeRate;
    private String nickname;
    private String email;

    // getters and setters for all fields
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
