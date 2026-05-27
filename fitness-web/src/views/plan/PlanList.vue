<template>
  <!-- 健身计划列表页：卡片网格展示所有计划，支持激活/删除/跳转编辑 -->
  <div class="plan-list-page">
    <!-- ==================== 页面标题与操作栏 ==================== -->
    <div class="page-header">
      <h2>健身计划</h2>
      <div class="header-actions">
        <el-button @click="$router.push('/plans/templates')">
          <el-icon><Collection /></el-icon>
          模板市场
        </el-button>
        <el-button type="primary" @click="$router.push('/plans/create')">
          <el-icon><Plus /></el-icon>
          创建计划
        </el-button>
      </div>
    </div>

    <!-- ==================== 加载骨架 ==================== -->
    <template v-if="loading">
      <el-row :gutter="16">
        <el-col v-for="i in 6" :key="i" :xs="24" :sm="12" :md="8" :lg="8" style="margin-bottom: 16px">
          <el-card>
            <el-skeleton :rows="5" animated />
          </el-card>
        </el-col>
      </el-row>
    </template>

    <!-- ==================== 错误状态 ==================== -->
    <template v-else-if="error">
      <el-result icon="error" title="数据加载失败" :sub-title="error">
        <template #extra>
          <el-button type="primary" @click="fetchPlans">重新加载</el-button>
        </template>
      </el-result>
    </template>

    <!-- ==================== 计划卡片网格 ==================== -->
    <template v-else>
      <template v-if="planList.length > 0">
        <el-row :gutter="16">
          <el-col
            v-for="plan in planList"
            :key="plan.id"
            :xs="24"
            :sm="12"
            :md="8"
            :lg="8"
            style="margin-bottom: 16px"
          >
            <el-card
              shadow="hover"
              :class="['plan-card', { 'plan-card--active': plan.isActive === 1 }]"
              @click="goEdit(plan.id)"
            >
              <!-- 卡片头部：计划名称 + 激活徽章 -->
              <div class="plan-card-header">
                <h3 class="plan-name">{{ plan.planName }}</h3>
                <el-tag v-if="plan.isActive === 1" type="success" size="small" effect="dark">
                  当前计划
                </el-tag>
              </div>

              <!-- 标签行：计划类型 + 分化类型 -->
              <div class="plan-tags">
                <el-tag :type="planTypeTag(plan.planType)" size="small" effect="plain">
                  {{ getPlanTypeLabel(plan.planType) }}
                </el-tag>
                <el-tag type="info" size="small" effect="plain">
                  {{ getSplitTypeLabel(plan.splitType) }}
                </el-tag>
              </div>

              <!-- 计划摘要信息 -->
              <div class="plan-meta">
                <span class="meta-item">
                  <el-icon><Calendar /></el-icon>
                  {{ formatDate(plan.createdAt) }}
                </span>
                <span class="meta-item" v-if="plan.trainingDays && plan.trainingDays.length > 0">
                  <el-icon><List /></el-icon>
                  {{ plan.trainingDays.length }} 天
                </span>
              </div>

              <!-- 操作按钮区 -->
              <div class="plan-card-actions" @click.stop>
                <el-button
                  v-if="plan.isActive !== 1"
                  type="success"
                  plain
                  size="small"
                  :loading="activatingId === plan.id"
                  @click="handleActivate(plan)"
                >
                  <el-icon><CircleCheck /></el-icon>
                  设为当前计划
                </el-button>
                <span v-else class="active-hint">已激活</span>
                <el-button
                  type="danger"
                  link
                  size="small"
                  @click="handleDelete(plan)"
                >
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </template>

      <!-- ==================== 空状态 ==================== -->
      <el-empty v-else description="还没有健身计划，快去创建一个吧" :image-size="140">
        <el-button type="primary" @click="$router.push('/plans/create')">
          创建计划
        </el-button>
        <el-button @click="$router.push('/plans/templates')">
          浏览模板
        </el-button>
      </el-empty>
    </template>
  </div>
</template>

<script setup lang="ts">
/**
 * 健身计划列表页
 * 以卡片网格展示所有计划，支持激活计划、删除计划、
 * 点击卡片跳转编辑页、跳转模板市场、新建计划
 */
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Collection, Calendar, List, CircleCheck, Delete } from '@element-plus/icons-vue'
import { getPlans, activatePlan, deletePlan } from '@/api/plan'
import { getDictOptions, type DictOption } from '@/api/dict'

const router = useRouter()

// ==================== 类型定义 ====================

