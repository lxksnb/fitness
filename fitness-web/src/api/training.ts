import request from './request'

/**
 * 训练记录 API 模块
 * 包含训练记录的 CRUD 操作和训练日历查询
 */

/** 获取训练记录列表 */
export function getTrainings(startDate: string, endDate: string) {
  return request.get('/training', { params: { startDate, endDate } })
}

/** 获取训练记录详情 */
export function getTrainingDetail(id: number) {
  return request.get(`/training/${id}`)
}

/** 创建训练记录 */
export function createTraining(data: any) {
  return request.post('/training', data)
}

/** 更新训练记录 */
export function updateTraining(id: number, data: any) {
  return request.put(`/training/${id}`, data)
}

/** 删除训练记录 */
export function deleteTraining(id: number) {
  return request.delete(`/training/${id}`)
}

/** 获取训练日历数据 */
export function getCalendar(year: number, month: number) {
  return request.get('/training/calendar', { params: { year, month } })
}
