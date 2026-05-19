import request from './request'

/**
 * 搜索食物库
 * @param keyword 搜索关键字，可选
 * @returns 食物列表
 */
export function searchFoods(keyword?: string) {
  return request.get('/foods', { params: { keyword } })
}

/**
 * 获取食物详情，包含所有营养单位变体
 * @param id 食物ID
 * @returns 食物详情
 */
export function getFoodDetail(id: number) {
  return request.get(`/foods/${id}`)
}

/**
 * 创建食物
 * @param data 食物数据
 * @returns 创建结果
 */
export function createFood(data: any) {
  return request.post('/foods', data)
}

/**
 * 更新食物
 * @param id 食物ID
 * @param data 更新数据
 * @returns 更新结果
 */
export function updateFood(id: number, data: any) {
  return request.put(`/foods/${id}`, data)
}

/**
 * 删除食物
 * @param id 食物ID
 * @returns 删除结果
 */
export function deleteFood(id: number) {
  return request.delete(`/foods/${id}`)
}
