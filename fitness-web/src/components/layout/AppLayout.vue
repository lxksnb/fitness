<template>
  <!-- 主布局：Dark Iron 暗黑美学 —— 左侧深色侧边栏 + 右侧暗色内容区 -->
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
 * 主布局组件 —— Dark Iron 暗黑美学
 * 左侧侧边栏 + 右侧(顶部栏 + 内容区) 的结构
 * 全部使用深色背景，琥珀金作为点缀
 */
import Sidebar from './Sidebar.vue'
import HeaderBar from './HeaderBar.vue'
</script>

<style scoped lang="scss">
/**
 * 布局样式 —— Dark Iron 暗黑美学
 * 全屏高度布局，深色背景体系
 */

.app-layout {
  height: 100vh;
  overflow: hidden;
}

// ==================== 侧边栏容器 ====================

.app-aside {
  background-color: #21252b !important;
  border-right: 1px solid #333840;
  overflow: hidden;
  transition: width 0.3s ease;
  box-shadow: 2px 0 12px rgba(0, 0, 0, 0.3);
}

// ==================== 主区域 ====================

.app-main {
  background-color: #1a1d23;
  overflow: hidden;
}

// ==================== 顶部栏容器 ====================

.app-header {
  --el-header-height: 52px;
  height: 52px;
  background: #1e2128;
  border-bottom: 1px solid #333840;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
  z-index: 10;

  // 底部微光琥珀线
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
      rgba(240, 165, 0, 0.15) 40%,
      rgba(240, 165, 0, 0.08) 60%,
      transparent 100%
    );
  }
}

// ==================== 内容区 ====================

.app-content {
  background-color: #1a1d23;
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
    background: #333840;
    border-radius: 3px;

    &:hover {
      background: #f0a500;
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
