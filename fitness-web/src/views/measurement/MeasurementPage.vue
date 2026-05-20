<template>
  <!-- 身体围度测量页面：测量记录表格、录入弹窗、围度趋势折线图 -->
  <div class="measurement-page">
    <!-- ==================== 页面标题与操作栏 ==================== -->
    <div class="page-header">
      <h2>身体围度</h2>
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
          @change="fetchMeasurements"
          style="width: 280px"
        />
        <el-button type="primary" @click="openDialog()">
          <el-icon><Plus /></el-icon>
          记录围度
        </el-button>
      </div>
    </div>

    <!-- ==================== 加载状态 ==================== -->
    <template v-if="loading">
      <el-card class="section">
        <el-skeleton :rows="5" animated />
      </el-card>
    </template>

    <!-- ==================== 错误状态 ==================== -->
    <template v-else-if="error">
      <el-result icon="error" title="数据加载失败" :sub-title="error">
        <template #extra>
          <el-button type="primary" @click="fetchMeasurements">重新加载</el-button>
        </template>
      </el-result>
    </template>

    <!-- ==================== 测量记录表格 ==================== -->
    <template v-else>
      <el-card class="section">
        <template #header>
          <span>测量记录</span>
        </template>
        <el-table v-if="measurementList.length > 0" :data="measurementList" stripe border style="width: 100%">
          <el-table-column prop="recordDate" label="日期" width="130" sortable>
            <template #default="{ row }">
              {{ formatDate(row.recordDate) }}
            </template>
          </el-table-column>
          <el-table-column prop="chestCm" label="胸围(cm)" width="100" sortable>
            <template #default="{ row }">{{ row.chestCm || '--' }}</template>
          </el-table-column>
          <el-table-column prop="waistCm" label="腰围(cm)" width="100" sortable>
            <template #default="{ row }">{{ row.waistCm || '--' }}</template>
          </el-table-column>
          <el-table-column prop="hipCm" label="臀围(cm)" width="100" sortable>
            <template #default="{ row }">{{ row.hipCm || '--' }}</template>
          </el-table-column>
          <el-table-column prop="leftArmCm" label="左臂(cm)" width="100">
            <template #default="{ row }">{{ row.leftArmCm || '--' }}</template>
          </el-table-column>
          <el-table-column prop="rightArmCm" label="右臂(cm)" width="100">
            <template #default="{ row }">{{ row.rightArmCm || '--' }}</template>
          </el-table-column>
          <el-table-column prop="leftThighCm" label="左大腿(cm)" width="110">
            <template #default="{ row }">{{ row.leftThighCm || '--' }}</template>
          </el-table-column>
          <el-table-column prop="rightThighCm" label="右大腿(cm)" width="110">
            <template #default="{ row }">{{ row.rightThighCm || '--' }}</template>
          </el-table-column>
          <el-table-column prop="neckCm" label="颈围(cm)" width="100">
            <template #default="{ row }">{{ row.neckCm || '--' }}</template>
          </el-table-column>
          <el-table-column prop="note" label="备注" min-width="150">
            <template #default="{ row }">{{ row.note || '--' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="80" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="openDialog(row)">编辑</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="暂无测量记录，点击上方按钮开始记录" :image-size="120" />
      </el-card>

      <!-- ==================== 围度趋势图 ==================== -->
      <el-card class="section">
        <template #header>
          <div class="card-header">
            <span>围度趋势</span>
            <div class="toggle-group">
              <el-checkbox
                v-for="field in measureFields"
                :key="field.key"
                v-model="field.visible"
                size="small"
                :label="field.label"
                border
                @change="buildTrendChart"
              />
            </div>
          </div>
        </template>
        <template v-if="trendOption">
          <v-chart :option="trendOption" style="height: 380px" autoresize />
        </template>
        <el-empty v-else description="暂无趋势数据" :image-size="100" />
      </el-card>
    </template>

    <!-- ==================== 记录围度弹窗 ==================== -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditing ? '编辑围度记录' : '记录围度'"
      width="560px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="测量日期" prop="recordDate">
          <el-date-picker
            v-model="form.recordDate"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="胸围(cm)">
              <el-input-number
                v-model="form.chestCm"
                :min="0"
                :max="200"
                :precision="1"
                controls-position="right"
                style="width: 100%"
                placeholder="胸围"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="腰围(cm)">
              <el-input-number
                v-model="form.waistCm"
                :min="0"
                :max="200"
                :precision="1"
                controls-position="right"
                style="width: 100%"
                placeholder="腰围"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="左臂(cm)">
              <el-input-number
                v-model="form.leftArmCm"
                :min="0"
                :max="100"
                :precision="1"
                controls-position="right"
                style="width: 100%"
                placeholder="左臂"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="右臂(cm)">
              <el-input-number
                v-model="form.rightArmCm"
                :min="0"
                :max="100"
                :precision="1"
                controls-position="right"
                style="width: 100%"
                placeholder="右臂"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="左大腿(cm)">
              <el-input-number
                v-model="form.leftThighCm"
                :min="0"
                :max="100"
                :precision="1"
                controls-position="right"
                style="width: 100%"
                placeholder="左大腿"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="右大腿(cm)">
              <el-input-number
                v-model="form.rightThighCm"
                :min="0"
                :max="100"
                :precision="1"
                controls-position="right"
                style="width: 100%"
                placeholder="右大腿"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="臀围(cm)">
              <el-input-number
                v-model="form.hipCm"
                :min="0"
                :max="200"
                :precision="1"
                controls-position="right"
                style="width: 100%"
                placeholder="臀围"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="颈围(cm)">
              <el-input-number
                v-model="form.neckCm"
                :min="0"
                :max="100"
                :precision="1"
                controls-position="right"
                style="width: 100%"
                placeholder="颈围"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input
            v-model="form.note"
            type="textarea"
            :rows="2"
            placeholder="备注信息"
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
 * 身体围度测量页面
 * 功能：测量记录表格、录入/编辑弹窗（所有围度字段可选）、
 * 围度趋势多线折线图（用户可切换显示哪些围度）
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { getMeasurements, saveMeasurement } from '@/api/measurement'
import VChart from 'vue-echarts'
import 'echarts'

