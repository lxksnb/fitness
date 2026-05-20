<template>
  <!-- 个人设置页面：账号信息 + 身体档案 -->
  <div class="profile-page">
    <div class="page-header">
      <h2>个人设置</h2>
    </div>

    <!-- ==================== 加载状态 ==================== -->
    <template v-if="loading">
      <el-card class="section">
        <el-skeleton :rows="6" animated />
      </el-card>
    </template>

    <!-- ==================== 错误状态 ==================== -->
    <template v-else-if="error">
      <el-result icon="error" title="数据加载失败" :sub-title="error">
        <template #extra>
          <el-button type="primary" @click="fetchProfile">重新加载</el-button>
        </template>
      </el-result>
    </template>

    <!-- ==================== 主内容 ==================== -->
    <template v-else>
      <el-form ref="profileFormRef" :model="profileForm" :rules="profileRules" label-width="110px">
        <!-- ==================== 账号信息 ==================== -->
        <el-card class="section">
          <template #header>
            <span class="section-title">账号信息</span>
          </template>

          <!-- 头像上传 -->
          <el-form-item label="头像" prop="avatar">
            <div class="avatar-section">
              <el-avatar :size="80" :src="profileForm.avatar" />
              <div class="avatar-upload">
                <el-upload
                  :action="uploadUrl"
                  :headers="uploadHeaders"
                  :show-file-list="false"
                  :before-upload="beforeAvatarUpload"
                  :on-success="handleAvatarSuccess"
                  :on-error="handleAvatarError"
                  accept="image/jpeg,image/png,image/gif,image/webp"
                  name="file"
                >
                  <el-button size="small" plain>更换头像</el-button>
                </el-upload>
                <div class="upload-tip">支持 JPG/PNG/GIF，不超过2MB</div>
              </div>
            </div>
          </el-form-item>

          <el-form-item label="昵称" prop="nickname">
            <el-input
              v-model="profileForm.nickname"
              placeholder="请输入昵称"
              maxlength="20"
              show-word-limit
              style="max-width: 360px"
            />
          </el-form-item>

          <el-form-item label="邮箱" prop="email">
            <el-input
              v-model="profileForm.email"
              placeholder="请输入邮箱"
              style="max-width: 360px"
            />
          </el-form-item>

          <el-form-item label="用户名">
            <el-input
              :model-value="profileForm.username"
              disabled
              style="max-width: 360px"
            />
          </el-form-item>

          <el-form-item label="角色">
            <el-tag :type="profileForm.role === 'ADMIN' ? 'danger' : 'primary'">
              {{ profileForm.role === 'ADMIN' ? '管理员' : '普通用户' }}
            </el-tag>
          </el-form-item>
        </el-card>

        <!-- ==================== 身体档案 ==================== -->
        <el-card class="section">
          <template #header>
            <span class="section-title">身体档案</span>
          </template>

          <el-form-item label="性别" prop="gender">
            <el-radio-group v-model="profileForm.gender">
              <el-radio label="MALE">男</el-radio>
              <el-radio label="FEMALE">女</el-radio>
              <el-radio label="OTHER">其他</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="出生日期" prop="birthday">
            <el-date-picker
              v-model="profileForm.birthday"
              type="date"
              placeholder="选择出生日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              style="max-width: 240px"
            />
          </el-form-item>

          <el-form-item label="身高(cm)" prop="heightCm">
            <el-input-number
              v-model="profileForm.heightCm"
              :min="50"
              :max="250"
              :precision="1"
              controls-position="right"
              style="width: 180px"
              placeholder="身高"
            />
          </el-form-item>

          <el-form-item label="目标类型" prop="targetType">
            <el-select v-model="profileForm.targetType" placeholder="选择目标类型" style="max-width: 200px">
              <el-option label="减脂" value="cut" />
              <el-option label="增肌" value="bulk" />
              <el-option label="维持" value="maintain" />
            </el-select>
          </el-form-item>

          <el-form-item label="目标体重(kg)" prop="targetWeightKg">
            <el-input-number
              v-model="profileForm.targetWeightKg"
              :min="20"
              :max="300"
              :precision="1"
              controls-position="right"
              style="width: 180px"
              placeholder="目标体重"
            />
          </el-form-item>

          <el-form-item label="目标日期" prop="targetDate">
            <el-date-picker
              v-model="profileForm.targetDate"
              type="date"
              placeholder="选择目标日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              style="max-width: 240px"
            />
          </el-form-item>

          <el-form-item label="每周变化率(kg)" prop="weeklyChangeRate">
            <el-input-number
              v-model="profileForm.weeklyChangeRate"
              :min="0.1"
              :max="5"
              :precision="1"
              :step="0.1"
              controls-position="right"
              style="width: 180px"
              placeholder="每周变化率"
            />
          </el-form-item>
        </el-card>

        <!-- ==================== 保存按钮 ==================== -->
        <div class="save-section">
          <el-button type="primary" :loading="saving" size="large" @click="handleSave">
            保存设置
          </el-button>
        </div>
      </el-form>
    </template>
  </div>
