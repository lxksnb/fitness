<template>
  <!-- 健身计划模板市场页：卡片网格展示模板，支持筛选、展开详情、一键导入 -->
  <div class="plan-template-page">
    <!-- ==================== 页面标题栏 ==================== -->
    <div class="page-header">
      <h2>计划模板市场</h2>
      <div class="header-actions">
        <el-button @click="$router.push('/plans')">
          <el-icon><ArrowLeft /></el-icon>
          返回计划列表
        </el-button>
        <el-button type="primary" @click="$router.push('/plans/create')">
          <el-icon><Plus /></el-icon>
          自定义创建
        </el-button>
      </div>
    </div>

    <!-- ==================== 筛选栏 ==================== -->
    <div class="filter-bar">
      <span class="filter-label">难度筛选：</span>
      <el-select v-model="difficultyFilter" placeholder="全部难度" clearable style="width: 160px" @change="applyFilter">
        <el-option label="全部难度" value="" />
        <el-option label="新手" value="BEGINNER" />
        <el-option label="中级" value="INTERMEDIATE" />
        <el-option label="高级" value="ADVANCED" />
      </el-select>
      <span class="filter-count">共 {{ filteredTemplates.length }} 个模板</span>
    </div>

    <!-- ==================== 加载骨架 ==================== -->
    <template v-if="loading">
      <el-row :gutter="16">
        <el-col v-for="i in 6" :key="i" :xs="24" :sm="12" :md="8" :lg="8" style="margin-bottom: 16px">
          <el-card class="template-card">
            <el-skeleton :rows="5" animated />
          </el-card>
        </el-col>
      </el-row>
    </template>

    <!-- ==================== 错误状态 ==================== -->
    <template v-else-if="error">
      <el-result icon="error" title="数据加载失败" :sub-title="error">
        <template #extra>
          <el-button type="primary" @click="fetchTemplates">重新加载</el-button>
        </template>
      </el-result>
    </template>

    <!-- ==================== 模板卡片网格 ==================== -->
    <template v-else>
      <template v-if="filteredTemplates.length > 0">
        <el-row :gutter="16">
          <el-col
            v-for="template in filteredTemplates"
            :key="template.id"
            :xs="24"
            :sm="12"
            :md="8"
            :lg="8"
            style="margin-bottom: 16px"
          >
            <el-card
              shadow="hover"
              :class="['template-card', { 'template-card--expanded': expandedId === template.id }]"
            >
              <!-- 卡片主体信息 -->
              <div class="template-card-body" @click="toggleExpand(template)">
                <!-- 模板名称 -->
                <h3 class="template-name">{{ template.name }}</h3>

                <!-- 描述文字（最多两行截断） -->
                <p class="template-desc" :title="template.description">
                  {{ template.description || '暂无描述' }}
                </p>

                <!-- 标签行：计划类型 + 分化类型 + 难度 -->
                <div class="template-tags">
                  <el-tag
                    :type="planTypeTag(template.planType)"
                    size="small"
                    effect="plain"
                  >
                    {{ getPlanTypeLabel(template.planType) }}
                  </el-tag>
                  <el-tag type="info" size="small" effect="plain">
                    {{ getSplitTypeLabel(template.splitType) }}
                  </el-tag>
                  <el-tag
                    :type="difficultyTag(template.difficulty)"
                    size="small"
                    effect="dark"
                  >
                    {{ getDifficultyLabel(template.difficulty) }}
                  </el-tag>
                </div>
              </div>

              <!-- 展开详情区域 -->
              <div v-if="expandedId === template.id" class="template-detail">
                <el-divider style="margin: 12px 0" />

                <!-- 训练日概览 -->
                <div class="detail-section">
                  <div class="detail-title">
                    <el-icon><List /></el-icon>
                    训练日安排（{{ template.trainingDays?.length || 0 }} 天）
                  </div>
                  <div class="training-days-list" v-if="template.trainingDays && template.trainingDays.length > 0">
                    <div
                      v-for="(day, idx) in template.trainingDays"
                      :key="idx"
                      class="training-day-item"
                    >
                      <el-tag
                        :type="day.dayType === 'TRAINING' ? 'primary' : 'info'"
                        size="small"
                        effect="plain"
                      >
                        第{{ day.dayOrder }}天 · {{ day.dayType === 'TRAINING' ? '训练日' : '休息日' }}
                      </el-tag>
                      <template v-if="day.dayType === 'TRAINING'">
                        <span class="day-detail">
                          {{ getTrainingTypeLabel(day.trainingType) }}
                          <template v-if="day.actions && day.actions.length > 0">
                            · {{ day.actions.length }} 个动作
                          </template>
                        </span>
                        <div class="action-mini-list" v-if="day.actions && day.actions.length > 0">
                          <span v-for="action in day.actions" :key="action.actionId" class="action-mini-tag">
                            {{ action.actionName }}
                          </span>
                        </div>
                      </template>
                    </div>
                  </div>
                  <span v-else class="detail-empty">暂无训练日数据</span>
                </div>

                <!-- 餐次配置概览 -->
                <div class="detail-section" v-if="template.mealConfigs && template.mealConfigs.length > 0">
                  <div class="detail-title">
                    <el-icon><Food /></el-icon>
                    餐次配置概览
                  </div>
                  <div class="meal-summary-grid">
                    <div
                      v-for="(config, idx) in template.mealConfigs.slice(0, 8)"
                      :key="idx"
                      class="meal-summary-item"
                    >
                      <el-tag size="small" :type="config.dayType === 'TRAINING' ? '' : 'info'">
                        {{ config.dayType === 'TRAINING' ? '训练' : '休息' }}
                      </el-tag>
                      <span class="meal-summary-name">{{ getMealLabel(config.mealType) }}</span>
                      <span class="meal-summary-ratio">
                        C{{ Math.round((config.carbRatio || 0) * 100) }}%
                        P{{ Math.round((config.proteinRatio || 0) * 100) }}%
                        F{{ Math.round((config.fatRatio || 0) * 100) }}%
                      </span>
                    </div>
                    <span v-if="template.mealConfigs.length > 8" class="detail-more">
                      ... 还有 {{ template.mealConfigs.length - 8 }} 项
                    </span>
                  </div>
                </div>

                <!-- 操作按钮 -->
                <div class="detail-actions">
                  <el-button
                    type="primary"
                    :loading="importingId === template.id"
                    @click.stop="handleImport(template)"
                  >
                    <el-icon><Download /></el-icon>
                    一键导入
                  </el-button>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </template>

      <!-- ==================== 空状态 ==================== -->
      <el-empty v-else description="暂无匹配的模板" :image-size="120">
        <el-button v-if="difficultyFilter" @click="difficultyFilter = ''; applyFilter()">
          清除筛选
        </el-button>
      </el-empty>
    </template>
  </div>
