import request from './request'

/**
 * 查询体重记录列表
 * @param startDate 开始日期
 * @param endDate 结束日期
 * @returns 体重记录列表
 */
export function getWeights(startDate: string, endDate: string) {
  return request.get('/weight', { params: { startDate, endDate } })
}

/**
 * 保存体重记录(同一天自动覆盖)
 * @param data 包含记录日期、体重(kg)和备注
 * @returns 保存结果
 */
export function saveWeight(data: { recordDate: string; weightKg: number; note?: string }) {
  return request.post('/weight', data)
}

/**
 * 获取体重趋势数据
 * @param days 查询天数，默认30天
 * @returns 体重趋势数据
 */
export function getWeightTrend(days: number = 30) {
  return request.get('/weight/trend', { params: { days } })
}
