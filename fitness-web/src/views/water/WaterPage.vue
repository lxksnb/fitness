<template>
  <!-- 饮水记录页面：今日饮水进度环、快捷添加按钮、今日记录列表 -->
  <div class="water-page">
    <!-- ==================== 页面标题 ==================== -->
    <div class="page-header">
      <h2>饮水记录</h2>
    </div>

    <!-- ==================== 加载状态 ==================== -->
    <template v-if="loading">
      <el-card class="section">
        <el-skeleton :rows="4" animated />
      </el-card>
    </template>

    <!-- ==================== 错误状态 ==================== -->
    <template v-else-if="error">
      <el-result icon="error" title="数据加载失败" :sub-title="error">
        <template #extra>
          <el-button type="primary" @click="fetchWaterData">重新加载</el-button>
        </template>
      </el-result>
    </template>

    <!-- ==================== 主内容 ==================== -->
    <template v-else>
      <!-- 今日饮水进度卡片 -->
      <el-card class="section water-progress-card">
        <div class="progress-layout">
          <!-- 圆形进度环 -->
          <div class="progress-ring-wrapper">
            <svg width="200" height="200" viewBox="0 0 200 200">
              <!-- 背景圆环 -->
              <circle cx="100" cy="100" r="85" fill="none" stroke="#e9ecef" stroke-width="18" />
              <!-- 进度圆环 -->
              <circle
                cx="100" cy="100" r="85"
                fill="none"
                :stroke="progressColor"
                stroke-width="18"
                :stroke-dasharray="circumference"
                :stroke-dashoffset="dashOffset"
                stroke-linecap="round"
                transform="rotate(-90 100 100)"
                class="progress-ring"
              />
              <!-- 中心文字 -->
              <text x="100" y="90" text-anchor="middle" font-size="32" font-weight="700" :fill="progressColor">
                {{ currentMl }}
              </text>
              <text x="100" y="115" text-anchor="middle" font-size="14" fill="#909399">
                / {{ targetMl }} ml
              </text>
              <text x="100" y="135" text-anchor="middle" font-size="12" fill="#c0c4cc">
                {{ Math.round(percentage) }}%
              </text>
            </svg>
          </div>

          <!-- 操作区 -->
          <div class="water-actions">
            <div class="date-selector">
              <span class="date-label">日期：</span>
              <el-date-picker
                v-model="selectedDate"
                type="date"
                placeholder="选择日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                @change="fetchWaterData"
                style="width: 160px"
              />
            </div>
            <div class="target-setting">
              <span class="target-label">目标：</span>
              <el-input-number
                v-model="targetMl"
                :min="500"
                :max="5000"
                :step="100"
                size="small"
                controls-position="right"
                style="width: 130px"
                @change="onTargetChange"
              />
              <span class="unit">ml</span>
            </div>

            <!-- 快捷添加按钮 -->
            <div class="quick-add">
              <span class="quick-label">快捷添加：</span>
              <div class="quick-buttons">
                <el-button size="small" round @click="quickAdd(100)">+100ml</el-button>
                <el-button size="small" round @click="quickAdd(200)">+200ml</el-button>
                <el-button size="small" round @click="quickAdd(300)">+300ml</el-button>
                <el-button size="small" round @click="quickAdd(500)">+500ml</el-button>
              </div>
            </div>

            <!-- 自定义水量输入 -->
            <div class="custom-add">
              <span class="custom-label">自定义：</span>
              <el-input-number
                v-model="customAmount"
                :min="50"
                :max="2000"
                :step="50"
                size="small"
                controls-position="right"
                style="width: 120px"
              />
              <span class="unit">ml</span>
              <el-button type="primary" size="small" :loading="adding" @click="quickAdd(customAmount)">
                添加
              </el-button>
            </div>
          </div>
        </div>
      </el-card>

      <!-- ==================== 今日饮水记录列表 ==================== -->
      <el-card class="section">
        <template #header>
          <div class="card-header">
            <span>今日饮水记录</span>
            <span class="card-header-count">共 {{ waterRecords.length }} 杯</span>
          </div>
        </template>
        <el-table v-if="waterRecords.length > 0" :data="waterRecords" stripe border style="width: 100%" size="small">
          <el-table-column prop="recordedAt" label="时间" width="120">
            <template #default="{ row }">
              {{ row.recordedAt || '--' }}
            </template>
          </el-table-column>
          <el-table-column label="水量" width="120" sortable>
            <template #default="{ row }">
              <span class="amount-cell">{{ row.amountMl }} ml</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" fixed="right">
            <template #default="{ row }">
              <el-popconfirm title="确认删除该记录？" @confirm="handleDeleteRecord(row.id!)">
                <template #reference>
                  <el-button type="danger" link size="small">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="今天还没喝水哦，记得补水！" :image-size="100" />
      </el-card>
    </template>
  </div>
</template>

<script setup lang="ts">
/**
 * 饮水记录页面
 * 功能：今日饮水进度圆环（当前/目标 ml）、快捷添加按钮（+100/200/300/500ml）、
 * 自定义水量输入、今日记录列表、删除记录
 */
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import { getTodayWater, getWaterList, saveWater } from '@/api/water'

// ==================== 类型定义 ====================

