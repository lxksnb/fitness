<template>
  <!-- 饮食概览页面：每日营养目标vs实际摄入对比，按餐次分解 -->
  <div class="diet-overview">
    <!-- ==================== 页面标题与日期选择 ==================== -->
    <div class="page-header">
      <h2>饮食概览</h2>
      <div class="header-actions">
        <el-date-picker
          v-model="selectedDate"
          type="date"
          placeholder="选择日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          @change="fetchData"
          style="width: 180px"
        />
        <el-button @click="$router.push('/diet')">
          <el-icon><Back /></el-icon>
          返回饮食记录
        </el-button>
      </div>
    </div>

    <!-- ==================== 加载骨架 ==================== -->
    <template v-if="loading">
      <el-row :gutter="16" class="section">
        <el-col :span="6" v-for="i in 4" :key="i">
          <el-card>
            <el-skeleton :rows="3" animated />
          </el-card>
        </el-col>
      </el-row>
      <el-card class="section">
        <el-skeleton :rows="6" animated />
      </el-card>
    </template>

    <!-- ==================== 错误状态 ==================== -->
    <template v-else-if="error">
      <el-result icon="error" title="数据加载失败" :sub-title="error">
        <template #extra>
          <el-button type="primary" @click="fetchData">重新加载</el-button>
        </template>
      </el-result>
    </template>

    <!-- ==================== 正常内容 ==================== -->
    <template v-else>
      <!-- 总结卡片行 -->
      <el-row :gutter="16" class="section">
        <el-col :span="6" v-for="card in summaryCards" :key="card.label">
          <el-card shadow="hover" class="summary-card">
            <div class="summary-label">{{ card.label }}</div>
            <div class="summary-value">{{ card.actual }} / {{ card.target }} {{ card.unit }}</div>
            <el-progress
              :percentage="card.percent"
              :color="card.color"
              :stroke-width="14"
              class="summary-progress"
            />
            <div class="summary-remain">
              {{ card.remainText }}
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 按餐次分解 -->
      <el-card header="餐次营养分解" class="section">
        <template v-if="mealBreakdown.length > 0">
          <div v-for="meal in mealBreakdown" :key="meal.mealType" class="meal-breakdown-row">
            <div class="meal-breakdown-header">
              <el-tag :type="mealTagType(meal.mealType)" size="small">
                {{ getMealLabel(meal.mealType) }}
              </el-tag>
              <span class="meal-cal-total">{{ formatNum(meal.totalCalories, 0) }} kcal</span>
            </div>
            <div class="meal-nutrition-bars">
              <div
                v-for="nut in meal.nutritionItems"
                :key="nut.name"
                class="meal-nut-bar"
              >
                <span class="nut-name">{{ nut.name }}</span>
                <el-progress
                  :percentage="nut.percent"
                  :color="nut.color"
                  :stroke-width="10"
                  class="nut-progress"
                >
                  <span class="nut-text">{{ formatNum(nut.actual, 0) }}/{{ formatNum(nut.target, 0) }}{{ nut.unit }}</span>
                </el-progress>
              </div>
            </div>
          </div>
        </template>
        <el-empty v-else description="暂无该日饮食数据" :image-size="100" />
      </el-card>

      <!-- 备注说明 -->
      <el-card class="section">
        <template #header>
          <span>饮食建议</span>
        </template>
        <div class="advice-section">
          <el-alert
            v-for="(advice, idx) in advices"
            :key="idx"
            :title="advice"
            :type="idx === 0 && advice.includes('超标') ? 'warning' : 'info'"
            :closable="false"
            show-icon
            style="margin-bottom: 8px"
          />
          <div v-if="advices.length === 0" style="color: #909399; font-size: 14px; text-align: center; padding: 12px;">
            保持均衡饮食，健康每一天！
          </div>
        </div>
      </el-card>
    </template>
  </div>
</template>

<script setup lang="ts">
/**
 * 饮食概览页面
 * 展示指定日期的营养目标vs实际摄入对比，
 * 包含热量、碳水、蛋白质、脂肪四大维度进度条，
 * 以及按餐次分解的营养摄入情况
 */
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Back } from '@element-plus/icons-vue'
import { getDailySummary } from '@/api/diet'

const router = useRouter()

// ==================== 类型定义 ====================

