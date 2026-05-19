package com.fitness.dto;

import java.util.Date;

/**
 * 身体照片记录请求DTO
 */
public class BodyPhotoDTO {
    /** 照片日期 */
    private Date photoDate;
    /** 照片角度: FRONT/SIDE/BACK */
    private String photoType;
    /** 照片URL */
    private String imageUrl;
    /** 备注 */
    private String note;

    public Date getPhotoDate() { return photoDate; }
    public void setPhotoDate(Date d) { this.photoDate = d; }
    public String getPhotoType() { return photoType; }
    public void setPhotoType(String s) { this.photoType = s; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String s) { this.imageUrl = s; }
    public String getNote() { return note; }
    public void setNote(String s) { this.note = s; }
}
