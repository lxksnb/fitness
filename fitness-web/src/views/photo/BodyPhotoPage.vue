<template>
  <!-- 身体照片页面：照片网格展示、上传弹窗、图片预览、删除 -->
  <div class="photo-page">
    <!-- ==================== 页面标题与操作栏 ==================== -->
    <div class="page-header">
      <h2>身体照片</h2>
      <div class="header-actions">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          :shortcuts="dateShortcuts"
          @change="fetchPhotos"
          style="width: 280px"
        />
        <el-button type="primary" @click="openUploadDialog()">
          <el-icon><Plus /></el-icon>
          上传照片
        </el-button>
      </div>
    </div>

    <!-- ==================== 加载状态 ==================== -->
    <template v-if="loading">
      <el-card class="section">
        <el-skeleton :rows="3" animated />
      </el-card>
    </template>

    <!-- ==================== 错误状态 ==================== -->
    <template v-else-if="error">
      <el-result icon="error" title="数据加载失败" :sub-title="error">
        <template #extra>
          <el-button type="primary" @click="fetchPhotos">重新加载</el-button>
        </template>
      </el-result>
    </template>

    <!-- ==================== 照片网格 ==================== -->
    <template v-else>
      <el-card v-if="photoList.length > 0" class="section">
        <template #header>
          <span>照片列表（{{ photoList.length }} 张）</span>
        </template>
        <div class="photo-grid">
          <el-card
            v-for="photo in photoList"
            :key="photo.id"
            class="photo-card"
            shadow="hover"
            :body-style="{ padding: '12px' }"
          >
            <!-- 照片缩略图 -->
            <div class="photo-thumb" @click="previewPhoto(photo.imageUrl || '')">
              <el-image
                :src="photo.imageUrl"
                fit="cover"
                style="width: 100%; height: 180px"
                :preview-src-list="previewSrcList"
                :initial-index="photoList.indexOf(photo)"
                preview-teleported
                hide-on-click-modal
              />
            </div>
            <!-- 照片信息 -->
            <div class="photo-info">
              <div class="photo-meta">
                <span class="photo-date">{{ formatDate(photo.photoDate || photo.recordDate || '') }}</span>
                <el-tag :type="getTypeTagColor(photo.photoType)" size="small">
                  {{ photo.photoTypeLabel || photo.photoType || '未分类' }}
                </el-tag>
              </div>
              <p v-if="photo.note" class="photo-note">{{ photo.note }}</p>
              <div class="photo-actions">
                <el-popconfirm title="确认删除该照片？" @confirm="handleDelete(photo.id!)">
                  <template #reference>
                    <el-button type="danger" link size="small">
                      <el-icon><Delete /></el-icon>
                      删除
                    </el-button>
                  </template>
                </el-popconfirm>
              </div>
            </div>
          </el-card>
        </div>
      </el-card>
      <el-empty v-else description="暂无身体照片，点击上方按钮上传" :image-size="120" />
    </template>

    <!-- ==================== 上传照片弹窗 ==================== -->
    <el-dialog
      v-model="dialogVisible"
      title="上传身体照片"
      width="500px"
      :close-on-click-modal="false"
      @closed="resetUploadForm"
    >
      <el-form ref="uploadFormRef" :model="uploadForm" :rules="uploadRules" label-width="100px">
        <el-form-item label="拍摄日期" prop="photoDate">
          <el-date-picker
            v-model="uploadForm.photoDate"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="照片类型" prop="photoType">
          <el-select v-model="uploadForm.photoType" placeholder="选择照片类型" style="width: 100%">
            <el-option label="正面" value="front" />
            <el-option label="侧面" value="side" />
            <el-option label="背面" value="back" />
          </el-select>
        </el-form-item>
        <el-form-item label="照片上传" prop="imageUrl">
          <div class="upload-area">
            <el-upload
              :action="uploadUrl"
              :headers="uploadHeaders"
              list-type="picture-card"
              :limit="1"
              :before-upload="beforeUpload"
              :on-success="handleUploadSuccess"
              :on-error="handleUploadError"
              :on-remove="handleUploadRemove"
              :file-list="uploadFileList"
              accept="image/jpeg,image/png,image/gif,image/webp"
              name="file"
            >
              <el-icon><Plus /></el-icon>
            </el-upload>
            <div class="upload-tip">支持 JPG/PNG/GIF/WebP 格式，单张不超过5MB</div>
          </div>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="uploadForm.note"
            type="textarea"
            :rows="3"
            placeholder="记录当时的体重、感受等"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleUploadSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
/**
 * 身体照片页面
 * 功能：照片网格展示（日期、类型标签、缩略图、备注）、
 * 上传弹窗（支持图片上传）、点击预览大图（el-image-viewer）、删除确认
 */
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import type { FormInstance, FormRules, UploadFile } from 'element-plus'
import { getPhotos, savePhoto, deletePhoto } from '@/api/bodyPhoto'

// ==================== 类型定义 ====================

/** 身体照片记录 */
interface PhotoRecord {
  id?: number
  photoDate: string
  recordDate?: string
  photoType: string
  photoTypeLabel?: string
  imageUrl: string
  note: string
}

// ==================== 状态 ====================

const loading = ref(true)
const error = ref('')
const saving = ref(false)
const dialogVisible = ref(false)
const uploadFormRef = ref<FormInstance>()
const uploadFileList = ref<UploadFile[]>([])

const photoList = ref<PhotoRecord[]>([])

/** 日期范围 */
const dateRange = ref<[string, string]>([getDefaultStartDate(), getTodayStr()])

