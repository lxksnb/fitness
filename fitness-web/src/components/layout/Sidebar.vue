<template>
  <!-- 侧边栏：薄荷绿清新主题 —— 白色背景 + 薄荷绿活跃色 -->
  <div class="sidebar">
    <!-- 品牌标识区 -->
    <div class="sidebar-brand">
      <span class="sidebar-logo">🏋️</span>
      <span class="sidebar-title">健身记录</span>
    </div>

    <!-- 导航菜单 -->
    <el-menu
      :default-active="route.path"
      background-color="#ffffff"
      text-color="#2d3436"
      active-text-color="#38b589"
      router
      class="sidebar-menu"
    >
      <el-menu-item index="/dashboard">
        <el-icon><HomeFilled /></el-icon>
        <span>首页看板</span>
      </el-menu-item>

      <el-menu-item index="/weight">
        <el-icon><TrendCharts /></el-icon>
        <span>体重管理</span>
      </el-menu-item>

      <el-sub-menu index="plan">
        <template #title>
          <el-icon><Document /></el-icon>
          <span>健身计划</span>
        </template>
        <el-menu-item index="/plans">我的计划</el-menu-item>
        <el-menu-item index="/plans/templates">模板市场</el-menu-item>
      </el-sub-menu>

      <el-menu-item index="/diet">
        <el-icon><Food /></el-icon>
        <span>饮食记录</span>
      </el-menu-item>

      <el-menu-item index="/foods">
        <el-icon><Menu /></el-icon>
        <span>食物库</span>
      </el-menu-item>

      <el-menu-item index="/actions">
        <el-icon><VideoPlay /></el-icon>
        <span>动作库</span>
      </el-menu-item>

      <el-menu-item index="/training">
        <el-icon><Trophy /></el-icon>
        <span>训练记录</span>
      </el-menu-item>

      <el-menu-item index="/measurement">
        <el-icon><ScaleToOriginal /></el-icon>
        <span>身体围度</span>
      </el-menu-item>

      <el-menu-item index="/photos">
        <el-icon><Picture /></el-icon>
        <span>身体照片</span>
      </el-menu-item>

      <el-menu-item index="/water">
        <el-icon><Coffee /></el-icon>
        <span>饮水记录</span>
      </el-menu-item>

      <!-- 管理员菜单：仅当用户角色为 ADMIN 时显示 -->
      <template v-if="userStore.role === 'ADMIN'">
        <el-sub-menu index="admin">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/admin/foods">食物库管理</el-menu-item>
          <el-menu-item index="/admin/actions">动作库管理</el-menu-item>
          <el-menu-item index="/admin/templates">模板管理</el-menu-item>
          <el-menu-item index="/admin/users">用户管理</el-menu-item>
          <el-menu-item index="/admin/dict">字典管理</el-menu-item>
        </el-sub-menu>
      </template>
    </el-menu>

    <!-- 底部装饰线 -->
    <div class="sidebar-footer">
      <div class="sidebar-footer-line" />
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 侧边栏组件 —— 薄荷绿清新主题
 * - 白色背景 (#ffffff) 区分于主背景
 * - 薄荷绿 (#38b589) 活跃菜单项
 * - 当前路由高亮对应菜单项，管理员角色显示管理菜单
 */
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  Coffee,
  Document,
  Food,
  HomeFilled,
  Menu,
  Picture,
  ScaleToOriginal,
  Setting,
  TrendCharts,
  Trophy,
  VideoPlay
} from '@element-plus/icons-vue'

const route = useRoute()
const userStore = useUserStore()
</script>

<style scoped lang="scss">
/**
 * 侧边栏样式 —— 薄荷绿清新主题
 * 品牌标识区薄荷绿文字 + 底部薄荷装饰线
 */

.sidebar {
  display: flex;
  flex-direction: column;
  height: 100%;
  background-color: #ffffff;
  overflow: hidden;
}

// ==================== 品牌标识区 ====================

.sidebar-brand {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 20px 16px 16px;
  border-bottom: 1px solid #e0e8e4;
  position: relative;

  // 底部薄荷光泽线
  &::after {
    content: '';
    position: absolute;
    bottom: -1px;
    left: 16px;
    right: 16px;
    height: 2px;
    background: linear-gradient(
      90deg,
      transparent 0%,
      rgba(56, 181, 137, 0.5) 20%,
      rgba(56, 181, 137, 0.6) 50%,
      rgba(56, 181, 137, 0.5) 80%,
      transparent 100%
    );
  }
}

.sidebar-logo {
  font-size: 22px;
  filter: drop-shadow(0 0 6px rgba(56, 181, 137, 0.3));
}

.sidebar-title {
  font-size: 17px;
  font-weight: 700;
  background: linear-gradient(135deg, #38b589, #2d9e73);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 1px;
}

// ==================== 导航菜单 ====================

.sidebar-menu {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  border-right: none !important;
  padding: 8px 0;

  // 自定义滚动条
  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background: #e0e8e4;
    border-radius: 2px;
  }

  // 菜单项样式增强
  :deep(.el-menu-item) {
    height: 44px;
    line-height: 44px;
    margin: 2px 8px;
    border-radius: 8px;
    font-size: 14px;
    border-left: 3px solid transparent;
    transition: all 0.2s ease;

    &:hover {
      background-color: rgba(56, 181, 137, 0.06) !important;
    }

    &.is-active {
      background: linear-gradient(
        90deg,
        rgba(56, 181, 137, 0.08) 0%,
        rgba(56, 181, 137, 0.02) 100%
      ) !important;
      border-left: 3px solid #38b589;
      font-weight: 600;
    }
  }

  // 子菜单标题样式
  :deep(.el-sub-menu__title) {
    height: 44px;
    line-height: 44px;
    margin: 2px 8px;
    border-radius: 8px;
    font-size: 14px;
    transition: all 0.2s ease;

    &:hover {
      background-color: rgba(56, 181, 137, 0.06) !important;
    }
  }

  // 子菜单展开时的标题
  :deep(.el-sub-menu.is-opened > .el-sub-menu__title) {
    color: #38b589 !important;
  }

  // 子菜单内嵌菜单
  :deep(.el-menu--inline) {
    background-color: transparent !important;

    .el-menu-item {
      padding-left: 56px !important;
      font-size: 13px;
      height: 38px;
      line-height: 38px;
    }
  }

  // 图标
  :deep(.el-icon) {
    font-size: 16px;
  }
}

// ==================== 底部装饰 ====================

.sidebar-footer {
  padding: 8px 16px 16px;
}

.sidebar-footer-line {
  height: 1px;
  background: linear-gradient(
    90deg,
    transparent 0%,
    rgba(56, 181, 137, 0.2) 30%,
    rgba(56, 181, 137, 0.15) 70%,
    transparent 100%
  );
}
</style>
