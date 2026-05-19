import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * 用户状态管理
 * 管理当前登录用户的昵称、角色、用户ID
 * 数据持久化到localStorage，刷新页面后仍保持登录状态
 */
export const useUserStore = defineStore('user', () => {
  /** 用户昵称 */
  const nickname = ref(localStorage.getItem('nickname') || '')
  /** 用户角色 (USER / ADMIN) */
  const role = ref(localStorage.getItem('role') || '')
  /** 用户ID */
  const userId = ref(Number(localStorage.getItem('userId')) || 0)

  /**
   * 设置用户信息(登录成功后调用)
   * @param data 包含昵称、角色和用户ID
   */
  function setUser(data: { nickname: string; role: string; userId: number }) {
    nickname.value = data.nickname
    role.value = data.role
    userId.value = data.userId
    localStorage.setItem('nickname', data.nickname)
    localStorage.setItem('role', data.role)
    localStorage.setItem('userId', String(data.userId))
  }

  /**
   * 退出登录
   * 清除localStorage中的所有数据并重置状态
   */
  function logout() {
    localStorage.clear()
    nickname.value = ''
    role.value = ''
    userId.value = 0
  }

  return { nickname, role, userId, setUser, logout }
})