// ==================== 类型定义 ====================

/** 测量记录 */
interface MeasurementRecord {
  id?: number
  recordDate: string
  chestCm: number | null
  waistCm: number | null
  leftArmCm: number | null
  rightArmCm: number | null
  leftThighCm: number | null
  rightThighCm: number | null
  hipCm: number | null
  neckCm: number | null
  note: string
}

// ==================== 可切换的围度字段 ====================

interface MeasureField {
  key: string
  label: string
  visible: boolean
  color: string
}

const measureFields = ref<MeasureField[]>([
  { key: 'chestCm', label: '胸围', visible: true, color: '#f56c6c' },
  { key: 'waistCm', label: '腰围', visible: true, color: '#409eff' },
  { key: 'hipCm', label: '臀围', visible: true, color: '#67c23a' },
  { key: 'leftArmCm', label: '左臂', visible: false, color: '#e6a23c' },
  { key: 'rightArmCm', label: '右臂', visible: false, color: '#e6a23c' },
  { key: 'leftThighCm', label: '左大腿', visible: false, color: '#909399' },
  { key: 'rightThighCm', label: '右大腿', visible: false, color: '#909399' },
  { key: 'neckCm', label: '颈围', visible: false, color: '#c0c4cc' }
])

// ==================== 状态 ====================

const loading = ref(true)
const error = ref('')
const saving = ref(false)
const dialogVisible = ref(false)
const isEditing = ref(false)
const formRef = ref<FormInstance>()
const measurementList = ref<MeasurementRecord[]>([])
const trendOption = ref<any>(null)

/** 日期范围 */
const dateRange = ref<[string, string]>([getDefaultStartDate(), getTodayStr()])

/** 日期快捷选项 */
const dateShortcuts = [
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
  }},
  { text: '最近半年', value: () => {
    const end = new Date()
    const start = new Date()
    start.setTime(start.getTime() - 3600 * 1000 * 24 * 180)
    return [start, end]
  }}
]

// ==================== 表单 ====================

const form = reactive({
  recordDate: getTodayStr(),
  chestCm: null as number | null,
  waistCm: null as number | null,
  leftArmCm: null as number | null,
  rightArmCm: null as number | null,
  leftThighCm: null as number | null,
  rightThighCm: null as number | null,
  hipCm: null as number | null,
  neckCm: null as number | null,
  note: ''
})

const formRules: FormRules = {
  recordDate: [{ required: true, message: '请选择测量日期', trigger: 'blur' }]
}

// ==================== 工具函数 ====================

