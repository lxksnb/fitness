<template>
  <!-- 图片上传组件：支持单图/多图上传，上传至 /api/v1/upload -->
  <div class="image-upload">
    <el-upload
      :action="uploadUrl"
      :headers="uploadHeaders"
      :list-type="listType"
      :limit="limit"
      :multiple="multiple"
      :accept="accept"
      :file-list="fileList"
      :before-upload="beforeUpload"
      :on-success="handleSuccess"
      :on-error="handleError"
      :on-remove="handleRemove"
      :disabled="disabled"
      name="file"
      v-bind="$attrs"
    >
      <!-- 图片墙模式 -->
      <template v-if="listType === 'picture-card'">
        <el-icon><Plus /></el-icon>
      </template>
      <!-- 普通按钮模式 -->
      <template v-else>
        <el-button type="primary" :disabled="disabled">
          <el-icon><Upload /></el-icon>
          <span>{{ buttonText }}</span>
        </el-button>
      </template>
      <!-- 上传提示 -->
      <template #tip>
        <div class="el-upload__tip">
          {{ tip }}
        </div>
      </template>
    </el-upload>
  </div>
</template>

<script setup lang="ts">
/**
 * 图片上传组件
 * 基于el-upload封装，自动附加JWT token，处理上传成功/失败回调
 * 支持图片墙(picture-card)和普通按钮两种模式
 */
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Upload } from '@element-plus/icons-vue'
import type { UploadFile, UploadRawFile } from 'element-plus'

// ==================== Props ====================

const props = withDefaults(defineProps<{
  /** v-model绑定，已上传图片的URL数组 */
  modelValue?: string[]
  /** 展示模式：picture-card 图片墙 / text 文字按钮 */
  listType?: 'picture-card' | 'text'
  /** 最大上传数量 */
  limit?: number
  /** 是否允许多选 */
  multiple?: boolean
  /** 接受的文件类型 */
  accept?: string
  /** 按钮文字 */
  buttonText?: string
  /** 上传提示 */
  tip?: string
  /** 是否禁用 */
  disabled?: boolean
  /** 最大文件大小(MB) */
  maxSize?: number
}>(), {
  modelValue: () => [],
  listType: 'picture-card',
  limit: 1,
  multiple: false,
  accept: 'image/jpeg,image/png,image/gif,image/webp',
  buttonText: '上传图片',
  tip: '支持 JPG/PNG/GIF/WebP 格式，单张不超过5MB',
  disabled: false,
  maxSize: 5
})

// ==================== Emits ====================

const emit = defineEmits<{
  /** 更新已上传图片URL数组 */
  'update:modelValue': [value: string[]]
}>()

// ==================== 数据 ====================

/** 上传接口地址 */
const uploadUrl = '/api/v1/upload'

/** 上传请求头，自动附加JWT token */
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('accessToken') || ''}`
}))

/** 已上传文件列表，用于el-upload展示 */
const fileList = ref<UploadFile[]>([])

/**
 * 将 modelValue URL 数组同步到 el-upload 的 fileList
 */
function syncFileList(urls: string[]) {
  if (urls && urls.length > 0) {
    fileList.value = urls.map((url, index) => ({
      name: `image-${index}`,
      url: url,
      status: 'success' as const,
      uid: Date.now() + index
    })) as any
  } else {
    fileList.value = []
  }
}

// 监听 modelValue 变化，同步 fileList，避免切换编辑对象时残留旧图片
watch(() => props.modelValue, syncFileList, { immediate: true })

// ==================== 上传处理 ====================

/**
 * 上传前校验：检查文件类型和大小
 * @param rawFile 原始文件
 * @returns 是否允许上传
 */
function beforeUpload(rawFile: UploadRawFile) {
  const validTypes = props.accept.split(',').map(t => t.trim())
  const fileType = rawFile.type

  // 检查文件类型
  if (!validTypes.includes(fileType) && validTypes.length > 0) {
    ElMessage.error(`图片格式仅支持 ${props.accept}`)
    return false
  }

  // 检查文件大小
  const maxBytes = props.maxSize * 1024 * 1024
  if (rawFile.size > maxBytes) {
    ElMessage.error(`图片大小不能超过 ${props.maxSize}MB`)
    return false
  }

  return true
}

/**
 * 上传成功回调
 * @param response 后端返回数据
 * @param _uploadFile 上传文件
 * @param _uploadFiles 全部文件列表
 */
function handleSuccess(response: any, _uploadFile: UploadFile, _uploadFiles: UploadFile[]) {
  // 后端返回的图片URL
  const imageUrl = response?.url || response?.data?.url || (typeof response?.data === 'string' ? response.data : '') || ''
  if (!imageUrl) {
    ElMessage.error('上传成功但未获取到图片地址')
    return
  }

  // 更新v-model
  const urls = [...props.modelValue]
  urls.push(imageUrl)
  emit('update:modelValue', urls)

  // 同步 fileList 以即时显示上传结果
  syncFileList(urls)

  ElMessage.success('图片上传成功')
}

/**
 * 上传失败回调
 * @param error 错误信息
 */
function handleError(error: any) {
  console.error('图片上传失败:', error)
  ElMessage.error('图片上传失败，请重试')
}

/**
 * 删除图片回调
 * @param uploadFile 被删除的文件
 */
function handleRemove(uploadFile: UploadFile, _uploadFiles: UploadFile[]) {
  const removedUrl = uploadFile.url || ''
  if (!removedUrl) return

  // 从v-model中移除对应URL
  const urls = props.modelValue.filter(url => url !== removedUrl)
  emit('update:modelValue', urls)
}

// ==================== 暴露方法 ====================

/** 重置文件列表 */
function reset() {
  fileList.value = []
}

/** 重新初始化文件列表 */
function refresh() {
  syncFileList(props.modelValue || [])
}

defineExpose({ reset, refresh })
</script>

<style scoped lang="scss">
/**
 * 图片上传组件样式
 */

.image-upload {
  .el-upload__tip {
    font-size: 12px;
    color: #909399;
    margin-top: 8px;
  }
}
</style>
