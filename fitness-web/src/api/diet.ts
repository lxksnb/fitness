import request from './request'

/**
 * 查询指定日期的饮食记录列表
 * @param date 日期字符串，格式 yyyy-MM-dd
 * @returns 饮食记录列表
 */
export function getDietList(date: string) {
  return request.get('/diet', { params: { date } })
}

/**
 * 创建饮食记录
 * @param data 饮食记录数据
 * @returns 创建结果
 */
export function createDiet(data: any) {
  return request.post('/diet', data)
}

/**
 * 更新饮食记录
 * @param id 记录ID
 * @param data 更新数据
 * @returns 更新结果
 */
export function updateDiet(id: number, data: any) {
  return request.put(`/diet/${id}`, data)
}

/**
 * 删除饮食记录
 * @param id 记录ID
 * @returns 删除结果
 */
export function deleteDiet(id: number) {
  return request.delete(`/diet/${id}`)
}

/**
 * 获取每日饮食汇总
 * @param date 日期字符串，格式 yyyy-MM-dd
 * @returns 当日营养汇总
 */
export function getDailySummary(date: string) {
  return request.get('/diet/daily-summary', { params: { date } })
}
