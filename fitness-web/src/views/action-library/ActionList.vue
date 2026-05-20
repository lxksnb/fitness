<template>
  <!-- 动作库页面：搜索筛选、卡片网格展示、新增/编辑/删除动作、查看重量递增历史 -->
  <div class="action-list-page">
    <!-- ==================== 页面标题与搜索筛选栏 ==================== -->
    <div class="page-header">
      <h2>动作库</h2>
      <div class="header-actions">
        <el-input
          v-model="keyword"
          placeholder="搜索动作名称..."
          clearable
          @keyup.enter="fetchActions"
          @clear="fetchActions"
          style="width: 220px"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select
          v-model="suitableFor"
          placeholder="适用部位筛选"
          clearable
          @change="fetchActions"
          style="width: 160px"
        >
          <el-option
            v-for="item in suitableOptions"
            :key="item.code"
            :label="item.name"
            :value="item.code"
          />
        </el-select>
        <el-button type="primary" @click="openDialog()">
          <el-icon><Plus /></el-icon>
          添加动作
        </el-button>
      </div>
    </div>

    <!-- ==================== 加载骨架 ==================== -->
    <template v-if="loading">
      <el-row :gutter="16">
        <el-col v-for="i in 6" :key="i" :xs="24" :sm="12" :md="8" :lg="6" style="margin-bottom: 16px">
          <el-card>
            <el-skeleton :rows="4" animated />
          </el-card>
        </el-col>
      </el-row>
    </template>

    <!-- ==================== 错误状态 ==================== -->
    <template v-else-if="error">
      <el-result icon="error" title="数据加载失败" :sub-title="error">
        <template #extra>
          <el-button type="primary" @click="fetchActions">重新加载</el-button>
        </template>
      </el-result>
    </template>

    <!-- ==================== 动作卡片网格 ==================== -->
    <template v-else>
      <template v-if="actionList.length > 0">
        <el-row :gutter="16">
          <el-col v-for="action in actionList" :key="action.id" :xs="24" :sm="12" :md="8" :lg="6" style="margin-bottom: 16px">
            <el-card shadow="hover" class="action-card">
              <!-- 动作图片 -->
              <div class="action-image" @click="openHistory(action)">
                <el-image
                  v-if="action.imageUrls && action.imageUrls.length > 0"
                  :src="action.imageUrls[0]"
                  fit="cover"
                  style="width: 100%; height: 160px; border-radius: 6px 6px 0 0"
                />
                <div v-else class="action-image-placeholder">
                  <el-icon :size="44"><VideoCamera /></el-icon>
                </div>
              </div>

              <!-- 动作信息 -->
              <div class="action-info">
                <div class="action-name-row">
                  <span class="action-name">{{ action.actionName }}</span>
                  <el-tag v-if="action.scope === 'SYSTEM'" size="small" type="info">系统</el-tag>
                </div>
                <div class="action-desc" v-if="action.description">
                  {{ action.description }}
                </div>

                <!-- 适用部位标签 -->
                <div class="action-tags" v-if="action.suitableFor && action.suitableFor.length > 0">
                  <el-tag
                    v-for="tag in action.suitableFor"
                    :key="tag"
                    size="small"
                    effect="plain"
                    type="primary"
                    class="suitable-tag"
                  >
                    {{ getSuitableLabel(tag) }}
                  </el-tag>
                </div>

                <!-- 视频链接 -->
                <div class="action-video" v-if="action.videoUrl">
                  <el-link :href="action.videoUrl" target="_blank" type="primary" :underline="false">
                    <el-icon><VideoPlay /></el-icon>
                    观看教学视频
                  </el-link>
                </div>
              </div>

              <!-- 操作按钮 -->
              <div class="action-card-actions" v-if="action.scope !== 'SYSTEM'">
                <el-button type="primary" link size="small" @click="openHistory(action)">
                  <el-icon><TrendCharts /></el-icon>
                  训练记录
                </el-button>
                <el-button type="primary" link size="small" @click="openDialog(action)">
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button type="danger" link size="small" @click="handleDelete(action)">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </div>
              <div v-else class="action-card-actions">
                <el-button type="primary" link size="small" @click="openHistory(action)">
                  <el-icon><TrendCharts /></el-icon>
                  训练记录
                </el-button>
                <span class="system-hint">系统数据</span>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </template>
      <el-empty v-else description="暂无动作数据，点击上方按钮添加" :image-size="120">
        <el-button type="primary" @click="openDialog()">添加动作</el-button>
      </el-empty>
    </template>

    <!-- ==================== 添加/编辑动作弹窗 ==================== -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditing ? '编辑动作' : '添加动作'"
      width="580px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="动作名称" prop="name">
          <el-input v-model="form.name" placeholder="如：杠铃卧推" />
        </el-form-item>

        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="描述动作要领..."
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="适用部位" prop="suitableFor">
          <el-checkbox-group v-model="form.suitableFor">
            <el-checkbox
              v-for="item in suitableOptions"
              :key="item.code"
              :label="item.code"
              :value="item.code"
            >
              {{ item.name }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <el-form-item label="动作图片">
          <ImageUpload
            v-model="form.imageUrls"
            :limit="1"
            :multiple="false"
          />
        </el-form-item>

        <el-form-item label="视频链接" prop="videoUrl">
          <el-input v-model="form.videoUrl" placeholder="如：https://www.youtube.com/watch?v=xxx" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- ==================== 训练记录历史弹窗 ==================== -->
    <el-dialog
      v-model="historyVisible"
      :title="`${historyActionName} - 训练记录`"
      width="700px"
      :close-on-click-modal="false"
    >
      <div v-if="historyLoading" style="padding: 20px">
        <el-skeleton :rows="5" animated />
      </div>
      <div v-else-if="historyError" style="text-align: center; padding: 20px">
        <el-result icon="error" title="加载失败" :sub-title="historyError" />
      </div>
      <div v-else-if="historyRecords.length > 0">
        <el-table :data="historyRecords" stripe border style="width: 100%">
          <el-table-column prop="recordDate" label="日期" width="130" sortable />
          <el-table-column prop="weightKg" label="重量 (kg)" width="120" sortable />
          <el-table-column prop="reps" label="次数" width="100" sortable />
          <el-table-column prop="sets" label="组数" width="100" />
          <el-table-column prop="note" label="备注" min-width="150">
            <template #default="{ row }">
              {{ row.note || '--' }}
            </template>
          </el-table-column>
        </el-table>
      </div>
      <el-empty v-else description="暂无训练记录" :image-size="100" />

      <template #footer>
        <el-button @click="historyVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
/**
 * 动作库页面
 * 支持搜索和部位筛选动作、卡片网格展示、
 * 新增/编辑/删除自定义动作（系统动作不可编辑），
 * 点击查看重量递增训练历史记录
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Edit, Delete, VideoCamera, VideoPlay, TrendCharts } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { searchActions, createAction, updateAction, deleteAction, getActionRecords } from '@/api/action'
import { getDict } from '@/api/dict'
import ImageUpload from '@/components/common/ImageUpload.vue'

// ==================== 类型定义 ====================

/** 动作列表项 */
interface ActionItem {
  id: number
  actionName: string
  description?: string
  imageUrls?: string[]
  videoUrl?: string
  suitableFor?: string[]
  scope?: string
}

/** 训练记录项 */
interface TrainingRecord {
  id?: number
  recordDate: string
  weightKg: number
  reps: number
  sets?: number
  note?: string
}

/** 部位字典选项 */
interface SuitableOption {
  code: string
  name: string
}

// ==================== 状态 ====================

const loading = ref(true)
const error = ref('')
const saving = ref(false)
const dialogVisible = ref(false)
const isEditing = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const keyword = ref('')
const suitableFor = ref('')

/** 动作列表 */
const actionList = ref<ActionItem[]>([])

/** 适用部位字典 */
const suitableOptions = ref<SuitableOption[]>([])

/** 训练历史弹窗状态 */
const historyVisible = ref(false)
const historyLoading = ref(false)
const historyError = ref('')
const historyActionName = ref('')
const historyRecords = ref<TrainingRecord[]>([])

// ==================== 表单 ====================

const form = reactive({
  name: '',
  description: '',
  suitableFor: [] as string[],
  imageUrls: [] as string[],
  videoUrl: ''
})

const formRules: FormRules = {
  name: [
    { required: true, message: '请输入动作名称', trigger: 'blur' },
    { max: 50, message: '动作名称不超过50个字符', trigger: 'blur' }
  ],
  description: [{ max: 500, message: '描述不超过500个字符', trigger: 'blur' }]
}

// ==================== 工具函数 ====================

/** 根据部位字典编码获取中文名称 */
function getSuitableLabel(code: string): string {
  const found = suitableOptions.value.find(item => item.code === code)
  return found?.name || code
}

// ==================== 数据获取 ====================

/** 获取适用部位字典 */
async function fetchSuitableOptions() {
  try {
    const res = await getDict('training_type') as any
    const list: any[] = Array.isArray(res) ? res : (res?.list || res?.records || [])
    // 字典API返回 { dictLabel, dictValue }，映射为 { code, name }
    suitableOptions.value = list.map((item: any) => ({
      code: item.dictValue,
      name: item.dictLabel
    }))
  } catch {
    // 默认部位选项兜底
    suitableOptions.value = [
      { code: 'CHEST', name: '练胸' },
      { code: 'BACK', name: '练背' },
      { code: 'LEGS', name: '练腿' },
      { code: 'SHOULDERS', name: '练肩' },
      { code: 'ARMS', name: '练手臂' },
      { code: 'CORE', name: '核心' },
      { code: 'CARDIO', name: '有氧' }
    ]
  }
}

/** 搜索动作列表 */
async function fetchActions() {
  loading.value = true
  error.value = ''
  try {
    const kw = keyword.value.trim() || undefined
    const sf = suitableFor.value || undefined
    const res = await searchActions(kw, sf) as any
    actionList.value = (Array.isArray(res) ? res : (res?.records || res?.list || [])) as ActionItem[]
  } catch (err: any) {
    error.value = err.message || '数据加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

// ==================== 弹窗操作 ====================

/** 打开新增/编辑弹窗 */
function openDialog(action?: ActionItem) {
  if (action) {
    isEditing.value = true
    editingId.value = action.id
    form.name = action.actionName
    form.description = action.description || ''
    // 后端返回逗号分隔字符串, 转为数组供checkbox展示
    const sf = (action as any).suitableFor
    form.suitableFor = Array.isArray(sf) ? sf : (typeof sf === 'string' ? sf.split(',').filter(Boolean) : [])
    form.imageUrls = action.imageUrls || []
    form.videoUrl = action.videoUrl || ''
  } else {
    isEditing.value = false
    editingId.value = null
    resetFormData()
  }
  dialogVisible.value = true
}

/** 重置表单数据 */
function resetFormData() {
  form.name = ''
  form.description = ''
  form.suitableFor = []
  form.imageUrls = []
  form.videoUrl = ''
}

/** 关闭弹窗后重置 */
function resetForm() {
  formRef.value?.resetFields()
  resetFormData()
}

/** 保存动作 */
async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const payload = {
      actionName: form.name,
      description: form.description || undefined,
      suitableFor: form.suitableFor.length > 0 ? form.suitableFor : undefined,
      imageUrls: form.imageUrls.length > 0 ? form.imageUrls : undefined,
      videoUrl: form.videoUrl || undefined
    }

    if (isEditing.value && editingId.value) {
      await updateAction(editingId.value, payload)
      ElMessage.success('动作已更新')
    } else {
      await createAction(payload)
      ElMessage.success('动作已添加')
    }

    dialogVisible.value = false
    await fetchActions()
  } catch (err: any) {
    ElMessage.error(err.message || '保存失败，请重试')
  } finally {
    saving.value = false
  }
}

/** 删除动作 */
async function handleDelete(action: ActionItem) {
  try {
    await ElMessageBox.confirm(
      `确定要删除"${action.actionName}"吗？此操作不可撤销。`,
      '删除确认',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    await deleteAction(action.id)
    ElMessage.success('已删除')
    await fetchActions()
  } catch (err: any) {
    if (err !== 'cancel' && err !== 'close') {
      ElMessage.error(err.message || '删除失败')
    }
  }
}

// ==================== 训练记录历史 ====================

/** 打开训练记录历史弹窗 */
async function openHistory(action: ActionItem) {
  historyActionName.value = action.actionName
  historyVisible.value = true
  historyLoading.value = true
  historyError.value = ''
  historyRecords.value = []

  try {
    const res = await getActionRecords(action.id) as any
    historyRecords.value = (Array.isArray(res) ? res : (res?.records || res?.list || [])) as TrainingRecord[]
  } catch (err: any) {
    historyError.value = err.message || '加载训练记录失败'
  } finally {
    historyLoading.value = false
  }
}

// ==================== 生命周期 ====================

onMounted(() => {
  fetchSuitableOptions()
  fetchActions()
})
</script>

<style scoped lang="scss">
/**
 * 动作库页面样式
 */

.action-list-page {
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

/* ==================== 动作卡片 ==================== */
.action-card {
  transition: box-shadow 0.2s;

  :deep(.el-card__body) {
    padding: 0;
  }
}

.action-image {
  cursor: pointer;

  .action-image-placeholder {
    width: 100%;
    height: 160px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #f5f7fa 0%, #e8eaed 100%);
    color: #c0c4cc;
  }
}

.action-info {
  padding: 12px 14px 8px;
}

.action-name-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;

  .action-name {
    font-size: 15px;
    font-weight: 600;
    color: #303133;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    flex: 1;
    margin-right: 8px;
  }
}

.action-desc {
  font-size: 13px;
  color: #909399;
  line-height: 1.5;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.action-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-bottom: 8px;

  .suitable-tag {
    font-size: 11px;
  }
}

.action-video {
  font-size: 13px;
  margin-bottom: 4px;
}

/* ==================== 卡片底部操作 ==================== */
.action-card-actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 4px;
  padding: 8px 14px 12px;
  border-top: 1px solid #f5f5f5;
}

.system-hint {
  font-size: 12px;
  color: #c0c4cc;
}
</style>
