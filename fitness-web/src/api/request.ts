import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

/**
 * 创建Axios实例，配置baseURL和超时时间
 */
const request = axios.create({
  baseURL: '/api/v1',
  timeout: 15000
})

/**
 * 请求拦截器：自动附加JWT token
 * 每次请求前从localStorage读取accessToken并附加到Authorization头
 */
request.interceptors.request.use(config => {
  const token = localStorage.getItem('accessToken')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

/**
 * 响应拦截器：统一处理错误和token刷新
 * - 成功响应: 提取data字段返回
 * - 401错误: 尝试用refreshToken刷新，失败则跳转登录页
 * - 其他错误: 弹出错误提示
 */
request.interceptors.response.use(
  response => {
    const { code, message, data } = response.data
    if (code === 200) return data
    ElMessage.error(message || '请求失败')
    return Promise.reject(new Error(message))
  },
  async error => {
    if (error.response?.status === 401) {
      const refreshToken = localStorage.getItem('refreshToken')
      if (refreshToken) {
        try {
          // 尝试刷新token
          const res = await axios.post('/api/v1/auth/refresh', null, { params: { refreshToken } })
          const { accessToken, refreshToken: newRefresh } = res.data.data
          localStorage.setItem('accessToken', accessToken)
          localStorage.setItem('refreshToken', newRefresh)
          // 重试原请求
          error.config.headers.Authorization = `Bearer ${accessToken}`
          return request(error.config)
        } catch {
          // Token刷新失败，清除登录状态并跳转登录页
          localStorage.clear()
          router.push('/login')
        }
      } else {
        // 无refreshToken，直接跳转登录页
        localStorage.clear()
        router.push('/login')
      }
    }
    return Promise.reject(error)
  }
)

export default request
