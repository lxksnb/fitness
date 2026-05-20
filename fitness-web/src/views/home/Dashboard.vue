<template>
  <!-- 首页看板：Dark Iron 暗黑健身美学 —— 聚合体重、热量、训练、营养进度、趋势图 -->
  <div class="dashboard">
    <!-- ==================== 欢迎栏 ==================== -->
    <div class="welcome-bar">
      <div class="welcome-info">
        <h2 class="welcome-title">
          <span class="welcome-greeting">欢迎回来，</span>
          <span class="welcome-name">{{ userStore.nickname }}</span>
        </h2>
        <p v-if="dashboard?.targetCalories" class="welcome-sub">
          <span class="welcome-target">
            <i class="welcome-dot" /> 今日目标 {{ formatNum(dashboard.targetCalories, 0) }} kcal
          </span>
          <template v-if="dashboard.targetCarb">
            <span class="welcome-sep">|</span>
            <span class="welcome-macro">碳 {{ formatNum(dashboard.targetCarb, 0) }}g</span>
          </template>
          <template v-if="dashboard.targetProtein">
            <span class="welcome-macro">蛋白 {{ formatNum(dashboard.targetProtein, 0) }}g</span>
          </template>
          <template v-if="dashboard.targetFat">
            <span class="welcome-macro">脂肪 {{ formatNum(dashboard.targetFat, 0) }}g</span>
          </template>
        </p>
        <p v-else class="welcome-sub welcome-hint">
          暂未设置目标计划，前往设置以获取个性化营养建议
        </p>
      </div>
      <div class="welcome-action">
        <el-tag
          v-if="dashboard?.targetCalories"
          type="success"
          size="large"
          round
          effect="dark"
          class="welcome-tag"
        >
          目标计划进行中
        </el-tag>
        <el-button v-else type="primary" size="small" @click="$router.push('/plans')">
          设置目标
        </el-button>
      </div>
    </div>

    <!-- ==================== 加载骨架 ==================== -->
    <template v-if="loading">
      <div class="stat-cards">
        <div v-for="i in 4" :key="'sk-card-' + i" class="stat-card-skeleton">
          <el-skeleton animated>
            <template #template>
              <div class="sk-inner">
                <el-skeleton-item variant="text" style="width: 50%; height: 14px" />
                <el-skeleton-item variant="text" style="width: 70%; height: 28px; margin: 10px 0" />
                <el-skeleton-item variant="text" style="width: 40%; height: 12px" />
              </div>
            </template>
          </el-skeleton>
        </div>
      </div>
      <div class="section-card">
        <div class="section-header">今日营养摄入</div>
        <div class="section-body">
          <el-skeleton :rows="4" animated />
        </div>
      </div>
      <div class="section-card">
        <div class="section-header">体重趋势 (近30天)</div>
        <div class="section-body">
          <el-skeleton :rows="5" animated />
        </div>
      </div>
      <el-row :gutter="16" class="section">
        <el-col :span="12">
          <div class="section-card">
            <div class="section-header">今日饮食</div>
            <div class="section-body">
              <el-skeleton :rows="3" animated />
            </div>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="section-card">
            <div class="section-header">今日训练</div>
            <div class="section-body">
              <el-skeleton :rows="3" animated />
            </div>
          </div>
        </el-col>
      </el-row>
    </template>

    <!-- ==================== 错误状态 ==================== -->
    <template v-else-if="error">
      <div class="error-card">
        <el-result icon="error" title="数据加载失败" :sub-title="error">
          <template #extra>
            <el-button type="primary" @click="fetchDashboard">重新加载</el-button>
          </template>
        </el-result>
      </div>
    </template>

    <!-- ==================== 正常内容 ==================== -->
    <template v-else-if="dashboard">
      <!-- ---- 统计卡片行 ---- -->
      <div class="stat-cards">
        <div
          v-for="card in statCards"
          :key="card.label"
          class="stat-card"
          :class="[`stat-card--${card.theme}`]"
        >
          <div class="stat-card-icon">
            <el-icon :size="18"><component :is="card.icon" /></el-icon>
          </div>
          <div class="stat-card-body">
            <div class="stat-label">{{ card.label }}</div>
            <div class="stat-value" :style="{ color: card.color }">{{ card.value }}</div>
            <div class="stat-sub">{{ card.sub }}</div>
          </div>
          <div class="stat-card-glow" :style="{ background: card.glow }" />
        </div>
      </div>

      <!-- ---- 营养摄入进度 ---- -->
      <div class="section-card">
        <div class="section-header">
          <span class="section-title">今日营养摄入</span>
          <span class="section-badge">宏量营养素</span>
        </div>
        <div class="section-body">
          <template v-if="nutritionBars.length">
            <div v-for="bar in nutritionBars" :key="bar.name" class="nutrition-row">
              <div class="nutrition-icon" :style="{ color: bar.color }">
                <el-icon :size="16"><component :is="bar.icon" /></el-icon>
              </div>
              <span class="nutrition-label">{{ bar.name }}</span>
              <div class="nutrition-progress-wrap">
                <div class="nutrition-progress-bar">
                  <div
                    class="nutrition-progress-fill"
                    :class="{ 'nutrition-progress-fill--breath': bar.percent > 0 && bar.percent < 100 }"
                    :style="{
                      width: bar.percent + '%',
                      background: bar.gradient || bar.color
                    }"
                  />
                </div>
                <span class="nutrition-progress-text">
                  {{ bar.actual }}/{{ bar.target }}{{ bar.unit }}
                </span>
              </div>
              <span class="nutrition-percent" :style="{ color: bar.color }">{{ bar.percent }}%</span>
            </div>
          </template>
          <el-empty v-else description="暂无营养目标数据" :image-size="80" />
        </div>
      </div>

      <!-- ---- 体重趋势图 ---- -->
      <div class="section-card">
        <div class="section-header">
          <span class="section-title">体重趋势 (近30天)</span>
          <span v-if="dashboard.weightTrend?.length" class="section-badge section-badge--blue">
            {{ dashboard.weightTrend.length }} 条记录
          </span>
        </div>
        <div class="section-body">
          <template v-if="weightChartOption">
            <v-chart :option="weightChartOption" style="height: 320px" autoresize />
          </template>
          <el-empty v-else description="暂无体重数据，请先记录体重" :image-size="80" />
        </div>
      </div>

      <!-- ---- 底部双栏：饮食 + 训练 ---- -->
      <el-row :gutter="16" class="section">
        <!-- 今日饮食 -->
        <el-col :span="12">
          <div class="section-card">
            <div class="section-header">
              <span class="section-title">今日饮食</span>
              <span v-if="dashboard.todayDietRecords?.length" class="section-badge">
                {{ dashboard.todayDietRecords.length }} 餐
              </span>
            </div>
            <div class="section-body">
              <template v-if="dashboard.todayDietRecords && dashboard.todayDietRecords.length">
                <div
                  v-for="(record, idx) in dashboard.todayDietRecords"
                  :key="idx"
                  class="diet-item"
                >
                  <el-tag
                    size="small"
                    :type="mealTagType(record.mealType) as any"
                    effect="dark"
                    class="diet-meal-tag"
                  >
                    {{ getMealLabel(record.mealType) }}
                  </el-tag>
                  <span class="diet-name">{{ record.foodName }}</span>
                  <span class="diet-cal">{{ formatNum(record.calories, 0) }} kcal</span>
                </div>
              </template>
              <el-empty v-else description="暂无饮食记录" :image-size="80" />
              <div class="card-action">
                <el-button type="primary" size="small" @click="$router.push('/diet')">
                  <el-icon><Plus /></el-icon> 添加饮食
                </el-button>
              </div>
            </div>
          </div>
        </el-col>

        <!-- 今日训练 -->
        <el-col :span="12">
          <div class="section-card">
            <div class="section-header">
              <span class="section-title">今日训练</span>
              <span
                v-if="dashboard.todayTraining || dashboard.todayTrainingType"
                class="section-badge section-badge--blue"
              >
                已完成
              </span>
              <span
                v-else-if="dashboard.scheduledDayType === 'REST'"
                class="section-badge section-badge--green"
              >
                休息日
              </span>
              <span
                v-else-if="dashboard.scheduledTrainingType"
                class="section-badge section-badge--blue"
              >
                今日计划
              </span>
            </div>
            <div class="section-body">
              <!-- 有训练记录 -->
              <template v-if="dashboard.todayTraining">
                <div class="training-detail">
                  <div class="training-type-row">
                    <el-tag type="primary" size="default" effect="dark">
                      {{ dashboard.todayTraining.trainingType || dashboard.todayTrainingType || '自由训练' }}
                    </el-tag>
                  </div>
                  <div class="training-meta">
                    <template v-if="dashboard.todayTraining.durationMinutes">
                      <span class="training-meta-item">
                        <el-icon><Timer /></el-icon>
                        {{ dashboard.todayTraining.durationMinutes }} 分钟
                      </span>
                    </template>
                    <template v-if="dashboard.todayTraining.note">
                      <span class="training-meta-item training-meta-note">
                        备注：{{ dashboard.todayTraining.note }}
                      </span>
                    </template>
                  </div>
                </div>
              </template>
              <!-- 有训练类型但无记录 -->
              <template v-else-if="dashboard.todayTrainingType">
                <div class="training-detail">
                  <div class="training-type-row">
                    <el-tag type="primary" size="default" effect="dark">
                      {{ dashboard.todayTrainingType }}
                    </el-tag>
                  </div>
                </div>
              </template>
              <!-- 计划中的训练日但未记录 -->
              <template v-else-if="dashboard.scheduledTrainingType">
                <div class="training-detail">
                  <div class="training-type-row">
                    <span class="training-plan-label">今日计划：</span>
                    <el-tag type="warning" size="default" effect="dark">
                      {{ dashboard.scheduledTrainingType }}
                    </el-tag>
                  </div>
                </div>
              </template>
              <!-- 休息日 -->
              <template v-else-if="dashboard.scheduledDayType === 'REST'">
                <div class="training-detail">
                  <div class="training-type-row">
                    <el-tag type="success" size="default" effect="dark">
                      今日休息
                    </el-tag>
                  </div>
                </div>
              </template>
              <!-- 无计划也无记录 -->
              <el-empty v-else description="今日暂无训练" :image-size="80" />
              <div class="card-action">
                <el-button type="primary" size="small" @click="$router.push('/training')">
                  <el-icon><Plus /></el-icon> 开始训练
                </el-button>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </template>
  </div>
