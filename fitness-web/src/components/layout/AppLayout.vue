<template>
  <!-- 主布局：薄荷绿清新主题 —— 白色侧边栏 + 浅薄荷绿内容区 -->
  <el-container class="app-layout">
    <!-- 侧边栏 -->
    <el-aside width="220px" class="app-aside">
      <Sidebar />
    </el-aside>

    <el-container class="app-main">
      <!-- 顶部栏 -->
      <el-header class="app-header">
        <HeaderBar />
      </el-header>

      <!-- 主内容区 -->
      <el-main class="app-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
/**
 * 主布局组件 —— 薄荷绿清新主题
 * 左侧白色侧边栏 + 右侧(白色顶部栏 + 浅薄荷绿内容区) 的结构
 * 全部使用浅色背景，薄荷绿作为点缀
 */
import Sidebar from './Sidebar.vue'
import HeaderBar from './HeaderBar.vue'
</script>

<style scoped lang="scss">
/**
 * 布局样式 —— 薄荷绿清新主题
 * 全屏高度布局，浅薄荷绿背景体系
 */

.app-layout {
  height: 100vh;
  overflow: hidden;
}

// ==================== 侧边栏容器 ====================

.app-aside {
  background-color: #ffffff !important;
  border-right: 1px solid #e0e8e4;
  overflow: hidden;
  transition: width 0.3s ease;
  box-shadow: 2px 0 12px rgba(0, 0, 0, 0.04);
}

// ==================== 主区域 ====================

.app-main {
  background-color: #f5faf7;
  overflow: hidden;
}

// ==================== 顶部栏容器 ====================

.app-header {
  --el-header-height: 52px;
  height: 52px;
  background: #ffffff;
  border-bottom: 1px solid #e0e8e4;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
  z-index: 10;

  // 底部微光薄荷线
  &::after {
    content: '';
    position: absolute;
    bottom: -1px;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(
      90deg,
      transparent 0%,
      rgba(56, 181, 137, 0.15) 40%,
      rgba(56, 181, 137, 0.08) 60%,
      transparent 100%
    );
  }
}

// ==================== 内容区 ====================

.app-content {
  background-color: #f5faf7;
  padding: 20px;
  overflow-y: auto;
  overflow-x: hidden;

  // 内容区自定义滚动条
  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background: #c0d4c8;
    border-radius: 3px;

    &:hover {
      background: #38b589;
    }
  }
}

// ==================== 响应式：移动端侧边栏收起 ====================

@media (max-width: 768px) {
  .app-aside {
    width: 64px !important;
  }

  .app-header {
    padding: 0 16px;
  }

  .app-content {
    padding: 12px;
  }
}
</style>
