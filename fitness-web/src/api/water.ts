import request from './request'

/**
 * 饮水记录 API 模块
 */

/** 获取当日饮水汇总信息 */
export function getTodayWater(date: string) {
  return request.get('/water/today', { params: { date } })
}

/** 获取饮水记录列表（按日期） */
export function getWaterList(date: string) {
  return request.get('/water', { params: { date } })
}

/** 保存饮水记录 */
export function saveWater(data: any) {
  return request.post('/water', data)
}
