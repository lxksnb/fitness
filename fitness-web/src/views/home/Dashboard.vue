<template>
  <!-- 首页看板：聚合展示体重、热量、训练、营养进度、趋势图等关键数据 -->
  <div class="dashboard">
    <!-- ==================== 欢迎栏 ==================== -->
    <div class="welcome-bar">
      <div class="welcome-info">
        <h2>欢迎，{{ userStore.nickname }}</h2>
        <p v-if="dashboard?.targetCalories" class="welcome-sub">
          今日目标热量 {{ dashboard.targetCalories }}kcal
          <template v-if="dashboard.targetCarb"> · 碳水 {{ formatNum(dashboard.targetCarb, 0) }}g</template>
          <template v-if="dashboard.targetProtein"> · 蛋白质 {{ formatNum(dashboard.targetProtein, 0) }}g</template>
          <template v-if="dashboard.targetFat"> · 脂肪 {{ formatNum(dashboard.targetFat, 0) }}g</template>
        </p>
        <p v-else class="welcome-sub welcome-hint">暂未设置目标计划，前往设置以获取个性化营养建议</p>
      </div>
      <el-tag v-if="dashboard?.targetCalories" type="success" size="large" round>目标计划进行中</el-tag>
      <el-button v-else type="primary" size="small" @click="$router.push('/plans')">设置目标</el-button>
    </div>

    <!-- ==================== 加载骨架 ==================== -->
    <template v-if="loading">
      <!-- 统计卡片骨架 -->
      <div class="stat-cards">
        <el-card v-for="i in 4" :key="'sk-card-' + i" shadow="hover" class="stat-card-item">
          <el-skeleton :rows="3" animated />
        </el-card>
      </div>
      <!-- 营养进度骨架 -->
      <el-card class="section" header="今日营养摄入">
        <el-skeleton :rows="4" animated />
      </el-card>
      <!-- 趋势图骨架 -->
      <el-card class="section" header="体重趋势 (近30天)">
        <el-skeleton :rows="5" animated />
      </el-card>
      <!-- 底部双栏骨架 -->
      <el-row :gutter="16" class="section">
        <el-col :span="12">
          <el-card header="今日饮食"><el-skeleton :rows="3" animated /></el-card>
        </el-col>
        <el-col :span="12">
          <el-card header="今日训练"><el-skeleton :rows="3" animated /></el-card>
        </el-col>
      </el-row>
    </template>

    <!-- ==================== 错误状态 ==================== -->
    <template v-else-if="error">
      <el-result icon="error" title="数据加载失败" :sub-title="error">
        <template #extra>
          <el-button type="primary" @click="fetchDashboard">重新加载</el-button>
        </template>
      </el-result>
    </template>

    <!-- ==================== 正常内容 ==================== -->
    <template v-else-if="dashboard">
      <!-- ---- 统计卡片行 ---- -->
      <div class="stat-cards">
        <el-card
          v-for="card in statCards"
          :key="card.label"
          shadow="hover"
          class="stat-card-item"
        >
          <div class="stat-label">{{ card.label }}</div>
          <div class="stat-value" :style="{ color: card.color }">{{ card.value }}</div>
          <div class="stat-sub">{{ card.sub }}</div>
        </el-card>
      </div>

      <!-- ---- 营养摄入进度 ---- -->
      <el-card header="今日营养摄入" class="section">
        <template v-if="nutritionBars.length">
          <div v-for="bar in nutritionBars" :key="bar.name" class="nutrition-row">
            <span class="nutrition-label">{{ bar.name }}</span>
            <el-progress
              :percentage="bar.percent"
              :color="bar.color"
              :stroke-width="18"
              class="nutrition-progress"
            >
              <span class="progress-text">{{ bar.actual }}/{{ bar.target }}{{ bar.unit }}</span>
            </el-progress>
            <span class="nutrition-percent">{{ bar.percent }}%</span>
          </div>
        </template>
        <el-empty v-else description="暂无营养目标数据" :image-size="80" />
      </el-card>

      <!-- ---- 体重趋势图 ---- -->
      <el-card header="体重趋势 (近30天)" class="section">
        <template v-if="weightChartOption">
          <v-chart :option="weightChartOption" style="height: 320px" autoresize />
        </template>
        <el-empty v-else description="暂无体重数据，快去记录吧" :image-size="120">
          <el-button type="primary" size="small" @click="$router.push('/weight')">记录体重</el-button>
        </el-empty>
      </el-card>

      <!-- ---- 底部双栏：饮食 + 训练 ---- -->
      <el-row :gutter="16" class="section">
        <!-- 今日饮食 -->
        <el-col :span="12">
          <el-card header="今日饮食">
            <template v-if="dashboard.todayDietRecords && dashboard.todayDietRecords.length">
              <div
                v-for="(record, idx) in dashboard.todayDietRecords"
                :key="idx"
                class="diet-item"
              >
                <el-tag size="small" :type="mealTagType(record.mealType)">
                  {{ getMealLabel(record.mealType) }}
                </el-tag>
                <span class="diet-name">{{ record.foodName }}</span>
                <span class="diet-cal">{{ formatNum(record.calories, 0) }}kcal</span>
              </div>
            </template>
            <el-empty v-else description="暂无饮食记录" :image-size="80" />
            <div class="card-action">
              <el-button type="primary" size="small" @click="$router.push('/diet')">
                <el-icon><Plus /></el-icon> 添加饮食
              </el-button>
            </div>
          </el-card>
        </el-col>

        <!-- 今日训练 -->
        <el-col :span="12">
          <el-card header="今日训练">
            <template v-if="dashboard.todayTraining">
              <div class="training-detail">
                <div class="training-type-row">
                  <el-tag type="primary" size="default">
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
                    <span class="training-meta-item">
                      备注：{{ dashboard.todayTraining.note }}
                    </span>
                  </template>
                </div>
              </div>
            </template>
            <el-empty v-else description="今日暂无训练" :image-size="80" />
            <div class="card-action">
              <el-button type="primary" size="small" @click="$router.push('/training')">
                <el-icon><Plus /></el-icon> 开始训练
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </template>
  </div>
