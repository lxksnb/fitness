<template>
  <!-- 体重管理页面：体重记录表格、录入弹窗、体重趋势图 -->
  <div class="weight-page">
    <!-- ==================== 页面标题与操作栏 ==================== -->
    <div class="page-header">
      <h2>体重管理</h2>
      <div class="header-actions">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          :shortcuts="dateShortcuts"
          @change="fetchWeights"
          style="width: 280px"
        />
        <el-button type="primary" @click="openDialog()">
          <el-icon><Plus /></el-icon>
          记录体重
        </el-button>
      </div>
    </div>

    <!-- ==================== 加载骨架 ==================== -->
    <template v-if="loading">
      <el-card class="section">
        <el-skeleton :rows="5" animated />
      </el-card>
    </template>

    <!-- ==================== 错误状态 ==================== -->
    <template v-else-if="error">
      <el-result icon="error" title="数据加载失败" :sub-title="error">
        <template #extra>
          <el-button type="primary" @click="fetchWeights">重新加载</el-button>
        </template>
      </el-result>
    </template>

    <!-- ==================== 体重记录表格 ==================== -->
    <template v-else>
      <el-card class="section">
        <template #header>
          <span>体重记录</span>
        </template>
        <el-table v-if="weightList.length > 0" :data="weightList" stripe border style="width: 100%">
          <el-table-column prop="recordDate" label="日期" width="140" sortable />
          <el-table-column prop="weightKg" label="体重 (kg)" width="140" sortable>
            <template #default="{ row }">
              <span :class="{ 'weight-change': true, up: row.weightChange > 0, down: row.weightChange < 0 }">
                {{ formatNum(row.weightKg) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="bmi" label="BMI" width="120">
            <template #default="{ row }">
              {{ row.bmi ? formatNum(row.bmi) : '--' }}
            </template>
          </el-table-column>
          <el-table-column prop="note" label="备注" min-width="200">
            <template #default="{ row }">
              {{ row.note || '--' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="openDialog(row)">编辑</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="暂无体重记录，点击上方按钮开始记录" :image-size="120" />
      </el-card>

      <!-- ==================== 体重趋势图 ==================== -->
      <el-card header="体重趋势 (近30天)" class="section">
        <template v-if="trendOption">
          <v-chart :option="trendOption" style="height: 360px" autoresize />
        </template>
        <el-empty v-else description="暂无趋势数据" :image-size="100" />
      </el-card>
    </template>

    <!-- ==================== 记录体重弹窗 ==================== -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditing ? '编辑体重记录' : '记录体重'"
      width="480px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="日期" prop="recordDate">
          <el-date-picker
            v-model="form.recordDate"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="体重 (kg)" prop="weightKg">
          <el-input-number
            v-model="form.weightKg"
            :min="20"
            :max="300"
            :precision="1"
            :step="0.1"
            controls-position="right"
            style="width: 100%"
            placeholder="请输入体重"
          />
        </el-form-item>
        <el-form-item label="备注" prop="note">
          <el-input
            v-model="form.note"
            type="textarea"
            :rows="3"
            placeholder="记录心情、饮食、运动等备注信息"
            maxlength="200"
            show-word-limit
          />
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
 * 体重管理页面
 * 支持日期范围筛选体重记录表格、录入/编辑体重、
 * ECharts体重趋势折线图展示
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { getWeights, saveWeight, getWeightTrend } from '@/api/weight'
import VChart from 'vue-echarts'
import 'echarts'

// ==================== 类型定义 ====================

/** 体重记录数据项 */
interface WeightRecord {
  id?: number
  recordDate: string
  weightKg: number
  bmi: number | null
  note?: string
  weightChange?: number
}

/** 趋势数据项 */
interface TrendItem {
  date: string
  weight: number
  bmi: number
}

// ==================== 状态 ====================

const loading = ref(true)
const error = ref('')
const saving = ref(false)
const dialogVisible = ref(false)
const isEditing = ref(false)
const formRef = ref<FormInstance>()
const weightList = ref<WeightRecord[]>([])
const trendOption = ref<any>(null)

/** 日期范围选择器 */
const dateRange = ref<[string, string]>([
  getDefaultStartDate(),
  getTodayStr()
])

/** 日期快捷选项 */
const dateShortcuts = [
  { text: '最近一周', value: () => {
    const end = new Date()
    const start = new Date()
    start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
    return [start, end]
  }},
  { text: '最近一月', value: () => {
    const end = new Date()
    const start = new Date()
    start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
    return [start, end]
  }},
  { text: '最近三月', value: () => {
    const end = new Date()
    const start = new Date()
    start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
    return [start, end]
  }}
]

// ==================== 表单 ====================

/** 表单数据 */
const form = reactive({
  recordDate: getTodayStr(),
  weightKg: 60,
  note: ''
})

/** 表单验证规则 */
const formRules: FormRules = {
  recordDate: [{ required: true, message: '请选择日期', trigger: 'blur' }],
  weightKg: [
    { required: true, message: '请输入体重', trigger: 'blur' },
    { type: 'number', min: 20, max: 300, message: '体重范围 20-300kg', trigger: 'blur' }
  ]
}

// ==================== 工具函数 ====================

/** 获取当日日期字符串 yyyy-MM-dd */
function getTodayStr(): string {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

/** 获取默认起始日期(30天前) */
function getDefaultStartDate(): string {
  const d = new Date()
  d.setTime(d.getTime() - 3600 * 1000 * 24 * 30)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

/** 格式化数字，保留指定位小数 */
function formatNum(val: number | null | undefined, decimals = 1): string {
  if (val == null || isNaN(val)) return '--'
  return Number(val).toFixed(decimals)
}

// ==================== 数据获取 ====================

/** 查询体重记录列表 */
async function fetchWeights() {
  loading.value = true
  error.value = ''
  try {
    const [startDate, endDate] = dateRange.value
    const res = await getWeights(startDate, endDate) as any
    const list = (Array.isArray(res) ? res : res?.records || res?.list || []) as WeightRecord[]

    // 计算每日体重变化
    let prevWeight: number | null = null
    weightList.value = list.map((item: WeightRecord) => {
      const change = prevWeight != null ? item.weightKg - prevWeight : 0
      prevWeight = item.weightKg
      return { ...item, weightChange: change }
    })
  } catch (err: any) {
    error.value = err.message || '数据加载失败，请稍后重试'
  } finally {
    loading.value = false
  }

  // 同时加载趋势数据
  fetchTrend()
}

/** 获取体重趋势数据并构建ECharts配置 */
async function fetchTrend() {
  try {
    const res = await getWeightTrend(30) as any
    const trend = (Array.isArray(res) ? res : res?.records || res?.list || []) as TrendItem[]

    if (!trend || trend.length === 0) {
      trendOption.value = null
      return
    }

    const dates = trend.map((t: TrendItem) => {
      if (!t.date) return ''
      const d = new Date(t.date)
      if (isNaN(d.getTime())) return String(t.date)
      return `${d.getMonth() + 1}/${d.getDate()}`
    })

    const weights = trend.map((t: TrendItem) => t.weight)
    const weightMin = Math.min(...weights)
    const weightMax = Math.max(...weights)
    const padding = Math.max((weightMax - weightMin) * 0.3, 0.5)

    trendOption.value = {
      tooltip: {
        trigger: 'axis' as const,
        backgroundColor: '#fff',
        borderColor: '#e4e7ed',
        textStyle: { color: '#303133', fontSize: 13 },
        formatter: (params: any) => {
          const p = Array.isArray(params) ? params[0] : params
          if (!p) return ''
          const idx = p.dataIndex
          const item = trend[idx]
          return `<div style="font-size:13px;margin-bottom:4px">${p.axisValue}</div>
            <div style="font-weight:600">体重 ${p.value} kg</div>
            ${item?.bmi ? `<div style="color:#909399;font-size:12px;margin-top:2px">BMI ${item.bmi.toFixed(1)}</div>` : ''}`
        }
      },
      grid: { left: '3%', right: '4%', bottom: '3%', top: '8%', containLabel: true },
      xAxis: {
        type: 'category' as const,
        data: dates,
        boundaryGap: false,
        axisLabel: { color: '#909399', fontSize: 11 },
        axisLine: { lineStyle: { color: '#dcdfe6' } },
        axisTick: { show: false }
      },
      yAxis: {
        type: 'value' as const,
        name: 'kg',
        nameTextStyle: { color: '#909399', fontSize: 11 },
        min: Math.floor(weightMin - padding),
        max: Math.ceil(weightMax + padding),
        axisLabel: { color: '#909399', fontSize: 11 },
        splitLine: { lineStyle: { color: '#f2f3f5', type: 'dashed' as const } }
      },
      series: [{
        name: '体重',
        data: weights,
        type: 'line' as const,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { color: '#409eff', width: 2.5 },
        itemStyle: {
          color: '#409eff',
          borderColor: '#fff',
          borderWidth: 2
        },
        areaStyle: {
          color: {
            type: 'linear' as const,
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(64, 158, 255, 0.25)' },
              { offset: 1, color: 'rgba(64, 158, 255, 0.02)' }
            ]
          }
        },
        markLine: {
          silent: true,
          symbol: 'none',
          data: [{
            type: 'average' as const,
            name: '平均值',
            lineStyle: { color: '#e6a23c', type: 'dashed' as const, width: 1.5 },
            label: { color: '#e6a23c', fontSize: 10, formatter: '均值 {c} kg' }
          }]
        }
      }]
    }
  } catch {
    // 趋势数据加载失败不影响主表格显示
    trendOption.value = null
  }
}

// ==================== 弹窗操作 ====================

/** 打开录入/编辑弹窗 */
function openDialog(row?: WeightRecord) {
  if (row) {
    isEditing.value = true
    form.recordDate = row.recordDate
    form.weightKg = row.weightKg
    form.note = row.note || ''
  } else {
    isEditing.value = false
    form.recordDate = getTodayStr()
    form.weightKg = 60
    form.note = ''
  }
  dialogVisible.value = true
}

/** 重置表单 */
function resetForm() {
  formRef.value?.resetFields()
  form.recordDate = getTodayStr()
  form.weightKg = 60
  form.note = ''
}

/** 保存体重记录 */
async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    await saveWeight({
      recordDate: form.recordDate,
      weightKg: form.weightKg,
      note: form.note || undefined
    })
    ElMessage.success(isEditing.value ? '体重记录已更新' : '体重记录已保存')
    dialogVisible.value = false
    await fetchWeights()
  } catch (err: any) {
    ElMessage.error(err.message || '保存失败，请重试')
  } finally {
    saving.value = false
  }
}

// ==================== 生命周期 ====================

onMounted(() => {
  fetchWeights()
})
</script>

<style scoped lang="scss">
/**
 * 体重管理页面样式
 */

.weight-page {
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

/* ==================== 体重变化颜色 ==================== */
.weight-change {
  font-weight: 500;

  &.up {
    color: #f56c6c; // 红色：体重上升
  }

  &.down {
    color: #67c23a; // 绿色：体重下降
  }
}
</style>