/** 饮水记录 */
interface WaterRecord {
  id?: number
  recordedAt: string
  amountMl?: number
}

// ==================== 状态 ====================

const loading = ref(true)
const error = ref('')
const adding = ref(false)

/** 当前日期 */
const selectedDate = ref(getTodayStr())
/** 目标水量 (ml) */
const targetMl = ref(2000)
/** 当日已饮水总量 */
const currentMl = ref(0)
/** 自定义输入量 */
const customAmount = ref(200)
/** 今日饮水记录列表 */
const waterRecords = ref<WaterRecord[]>([])

// ==================== SVG 进度环 ====================

const radius = 85
const circumference = 2 * Math.PI * radius

/** 完成百分比 */
const percentage = computed(() => {
  if (targetMl.value <= 0) return 0
  return Math.min((currentMl.value / targetMl.value) * 100, 100)
})

/** 进度条偏移 */
const dashOffset = computed(() => {
  const progress = percentage.value / 100
  return circumference * (1 - progress)
})

/** 进度颜色：根据完成度变化 */
const progressColor = computed(() => {
  if (percentage.value >= 100) return '#67c23a'
  if (percentage.value >= 70) return '#409eff'
  if (percentage.value >= 40) return '#e6a23c'
  return '#f56c6c'
})

// ==================== 工具函数 ====================

function getTodayStr(): string {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

// ==================== 数据获取 ====================

async function fetchWaterData() {
  loading.value = true
  error.value = ''
  try {
    const date = selectedDate.value
    // 并行获取汇总和列表
    const [todayRes, listRes] = await Promise.all([
      getTodayWater(date).catch(() => null),
      getWaterList(date).catch(() => null)
    ]) as any[]

    // 处理汇总数据
    if (todayRes) {
      const summary = todayRes ?? {}
      currentMl.value = summary.totalMl || summary.total || summary.currentMl || 0
      if (summary.targetMl || summary.target) {
        targetMl.value = summary.targetMl || summary.target || 2000
      }
    }

    // 处理列表数据
    if (listRes) {
      const list = Array.isArray(listRes) ? listRes : listRes?.records || listRes?.list || []
      waterRecords.value = list as WaterRecord[]
    } else {
      waterRecords.value = []
    }
  } catch (err: any) {
    error.value = err.message || '数据加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

// ==================== 操作方法 ====================

/** 快捷添加饮水 */
async function quickAdd(amountMl: number) {
  if (amountMl <= 0) return
  adding.value = true
  try {
    await saveWater({
      recordDate: selectedDate.value,
      amountMl
    })
    // 乐观更新
    currentMl.value += amountMl
    const now = new Date()
    const timeStr = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`
    waterRecords.value.unshift({
      id: Date.now(),
      recordedAt: timeStr,
      amountMl
    })
    ElMessage.success(`已添加 ${amountMl}ml 饮水`)
  } catch (err: any) {
    ElMessage.error(err.message || '添加失败，请重试')
    // 失败后重新加载数据
    await fetchWaterData()
  } finally {
    adding.value = false
  }
}

/** 删除饮水记录 */
async function handleDeleteRecord(id: number) {
  try {
    await saveWater({ id, delete: true })
    waterRecords.value = waterRecords.value.filter(r => r.id !== id)
    // 重新计算总量
    currentMl.value = waterRecords.value.reduce((sum, r) => sum + (r.amountMl || 0), 0)
    ElMessage.success('记录已删除')
  } catch (err: any) {
    ElMessage.error(err.message || '删除失败，请重试')
  }
}

/** 目标水量变更 */
function onTargetChange(_val: number | undefined) {
  // 目标变更由用户手动管理，不做持久化（调用时不处理）
}

// ==================== 生命周期 ====================

onMounted(() => {
  fetchWaterData()
})
</script>

<style scoped lang="scss">
/**
 * 饮水记录页面样式
 */

.water-page {
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
}

.section {
  margin-bottom: 16px;
}

/* ==================== 饮水进度卡片 ==================== */
.water-progress-card {
  .progress-layout {
    display: flex;
    align-items: center;
    gap: 40px;
    flex-wrap: wrap;

    .progress-ring-wrapper {
      flex-shrink: 0;

      .progress-ring {
        transition: stroke-dashoffset 0.6s ease, stroke 0.5s ease;
      }
    }

    .water-actions {
      flex: 1;
      min-width: 280px;
      display: flex;
      flex-direction: column;
      gap: 14px;

      .date-selector,
      .target-setting,
      .quick-add,
      .custom-add {
        display: flex;
        align-items: center;
        gap: 8px;
      }

      .date-label,
      .target-label,
      .quick-label,
      .custom-label {
        font-size: 14px;
        color: #606266;
        white-space: nowrap;
        min-width: 60px;
      }

      .unit {
        font-size: 13px;
        color: #909399;
      }

      .quick-buttons {
        display: flex;
        gap: 6px;
        flex-wrap: wrap;
      }
    }
  }
}

/* ==================== 卡片头部 ==================== */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .card-header-count {
    font-size: 13px;
    color: #909399;
  }
}

/* ==================== 水量单元格 ==================== */
.amount-cell {
  font-weight: 500;
  color: #409eff;
}
</style>
