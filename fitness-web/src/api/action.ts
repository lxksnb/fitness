import request from './request'

/**
 * 搜索动作库
 * @param keyword 搜索关键字，可选
 * @param muscleCode 肌群筛选，可选
 * @returns 动作列表
 */
export function searchActions(keyword?: string, muscleCode?: string, scope?: 'SYSTEM' | 'USER') {
  return request.get('/actions', { params: { keyword, muscleCode, scope } })
}

/**
 * 创建动作
 * @param data 动作数据
 * @returns 创建结果
 */
export function createAction(data: any) {
  return request.post('/actions', data)
}

/**
 * 更新动作
 * @param id 动作ID
 * @param data 更新数据
 * @returns 更新结果
 */
export function updateAction(id: number, data: any) {
  return request.put(`/actions/${id}`, data)
}

/**
 * 删除动作
 * @param id 动作ID
 * @returns 删除结果
 */
export function deleteAction(id: number) {
  return request.delete(`/actions/${id}`)
}

/**
 * 获取动作的训练记录（重量递增历史）
 * @param id 动作ID
 * @returns 训练记录列表
 */
export function getActionRecords(id: number) {
  return request.get(`/actions/${id}/records`)
}
