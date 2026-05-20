<template>
  <!-- 训练记录页面：日历热力图、训练记录表格、训练录入弹窗、休息计时器 -->
  <div class="training-page">
    <!-- ==================== 页面标题与操作栏 ==================== -->
    <div class="page-header">
      <h2>训练记录</h2>
      <div class="header-actions">
        <!-- 休息计时器浮窗按钮 -->
        <el-badge :value="timerRunning ? displayTime : ''" :hidden="!timerRunning">
          <el-button @click="showTimer = !showTimer" :type="showTimer ? 'warning' : 'default'" circle>
            <el-icon><Timer /></el-icon>
          </el-button>
        </el-badge>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          :shortcuts="dateShortcuts"
          @change="fetchTrainings"
          style="width: 280px"
        />
        <el-button type="primary" @click="openTrainingDialog()">
          <el-icon><Plus /></el-icon>
          开始训练
        </el-button>
      </div>
    </div>

    <!-- ==================== 休息计时器面板 ==================== -->
    <el-collapse-transition>
      <el-card v-show="showTimer" class="section timer-card">
        <RestTimer ref="restTimerRef" :default-minutes="1.5" :size="180" @complete="onRestComplete" />
      </el-card>
    </el-collapse-transition>

    <!-- ==================== 训练日历热力图 ==================== -->
    <el-card class="section">
      <template #header>
        <div class="card-header">
          <span>训练日历</span>
          <el-select v-model="calendarYear" size="small" style="width: 100px" @change="fetchCalendar">
            <el-option v-for="y in yearOptions" :key="y" :label="String(y)" :value="y" />
          </el-select>
        </div>
      </template>
      <div v-loading="calendarLoading">
        <v-chart v-if="calendarOption" :option="calendarOption" style="height: 200px" autoresize />
        <el-empty v-else description="暂无训练日历数据" :image-size="80" />
      </div>
    </el-card>

    <!-- ==================== 加载状态 ==================== -->
    <template v-if="loading">
      <el-card class="section">
        <el-skeleton :rows="6" animated />
      </el-card>
    </template>

    <!-- ==================== 错误状态 ==================== -->
    <template v-else-if="error">
      <el-result icon="error" title="数据加载失败" :sub-title="error">
        <template #extra>
          <el-button type="primary" @click="fetchTrainings">重新加载</el-button>
        </template>
      </el-result>
    </template>

    <!-- ==================== 训练记录表格 ==================== -->
    <template v-else>
      <el-card class="section">
        <template #header>
          <span>训练记录</span>
        </template>
        <el-table
          v-if="trainingList.length > 0"
          :data="trainingList"
          stripe
          border
          style="width: 100%"
          row-key="id"
        >
          <el-table-column type="expand">
            <template #default="{ row }">
              <div class="expand-actions">
                <h4>动作详情</h4>
                <el-table v-if="row.details && row.details.length" :data="row.details" size="small" border>
                  <el-table-column prop="actionName" label="动作名称" />
                  <el-table-column prop="sets" label="组数" width="80" />
                  <el-table-column prop="weightKg" label="重量(kg)" width="100" />
                </el-table>
                <el-empty v-else description="无动作记录" :image-size="60" />
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="recordDate" label="日期" width="130" sortable>
            <template #default="{ row }">
              {{ formatDate(row.recordDate) }}
            </template>
          </el-table-column>
          <el-table-column prop="trainingType" label="训练类型" width="120">
            <template #default="{ row }">
              <el-tag :type="getTypeTag(row.trainingType)" size="small">
                {{ row.trainingTypeLabel || row.trainingType || '--' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="startTime" label="开始时间" width="110">
            <template #default="{ row }">
              {{ row.startTime || '--' }}
            </template>
          </el-table-column>
          <el-table-column prop="endTime" label="结束时间" width="110">
            <template #default="{ row }">
              {{ row.endTime || '--' }}
            </template>
          </el-table-column>
          <el-table-column prop="durationMinutes" label="时长(分钟)" width="100" sortable>
            <template #default="{ row }">
              {{ row.durationMinutes ?? '--' }}
            </template>
          </el-table-column>
          <el-table-column prop="note" label="备注" min-width="160">
            <template #default="{ row }">
              {{ row.note || '--' }}
            </template>
          </el-table-column>
          <el-table-column label="动作数" width="80">
            <template #default="{ row }">
              <el-tag round size="small">{{ row.details?.length || 0 }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="openTrainingDialog(row)">编辑</el-button>
              <el-popconfirm title="确认删除该训练记录？" @confirm="handleDelete(row.id!)">
                <template #reference>
                  <el-button type="danger" link size="small">删除</el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="暂无训练记录，点击「开始训练」记录你的训练吧" :image-size="120" />
      </el-card>
    </template>

    <!-- ==================== 训练录入 / 编辑弹窗 ==================== -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditing ? '编辑训练记录' : '开始训练'"
      :fullscreen="isFullScreen"
      width="700px"
      :close-on-click-modal="false"
      @closed="resetTrainingForm"
      destroy-on-close
    >
      <el-form ref="trainingFormRef" :model="trainingForm" :rules="trainingRules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="训练日期" prop="recordDate">
              <el-date-picker
                v-model="trainingForm.recordDate"
                type="date"
                placeholder="选择日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="训练类型" prop="trainingType">
              <el-select v-model="trainingForm.trainingType" placeholder="选择训练类型" style="width: 100%">
                <el-option
                  v-for="item in trainingTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-time-picker
                v-model="trainingForm.startTime"
                placeholder="开始时间"
                format="HH:mm"
                value-format="HH:mm"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-time-picker
                v-model="trainingForm.endTime"
                placeholder="结束时间"
                format="HH:mm"
                value-format="HH:mm"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="时长(分钟)" prop="durationMinutes">
              <el-input-number
                v-model="trainingForm.durationMinutes"
                :min="1"
                :max="600"
                controls-position="right"
                style="width: 100%"
                placeholder="训练时长"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备注">
              <el-input v-model="trainingForm.note" placeholder="训练备注" maxlength="200" show-word-limit />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- ==================== 动作列表 ==================== -->
        <el-form-item label="训练动作" class="action-list-form-item">
          <div class="action-list-container">
            <el-table :data="trainingForm.details" size="small" border>
              <el-table-column label="动作" min-width="180">
                <template #default="{ row: act, $index }">
                  <el-select
                    v-model="act.actionId"
                    filterable
                    remote
                    reserve-keyword
                    placeholder="搜索动作"
                    :remote-method="searchActionsRemote"
                    :loading="actionSearchLoading"
                    style="width: 100%"
                    @change="(val: number) => onActionSelect(val, $index)"
                  >
                    <el-option
                      v-for="opt in actionOptions"
                      :key="opt.value"
                      :label="opt.label"
                      :value="opt.value"
                    />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="组数" width="90">
                <template #default="{ row: act }">
                  <el-input-number
                    v-model="act.sets"
                    :min="1"
                    :max="20"
                    controls-position="right"
                    size="small"
                    style="width: 100%"
                  />
                </template>
              </el-table-column>
              <el-table-column label="重量(kg)" width="110">
                <template #default="{ row: act }">
                  <el-input-number
                    v-model="act.weightKg"
                    :min="0"
                    :max="999"
                    :precision="1"
                    :step="2.5"
                    controls-position="right"
                    size="small"
                    style="width: 100%"
                  />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="70" fixed="right">
                <template #default="{ $index }">
                  <el-button type="danger" link size="small" @click="removeActionRow($index)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div class="action-bar">
              <el-button size="small" @click="addActionRow">
                <el-icon><Plus /></el-icon>
                添加动作
              </el-button>
              <el-button size="small" :loading="loadingPlan" @click="loadFromPlanDialog = true">
                从计划加载
              </el-button>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleTrainingSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- ==================== 从训练计划加载弹窗 ==================== -->
    <el-dialog
      v-model="loadFromPlanDialog"
      title="从训练计划加载"
      width="450px"
      append-to-body
    >
      <el-form label-width="100px">
        <el-form-item label="选择训练日">
          <el-select v-model="selectedPlanDay" placeholder="请选择训练日" style="width: 100%">
            <el-option
              v-for="day in planDays"
              :key="day.id"
              :label="day.name || `第${day.dayNumber}天`"
              :value="day.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="loadFromPlanDialog = false">取消</el-button>
        <el-button type="primary" @click="handleLoadPlan">加载</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
/**
 * 训练记录页面
 * 功能：训练日历热力图（ECharts）、训练记录表格（可展开查看动作详情）、
 * 训练录入/编辑弹窗（含动态动作列表、从计划加载）、删除确认、组间休息计时器
 */
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Timer } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getTrainings,
  getTrainingDetail,
  createTraining,
  updateTraining,
  deleteTraining,
  getCalendar
} from '@/api/training'
import { getDict } from '@/api/dict'
import { searchActions } from '@/api/action'
import VChart from 'vue-echarts'
import 'echarts'
import RestTimer from '@/components/common/RestTimer.vue'

// ==================== 类型定义 ====================

/** 训练动作项 */
interface ActionItem {
  actionId: number | null
  actionName: string
  sets: number
  weightKg: number
}

/** 训练记录 */
interface TrainingRecord {
  id?: number
  recordDate: string
  trainingType: string
  trainingTypeLabel?: string
  startTime: string | null
  endTime: string | null
  durationMinutes: number | null
  note: string
  details: ActionItem[]
}

/** 计划训练日 */
interface PlanDay {
  id: number
  name: string
  dayNumber: number
  actions: ActionItem[]
}

// ==================== 状态 ====================

const loading = ref(true)
const error = ref('')
const saving = ref(false)
const loadingPlan = ref(false)
const calendarLoading = ref(false)

/** 日期范围 */
const dateRange = ref<[string, string]>([getDefaultStartDate(), getTodayStr()])

/** 训练记录列表 */
const trainingList = ref<TrainingRecord[]>([])

/** 弹窗相关 */
const dialogVisible = ref(false)
const isEditing = ref(false)
const editingId = ref<number | null>(null)
const isFullScreen = ref(false)
const trainingFormRef = ref<FormInstance>()

/** 休息计时器 */
const showTimer = ref(false)
const timerRunning = ref(false)
const displayTime = ref('')
const restTimerRef = ref<InstanceType<typeof RestTimer>>()

/** 训练类型字典 */
const trainingTypeOptions = ref<{ label: string; value: string }[]>([])

/** 动作搜索 */
const actionOptions = ref<{ label: string; value: number }[]>([])
const actionSearchLoading = ref(false)

/** 从计划加载 */
const loadFromPlanDialog = ref(false)
const selectedPlanDay = ref<number | null>(null)
const planDays = ref<PlanDay[]>([])

/** 日历 */
const calendarYear = ref(new Date().getFullYear())
const calendarOption = ref<any>(null)
const yearOptions = computed(() => {
  const current = new Date().getFullYear()
  return [current - 1, current, current + 1]
})

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

// ==================== 训练表单 ====================

const defaultActions = (): ActionItem[] => [{ actionId: null, actionName: '', sets: 3, weightKg: 0 }]

const trainingForm = reactive({
  recordDate: getTodayStr(),
  trainingType: '',
  startTime: null as string | null,
  endTime: null as string | null,
  durationMinutes: null as number | null,
  note: '',
  details: defaultActions()
})

const trainingRules: FormRules = {
  recordDate: [{ required: true, message: '请选择训练日期', trigger: 'blur' }],
  trainingType: [{ required: true, message: '请选择训练类型', trigger: 'change' }]
}

// ==================== 工具函数 ====================

function getTodayStr(): string {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function getDefaultStartDate(): string {
  const d = new Date()
  d.setTime(d.getTime() - 3600 * 1000 * 24 * 30)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function formatDate(dateStr: string) {
  if (!dateStr) return '--'
  const d = new Date(dateStr)
  if (isNaN(d.getTime())) return dateStr
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function getTypeTag(type: string): 'success' | 'warning' | 'danger' | 'info' | '' {
  const map: Record<string, 'success' | 'warning' | 'danger' | 'info'> = {
    strength: 'danger',
    hypertrophy: 'warning',
    endurance: 'success',
    cardio: 'info',
    flexibility: 'info'
  }
  return map[type] || 'info'
}

// ==================== 数据获取 ====================

/** 获取训练类型字典 */
async function fetchTrainingTypes() {
  try {
    const res: any = await getDict('training_type')
    trainingTypeOptions.value = (Array.isArray(res) ? res : []).map((item: any) => ({
      label: item.label || item.name || item.dictLabel,
      value: item.value || item.code || item.dictValue
    }))
  } catch {
    // 字典加载失败使用默认值
    trainingTypeOptions.value = [
      { label: '力量训练', value: 'strength' },
      { label: '增肌训练', value: 'hypertrophy' },
      { label: '耐力训练', value: 'endurance' },
      { label: '有氧运动', value: 'cardio' },
      { label: '柔韧性', value: 'flexibility' }
    ]
  }
}

/** 获取训练记录列表 */
async function fetchTrainings() {
  loading.value = true
  error.value = ''
  try {
    const [startDate, endDate] = dateRange.value
    const res = await getTrainings(startDate, endDate) as any
    const list = (Array.isArray(res) ? res : res?.records || res?.list || []) as TrainingRecord[]
    trainingList.value = list
  } catch (err: any) {
    error.value = err.message || '数据加载失败，请稍后重试'
  } finally {
    loading.value = false
  }

  // 同时刷新日历
  fetchCalendar()
}

/** 获取训练日历数据 */
async function fetchCalendar() {
  calendarLoading.value = true
  try {
    const year = calendarYear.value
    // 并行加载全年12个月的数据
    const results = await Promise.all(
      Array.from({ length: 12 }, (_, i) => getCalendar(year, i + 1))
    ) as any[]
    const allData: [string, number][] = []
    for (const res of results) {
      const monthData = Array.isArray(res) ? res : res?.records || res?.list || res || []
      monthData.forEach((item: any) => {
        const date = item.date || item.trainingDate || item.calendarDate
        const count = item.count ?? 1
        if (date) allData.push([date, count])
      })
    }

    if (allData.length === 0) {
      calendarOption.value = null
      return
    }

    calendarOption.value = {
      tooltip: {
        formatter: (params: any) => {
          const d = params?.data?.[0] || ''
          const c = params?.data?.[1] || 0
          return `训练 ${c} 次`
        }
      },
      visualMap: {
        min: 0,
        max: Math.max(...allData.map(d => d[1]), 1),
        type: 'piecewise',
        orient: 'horizontal',
        left: 'center',
        top: 0,
        pieces: [
          { min: 3, color: '#1a5e20' },
          { min: 2, max: 2, color: '#4caf50' },
          { min: 1, max: 1, color: '#a5d6a7' },
          { min: 0, max: 0, color: '#f5f5f5' }
        ]
      },
      calendar: {
        top: 60,
        left: 40,
        right: 20,
        range: String(year),
        cellSize: ['auto', 15],
        dayLabel: { firstDay: 1, nameMap: 'ZH' },
        monthLabel: { nameMap: 'ZH' },
        itemStyle: { borderColor: '#fff', borderWidth: 2, borderRadius: 3 },
        yearLabel: { show: false }
      },
      series: [{
        type: 'heatmap',
        coordinateSystem: 'calendar',
        data: allData
      }]
    }
  } catch {
    calendarOption.value = null
  } finally {
    calendarLoading.value = false
  }
}

/** 搜索动作（远程搜索） */
async function searchActionsRemote(query: string) {
  if (!query || query.length < 1) {
    actionOptions.value = []
    return
  }
  actionSearchLoading.value = true
  try {
    const res: any = await searchActions(query)
    const list = Array.isArray(res) ? res : res?.records || res?.list || []
    actionOptions.value = list.map((item: any) => ({
      label: item.name || item.actionName || item.label,
      value: item.id || item.actionId
    }))
  } catch {
    actionOptions.value = []
  } finally {
    actionSearchLoading.value = false
  }
}

/** 动作选中回调 */
function onActionSelect(val: number | null, index: number) {
  if (val == null) return
  const selected = actionOptions.value.find(opt => opt.value === val)
  if (selected) {
    trainingForm.details[index].actionName = selected.label
  }
}

// ==================== 动作行操作 ====================

function addActionRow() {
  trainingForm.details.push({ actionId: null, actionName: '', sets: 3, weightKg: 0 })
}

function removeActionRow(index: number) {
  if (trainingForm.details.length <= 1) {
    ElMessage.warning('至少保留一个动作')
    return
  }
  trainingForm.details.splice(index, 1)
}

// ==================== 弹窗操作 ====================

/** 打开训练录入/编辑弹窗 */
function openTrainingDialog(row?: TrainingRecord) {
  loadFromPlanDialog.value = false

  if (row) {
    isEditing.value = true
    editingId.value = row.id ?? null
    trainingForm.recordDate = row.recordDate
    trainingForm.trainingType = row.trainingType
    trainingForm.startTime = row.startTime || null
    trainingForm.endTime = row.endTime || null
    trainingForm.durationMinutes = row.durationMinutes ?? null
    trainingForm.note = row.note || ''
    trainingForm.details = row.details?.length
      ? row.details.map(a => ({ ...a }))
      : defaultActions()
  } else {
    isEditing.value = false
    editingId.value = null
    trainingForm.recordDate = getTodayStr()
    trainingForm.trainingType = ''
    trainingForm.startTime = null
    trainingForm.endTime = null
    trainingForm.durationMinutes = null
    trainingForm.note = ''
    trainingForm.details = defaultActions()
  }
  dialogVisible.value = true
}

function resetTrainingForm() {
  trainingFormRef.value?.resetFields()
}

/** 处理从计划加载 */
async function handleLoadPlan() {
  if (!selectedPlanDay.value) {
    ElMessage.warning('请选择训练日')
    return
  }
  // 从已选训练日加载动作
  const day = planDays.value.find(d => d.id === selectedPlanDay.value)
  if (day && day.actions?.length) {
    trainingForm.details = day.actions.map(a => ({
      actionId: a.actionId,
      actionName: a.actionName || '',
      sets: a.sets || 3,
      weightKg: a.weightKg || (a as any).weight || 0
    }))
    ElMessage.success(`已从计划加载 ${day.actions.length} 个动作`)
  } else {
    ElMessage.warning('该训练日无动作数据')
  }
  loadFromPlanDialog.value = false
  selectedPlanDay.value = null
}

/** 保存训练记录 */
async function handleTrainingSave() {
  const valid = await trainingFormRef.value?.validate().catch(() => false)
  if (!valid) return

  // 过滤无效动作（未选择动作的行）
  const validDetails = trainingForm.details.filter(a => a.actionId != null)

  saving.value = true
  try {
    const payload = {
      recordDate: trainingForm.recordDate,
      trainingType: trainingForm.trainingType,
      startTime: trainingForm.startTime,
      endTime: trainingForm.endTime,
      durationMinutes: trainingForm.durationMinutes,
      note: trainingForm.note || undefined,
      details: validDetails
    }

    if (isEditing.value && editingId.value) {
      await updateTraining(editingId.value, payload)
      ElMessage.success('训练记录已更新')
    } else {
      await createTraining(payload)
      ElMessage.success('训练记录已保存')
    }
    dialogVisible.value = false
    await fetchTrainings()
  } catch (err: any) {
    ElMessage.error(err.message || '保存失败，请重试')
  } finally {
    saving.value = false
  }
}

/** 删除训练记录 */
async function handleDelete(id: number) {
  try {
    await deleteTraining(id)
    ElMessage.success('训练记录已删除')
    await fetchTrainings()
  } catch (err: any) {
    ElMessage.error(err.message || '删除失败，请重试')
  }
}

// ==================== 休息计时器 ====================

function onRestComplete() {
  ElMessage.success('休息时间到！准备下一组训练')
  // 尝试振动提醒（移动端）
  if (navigator.vibrate) {
    navigator.vibrate([300, 100, 300])
  }
}

// ==================== 生命周期 ====================

onMounted(() => {
  fetchTrainingTypes()
  fetchTrainings()
})
</script>

<style scoped lang="scss">
/**
 * 训练记录页面样式
 */

.training-page {
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

/* ==================== 计时器卡片 ==================== */
.timer-card {
  :deep(.el-card__body) {
    display: flex;
    justify-content: center;
  }
}

/* ==================== 卡片头部 ==================== */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* ==================== 展开动作详情 ==================== */
.expand-actions {
  padding: 12px 20px;

  h4 {
    margin: 0 0 10px;
    font-size: 14px;
    color: #606266;
  }
}

/* ==================== 动作列表容器 ==================== */
.action-list-form-item {
  :deep(.el-form-item__content) {
    display: block;
  }
}

.action-list-container {
  width: 100%;

  .action-bar {
    display: flex;
    gap: 10px;
    margin-top: 10px;
  }
}
</style>
