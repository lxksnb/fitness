import request from './request'

/**
 * 获取所有健身计划
 * @returns 计划列表
 */
export function getPlans() {
  return request.get('/plans')
}

/**
 * 获取计划详情（含嵌套的训练日、动作、餐次配置）
 * @param id 计划ID
 * @returns 计划完整详情
 */
export function getPlanDetail(id: number) {
  return request.get(`/plans/${id}`)
}

/**
 * 创建健身计划
 * @param data 计划DTO（含嵌套训练日、餐次配置）
 * @returns 创建结果
 */
export function createPlan(data: any) {
  return request.post('/plans', data)
}

/**
 * 更新健身计划（删除旧子项后重建）
 * @param id 计划ID
 * @param data 计划DTO
 * @returns 更新结果
 */
export function updatePlan(id: number, data: any) {
  return request.put(`/plans/${id}`, data)
}

/**
 * 删除健身计划
 * @param id 计划ID
 * @returns 删除结果
 */
export function deletePlan(id: number) {
  return request.delete(`/plans/${id}`)
}

/**
 * 激活计划（设为当前计划）
 * @param id 计划ID
 * @returns 激活结果
 */
export function activatePlan(id: number) {
  return request.put(`/plans/${id}/activate`)
}

export function skipPlanCurrentDay(id: number) {
  return request.put(`/plans/${id}/progress/skip`)
}

/**
 * 获取计划模板列表
 * @returns 模板列表
 */
export function getTemplates() {
  return request.get('/plan-templates')
}

/**
 * 从模板导入计划
 * @param id 模板ID
 * @returns 新创建的计划数据（含ID，用于跳转编辑页）
 */
export function importTemplate(id: number) {
  return request.post(`/plan-templates/${id}/import`)
}
