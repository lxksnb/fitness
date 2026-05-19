import request from './request'

/**
 * 身体围度测量 API 模块
 */

/** 获取测量记录列表（按日期范围筛选） */
export function getMeasurements(startDate: string, endDate: string) {
  return request.get('/measurement', { params: { startDate, endDate } })
}

/** 保存测量记录（新增或更新） */
export function saveMeasurement(data: any) {
  return request.post('/measurement', data)
}