function getTodayStr(): string {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function getDefaultStartDate(): string {
  const d = new Date()
  d.setTime(d.getTime() - 3600 * 1000 * 24 * 90)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function formatDate(dateStr: string) {
  if (!dateStr) return '--'
  const d = new Date(dateStr)
  if (isNaN(d.getTime())) return dateStr
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

// ==================== 数据获取 ====================

async function fetchMeasurements() {
  loading.value = true
  error.value = ''
  try {
    const [startDate, endDate] = dateRange.value
    const res = await getMeasurements(startDate, endDate) as any
    const list = (Array.isArray(res) ? res : res?.records || res?.list || []) as MeasurementRecord[]
    // 按日期升序排列（用于趋势图）
    measurementList.value = list.sort((a, b) => {
      return new Date(a.recordDate).getTime() - new Date(b.recordDate).getTime()
    })
  } catch (err: any) {
    error.value = err.message || '数据加载失败，请稍后重试'
  } finally {
    loading.value = false
  }

  // 构建趋势图
  buildTrendChart()
}

/** 构建围度趋势多线图 */
function buildTrendChart() {
  const data = measurementList.value
  if (data.length === 0) {
    trendOption.value = null
    return
  }

  const dates = data.map(item => {
    const d = new Date(item.recordDate)
    return `${d.getMonth() + 1}/${d.getDate()}`
  })

  const visibleFields = measureFields.value.filter(f => f.visible)
  if (visibleFields.length === 0) {
    trendOption.value = null
    return
  }

  const series = visibleFields.map(field => ({
    name: field.label,
    type: 'line' as const,
    data: data.map(item => (item as any)[field.key] ?? null),
    smooth: true,
    symbol: 'circle',
    symbolSize: 4,
    lineStyle: { color: field.color, width: 2 },
    itemStyle: { color: field.color }
  }))

  trendOption.value = {
    tooltip: {
      trigger: 'axis' as const,
      backgroundColor: '#fff',
      borderColor: '#e4e7ed',
      textStyle: { color: '#303133', fontSize: 13 }
    },
    legend: {
      data: visibleFields.map(f => f.label),
      bottom: 0,
      textStyle: { fontSize: 12 }
    },
    grid: { left: '3%', right: '4%', bottom: '10%', top: '5%', containLabel: true },
    xAxis: {
      type: 'category' as const,
      data: dates,
      boundaryGap: false,
      axisLabel: { color: '#909399', fontSize: 11 },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value' as const,
      name: 'cm',
      axisLabel: { color: '#909399', fontSize: 11 },
      splitLine: { lineStyle: { color: '#f2f3f5', type: 'dashed' as const } }
    },
    series
  }
}

// ==================== 弹窗操作 ====================

function openDialog(row?: MeasurementRecord) {
  if (row) {
    isEditing.value = true
    form.recordDate = row.recordDate
    form.chestCm = row.chestCm
    form.waistCm = row.waistCm
    form.leftArmCm = row.leftArmCm
    form.rightArmCm = row.rightArmCm
    form.leftThighCm = row.leftThighCm
    form.rightThighCm = row.rightThighCm
    form.hipCm = row.hipCm
    form.neckCm = row.neckCm
    form.note = row.note || ''
  } else {
    isEditing.value = false
    form.recordDate = getTodayStr()
    form.chestCm = null
    form.waistCm = null
    form.leftArmCm = null
    form.rightArmCm = null
    form.leftThighCm = null
    form.rightThighCm = null
    form.hipCm = null
    form.neckCm = null
    form.note = ''
  }
  dialogVisible.value = true
}

function resetForm() {
  formRef.value?.resetFields()
}

async function handleSave() {
  saving.value = true
  try {
    const payload: any = { recordDate: form.recordDate }
    // 只传有值的字段
    const fieldKeys = ['chestCm', 'waistCm', 'leftArmCm', 'rightArmCm', 'leftThighCm', 'rightThighCm', 'hipCm', 'neckCm']
    fieldKeys.forEach(key => {
      const val = (form as any)[key]
      if (val != null) payload[key] = val
    })
    if (form.note) payload.note = form.note

    await saveMeasurement(payload)
    ElMessage.success(isEditing.value ? '围度记录已更新' : '围度记录已保存')
    dialogVisible.value = false
    await fetchMeasurements()
  } catch (err: any) {
    ElMessage.error(err.message || '保存失败，请重试')
  } finally {
    saving.value = false
  }
}

// ==================== 生命周期 ====================

onMounted(() => {
  fetchMeasurements()
})
</script>

<style scoped lang="scss">
/**
 * 身体围度测量页面样式
 */

.measurement-page {
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

.section {
  margin-bottom: 16px;
}

/* ==================== 卡片头部：标题 + 图例切换 ==================== */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;

  .toggle-group {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
  }
}
</style>
