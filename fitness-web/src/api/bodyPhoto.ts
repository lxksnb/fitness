import request from './request'

/**
 * 身体照片 API 模块
 */

/** 获取照片列表（按日期范围筛选） */
export function getPhotos(startDate: string, endDate: string) {
  return request.get('/body-photo', { params: { startDate, endDate } })
}

/** 保存照片记录 */
export function savePhoto(data: any) {
  return request.post('/body-photo', data)
}

/** 删除照片记录 */
export function deletePhoto(id: number) {
  return request.delete(`/body-photo/${id}`)
}
