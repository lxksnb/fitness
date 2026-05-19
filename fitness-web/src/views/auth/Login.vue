<template>
  <!-- 登录页: 全屏居中布局 -->
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <h2 style="text-align: center; margin: 0">用户登录</h2>
      </template>
      <!-- 登录表单 -->
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
        size="large"
        @submit.prevent="handleLogin"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" style="width: 100%" @click="handleLogin">
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      <!-- 注册链接 -->
      <div style="text-align: center">
        还没有账号？
        <el-link type="primary" @click="$router.push('/register')">立即注册</el-link>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
/**
 * 登录页面组件
 * 提供用户名密码登录，成功后存储token并跳转到首页
 */
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { login } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

/** 表单数据 */
const form = reactive({
  username: '',
  password: ''
})

/** 表单校验规则 */
const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3到20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 30, message: '密码长度在6到30个字符', trigger: 'blur' }
  ]
}

/** 处理登录: 校验表单 -> 调用API -> 存储token -> 跳转首页 */
async function handleLogin() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    const res: any = await login({
      username: form.username,
      password: form.password
    })
    // 存储token到localStorage
    localStorage.setItem('accessToken', res.accessToken)
    localStorage.setItem('refreshToken', res.refreshToken)
    // 存储用户信息到Pinia store
    userStore.setUser({
      nickname: res.nickname,
      role: res.role,
      userId: res.userId
    })
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (err: any) {
    // 错误信息由axios拦截器统一处理，此处仅补充
    if (err.response?.data?.message) {
      ElMessage.error(err.response.data.message)
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/**
 * 登录页样式: 全屏居中灰色背景
 */
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f7fa;
}
.login-card {
  width: 420px;
}
</style>