</template>

<script setup lang="ts">
/**
 * 首页看板组件 —— Dark Iron 暗黑健身美学
 * 聚合展示用户今日体重变化、热量摄入、营养进度、体重趋势图、
 * 今日饮食记录、今日训练等核心数据
 *
 * 设计理念：黄昏健身房的暗黑美学，琥珀金为主强调色，冷蓝用于数据
 */
import { ref, computed, onMounted, shallowRef } from 'vue'
import { useRouter } from 'vue-router'
import {
  Plus,
  Timer,
  Apple,
  Grape,
  Chicken,
  Coffee,
  ScaleToOriginal,
  TrendCharts,
  Trophy,
  Calendar
} from '@element-plus/icons-vue'
import { getDashboard } from '@/api/dashboard'
import { useUserStore } from '@/stores/user'

// 注册 ECharts 全部模块（确保 vue-echarts 可正常渲染）
import 'echarts'
import VChart from 'vue-echarts'

const router = useRouter()
const userStore = useUserStore()

/** 加载中状态 */
const loading = ref(true)
/** 错误信息，为空表示无错误 */
const error = ref('')
/** 看板数据 */
const dashboard = ref<DashboardData | null>(null)

// ==================== 类型定义 ====================

/** 体重趋势数据项 */
interface WeightTrendItem {
  date: string
  weight: number
  bmi?: number
}

