import request from './request'

/**
 * 获取字典数据
 * @param code 字典编码，如 meal_type、suitable_for、unit_type
 * @returns 字典项列表
 */
export function getDict(code: string) {
  return request.get(`/dict/${code}`)
}