</template>

<script setup lang="ts">
/**
 * 首页看板组件
 * 聚合展示用户今日体重变化、热量摄入、营养进度、体重趋势图、
 * 今日饮食记录、今日训练等核心数据
 */
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Timer } from '@element-plus/icons-vue'
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
  bmi: number
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
function mealTagType(type: string): 'success' | 'warning' | 'danger' | 'info' | '' {
  const map: Record<string, string> = {
    breakfast: 'success',
    lunch: 'warning',
    dinner: 'danger',
    snack: ''
  }
  return (map[type] as any) || ''
}

// ==================== 统计卡片 ====================

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
      color: d.weightChange != null && d.weightChange > 0 ? '#f56c6c' : '#67c23a'
    },
    {
      label: '今日热量',
      value: d.targetCalories != null
        ? `${formatNum(d.actualCalories, 0)}/${formatNum(d.targetCalories, 0)}`
        : (d.actualCalories != null ? `${formatNum(d.actualCalories, 0)}` : '--'),
      sub: 'kcal',
      color: d.actualCalories != null && d.targetCalories != null && d.actualCalories > d.targetCalories
        ? '#f56c6c'
        : '#e6a23c'
    },
    {
      label: '今日训练',
      value: d.todayTrainingType || (d.todayTraining ? '已完成' : '暂无'),
      sub: d.todayTraining?.durationMinutes
        ? `${d.todayTraining.durationMinutes} 分钟`
        : (d.todayTrainingType ? '查看详情' : '未训练'),
      color: d.todayTrainingType ? '#409eff' : '#909399'
    },
    {
      label: '连续打卡',
      value: (d.streakDays ?? 0) > 0 ? `${d.streakDays} 天` : '-- 天',
      sub: (d.streakDays ?? 0) > 0 ? (d.streakDays! >= 7 ? '太厉害了！' : '继续加油！') : '今天开始锻炼吧',
      color: (d.streakDays ?? 0) >= 7 ? '#f56c6c' : (d.streakDays ?? 0) > 0 ? '#e6a23c' : '#909399'
    }
  ]
})

// ==================== 营养进度条 ====================

