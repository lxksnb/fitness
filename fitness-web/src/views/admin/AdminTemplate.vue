<template>
  <!-- 管理后台 - 计划模板管理 -->
  <div class="admin-template-page">
    <!-- ==================== 页面标题与搜索栏 ==================== -->
    <div class="page-header">
      <h2>模板管理</h2>
      <div class="header-actions">
        <el-input
          v-model="keyword"
          placeholder="搜索模板名称..."
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
          创建模板
        </el-button>
      </div>
    </div>

    <!-- ==================== 提示信息 ==================== -->
    <el-alert
      type="info"
      :closable="false"
      show-icon
      style="margin-bottom: 16px"
    >
      <template #title>
        完整模板编辑功能请使用计划编辑器创建后由管理员标记为模板
      </template>
    </el-alert>

    <!-- ==================== 数据表格 ==================== -->
    <el-table
      v-loading="loading"
      :data="tableData"
      border
      stripe
      style="width: 100%"
      empty-text="暂无模板数据"
    >
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="name" label="模板名称" min-width="160" />
      <el-table-column label="描述" min-width="200" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.description || '--' }}
        </template>
      </el-table-column>
      <el-table-column label="计划类型" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="planTypeTag(row.planType)" size="small">
            {{ getPlanTypeLabel(row.planType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="分化方式" width="110" align="center">
        <template #default="{ row }">
          {{ getSplitTypeLabel(row.splitType) }}
        </template>
      </el-table-column>
      <el-table-column label="难度" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="difficultyTag(row.difficulty)" size="small" effect="dark">
            {{ getDifficultyLabel(row.difficulty) }}
          </el-tag>
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

    <!-- ==================== 添加/编辑弹窗（简化版基本信息） ==================== -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditing ? '编辑模板' : '创建模板'"
      width="580px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="模板名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入模板名称" maxlength="50" show-word-limit />
        </el-form-item>

        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="描述模板内容..."
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="计划类型" prop="planType">
          <el-select v-model="form.planType" style="width: 100%">
            <el-option
              v-for="item in planTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="分化方式" prop="splitType">
          <el-select v-model="form.splitType" style="width: 100%">
            <el-option
              v-for="item in splitTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="难度" prop="difficulty">
          <el-select v-model="form.difficulty" style="width: 100%">
            <el-option
              v-for="item in difficultyOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
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
 * 管理后台 - 计划模板管理页面
 * 管理员管理模板基本信息（名称、描述、类型、分化方式、难度）
 * 完整模板编辑（含训练日和餐次配置）需在计划编辑器中创建后导入
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Edit, Delete } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { getAdminTemplates, createAdminTemplate, updateAdminTemplate, deleteAdminTemplate } from '@/api/admin'
import { getDictOptions, type DictOption } from '@/api/dict'

// ==================== 类型定义 ====================

/** 模板列表项 */
interface TemplateItem {
  id: number
  name: string
  description?: string
  planType: string
  splitType: string
  difficulty: string
  createdAt?: string
}

// ==================== 字典选项 ====================

const planTypeOptions = ref<DictOption[]>([])
const splitTypeOptions = ref<DictOption[]>([])
const difficultyOptions = ref<DictOption[]>([])

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
const tableData = ref<TemplateItem[]>([])

// ==================== 表单 ====================

const form = reactive({
  name: '',
  description: '',
  planType: 'BULK',
  splitType: 'FOUR_DAY',
  difficulty: 'BEGINNER'
})

const formRules: FormRules = {
  name: [
    { required: true, message: '请输入模板名称', trigger: 'blur' },
    { max: 50, message: '模板名称不超过50个字符', trigger: 'blur' }
  ],
  planType: [
    { required: true, message: '请选择计划类型', trigger: 'change' }
  ],
  splitType: [
    { required: true, message: '请选择分化方式', trigger: 'change' }
  ],
  difficulty: [
    { required: true, message: '请选择难度', trigger: 'change' }
  ]
}

// ==================== 工具函数 ====================

function getPreferredOptionValue(options: DictOption[], preferred: string): string {
  return options.find(item => item.value === preferred)?.value || options[0]?.value || ''
}

function getOptionLabel(options: DictOption[], value: string): string {
  return options.find(item => item.value === value)?.label || value
}

/** 计划类型 → 中文 */
function getPlanTypeLabel(type: string): string {
  return getOptionLabel(planTypeOptions.value, type)
}

/** 分化类型 → 中文 */
function getSplitTypeLabel(type: string): string {
  return getOptionLabel(splitTypeOptions.value, type)
}

/** 难度 → 中文 */
function getDifficultyLabel(level: string): string {
  return getOptionLabel(difficultyOptions.value, level)
}

/** 计划类型 → el-tag 颜色 */
function planTypeTag(type: string): string {
  const map: Record<string, string> = {
    CUT: 'danger', BULK: 'primary', MAINTAIN: 'info'
  }
  return map[type] || 'info'
}

/** 难度 → el-tag 颜色 */
function difficultyTag(level: string): string {
  const map: Record<string, string> = {
    BEGINNER: 'success', INTERMEDIATE: 'warning', ADVANCED: 'danger'
  }
  return map[level] || 'info'
}

/** 格式化日期 */
function formatDate(dateStr?: string): string {
  if (!dateStr) return '--'
  const d = new Date(dateStr)
  if (isNaN(d.getTime())) return dateStr
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

// ==================== 数据获取 ====================

async function fetchDictData() {
  const [planTypes, splitTypes, difficultyLevels] = await Promise.all([
    getDictOptions('plan_type'),
    getDictOptions('split_type'),
    getDictOptions('difficulty')
  ])

  planTypeOptions.value = planTypes
  splitTypeOptions.value = splitTypes
  difficultyOptions.value = difficultyLevels
}

/** 获取模板列表 */
async function fetchList() {
  loading.value = true
  error.value = ''
  try {
    const kw = keyword.value.trim() || undefined
    const res = await getAdminTemplates({ keyword: kw }) as any
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
function openDialog(template?: TemplateItem) {
  if (template) {
    isEditing.value = true
    editingId.value = template.id
    form.name = template.name
    form.description = template.description || ''
    form.planType = template.planType || getPreferredOptionValue(planTypeOptions.value, 'BULK')
    form.splitType = template.splitType || getPreferredOptionValue(splitTypeOptions.value, 'FOUR_DAY')
    form.difficulty = template.difficulty || getPreferredOptionValue(difficultyOptions.value, 'BEGINNER')
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
  form.planType = getPreferredOptionValue(planTypeOptions.value, 'BULK')
  form.splitType = getPreferredOptionValue(splitTypeOptions.value, 'FOUR_DAY')
  form.difficulty = getPreferredOptionValue(difficultyOptions.value, 'BEGINNER')
}

/** 关闭弹窗后重置 */
function resetForm() {
  formRef.value?.resetFields()
  resetFormData()
}

/** 保存模板 */
async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const payload = {
      name: form.name,
      description: form.description || undefined,
      planType: form.planType,
      splitType: form.splitType,
      difficulty: form.difficulty
    }

    if (isEditing.value && editingId.value) {
      await updateAdminTemplate(editingId.value, payload)
      ElMessage.success('模板已更新')
    } else {
      await createAdminTemplate(payload)
      ElMessage.success('模板已创建')
    }

    dialogVisible.value = false
    await fetchList()
  } catch (err: any) {
    ElMessage.error(err.message || '保存失败，请重试')
  } finally {
    saving.value = false
  }
}

/** 删除模板 */
async function handleDelete(template: TemplateItem) {
  try {
    await ElMessageBox.confirm(
      `确定要删除"${template.name}"吗？此操作不可撤销。`,
      '删除确认',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    await deleteAdminTemplate(template.id)
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
  fetchDictData()
  fetchList()
})
</script>

<style scoped lang="scss">
/**
 * 管理后台 - 计划模板管理页面样式
 */

.admin-template-page {
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
</style>