/** 饮食记录数据项 */
interface DietRecordItem {
  id?: number
  mealType: string
  foodName: string
  calories: number
  carbGrams?: number
  proteinGrams?: number
  fatGrams?: number
  imageUrl?: string
}

/** 训练记录数据项 */
interface TrainingItem {
  id?: number
  trainingType?: string
  durationMinutes?: number
  caloriesBurned?: number
  note?: string
}

/** 看板聚合数据 */
interface DashboardData {
  todayWeight: number | null
  yesterdayWeight: number | null
  weightChange: number | null
  targetCalories: number | null
  actualCalories: number | null
  targetCarb: number | null
  actualCarb: number | null
  targetProtein: number | null
  actualProtein: number | null
  targetFat: number | null
  actualFat: number | null
  waterTotal: number
  waterTarget: number
  streakDays: number
  todayTrainingType: string | null
  scheduledTrainingType: string | null
  scheduledDayType: string | null
  scheduledDayOrder: number | null
  totalPlanDays: number | null
  weightTrend: WeightTrendItem[]
  todayDietRecords: DietRecordItem[]
  todayTraining: TrainingItem | null
}

// ==================== 数据获取 ====================

/** 获取首页看板数据 */
async function fetchDashboard() {
  loading.value = true
  error.value = ''
  try {
    const res = await getDashboard() as any
    dashboard.value = res as DashboardData
  } catch (err: any) {
    error.value = err.message || '数据加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

// ==================== 工具函数 ====================

/** 格式化数字，保留指定小数位，null/undefined 返回 '--' */
function formatNum(val: number | null | undefined, decimals = 1): string {
  if (val == null) return '--'
  return Number(val).toFixed(decimals)
}

/** 格式化体重变化值，带正负号 */
function formatChange(change: number | null | undefined): string {
  if (change == null) return '--'
  const sign = change > 0 ? '+' : ''
  return `${sign}${change.toFixed(1)}`
}

/** 将后端餐食类型映射为中文 */
function getMealLabel(type: string): string {
  const map: Record<string, string> = {
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
    breakfast: 'success',
    lunch: 'warning',
    dinner: 'danger',
    snack: 'info'
  }
  return map[type] || 'info'
}

// ==================== 统计卡片 ====================

/** Dark Iron 主题色常量 */
const CARD_THEMES = {
  weight: {
    color: '#f0a500',
    glow: 'rgba(240, 165, 0, 0.06)',
    icon: shallowRef(ScaleToOriginal)
  },
  calories: {
    color: '#f0a500',
    glow: 'rgba(240, 165, 0, 0.06)',
    icon: shallowRef(Apple)
  },
  training: {
    color: '#4da6ff',
    glow: 'rgba(77, 166, 255, 0.06)',
    icon: shallowRef(Trophy)
  },
  streak: {
    color: '#a371f7',
    glow: 'rgba(163, 113, 247, 0.06)',
    icon: shallowRef(Calendar)
  }
}

/** 顶部4个统计卡片数据 */
const statCards = computed(() => {
  if (!dashboard.value) return []
  const d = dashboard.value

  return [
    {
      label: '今日体重',
      value: d.todayWeight != null ? `${formatNum(d.todayWeight)} kg` : '-- kg',
      sub: d.weightChange != null
        ? `较昨日 ${formatChange(d.weightChange)} kg`
        : (d.todayWeight != null ? '暂无昨日数据' : '暂无数据'),
      color: d.weightChange != null && d.weightChange > 0 ? '#f85149' : '#3fb950',
      theme: 'weight',
      icon: CARD_THEMES.weight.icon,
      glow: CARD_THEMES.weight.glow
    },
    {
      label: '今日热量',
      value: d.targetCalories != null
        ? `${formatNum(d.actualCalories, 0)}/${formatNum(d.targetCalories, 0)}`
        : (d.actualCalories != null ? `${formatNum(d.actualCalories, 0)}` : '--'),
      sub: 'kcal',
      color: d.actualCalories != null && d.targetCalories != null && d.actualCalories > d.targetCalories
        ? '#f85149'
        : '#f0a500',
      theme: 'calories',
      icon: CARD_THEMES.calories.icon,
      glow: CARD_THEMES.calories.glow
    },
    {
      label: '今日训练',
      value: d.todayTrainingType || d.scheduledTrainingType
        || (d.scheduledDayType === 'REST' ? '休息日' : '暂无计划'),
      sub: d.todayTraining?.durationMinutes
        ? `${d.todayTraining.durationMinutes} 分钟`
        : (d.scheduledTrainingType ? '计划训练' : (d.todayTrainingType ? '已完成' : '')),
      color: d.todayTrainingType || d.scheduledTrainingType ? '#4da6ff' : '#5c6068',
      theme: 'training',
      icon: CARD_THEMES.training.icon,
      glow: CARD_THEMES.training.glow
    },
    {
      label: '连续打卡',
      value: (d.streakDays ?? 0) > 0 ? `${d.streakDays} 天` : '-- 天',
      sub: (d.streakDays ?? 0) > 0 ? (d.streakDays! >= 7 ? '太厉害了！' : '继续加油！') : '今天开始锻炼吧',
      color: (d.streakDays ?? 0) >= 7 ? '#f0a500' : (d.streakDays ?? 0) > 0 ? '#a371f7' : '#5c6068',
      theme: 'streak',
      icon: CARD_THEMES.streak.icon,
      glow: CARD_THEMES.streak.glow
    }
  ]
})

// ==================== 营养进度条 ====================

/** 营养图标映射 */
const nutritionIcons: Record<string, any> = {
  '碳水': shallowRef(Apple),
  '蛋白质': shallowRef(Chicken),
  '脂肪': shallowRef(Grape),
  '饮水': shallowRef(Coffee)
}

/** 四大营养进度条：碳水、蛋白质、脂肪、饮水 */
const nutritionBars = computed(() => {
  if (!dashboard.value) return []
  const d = dashboard.value
  const bars: NutritionBar[] = []

  // 碳水 —— 琥珀金
  if (d.targetCarb != null && d.targetCarb > 0) {
    bars.push({
      name: '碳水',
      actual: formatNum(d.actualCarb, 0),
      target: formatNum(d.targetCarb, 0),
      unit: 'g',
      percent: Math.min(Math.round(((d.actualCarb ?? 0) / d.targetCarb) * 100), 100),
      color: '#f0a500',
      gradient: 'linear-gradient(90deg, #f0a500, #d48900)',
      icon: nutritionIcons['碳水']
    })
  }

  // 蛋白质 —— 紫色
  if (d.targetProtein != null && d.targetProtein > 0) {
    bars.push({
      name: '蛋白质',
      actual: formatNum(d.actualProtein, 0),
      target: formatNum(d.targetProtein, 0),
      unit: 'g',
      percent: Math.min(Math.round(((d.actualProtein ?? 0) / d.targetProtein) * 100), 100),
      color: '#a371f7',
      gradient: 'linear-gradient(90deg, #a371f7, #8b5cf6)',
      icon: nutritionIcons['蛋白质']
    })
  }

  // 脂肪 —— 冷蓝
  if (d.targetFat != null && d.targetFat > 0) {
    bars.push({
      name: '脂肪',
      actual: formatNum(d.actualFat, 0),
      target: formatNum(d.targetFat, 0),
      unit: 'g',
      percent: Math.min(Math.round(((d.actualFat ?? 0) / d.targetFat) * 100), 100),
      color: '#4da6ff',
      gradient: 'linear-gradient(90deg, #4da6ff, #2e7fd9)',
      icon: nutritionIcons['脂肪']
    })
  }

  // 饮水 —— 青色（始终显示，默认目标 2000ml）
  bars.push({
    name: '饮水',
    actual: String(d.waterTotal ?? 0),
    target: String(d.waterTarget ?? 2000),
    unit: 'ml',
    percent: (d.waterTarget ?? 2000) > 0
      ? Math.min(Math.round(((d.waterTotal ?? 0) / (d.waterTarget ?? 2000)) * 100), 100)
      : 0,
    color: '#39d1d9',
    gradient: 'linear-gradient(90deg, #39d1d9, #2ab8c0)',
    icon: nutritionIcons['饮水']
  })

  return bars
})

/** 营养进度条数据项 */
interface NutritionBar {
  name: string
  actual: string
  target: string
  unit: string
  percent: number
  color: string
  gradient: string
  icon: any
}

// ==================== 体重趋势图 ====================

/** Dark Iron 主题 ECharts 颜色常量 */
const chartColors = {
  bg: '#1a1d23',
  line: '#f0a500',
  fill: 'rgba(240, 165, 0, 0.12)',
  grid: 'rgba(255, 255, 255, 0.05)',
  text: '#9ca0a8',
  average: '#4da6ff',
  point: '#f0a500'
}

/** ECharts 体重趋势折线图配置 —— Dark Iron 暗黑主题 */
const weightChartOption = computed(() => {
  if (!dashboard.value?.weightTrend?.length) return null

  const trend = dashboard.value.weightTrend

  // 解析日期数组，展示为 M/D 格式
  const dates = trend.map((t) => {
    if (!t.date) return ''
    const d = new Date(t.date)
    if (isNaN(d.getTime())) return String(t.date)
    return `${d.getMonth() + 1}/${d.getDate()}`
  })

  const weights = trend.map((t) => t.weight)
  const weightMin = Math.min(...weights)
  const weightMax = Math.max(...weights)
  const padding = Math.max((weightMax - weightMin) * 0.2, 1)

  return {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis' as const,
      backgroundColor: '#252830',
      borderColor: '#333840',
      borderWidth: 1,
      textStyle: { color: '#e8e8ed', fontSize: 13 },
      axisPointer: {
        type: 'line' as const,
        lineStyle: { color: '#333840', type: 'dashed' as const }
      },
      formatter: (params: any) => {
        const p = Array.isArray(params) ? params[0] : params
        if (!p) return ''
        return `<div style="font-size:13px;color:#9ca0a8">${p.axisValue}</div>
          <div style="font-weight:700;margin-top:6px;color:#f0a500;font-size:16px;font-family:'JetBrains Mono',monospace">${p.value} kg</div>`
      }
    },
    legend: {
      data: ['体重', '平均值'],
      bottom: 0,
      textStyle: { color: '#9ca0a8', fontSize: 11 },
      itemWidth: 12,
      itemHeight: 2
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      top: '6%',
      containLabel: true,
      borderColor: 'rgba(255,255,255,0.05)'
    },
    xAxis: {
      type: 'category' as const,
      data: dates,
      boundaryGap: false,
      axisLabel: {
        color: '#5c6068',
        fontSize: 10
      },
      axisLine: { lineStyle: { color: '#333840' } },
      axisTick: { show: false },
      splitLine: { show: false }
    },
    yAxis: {
      type: 'value' as const,
      name: 'kg',
      nameTextStyle: { color: '#5c6068', fontSize: 11 },
      min: Math.floor(weightMin - padding * 10) / 10,
      max: Math.ceil(weightMax + padding * 10) / 10,
      interval: Math.max(Math.round((weightMax - weightMin) / 5 * 10) / 10, 0.5),
      axisLabel: {
        color: '#5c6068',
        fontSize: 10
      },
      splitLine: {
        lineStyle: { color: 'rgba(255, 255, 255, 0.05)', type: 'dashed' as const }
      }
    },
    series: [
      {
        name: '体重',
        data: weights,
        type: 'line' as const,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        showSymbol: false,
        emphasis: {
          focus: 'series' as const,
          itemStyle: { borderWidth: 3 }
        },
        lineStyle: { color: '#f0a500', width: 2.5 },
        itemStyle: {
          color: '#f0a500',
          borderColor: '#1a1d23',
          borderWidth: 2
        },
        areaStyle: {
          color: {
            type: 'linear' as const,
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(240, 165, 0, 0.2)' },
              { offset: 0.5, color: 'rgba(240, 165, 0, 0.06)' },
              { offset: 1, color: 'rgba(240, 165, 0, 0.01)' }
            ]
          }
        },
        markLine: {
          silent: true,
          symbol: 'none',
          data: [
            {
              type: 'average' as const,
              name: '平均值',
              lineStyle: { color: '#4da6ff', type: 'dashed' as const, width: 1.5 },
              label: {
                color: '#4da6ff',
                fontSize: 10,
                formatter: '均值 {c} kg',
                position: 'insideEndTop' as const
              }
            }
          ]
        }
      }
    ]
  }
})

