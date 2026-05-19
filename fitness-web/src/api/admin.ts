import request from './request'

// ==================== 管理后台 API 模块 ====================

// ---------- 食物库管理 ----------

/** 获取系统食物列表 */
export function getAdminFoods(params?: { keyword?: string }) {
  return request.get('/admin/foods', { params })
}

/** 创建系统食物 */
export function createAdminFood(data: any) {
  return request.post('/admin/foods', data)
}

/** 更新系统食物 */
export function updateAdminFood(id: number, data: any) {
  return request.put(`/admin/foods/${id}`, data)
}

/** 删除系统食物（软删除） */
export function deleteAdminFood(id: number) {
  return request.delete(`/admin/foods/${id}`)
}

// ---------- 动作库管理 ----------

/** 获取系统动作列表 */
export function getAdminActions(params?: { keyword?: string }) {
  return request.get('/admin/actions', { params })
}

/** 创建系统动作 */
export function createAdminAction(data: any) {
  return request.post('/admin/actions', data)
}

/** 更新系统动作 */
export function updateAdminAction(id: number, data: any) {
  return request.put(`/admin/actions/${id}`, data)
}

/** 删除系统动作 */
export function deleteAdminAction(id: number) {
  return request.delete(`/admin/actions/${id}`)
}

// ---------- 模板管理 ----------

/** 获取模板列表 */
export function getAdminTemplates(params?: { keyword?: string }) {
  return request.get('/admin/templates', { params })
}

/** 创建模板 */
export function createAdminTemplate(data: any) {
  return request.post('/admin/templates', data)
}

/** 更新模板 */
export function updateAdminTemplate(id: number, data: any) {
  return request.put(`/admin/templates/${id}`, data)
}

/** 删除模板 */
export function deleteAdminTemplate(id: number) {
  return request.delete(`/admin/templates/${id}`)
}

// ---------- 用户管理 ----------

/** 获取用户列表 */
export function getAdminUsers(params?: { keyword?: string }) {
  return request.get('/admin/users', { params })
}

/** 切换用户状态（启用/禁用） */
export function updateUserStatus(id: number, status: 'ACTIVE' | 'DISABLED') {
  return request.put(`/admin/users/${id}/status`, null, { params: { status } })
}