/** 计划列表项 */
interface PlanItem {
  id: number
  planName: string
  planType: string
  splitType: string
  isActive: number
  createdAt: string
  trainingDays?: any[]
}

// ==================== 状态 ====================

const loading = ref(true)
const error = ref('')
const activatingId = ref<number | null>(null)

/** 计划列表 */
const planList = ref<PlanItem[]>([])
const planTypeOptions = ref<DictOption[]>([])
const splitTypeOptions = ref<DictOption[]>([])

// ==================== 工具函数 ====================

/** 根据计划类型返回 el-tag 样式 */
function planTypeTag(type: string): string {
  const map: Record<string, string> = {
    CUT: 'danger',
    BULK: 'primary',
    MAINTAIN: 'info',
    '减脂': 'danger',
    '增肌': 'primary',
    '维持': 'info'
  }
  return map[type] || ''
}

function getPlanTypeLabel(type?: string): string {
  if (!type) return ''
  return planTypeOptions.value.find(item => item.value === type)?.label || type
}

function getSplitTypeLabel(type?: string): string {
  if (!type) return ''
  return splitTypeOptions.value.find(item => item.value === type)?.label || type
}

/** 格式化日期 */
function formatDate(dateStr: string): string {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

// ==================== 导航 ====================

/** 跳转编辑页 */
function goEdit(id: number) {
  router.push(`/plans/${id}/edit`)
}

// ==================== 数据获取 ====================

/** 获取计划列表 */
async function fetchPlans() {
  loading.value = true
  error.value = ''
  try {
    const res = await getPlans() as any
    planList.value = (Array.isArray(res) ? res : (res?.records || res?.list || [])) as PlanItem[]
  } catch (err: any) {
    error.value = err.message || '数据加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

async function fetchDictData() {
  try {
    const [planTypes, splitTypes] = await Promise.all([
      getDictOptions('plan_type'),
      getDictOptions('split_type')
    ])
    planTypeOptions.value = planTypes
    splitTypeOptions.value = splitTypes
  } catch {
    planTypeOptions.value = []
    splitTypeOptions.value = []
  }
}

// ==================== 操作 ====================

/** 激活计划 */
async function handleActivate(plan: PlanItem) {
  try {
    await ElMessageBox.confirm(
      `确定将"${plan.planName}"设为当前计划吗？`,
      '激活确认',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'info' }
    )
    activatingId.value = plan.id
    await activatePlan(plan.id)
    ElMessage.success(`"${plan.planName}"已设为当前计划`)
    await fetchPlans()
  } catch (err: any) {
    if (err !== 'cancel' && err !== 'close') {
      ElMessage.error(err.message || '激活失败')
    }
  } finally {
    activatingId.value = null
  }
}

/** 删除计划 */
async function handleDelete(plan: PlanItem) {
  try {
    await ElMessageBox.confirm(
      `确定要删除"${plan.planName}"吗？此操作不可撤销。`,
      '删除确认',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    await deletePlan(plan.id)
    ElMessage.success('已删除')
    await fetchPlans()
  } catch (err: any) {
    if (err !== 'cancel' && err !== 'close') {
      ElMessage.error(err.message || '删除失败')
    }
  }
}

// ==================== 生命周期 ====================

onMounted(() => {
  fetchDictData()
  fetchPlans()
})
</script>

<style scoped lang="scss">
/**
 * 健身计划列表页样式
 */

.plan-list-page {
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

/* ==================== 计划卡片 ==================== */
.plan-card {
  cursor: pointer;
  transition: border-color 0.2s, box-shadow 0.2s;

  &:hover {
    border-color: #409eff;
  }

  // 当前计划高亮：绿色边框
  &--active {
    border-color: #67c23a;
    box-shadow: 0 0 12px rgba(103, 194, 58, 0.15);
    background: linear-gradient(135deg, #f0f9eb 0%, #ffffff 100%);

    &:hover {
      border-color: #67c23a;
      box-shadow: 0 2px 16px rgba(103, 194, 58, 0.2);
    }
  }

  :deep(.el-card__body) {
    padding: 16px 18px;
  }
}

.plan-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;

  .plan-name {
    margin: 0;
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    flex: 1;
    margin-right: 8px;
  }
}

.plan-tags {
  display: flex;
  gap: 6px;
  margin-bottom: 12px;
}

.plan-meta {
  display: flex;
  gap: 16px;
  margin-bottom: 14px;

  .meta-item {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 13px;
    color: #909399;
  }
}

.plan-card-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 10px;
  border-top: 1px solid #f0f0f0;

  .active-hint {
    font-size: 13px;
    color: #67c23a;
    font-weight: 500;
  }
}
</style>