// ==================== 生命周期 ====================

onMounted(() => {
  fetchDashboard()
})
</script>

<style scoped lang="scss">
/**
 * 首页看板样式 —— Dark Iron 暗黑健身美学
 * 设计理念：
 *   - 深炭灰底色营造黄昏健身房氛围
 *   - 琥珀金 (#f0a500) 作为主强调色，用于热量/体重
 *   - 冷蓝 (#4da6ff) 用于数据和图表
 *   - 数字使用等宽字体增强数据感
 *   - 微动效：呼吸发光、卡片悬停抬升、交错加载
 */

// ==================== 容器 ====================

.dashboard {
  padding: 4px;
  animation: dashboard-fade-in 0.5s ease both;
}

@keyframes dashboard-fade-in {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// ==================== 欢迎栏 ====================

.welcome-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  margin-bottom: 20px;
  background: linear-gradient(135deg, rgba(240, 165, 0, 0.06) 0%, rgba(240, 165, 0, 0.01) 100%);
  border: 1px solid #333840;
  border-radius: 12px;
  border-bottom: 2px solid rgba(240, 165, 0, 0.3);
  position: relative;
  overflow: hidden;

  // 底部琥珀光泽线
  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 20%;
    right: 20%;
    height: 1px;
    background: linear-gradient(90deg, transparent, rgba(240, 165, 0, 0.4), transparent);
  }
}

