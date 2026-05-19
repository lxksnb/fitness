<template>
  <el-container style="height: 100vh">
    <!-- 侧边栏 -->
    <el-aside width="220px" style="background-color: #304156">
      <Sidebar />
    </el-aside>
    <el-container>
      <!-- 顶部栏 -->
      <el-header style="background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,.08); padding: 0 20px; display: flex; align-items: center; justify-content: space-between">
        <HeaderBar />
      </el-header>
      <!-- 主内容区 -->
      <el-main style="background: #f5f7fa; padding: 20px">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
/**
 * 主布局组件
 * 左侧侧边栏 + 右侧(顶部栏 + 内容区) 的结构
 */
import Sidebar from './Sidebar.vue'
import HeaderBar from './HeaderBar.vue'
</script>