/** 日期快捷选项 */
const dateShortcuts = [
  { text: '最近一月', value: () => {
    const end = new Date()
    const start = new Date()
    start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
    return [start, end]
  }},
  { text: '最近三月', value: () => {
    const end = new Date()
    const start = new Date()
    start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
    return [start, end]
  }},
  { text: '最近半年', value: () => {
    const end = new Date()
    const start = new Date()
    start.setTime(start.getTime() - 3600 * 1000 * 24 * 180)
    return [start, end]
  }}
]

/** 上传接口配置 */
const uploadUrl = '/api/v1/upload'
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('accessToken') || ''}`
}))

/** 图片预览列表 */
const previewSrcList = computed(() => {
  return photoList.value.map(p => p.imageUrl).filter(Boolean)
})

// ==================== 表单 ====================

const uploadForm = reactive({
  photoDate: getTodayStr(),
  photoType: 'front',
  imageUrl: '',
  note: ''
})

const uploadRules: FormRules = {
  photoDate: [{ required: true, message: '请选择拍摄日期', trigger: 'blur' }],
  photoType: [{ required: true, message: '请选择照片类型', trigger: 'change' }],
  imageUrl: [{ required: true, message: '请上传照片', trigger: 'change' }]
}

// ==================== 工具函数 ====================

function getTodayStr(): string {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function getDefaultStartDate(): string {
  const d = new Date()
  d.setTime(d.getTime() - 3600 * 1000 * 24 * 90)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function formatDate(dateStr: string) {
  if (!dateStr) return '--'
  const d = new Date(dateStr)
  if (isNaN(d.getTime())) return dateStr
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function getTypeTagColor(type: string) {
  const map: Record<string, 'success' | 'warning' | 'info' | ''> = {
    front: 'success',
    正面: 'success',
    side: 'warning',
    侧面: 'warning',
    back: 'info',
    背面: 'info'
  }
  return map[type] || ''
}

// ==================== 数据获取 ====================

async function fetchPhotos() {
  loading.value = true
  error.value = ''
  try {
    const [startDate, endDate] = dateRange.value
    const res = await getPhotos(startDate, endDate) as any
    const list = (Array.isArray(res) ? res : res?.records || res?.list || []) as PhotoRecord[]
    photoList.value = list
  } catch (err: any) {
    error.value = err.message || '数据加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

// ==================== 弹窗操作 ====================

function openUploadDialog() {
  uploadForm.photoDate = getTodayStr()
  uploadForm.photoType = 'front'
  uploadForm.imageUrl = ''
  uploadForm.note = ''
  uploadFileList.value = []
  dialogVisible.value = true
}

function resetUploadForm() {
  uploadFormRef.value?.resetFields()
  uploadFileList.value = []
}

// ==================== 上传处理 ====================

function beforeUpload(file: any) {
  const validTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!validTypes.includes(file.type)) {
    ElMessage.error('图片格式仅支持 JPG/PNG/GIF/WebP')
    return false
  }
  const maxSize = 5 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

function handleUploadSuccess(response: any) {
  const imageUrl = response?.url || response?.data?.url || ''
  if (imageUrl) {
    uploadForm.imageUrl = imageUrl
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error('上传成功但未获取到图片地址')
  }
}

function handleUploadError() {
  ElMessage.error('图片上传失败，请重试')
}

function handleUploadRemove() {
  uploadForm.imageUrl = ''
}

/** 预览照片（由 el-image 内置的 preview-src-list 处理，这里不需要额外操作） */
function previewPhoto(_url: string) {
  // el-image 的 preview-src-list + preview-teleported 已处理预览
}

// ==================== 保存与删除 ====================

async function handleUploadSave() {
  if (!uploadForm.imageUrl) {
    ElMessage.warning('请先上传照片')
    return
  }
  const valid = await uploadFormRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    await savePhoto({
      photoDate: uploadForm.photoDate,
      photoType: uploadForm.photoType,
      imageUrl: uploadForm.imageUrl,
      note: uploadForm.note || undefined
    })
    ElMessage.success('照片已保存')
    dialogVisible.value = false
    await fetchPhotos()
  } catch (err: any) {
    ElMessage.error(err.message || '保存失败，请重试')
  } finally {
    saving.value = false
  }
}

async function handleDelete(id: number) {
  try {
    await deletePhoto(id)
    ElMessage.success('照片已删除')
    await fetchPhotos()
  } catch (err: any) {
    ElMessage.error(err.message || '删除失败，请重试')
  }
}

// ==================== 生命周期 ====================

onMounted(() => {
  fetchPhotos()
})
</script>

<style scoped lang="scss">
/**
 * 身体照片页面样式
 */

.photo-page {
  padding: 4px;
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

  .header-actions {
    display: flex;
    align-items: center;
    gap: 12px;
  }
}

.section {
  margin-bottom: 16px;
}

/* ==================== 照片网格 ==================== */
.photo-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;

  .photo-card {
    transition: transform 0.2s ease;

    &:hover {
      transform: translateY(-2px);
    }

    .photo-thumb {
      cursor: pointer;
      border-radius: 6px;
      overflow: hidden;
      margin-bottom: 10px;
    }

    .photo-info {
      .photo-meta {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 6px;

        .photo-date {
          font-size: 13px;
          color: #606266;
          font-weight: 500;
        }
      }

      .photo-note {
        font-size: 12px;
        color: #909399;
        margin: 6px 0;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .photo-actions {
        text-align: right;
      }
    }
  }
}

/* ==================== 上传区域 ==================== */
.upload-area {
  .upload-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 6px;
  }
}
</style>