.welcome-info {
  .welcome-title {
    margin: 0 0 6px 0;
    font-size: 22px;
    font-weight: 700;
    color: #e8e8ed;
    letter-spacing: -0.3px;
  }

  .welcome-greeting {
    color: #9ca0a8;
    font-weight: 400;
  }

  .welcome-name {
    color: #f0a500;
  }

  .welcome-sub {
    margin: 0;
    font-size: 13px;
    color: #9ca0a8;
    line-height: 1.6;
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 2px 6px;
  }

  .welcome-target {
    color: #e8e8ed;
    font-weight: 500;
  }

  .welcome-dot {
    display: inline-block;
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: #3fb950;
    margin-right: 4px;
    box-shadow: 0 0 6px rgba(63, 185, 80, 0.4);
  }

  .welcome-sep {
    color: #5c6068;
    margin: 0 4px;
  }

  .welcome-macro {
    color: #9ca0a8;
    font-size: 12px;
    background: rgba(255, 255, 255, 0.04);
    padding: 2px 8px;
    border-radius: 4px;
  }

  .welcome-hint {
    opacity: 0.6;
    font-style: italic;
    color: #5c6068;
  }
}

.welcome-action {
  flex-shrink: 0;

  .welcome-tag {
    background: rgba(63, 185, 80, 0.12) !important;
    border: 1px solid rgba(63, 185, 80, 0.25) !important;
    color: #3fb950 !important;
    font-weight: 500;
  }
}