</template>

<script setup lang="ts">
/**
 * 个人设置页面
 * 功能：账号信息（头像上传、昵称、邮箱、只读用户名/角色）+ 身体档案
 * （性别、生日、身高、目标类型、目标体重、目标日期、每周变化率）
 * 保存时调用 /api/v1/user/profile PUT 接口
 */
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getProfile, updateProfile } from '@/api/user'
import { useUserStore } from '@/stores/user'

// ==================== 类型定义 ====================

/** 用户个人信息 */
interface UserProfile {
  avatar: string
  nickname: string
  email: string
  username: string
  role: string
  gender: string
  birthday: string | null
  heightCm: number | null
  targetType: string
  targetWeightKg: number | null
  targetDate: string | null
  weeklyChangeRate: number | null
}

// ==================== 状态 ====================

const loading = ref(true)
const error = ref('')
const saving = ref(false)
const profileFormRef = ref<FormInstance>()

/** 上传接口配置 */
const uploadUrl = '/api/v1/upload'
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('accessToken') || ''}`
}))

// ==================== 表单 ====================

const profileForm = reactive<UserProfile>({
  avatar: '',
  nickname: '',
  email: '',
  username: '',
  role: '',
  gender: 'MALE',
  birthday: null,
  heightCm: null,
  targetType: 'maintain',
  targetWeightKg: null,
  targetDate: null,
  weeklyChangeRate: null
})

const profileRules: FormRules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 1, max: 20, message: '昵称长度 1-20 字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

// ==================== 数据获取 ====================

async function fetchProfile() {
  loading.value = true
  error.value = ''
  try {
    const res = await getProfile() as any
    const data = res ?? {}
    profileForm.avatar = data.avatar || ''
    profileForm.nickname = data.nickname || ''
    profileForm.email = data.email || ''
    profileForm.username = data.username || ''
    profileForm.role = data.role || 'USER'
    profileForm.gender = data.gender || 'MALE'
    profileForm.birthday = data.birthday || null
    profileForm.heightCm = data.heightCm ?? null
    profileForm.targetType = data.targetType || 'maintain'
    profileForm.targetWeightKg = data.targetWeightKg ?? null
    profileForm.targetDate = data.targetDate || null
    profileForm.weeklyChangeRate = data.weeklyChangeRate ?? data.weeklyChange ?? null
  } catch (err: any) {
    error.value = err.message || '数据加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

// ==================== 头像上传 ====================

function beforeAvatarUpload(file: any) {
  const validTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!validTypes.includes(file.type)) {
    ElMessage.error('图片格式仅支持 JPG/PNG/GIF/WebP')
    return false
  }
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('头像大小不能超过 2MB')
    return false
  }
  return true
}

function handleAvatarSuccess(response: any) {
  const imageUrl = response?.url || response?.data?.url || ''
  if (imageUrl) {
    profileForm.avatar = imageUrl
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error('上传成功但未获取到图片地址')
  }
}

function handleAvatarError() {
  ElMessage.error('头像上传失败，请重试')
}

// ==================== 保存 ====================

async function handleSave() {
  const valid = await profileFormRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const payload: any = {
      avatar: profileForm.avatar,
      nickname: profileForm.nickname,
      email: profileForm.email,
      gender: profileForm.gender,
      birthday: profileForm.birthday,
      heightCm: profileForm.heightCm,
      targetType: profileForm.targetType,
      targetWeightKg: profileForm.targetWeightKg,
      targetDate: profileForm.targetDate,
      weeklyChangeRate: profileForm.weeklyChangeRate
    }
    await updateProfile(payload)
    ElMessage.success('个人信息已保存')

    // 同步用户状态到 userStore，使顶部导航栏立即更新
    const userStore = useUserStore()
    userStore.setUser({
      nickname: profileForm.nickname,
      role: profileForm.role,
      userId: userStore.userId
    })
  } catch (err: any) {
    ElMessage.error(err.message || '保存失败，请重试')
  } finally {
    saving.value = false
  }
}

// ==================== 生命周期 ====================

onMounted(() => {
  fetchProfile()
})
</script>

<style scoped lang="scss">
/**
 * 个人设置页面样式
 */

.profile-page {
  padding: 4px;
  max-width: 800px;
}

/* ==================== 页面标题栏 ==================== */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  h2 {
    margin: 0;
    font-size: 22px;
    font-weight: 600;
    color: #303133;
  }
}

.section {
  margin-bottom: 20px;
}

/* ==================== 分区标题 ==================== */
.section-title {
  font-weight: 600;
  font-size: 15px;
  color: #303133;
}

/* ==================== 头像上传区域 ==================== */
.avatar-section {
  display: flex;
  align-items: center;
  gap: 16px;

  .avatar-upload {
    .upload-tip {
      font-size: 12px;
      color: #909399;
      margin-top: 4px;
    }
  }
}

/* ==================== 保存按钮区域 ==================== */
.save-section {
  text-align: center;
  padding: 20px 0 40px;
}
</style>
