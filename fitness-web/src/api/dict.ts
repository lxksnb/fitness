import request from './request'

export interface DictOption {
  label: string
  value: string
  sort?: number
}

export interface DictData {
  dictLabel?: string
  dictValue?: string
  label?: string
  value?: string
  name?: string
  code?: string
  sort?: number
}

/**
 * 获取字典数据
 * @param code 字典编码，如 meal_type、suitable_for、unit_type
 * @returns 字典项列表
 */
export function getDict(code: string) {
  return request.get(`/dict/${code}`)
}

export function toDictOptions(data: unknown): DictOption[] {
  const list = Array.isArray(data) ? data : []
  return list
    .map((item: DictData) => ({
      label: item.dictLabel || item.label || item.name || '',
      value: item.dictValue || item.value || item.code || '',
      sort: item.sort
    }))
    .filter(item => item.label && item.value)
}

/**
 * 获取适用于前端下拉框的字典选项
 */
export async function getDictOptions(code: string): Promise<DictOption[]> {
  const data = await getDict(code)
  return toDictOptions(data)
}
