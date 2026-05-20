<template>
  <!-- 顶部栏：薄荷绿清新主题 —— 极简白色 + 薄荷绿点缀 -->
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
 * 顶部栏组件 —— 薄荷绿清新主题
 * - 极简设计，白色背景
 * - 用户头像薄荷绿环
 * - 底部薄荷色微光分割线
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
 * 顶部栏样式 —— 薄荷绿清新主题
 * 24px 高度，薄荷绿底线，圆形头像薄荷绿环
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
  background: linear-gradient(135deg, #38b589, #2d9e73);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;

  // 薄荷绿光环
  &::after {
    content: '';
    position: absolute;
    inset: -2px;
    border-radius: 50%;
    border: 2px solid rgba(56, 181, 137, 0.3);
    transition: border-color 0.3s ease;
  }

  &:hover::after {
    border-color: rgba(56, 181, 137, 0.6);
  }
}

.header-avatar-text {
  font-size: 14px;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: 0.5px;
}

.header-greeting {
  font-size: 13px;
  color: #636e72;
}

.header-nickname {
  font-size: 14px;
  font-weight: 600;
  color: #2d3436;
}

// ==================== 操作按钮区 ====================

.header-actions {
  display: flex;
  align-items: center;
  gap: 4px;
}

.header-action-btn {
  color: #636e72 !important;
  font-size: 13px;
  padding: 6px 12px;
  border-radius: 6px;
  transition: all 0.2s ease;

  .el-icon {
    font-size: 15px;
  }

  &:hover {
    color: #38b589 !important;
    background-color: rgba(56, 181, 137, 0.06) !important;
  }

  &--danger:hover {
    color: #e74c3c !important;
    background-color: rgba(231, 76, 60, 0.06) !important;
  }
}
</style>
