<template>
  <!-- 欢迎信息 -->
  <div>欢迎, {{ userStore.nickname || '用户' }}</div>
  <!-- 操作按钮 -->
  <div style="display: flex; align-items: center; gap: 16px">
    <el-button text @click="$router.push('/profile')">个人设置</el-button>
    <el-button text type="danger" @click="handleLogout">退出登录</el-button>
  </div>
</template>

<script setup lang="ts">
/**
 * 顶部栏组件
 * 显示欢迎信息和用户操作按钮(个人设置、退出登录)
 */
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()

/** 退出登录: 清除用户状态并跳转到登录页 */
function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>