</template>

<script setup lang="ts">
/**
 * 健身计划模板市场页
 * 以卡片网格展示系统预置的健身计划模板，支持：
 * - 难度筛选（新手/中级/高级）
 * - 点击展开查看训练日和餐次详情
 * - 一键导入模板创建个人计划
 */
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft, Plus, List, Food as FoodIcon, Download
} from '@element-plus/icons-vue'
import { getTemplates, importTemplate } from '@/api/plan'
import { getDictOptions, type DictOption } from '@/api/dict'

const router = useRouter()

// ==================== 类型定义 ====================

/** 模板项 */
interface TemplateItem {
  id: number
  name: string
  description: string
  planType: string
  splitType: string
  difficulty: string
  trainingDays?: TrainingDayBrief[]
  mealConfigs?: MealConfigBrief[]
  [key: string]: any
}

/** 训练日简要信息 */
interface TrainingDayBrief {
  dayOrder: number
  dayType: string
  trainingType?: string
  actions?: ActionBrief[]
}

/** 动作简要信息 */
interface ActionBrief {
  actionId: number
  actionName: string
}

/** 餐次配置简要信息 */
interface MealConfigBrief {
  dayType: string
  mealType: string
  carbRatio: number
  proteinRatio: number
  fatRatio: number
}