/** 每日汇总数据 */
interface DailySummary {
  targetCalories: number | null
  actualCalories: number | null
  targetCarb: number | null
  actualCarb: number | null
  targetProtein: number | null
  actualProtein: number | null
  targetFat: number | null
  actualFat: number | null
  mealBreakdowns: MealBreakdown[]
}

/** 餐次营养分解 */
interface MealBreakdown {
  mealType: string
  totalCalories: number
  actualCarb: number
  actualProtein: number
  actualFat: number
}

/** 总结卡片数据 */
interface SummaryCard {
  label: string
  actual: string
  target: string
  unit: string
  percent: number
  color: string
  remainText: string
}

/** 餐次营养进度条项 */
interface MealNutritionBar {
  name: string
  actual: number
  target: number
  unit: string
  percent: number
  color: string
}

/** 餐次分解视图数据 */
interface MealBreakdownView {
  mealType: string
  totalCalories: number
  nutritionItems: MealNutritionBar[]
}

// ==================== 状态 ====================

const loading = ref(true)
const error = ref('')
const selectedDate = ref(getTodayStr())
const summary = ref<DailySummary | null>(null)

// ==================== 工具函数 ====================

/** 获取当日日期字符串 yyyy-MM-dd */
function getTodayStr(): string {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

/** 格式化数字 */
function formatNum(val: number | null | undefined, decimals = 1): string {
  if (val == null || isNaN(val)) return '0'
  return Number(val).toFixed(decimals)
}

/** 计算百分比，上限100% */
function calcPercent(actual: number | null | undefined, target: number | null | undefined): number {
  if (!target || target <= 0) return 0
  return Math.min(Math.round(((actual ?? 0) / target) * 100), 100)
}

/** 将后端餐食类型映射为中文 */
function getMealLabel(type: string): string {
  const map: Record<string, string> = {
    BREAKFAST: '早餐',
    LUNCH: '午餐',
    DINNER: '晚餐',
    SUPPER: '夜宵',
    PRE_WORKOUT: '练前餐',
    POST_WORKOUT: '练后餐',
    OTHER: '其他餐',
    breakfast: '早餐',
    lunch: '午餐',
    dinner: '晚餐',
    snack: '加餐'
  }
  return map[type] || type
}

/** 根据餐食类型返回 el-tag 的 type */
function mealTagType(type: string): string {
  const map: Record<string, string> = {
    BREAKFAST: 'success',
    LUNCH: 'warning',
    DINNER: '',
    SUPPER: 'info',
    PRE_WORKOUT: 'primary',
    POST_WORKOUT: 'success',
    OTHER: 'info',
    breakfast: 'success',
    lunch: 'warning',
    dinner: '',
    snack: 'info'
  }
  return map[type] || ''
}

// ==================== 计算属性 ====================

/** 顶部4张总结卡片 */
const summaryCards = computed<SummaryCard[]>(() => {
  if (!summary.value) return []
  const s = summary.value

  const items: { label: string; actual: number | null; target: number | null; unit: string; color: string }[] = [
    { label: '热量', actual: s.actualCalories, target: s.targetCalories, unit: 'kcal', color: '#e6a23c' },
    { label: '碳水', actual: s.actualCarb, target: s.targetCarb, unit: 'g', color: '#409eff' },
    { label: '蛋白质', actual: s.actualProtein, target: s.targetProtein, unit: 'g', color: '#67c23a' },
    { label: '脂肪', actual: s.actualFat, target: s.targetFat, unit: 'g', color: '#f56c6c' }
  ]

  return items.map(item => {
    const percent = calcPercent(item.actual, item.target)
    const remain = (item.target ?? 0) - (item.actual ?? 0)
    let remainText = ''
    if (!item.target || item.target <= 0) {
      remainText = '未设置目标'
    } else if (remain > 0) {
      remainText = `还可摄入 ${formatNum(remain, item.unit === 'kcal' ? 0 : 1)} ${item.unit}`
    } else {
      remainText = `已超标 ${formatNum(Math.abs(remain), item.unit === 'kcal' ? 0 : 1)} ${item.unit}`
    }

    const color = percent >= 100 ? '#f56c6c' : percent >= 80 ? '#e6a23c' : item.color

    return {
      label: item.label,
      actual: formatNum(item.actual, item.unit === 'kcal' ? 0 : 1),
      target: formatNum(item.target, item.unit === 'kcal' ? 0 : 1),
      unit: item.unit,
      percent,
      color,
      remainText
    }
  })
})

/** 按餐次营养分解（纵向进度条） */
const mealBreakdown = computed<MealBreakdownView[]>(() => {
  if (!summary.value?.mealBreakdowns) return []

  const breakdowns = summary.value.mealBreakdowns
  const s = summary.value

  return breakdowns.map(bd => {
    // 每个餐次的碳水/蛋白质/脂肪 进度 = 餐次实际 / 总目标
    const nutritionItems: MealNutritionBar[] = [
      {
        name: '碳水',
        actual: bd.actualCarb ?? 0,
        target: s.targetCarb ?? 0,
        unit: 'g',
        percent: calcPercent(bd.actualCarb, s.targetCarb),
        color: '#409eff'
      },
      {
        name: '蛋白质',
        actual: bd.actualProtein ?? 0,
        target: s.targetProtein ?? 0,
        unit: 'g',
        percent: calcPercent(bd.actualProtein, s.targetProtein),
        color: '#67c23a'
      },
      {
        name: '脂肪',
        actual: bd.actualFat ?? 0,
        target: s.targetFat ?? 0,
        unit: 'g',
        percent: calcPercent(bd.actualFat, s.targetFat),
        color: '#e6a23c'
      }
    ]

    return {
      mealType: bd.mealType,
      totalCalories: bd.totalCalories ?? 0,
      nutritionItems
    }
  })
})

/** 根据实际摄入与目标的对比生成饮食建议 */
const advices = computed<string[]>(() => {
  if (!summary.value) return []
  const s = summary.value
  const tips: string[] = []

  if (s.targetCalories && s.actualCalories) {
    if (s.actualCalories > s.targetCalories * 1.2) {
      tips.push('今日热量摄入严重超标，建议控制食量，减少高热量食物')
    } else if (s.actualCalories > s.targetCalories) {
      tips.push('今日热量摄入略超标，适当减少后续餐次分量')
    } else if (s.actualCalories < s.targetCalories * 0.6) {
      tips.push('今日热量摄入偏低，请注意补充足够营养')
    } else if (s.actualCalories < s.targetCalories * 0.8) {
      tips.push('今日热量摄入略低于目标，可适当加餐')
    } else {
      tips.push('热量摄入在合理范围内，继续保持！')
    }
  }

  if (s.targetProtein && s.actualProtein && s.actualProtein < s.targetProtein * 0.7) {
    tips.push('蛋白质摄入不足，建议增加鸡胸肉、鱼类、豆制品等高蛋白食物')
  }

  if (s.targetCarb && s.actualCarb && s.actualCarb > s.targetCarb * 1.3) {
    tips.push('碳水摄入偏高，注意控制主食和甜食摄入量')
  }

  return tips
})

// ==================== 数据获取 ====================

/** 加载每日饮食汇总 */
async function fetchData() {
  loading.value = true
  error.value = ''
  try {
    const res = await getDailySummary(selectedDate.value) as any
    summary.value = res as DailySummary
  } catch (err: any) {
    error.value = err.message || '数据加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

// ==================== 生命周期 ====================

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
/**
 * 饮食概览页面样式
 */

.diet-overview {
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

/* ==================== 通用分区间距 ==================== */
.section {
  margin-bottom: 16px;
}

/* ==================== 总结卡片 ==================== */
.summary-card {
  text-align: center;

  :deep(.el-card__body) {
    padding: 20px 16px;
  }
}

.summary-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
  font-weight: 500;
}

.summary-value {
  font-size: 20px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 12px;
}

.summary-progress {
  margin-bottom: 8px;
}

.summary-remain {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

/* ==================== 餐次分解 ==================== */
.meal-breakdown-row {
  padding: 16px 0;
  border-bottom: 1px solid #f5f5f5;

  &:last-child {
    border-bottom: none;
    padding-bottom: 0;
  }

  &:first-child {
    padding-top: 0;
  }
}

.meal-breakdown-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;

  .meal-cal-total {
    font-size: 14px;
    font-weight: 600;
    color: #e6a23c;
  }
}

.meal-nutrition-bars {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding-left: 4px;
}

.meal-nut-bar {
  display: flex;
  align-items: center;
  gap: 10px;

  .nut-name {
    width: 40px;
    font-size: 13px;
    color: #606266;
    flex-shrink: 0;
  }

  .nut-progress {
    flex: 1;
  }

  .nut-text {
    font-size: 11px;
    color: #909399;
  }
}

/* ==================== 建议区域 ==================== */
.advice-section {
  padding: 4px 0;
}
</style>
