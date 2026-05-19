/**
 * 计算热量: 碳水×4 + 蛋白质×4 + 脂肪×9
 * @param carb 碳水(克)
 * @param protein 蛋白质(克)
 * @param fat 脂肪(克)
 * @returns 总热量(千卡)
 */
export function calcCalories(carb: number, protein: number, fat: number): number {
  return carb * 4 + protein * 4 + fat * 9
}

/**
 * 日期格式化
 * @param date 日期对象或字符串
 * @param fmt 格式化模板 (默认 'yyyy-MM-dd')
 * @returns 格式化后的日期字符串
 */
export function formatDate(date: Date | string, fmt = 'yyyy-MM-dd'): string {
  const d = new Date(date)
  const o: Record<string, number> = {
    'M+': d.getMonth() + 1,
    'd+': d.getDate(),
    'h+': d.getHours(),
    'm+': d.getMinutes(),
    's+': d.getSeconds()
  }
  let result = fmt
  if (/(y+)/.test(result)) {
    result = result.replace(RegExp.$1, String(d.getFullYear()).slice(4 - RegExp.$1.length))
  }
  for (const k in o) {
    if (new RegExp(`(${k})`).test(result)) {
      result = result.replace(RegExp.$1, RegExp.$1.length === 1 ? String(o[k]) : `0${o[k]}`)
    }
  }
  return result
}