// ==================== 统计卡片 ====================

.stat-cards {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  flex: 1;
  background: linear-gradient(180deg, #252830 0%, #1e2128 100%);
  border: 1px solid #333840;
  border-radius: 12px;
  padding: 18px 16px;
  position: relative;
  overflow: hidden;
  cursor: default;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    border-color: rgba(240, 165, 0, 0.4);
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.3);

    .stat-card-glow {
      opacity: 1;
    }

    .stat-card-icon {
      transform: scale(1.1);
      opacity: 1;
    }
  }
}

.stat-card-icon {
  position: absolute;
  top: 14px;
  right: 14px;
  color: #5c6068;
  opacity: 0.5;
  transition: all 0.3s ease;
}

.stat-card-body {
  position: relative;
  z-index: 1;
}

.stat-card-glow {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  border-radius: 50%;
  opacity: 0;
  transition: opacity 0.4s ease;
  pointer-events: none;
  filter: blur(40px);
}

.stat-card--weight {
  .stat-card-icon { color: #f0a500; opacity: 0.6; }
}
.stat-card--calories {
  .stat-card-icon { color: #f0a500; opacity: 0.6; }
}
.stat-card--training {
  .stat-card-icon { color: #4da6ff; opacity: 0.6; }
}
.stat-card--streak {
  .stat-card-icon { color: #a371f7; opacity: 0.6; }
}

// 骨架卡片
.stat-card-skeleton {
  flex: 1;
  background: linear-gradient(180deg, #252830 0%, #1e2128 100%);
  border: 1px solid #333840;
  border-radius: 12px;
  padding: 18px 16px;

  .sk-inner {
    display: flex;
    flex-direction: column;
    align-items: center;
  }
}

.stat-label {
  font-size: 12px;
  color: #9ca0a8;
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  font-weight: 500;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 6px;
  line-height: 1.2;
  font-family: 'JetBrains Mono', 'Fira Code', 'Consolas', monospace;
  letter-spacing: -0.5px;
}

.stat-sub {
  font-size: 12px;
  color: #5c6068;
  font-weight: 400;
}

// ==================== 通用分区卡片 ====================

.section {
  margin-bottom: 20px;
}

.section-card {
  background: linear-gradient(180deg, #252830 0%, #1e2128 100%);
  border: 1px solid #333840;
  border-radius: 12px;
  margin-bottom: 20px;
  overflow: hidden;
  transition: all 0.3s ease;

  &:hover {
    border-color: rgba(240, 165, 0, 0.25);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.25);
  }
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 20px;
  border-bottom: 1px solid #333840;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #e8e8ed;
}

.section-badge {
  font-size: 11px;
  color: #f0a500;
  background: rgba(240, 165, 0, 0.1);
  padding: 3px 10px;
  border-radius: 20px;
  font-weight: 500;
  letter-spacing: 0.3px;

  &--blue {
    color: #4da6ff;
    background: rgba(77, 166, 255, 0.1);
  }

  &--green {
    color: #3fb950;
    background: rgba(63, 185, 80, 0.1);
  }
}

.section-body {
  padding: 20px;
}

// ==================== 营养进度条 ====================

.nutrition-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 6px 0;

  &:not(:last-child) {
    margin-bottom: 14px;
  }
}

.nutrition-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.04);
  border-radius: 8px;
  flex-shrink: 0;
}

.nutrition-label {
  width: 50px;
  font-size: 13px;
  font-weight: 500;
  color: #9ca0a8;
  flex-shrink: 0;
}

.nutrition-progress-wrap {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nutrition-progress-bar {
  height: 10px;
  background: #1e2128;
  border-radius: 10px;
  overflow: hidden;
  position: relative;
}

.nutrition-progress-fill {
  height: 100%;
  border-radius: 10px;
  transition: width 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;

  // 微光扫过效果
  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(
      90deg,
      transparent 0%,
      rgba(255, 255, 255, 0.15) 50%,
      transparent 100%
    );
    animation: nutrition-shimmer 2.5s ease-in-out infinite;
  }

  // 呼吸动画 (未满时)
  &--breath {
    animation: nutrition-breath 3s ease-in-out infinite;
  }
}

@keyframes nutrition-shimmer {
  0%, 100% { transform: translateX(-100%); }
  50% { transform: translateX(100%); }
}

@keyframes nutrition-breath {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.85; }
}

.nutrition-progress-text {
  font-size: 11px;
  color: #5c6068;
  font-family: 'JetBrains Mono', 'Fira Code', 'Consolas', monospace;
}

.nutrition-percent {
  width: 42px;
  font-size: 14px;
  font-weight: 700;
  text-align: right;
  flex-shrink: 0;
  font-family: 'JetBrains Mono', 'Fira Code', 'Consolas', monospace;
}

// ==================== 饮食列表 ====================

.diet-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;

  &:not(:last-child) {
    border-bottom: 1px solid rgba(255, 255, 255, 0.04);
  }
}