// ==================== 状态 ====================

const loading = ref(true)
const error = ref('')
const importingId = ref<number | null>(null)
const difficultyFilter = ref('')
const expandedId = ref<number | null>(null)

/** 所有模板 */
const templates = ref<TemplateItem[]>([])
const planTypeOptions = ref<DictOption[]>([])
const splitTypeOptions = ref<DictOption[]>([])
const difficultyOptions = ref<DictOption[]>([])
const trainingTypeOptions = ref<DictOption[]>([])
const mealTypeOptions = ref<DictOption[]>([])

/** 筛选后的模板 */
const filteredTemplates = computed(() => {
  if (!difficultyFilter.value) return templates.value
  return templates.value.filter(t => t.difficulty === difficultyFilter.value)
})

// ==================== 工具函数 ====================

/** 计划类型 → 中文 */
function getPlanTypeLabel(type: string): string {
  return getDictLabel(planTypeOptions.value, type)
}

/** 分化类型 → 中文 */
function getSplitTypeLabel(type: string): string {
  return getDictLabel(splitTypeOptions.value, type)
}

/** 难度 → 中文 */
function getDifficultyLabel(level: string): string {
  return getDictLabel(difficultyOptions.value, level)
}

/** 训练部位 → 中文 */
function getTrainingTypeLabel(type?: string): string {
  if (!type) return ''
  return getDictLabel(trainingTypeOptions.value, type)
}

/** 餐次类型 → 中文 */
function getMealLabel(type: string): string {
  return getDictLabel(mealTypeOptions.value, type)
}

function getDictLabel(options: DictOption[], value: string): string {
  return options.find(item => item.value === value)?.label || value
}

/** 计划类型 → el-tag 颜色 */
function planTypeTag(type: string): string {
  const map: Record<string, string> = {
    CUT: 'danger',
    BULK: 'primary',
    MAINTAIN: 'info',
    '减脂': 'danger',
    '增肌': 'primary',
    '维持': 'info'
  }
  return map[type] || 'info'
}

/** 难度 → el-tag 颜色（绿/橙/红） */
function difficultyTag(level: string): string {
  const map: Record<string, string> = {
    BEGINNER: 'success',
    INTERMEDIATE: 'warning',
    ADVANCED: 'danger',
    '新手': 'success',
    '中级': 'warning',
    '高级': 'danger'
  }
  return map[level] || 'info'
}

// ==================== 交互 ====================

/** 展开/收起模板详情 */
function toggleExpand(template: TemplateItem) {
  if (expandedId.value === template.id) {
    expandedId.value = null
  } else {
    expandedId.value = template.id
  }
}

/** 应用筛选 */
function applyFilter() {
  expandedId.value = null
}

// ==================== 数据获取 ====================

async function fetchDictData() {
  const [planTypes, splitTypes, difficultyLevels, trainingTypes, mealTypes] = await Promise.all([
    getDictOptions('plan_type'),
    getDictOptions('split_type'),
    getDictOptions('difficulty'),
    getDictOptions('training_type'),
    getDictOptions('meal_type')
  ])

  planTypeOptions.value = planTypes
  splitTypeOptions.value = splitTypes
  difficultyOptions.value = difficultyLevels
  trainingTypeOptions.value = trainingTypes
  mealTypeOptions.value = mealTypes
}

