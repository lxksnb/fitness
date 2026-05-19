import request from './request'

/**
 * 用户信息 API 模块
 * 包含个人信息、身体档案的查询和更新
 */

/** 获取用户个人信息和身体档案 */
export function getProfile() {
  return request.get('/user/profile')
}

/** 更新用户个人信息和身体档案 */
export function updateProfile(data: any) {
  return request.put('/user/profile', data)
}
