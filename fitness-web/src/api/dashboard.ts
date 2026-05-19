import request from './request'

/**
 * 首页看板API
 * 获取今日体重、饮食汇总、饮水、训练、连续打卡、体重趋势等聚合数据
 */
export function getDashboard() {
  return request.get('/dashboard')
}