.diet-meal-tag {
  flex-shrink: 0;
}

.diet-name {
  flex: 1;
  font-size: 13px;
  color: #e8e8ed;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.diet-cal {
  font-size: 12px;
  color: #f0a500;
  flex-shrink: 0;
  font-family: 'JetBrains Mono', 'Fira Code', 'Consolas', monospace;
  font-weight: 500;
}

// ==================== 训练详情 ====================

.training-detail {
  padding: 4px 0;
}

.training-type-row {
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.training-plan-label {
  font-size: 13px;
  color: #9ca0a8;
}

.training-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 13px;
  color: #9ca0a8;
}

.training-meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #9ca0a8;

  .el-icon {
    color: #4da6ff;
  }
}

.training-meta-note {
  color: #5c6068;
  font-size: 12px;
  font-style: italic;
}

// ==================== 卡片底部操作按钮 ====================

.card-action {
  margin-top: 16px;
  text-align: center;
  padding-top: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.04);
}

// ==================== 错误卡片 ====================

.error-card {
  background: linear-gradient(180deg, #252830 0%, #1e2128 100%);
  border: 1px solid #333840;
  border-radius: 12px;
  padding: 40px 20px;
  text-align: center;
}

// ==================== 响应式 ====================

@media (max-width: 768px) {
  .stat-cards {
    flex-wrap: wrap;
  }

  .stat-card {
    flex: 1 1 calc(50% - 8px);
    min-width: 140px;
  }

  .welcome-bar {
    flex-direction: column;
    gap: 12px;
    text-align: center;
    padding: 16px;
  }

  .welcome-sub {
    justify-content: center;
  }
}
</style>