/** 四大营养进度条：碳水、蛋白质、脂肪、饮水 */
const nutritionBars = computed(() => {
  if (!dashboard.value) return []
  const d = dashboard.value
  const bars: NutritionBar[] = []

  // 碳水
  if (d.targetCarb != null && d.targetCarb > 0) {
    bars.push({
      name: '碳水',
      actual: formatNum(d.actualCarb, 0),
      target: formatNum(d.targetCarb, 0),
      unit: 'g',
      percent: Math.min(Math.round(((d.actualCarb ?? 0) / d.targetCarb) * 100), 100),
      color: '#409eff'
    })
  }

  // 蛋白质
  if (d.targetProtein != null && d.targetProtein > 0) {
    bars.push({
      name: '蛋白质',
      actual: formatNum(d.actualProtein, 0),
      target: formatNum(d.targetProtein, 0),
      unit: 'g',
      percent: Math.min(Math.round(((d.actualProtein ?? 0) / d.targetProtein) * 100), 100),
      color: '#67c23a'
    })
  }

  // 脂肪
  if (d.targetFat != null && d.targetFat > 0) {
    bars.push({
      name: '脂肪',
      actual: formatNum(d.actualFat, 0),
      target: formatNum(d.targetFat, 0),
      unit: 'g',
      percent: Math.min(Math.round(((d.actualFat ?? 0) / d.targetFat) * 100), 100),
      color: '#e6a23c'
    })
  }

  // 饮水（始终显示，默认目标 2000ml）
  bars.push({
    name: '饮水',
    actual: String(d.waterTotal ?? 0),
    target: String(d.waterTarget ?? 2000),
    unit: 'ml',
    percent: (d.waterTarget ?? 2000) > 0
      ? Math.min(Math.round(((d.waterTotal ?? 0) / (d.waterTarget ?? 2000)) * 100), 100)
      : 0,
    color: '#06b4d8'
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
}

// ==================== 体重趋势图 ====================

/** ECharts 体重趋势折线图配置 */
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
    tooltip: {
      trigger: 'axis' as const,
      backgroundColor: '#fff',
      borderColor: '#e4e7ed',
      textStyle: { color: '#303133' },
      formatter: (params: any) => {
        const p = Array.isArray(params) ? params[0] : params
        if (!p) return ''
        return `<div style="font-size:13px">${p.axisValue}</div>
          <div style="font-weight:600;margin-top:4px">体重 ${p.value} kg</div>`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '8%',
      containLabel: true
    },
    xAxis: {
      type: 'category' as const,
      data: dates,
      boundaryGap: false,
      axisLabel: {
        color: '#909399',
        fontSize: 11
      },
      axisLine: { lineStyle: { color: '#dcdfe6' } },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value' as const,
      name: 'kg',
      nameTextStyle: { color: '#909399', fontSize: 11 },
      min: Math.floor(weightMin - padding * 10) / 10,
      max: Math.ceil(weightMax + padding * 10) / 10,
      interval: Math.max(Math.round((weightMax - weightMin) / 5 * 10) / 10, 0.5),
      axisLabel: {
        color: '#909399',
        fontSize: 11
      },
      splitLine: { lineStyle: { color: '#f2f3f5', type: 'dashed' as const } }
    },
    series: [
      {
        name: '体重',
        data: weights,
        type: 'line' as const,
        smooth: true,
        symbol: 'circle',
        symbolSize: 5,
        lineStyle: { color: '#409eff', width: 2.5 },
        itemStyle: {
          color: '#409eff',
          borderColor: '#fff',
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
              { offset: 0, color: 'rgba(64, 158, 255, 0.25)' },
              { offset: 1, color: 'rgba(64, 158, 255, 0.02)' }
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
              lineStyle: { color: '#e6a23c', type: 'dashed' as const, width: 1.5 },
              label: { color: '#e6a23c', fontSize: 10, formatter: '均值 {c} kg' }
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
 * 首页看板样式
 * 采用卡片式布局，分区展示各维度数据
 */

.dashboard {
  padding: 4px;
}

/* ==================== 欢迎栏 ==================== */
.welcome-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  margin-bottom: 16px;
  background: linear-gradient(135deg, #409eff 0%, #337ecc 100%);
  border-radius: 10px;
  color: #fff;

  .welcome-info {
    h2 {
      margin: 0 0 4px 0;
      font-size: 22px;
      font-weight: 600;
    }

    .welcome-sub {
      margin: 0;
      font-size: 13px;
      opacity: 0.9;
      line-height: 1.6;
    }

    .welcome-hint {
      opacity: 0.75;
      font-style: italic;
    }
  }
}

/* ==================== 统计卡片行 ==================== */
.stat-cards {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;

  .stat-card-item {
    flex: 1;

    :deep(.el-card__body) {
      padding: 18px 16px;
      text-align: center;
    }
  }
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  margin-bottom: 6px;
  line-height: 1.3;
}

.stat-sub {
  font-size: 12px;
  color: #c0c4cc;
}

/* ==================== 通用分区间距 ==================== */
.section {
  margin-bottom: 16px;
}

/* ==================== 营养进度条 ==================== */
.nutrition-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 4px 0;

  &:not(:last-child) {
    margin-bottom: 12px;
  }
}

.nutrition-label {
  width: 50px;
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  flex-shrink: 0;
}

.nutrition-progress {
  flex: 1;

  .progress-text {
    font-size: 12px;
    color: #606266;
  }
}

.nutrition-percent {
  width: 40px;
  font-size: 13px;
  font-weight: 600;
  color: #606266;
  text-align: right;
  flex-shrink: 0;
}

/* ==================== 饮食列表 ==================== */
.diet-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;

  &:not(:last-child) {
    border-bottom: 1px solid #f5f5f5;
  }
}

.diet-name {
  flex: 1;
  font-size: 13px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.diet-cal {
  font-size: 12px;
  color: #909399;
  flex-shrink: 0;
}

/* ==================== 训练详情 ==================== */
.training-detail {
  padding: 4px 0;
}

.training-type-row {
  margin-bottom: 8px;
}

.training-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 13px;
  color: #606266;
}

.training-meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* ==================== 卡片底部操作按钮 ==================== */
.card-action {
  margin-top: 12px;
  text-align: center;
}
</style>
