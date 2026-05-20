<template>
  <!-- 管理后台 - 系统动作库管理 -->
  <div class="admin-action-page">
    <!-- ==================== 页面标题与搜索栏 ==================== -->
    <div class="page-header">
      <h2>动作库管理</h2>
      <div class="header-actions">
        <el-input
          v-model="keyword"
          placeholder="搜索动作名称..."
          clearable
          @keyup.enter="fetchList"
          @clear="fetchList"
          style="width: 260px"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="openDialog()">
          <el-icon><Plus /></el-icon>
          添加系统动作
        </el-button>
      </div>
    </div>

    <!-- ==================== 数据表格 ==================== -->
    <el-table
      v-loading="loading"
      :data="tableData"
      border
      stripe
      style="width: 100%"
      empty-text="暂无系统动作数据"
    >
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column label="图片" width="90" align="center">
        <template #default="{ row }">
          <el-image
            v-if="row.imageUrls && row.imageUrls.length > 0"
            :src="row.imageUrls[0]"
            fit="cover"
            style="width: 50px; height: 50px; border-radius: 4px"
            :preview-src-list="row.imageUrls"
          />
          <span v-else class="no-image">--</span>
        </template>
      </el-table-column>
      <el-table-column prop="actionName" label="动作名称" min-width="160" />
      <el-table-column label="适用部位" min-width="200">
        <template #default="{ row }">
          <template v-if="row.suitableFor && row.suitableFor.length > 0">
            <el-tag
              v-for="tag in row.suitableFor"
              :key="tag"
              size="small"
              effect="plain"
              type="primary"
              style="margin-right: 4px"
            >
              {{ getSuitableLabel(tag) }}
            </el-tag>
          </template>
          <span v-else style="color: #c0c4cc">--</span>
        </template>
      </el-table-column>
      <el-table-column label="视频链接" width="100" align="center">
        <template #default="{ row }">
          <el-link v-if="row.videoUrl" :href="row.videoUrl" target="_blank" type="primary" :underline="false">
            查看
          </el-link>
          <span v-else style="color: #c0c4cc">--</span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="180" align="center">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="openDialog(row)">
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row)">
            <el-icon><Delete /></el-icon>
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- ==================== 分页 ==================== -->
    <div class="pagination-wrapper" v-if="total > pageSize">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchList"
      />
    </div>

    <!-- ==================== 错误状态 ==================== -->
    <template v-if="!loading && error">
      <el-result icon="error" title="数据加载失败" :sub-title="error">
        <template #extra>
          <el-button type="primary" @click="fetchList">重新加载</el-button>
        </template>
      </el-result>
    </template>

    <!-- ==================== 添加/编辑弹窗 ==================== -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditing ? '编辑系统动作' : '添加系统动作'"
      width="620px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="动作名称" prop="name">
          <el-input v-model="form.name" placeholder="如：杠铃卧推" maxlength="50" show-word-limit />
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
  </div>
</template>

<script setup lang="ts">
/**
 * 管理后台 - 系统动作库管理页面
 * 管理员管理系统级别的动作库，支持搜索、新增、编辑、删除
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Edit, Delete } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { getAdminActions, createAdminAction, updateAdminAction, deleteAdminAction } from '@/api/admin'
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
  createdAt?: string
  scope?: string
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
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

/** 表格数据 */
const tableData = ref<ActionItem[]>([])

/** 适用部位字典 */
const suitableOptions = ref<SuitableOption[]>([])

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

/** 格式化日期 */
function formatDate(dateStr?: string): string {
  if (!dateStr) return '--'
  const d = new Date(dateStr)
  if (isNaN(d.getTime())) return dateStr
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
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

/** 获取动作列表 */
async function fetchList() {
  loading.value = true
  error.value = ''
  try {
    const kw = keyword.value.trim() || undefined
    const res = await getAdminActions({ keyword: kw }) as any
    if (res && (res.records || res.list)) {
      const list = res.records || res.list || []
      tableData.value = list
      total.value = res.total || list.length
    } else if (Array.isArray(res)) {
      tableData.value = res
      total.value = res.length
    } else {
      tableData.value = []
      total.value = 0
    }
  } catch (err: any) {
    error.value = err.message || '数据加载失败，请稍后重试'
    tableData.value = []
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
    form.suitableFor = action.suitableFor || []
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
      await updateAdminAction(editingId.value, payload)
      ElMessage.success('系统动作已更新')
    } else {
      await createAdminAction(payload)
      ElMessage.success('系统动作已添加')
    }

    dialogVisible.value = false
    await fetchList()
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
    await deleteAdminAction(action.id)
    ElMessage.success('已删除')
    await fetchList()
  } catch (err: any) {
    if (err !== 'cancel' && err !== 'close') {
      ElMessage.error(err.message || '删除失败')
    }
  }
}

// ==================== 生命周期 ====================

onMounted(() => {
  fetchSuitableOptions()
  fetchList()
})
</script>

<style scoped lang="scss">
/**
 * 管理后台 - 系统动作库管理页面样式
 */

.admin-action-page {
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

/* ==================== 分页 ==================== */
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

/* ==================== 无图片占位 ==================== */
.no-image {
  color: #c0c4cc;
  font-size: 12px;
}
</style>
