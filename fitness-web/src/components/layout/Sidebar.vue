<template>
  <!-- 侧边栏标题 -->
  <div style="padding: 16px; color: #fff; font-size: 18px; font-weight: bold; text-align: center; border-bottom: 1px solid rgba(255,255,255,0.1)">
    🏋️ 健身记录
  </div>
  <!-- 导航菜单 -->
  <el-menu
    :default-active="route.path"
    background-color="#304156"
    text-color="#bfcbd9"
    active-text-color="#409EFF"
    router
    style="border-right: none"
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
      <el-icon><Drink /></el-icon>
      <span>饮水记录</span>
    </el-menu-item>

    <!-- 管理员菜单: 仅当用户角色为ADMIN时显示 -->
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
      </el-sub-menu>
    </template>
  </el-menu>
</template>

<script setup lang="ts">
/**
 * 侧边栏组件
 * 根据当前路由高亮对应菜单项，管理员角色显示管理菜单
 */
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const userStore = useUserStore()
</script>
