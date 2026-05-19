import request from './request'

/**
 * 登录接口
 * @param data 包含用户名和密码
 * @returns 返回token和用户信息
 */
export function login(data: { username: string; password: string }) {
  return request.post('/auth/login', data)
}

/**
 * 注册接口
 * @param data 包含用户名、密码和昵称
 * @returns 返回注册结果
 */
export function register(data: { username: string; password: string; nickname: string }) {
  return request.post('/auth/register', data)
}
