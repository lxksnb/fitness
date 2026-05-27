<template>
  <div class="admin-template-page">
    <!-- 页面标题与操作栏 -->
    <div class="page-header">
      <h2>模板管理</h2>
      <div class="header-actions">
        <el-input
          v-model="keyword"
          placeholder="搜索模板名称..."
          clearable
          @clear="fetchList"
          style="width: 260px"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="$router.push('/admin/templates/create')">
          <el-icon><Plus /></el-icon>
          创建模板
        </el-button>
      </div>
    </div>

    <!-- 数据表格 -->
    <el-table
      v-loading="loading"
      :data="tableData"
      border
      stripe
      style="width: 100%"
      empty-text="暂无模板数据"
    >
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="templateName" label="模板名称" min-width="160" />
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
          <el-button type="primary" link size="small" @click="$router.push(`/admin/templates/${row.id}/edit`)">
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

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="total > pageSize">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchList"
      />
    </div>

    <!-- 错误状态 -->
    <template v-if="!loading && error">
      <el-result icon="error" title="数据加载失败" :sub-title="error">
        <template #extra>
          <el-button type="primary" @click="fetchList">重新加载</el-button>
        </template>
      </el-result>
    </template>
  </div>
</template>

<script setup lang="ts">
/**
 * 管理后台 - 计划模板管理页面
 * 管理员管理模板基本信息（名称、描述、类型、分化方式、难度）
 * 创建和编辑跳转到专用编辑器页面进行完整编辑
 */
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Edit, Delete } from '@element-plus/icons-vue'
import { getAdminTemplates, deleteAdminTemplate } from '@/api/admin'
import { getDictOptions, type DictOption } from '@/api/dict'

// ==================== 类型定义 ====================

interface TemplateItem {
  id: number
  templateName: string
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
const keyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
let searchTimer: ReturnType<typeof setTimeout> | null = null

const tableData = ref<TemplateItem[]>([])

// ==================== 工具函数 ====================

function getOptionLabel(options: DictOption[], value: string): string {
  return options.find(item => item.value === value)?.label || value
}

function getPlanTypeLabel(type: string): string {
  return getOptionLabel(planTypeOptions.value, type)
}

function getSplitTypeLabel(type: string): string {
  return getOptionLabel(splitTypeOptions.value, type)
}

function getDifficultyLabel(level: string): string {
  return getOptionLabel(difficultyOptions.value, level)
}

function planTypeTag(type: string): string {
  const map: Record<string, string> = {
    CUT: 'danger', BULK: 'primary', MAINTAIN: 'info'
  }
  return map[type] || 'info'
}

function difficultyTag(level: string): string {
  const map: Record<string, string> = {
    BEGINNER: 'success', INTERMEDIATE: 'warning', ADVANCED: 'danger'
  }
  return map[level] || 'info'
}

function formatDate(dateStr?: string): string {
  if (!dateStr) return '--'
  const d = new Date(dateStr)
  if (isNaN(d.getTime())) return dateStr
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

function scheduleFetchList() {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    fetchList()
  }, 300)
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

async function handleDelete(template: TemplateItem) {
  try {
    await ElMessageBox.confirm(
      `确定要删除"${template.templateName}"吗？此操作不可撤销。`,
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

watch(keyword, scheduleFetchList)
</script>

<style scoped lang="scss">
.admin-template-page {
  padding: 4px;
}

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

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