/** 获取模板列表 */
async function fetchTemplates() {
  loading.value = true
  error.value = ''
  try {
    const res = await getTemplates() as any
    templates.value = Array.isArray(res) ? res : (res?.records || res?.list || [])
  } catch (err: any) {
    error.value = err.message || '数据加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

// ==================== 导入操作 ====================

/** 一键导入模板 */
async function handleImport(template: TemplateItem) {
  try {
    await ElMessageBox.confirm(
      `确定要导入"${template.name}"模板吗？导入后将创建一个新的个人计划。`,
      '导入确认',
      {
        confirmButtonText: '确定导入',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    importingId.value = template.id
    const result = await importTemplate(template.id) as any

    ElMessage.success(`"${template.name}"已成功导入`)
    // 如果有返回ID则跳转编辑页，否则回到计划列表
    const newId = result?.id || result?.planId
    if (newId) {
      router.push(`/plans/${newId}/edit`)
    } else {
      router.push('/plans')
    }
  } catch (err: any) {
    if (err !== 'cancel' && err !== 'close') {
      ElMessage.error(err.message || '导入失败，请稍后重试')
    }
  } finally {
    importingId.value = null
  }
}

// ==================== 生命周期 ====================

onMounted(() => {
  fetchDictData()
  fetchTemplates()
})
</script>

<style scoped lang="scss">
/**
 * 健身计划模板市场页样式
 */

.plan-template-page {
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
    gap: 12px;
  }
}

/* ==================== 筛选栏 ==================== */
.filter-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
  padding: 10px 16px;
  background: #fafafa;
  border-radius: 6px;
  border: 1px solid #ebeef5;

  .filter-label {
    font-size: 14px;
    color: #606266;
    font-weight: 500;
  }

  .filter-count {
    margin-left: auto;
    font-size: 13px;
    color: #909399;
  }
}

/* ==================== 模板卡片 ==================== */
.template-card {
  cursor: pointer;
  transition: all 0.25s ease;

  &:hover {
    border-color: #409eff;
    box-shadow: 0 2px 12px rgba(64, 158, 255, 0.12);
  }

  &--expanded {
    border-color: #409eff;
    box-shadow: 0 2px 16px rgba(64, 158, 255, 0.18);
  }

  :deep(.el-card__body) {
    padding: 16px 18px;
  }
}

.template-card-body {
  // 点击主体
}

.template-name {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  line-height: 1.3;
}

.template-desc {
  margin: 0 0 12px 0;
  font-size: 13px;
  color: #909399;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  min-height: 39px;
}

.template-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

/* ==================== 展开详情 ==================== */
.template-detail {
  cursor: default;
}

.detail-section {
  margin-bottom: 12px;

  .detail-title {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 8px;
  }

  .detail-empty {
    font-size: 12px;
    color: #c0c4cc;
  }
}

/* 训练日列表 */
.training-days-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.training-day-item {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #606266;

  .day-detail {
    color: #909399;
  }
}

.action-mini-list {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  padding-left: 4px;
  width: 100%;
}

.action-mini-tag {
  display: inline-block;
  padding: 1px 6px;
  background: #ecf5ff;
  color: #409eff;
  font-size: 11px;
  border-radius: 3px;
  white-space: nowrap;
}

/* 餐次配置概览网格 */
.meal-summary-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.meal-summary-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #606266;

  .meal-summary-name {
    min-width: 48px;
  }

  .meal-summary-ratio {
    color: #909399;
    font-size: 11px;
  }
}

.detail-more {
  font-size: 12px;
  color: #909399;
}

/* 详情操作按钮 */
.detail-actions {
  display: flex;
  justify-content: center;
  padding-top: 10px;
  margin-top: 10px;
  border-top: 1px solid #f0f0f0;
}

/* ==================== 响应式调整 ==================== */
@media (max-width: 768px) {
  .plan-template-page {
    padding: 2px;
  }

  .filter-bar {
    flex-wrap: wrap;
  }

  .template-name {
    font-size: 15px;
  }

  .template-card {
    :deep(.el-card__body) {
      padding: 12px 14px;
    }
  }
}
</style>
