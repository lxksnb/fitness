<template>
  <!-- 顶部栏：Dark Iron 暗黑美学 —— 极简风格 + 琥珀金点缀 -->
  <div class="header-bar">
    <!-- 欢迎信息 -->
    <div class="header-user">
      <div class="header-avatar">
        <span class="header-avatar-text">
          {{ (userStore.nickname || '用户').charAt(0).toUpperCase() }}
        </span>
      </div>
      <span class="header-greeting">欢迎，</span>
      <span class="header-nickname">{{ userStore.nickname || '用户' }}</span>
    </div>

    <!-- 操作按钮区 -->
    <div class="header-actions">
      <el-button text class="header-action-btn" @click="$router.push('/profile')">
        <el-icon><User /></el-icon>
        <span>个人设置</span>
      </el-button>
      <el-button text class="header-action-btn header-action-btn--danger" @click="handleLogout">
        <el-icon><SwitchButton /></el-icon>
        <span>退出登录</span>
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 顶部栏组件 —— Dark Iron 暗黑美学
 * - 极简设计，深色背景
 * - 用户头像琥珀金环
 * - 底部琥珀色微光分割线
 */
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { User, SwitchButton } from '@element-plus/icons-vue'

const userStore = useUserStore()
const router = useRouter()

/** 退出登录：清除用户状态并跳转到登录页 */
function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped lang="scss">
/**
 * 顶部栏样式 —— Dark Iron 暗黑美学
 * 24px 高度，琥珀金底线，圆形头像琥珀环
 */

.header-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  height: 100%;
}

// ==================== 用户信息区 ====================

.header-user {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: linear-gradient(135deg, #f0a500, #d48900);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;

  // 琥珀光环
  &::after {
    content: '';
    position: absolute;
    inset: -2px;
    border-radius: 50%;
    border: 2px solid rgba(240, 165, 0, 0.3);
    transition: border-color 0.3s ease;
  }

  &:hover::after {
    border-color: rgba(240, 165, 0, 0.6);
  }
}

.header-avatar-text {
  font-size: 14px;
  font-weight: 700;
  color: #1a1d23;
  letter-spacing: 0.5px;
}

.header-greeting {
  font-size: 13px;
  color: #9ca0a8;
}

.header-nickname {
  font-size: 14px;
  font-weight: 600;
  color: #e8e8ed;
}

// ==================== 操作按钮区 ====================

.header-actions {
  display: flex;
  align-items: center;
  gap: 4px;
}

.header-action-btn {
  color: #9ca0a8 !important;
  font-size: 13px;
  padding: 6px 12px;
  border-radius: 6px;
  transition: all 0.2s ease;

  .el-icon {
    font-size: 15px;
  }

  &:hover {
    color: #f0a500 !important;
    background-color: rgba(240, 165, 0, 0.06) !important;
  }

  &--danger:hover {
    color: #f85149 !important;
    background-color: rgba(248, 81, 73, 0.06) !important;
  }
}
</style>
