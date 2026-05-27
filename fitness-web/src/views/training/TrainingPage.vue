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

    <!-- ==================== 训练日历 ==================== -->
    <el-card ref="calendarSectionRef" class="section training-calendar-card">
      <template #header>
        <div class="card-header">
          <span>训练日历</span>
          <div class="calendar-tools">
            <div class="calendar-legend">
              <span
                v-for="item in calendarLegend"
                :key="item.value"
                class="legend-item"
              >
                <span class="legend-dot" :style="{ backgroundColor: item.color }" />
                {{ item.label }}
              </span>
            </div>
            <el-select v-model="calendarYear" size="small" style="width: 100px" @change="fetchCalendar">
              <el-option v-for="y in yearOptions" :key="y" :label="String(y)" :value="y" />
            </el-select>
            <el-select v-model="calendarMonth" size="small" style="width: 86px" @change="fetchCalendar">
              <el-option v-for="m in monthOptions" :key="m" :label="`${m}月`" :value="m" />
            </el-select>
          </div>
        </div>
      </template>
      <div v-loading="calendarLoading">
        <div class="calendar-weekdays">
          <span v-for="day in weekDays" :key="day">{{ day }}</span>
        </div>
        <div class="calendar-grid">
          <div
            v-for="day in calendarDays"
            :key="day.key"
            class="calendar-day"
            :class="{ 'calendar-day--muted': !day.inMonth, 'calendar-day--today': day.isToday }"
          >
            <div class="calendar-day-number">{{ day.dayOfMonth }}</div>
            <div v-if="day.records.length" class="calendar-records">
              <div
                v-for="(record, recordIndex) in day.records"
                :key="record.id || `${day.key}-${record.trainingType}-${recordIndex}`"
                class="calendar-record"
                :style="{ backgroundColor: getTrainingTypeColor(record.trainingType) }"
                :title="`${getTrainingTypeLabel(record.trainingType)} ${record.durationMinutes || 0}分钟`"
              >
                <span class="calendar-record-type">{{ getTrainingTypeLabel(record.trainingType) }}</span>
                <span v-if="record.durationMinutes" class="calendar-record-duration">
                  {{ record.durationMinutes }}分钟
                </span>
              </div>
            </div>
          </div>
        </div>
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
                format="HH:mm:00"
                value-format="HH:mm:00"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-time-picker
                v-model="trainingForm.endTime"
                placeholder="结束时间"
                format="HH:mm:00"
                value-format="HH:mm:00"
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
                    v-model="act.actionName"
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
              <el-button size="small" :loading="loadingPlan" @click="openLoadFromPlanDialog">
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
        <el-form-item label="选择计划">
          <el-select
            v-model="selectedPlanId"
            placeholder="请选择训练计划"
            filterable
            :loading="planLoading"
            style="width: 100%"
            @change="handlePlanSelect"
          >
            <el-option
              v-for="plan in planOptions"
              :key="plan.id"
              :label="plan.planName"
              :value="plan.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="选择训练日">
          <el-select v-model="selectedPlanDay" placeholder="请选择训练日" style="width: 100%" :disabled="!selectedPlanId">
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
 * 功能：训练月历、训练记录表格（可展开查看动作详情）、
 * 训练录入/编辑弹窗（含动态动作列表、从计划加载）、删除确认、组间休息计时器
 */
import { ref, reactive, computed, nextTick, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Timer } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getTrainings,
  getTrainingDetail,
  createTraining,
  updateTraining,
  deleteTraining
} from '@/api/training'
import { getDictOptions } from '@/api/dict'
import { searchActions } from '@/api/action'
import { getPlans, getPlanDetail } from '@/api/plan'
import RestTimer from '@/components/common/RestTimer.vue'

const route = useRoute()

// ==================== 类型定义 ====================

/** 训练动作项 */
interface ActionItem {
  actionId: number | null
  actionName: string
  sets: number
  weightKg: number
  sortOrder?: number
}

/** 训练记录 */
interface TrainingRecord {
  id?: number
  recordDate: string
  planId?: number | null
  trainingDayId?: number | null
  trainingType: string
  trainingTypeLabel?: string
  startTime: string | null
  endTime: string | null
  durationMinutes: number | null
  note: string
  details: ActionItem[]
}

interface CalendarDay {
  key: string
  dayOfMonth: number | string
  inMonth: boolean
  isToday: boolean
  records: TrainingRecord[]
}

/** 计划训练日 */
interface PlanDay {
  id: number
  name: string
  dayNumber: number
  trainingType?: string
  actions: ActionItem[]
}

/** 训练计划选项 */
interface PlanOption {
  id: number
  planName: string
  isActive?: number
}

// ==================== 状态 ====================

const loading = ref(true)
const error = ref('')
const saving = ref(false)
const loadingPlan = ref(false)
const calendarLoading = ref(false)
const calendarSectionRef = ref<any>()

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
const planLoading = ref(false)
const selectedPlanId = ref<number | null>(null)
const selectedPlanDay = ref<number | null>(null)
const planOptions = ref<PlanOption[]>([])
const planDays = ref<PlanDay[]>([])

/** 日历 */
const calendarYear = ref(new Date().getFullYear())
const calendarMonth = ref(new Date().getMonth() + 1)
const calendarRecords = ref<TrainingRecord[]>([])
const weekDays = ['一', '二', '三', '四', '五', '六', '日']
const yearOptions = computed(() => {
  const current = new Date().getFullYear()
  return [current - 1, current, current + 1]
})
const monthOptions = computed(() => Array.from({ length: 12 }, (_, index) => index + 1))

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
  planId: null as number | null,
  trainingDayId: null as number | null,
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
    CHEST: 'danger',
    BACK: 'success',
    LEGS: 'warning',
    SHOULDER: 'info',
    ARMS: 'danger',
    CORE: 'success',
    CARDIO: 'info',
    REST: 'info',
    strength: 'danger',
    hypertrophy: 'warning',
    endurance: 'success',
    cardio: 'info',
    flexibility: 'info'
  }
  return map[type] || 'info'
}

function getTrainingTypeColor(type?: string): string {
  const map: Record<string, string> = {
    CHEST: '#ef6f6c',
    BACK: '#2f9e75',
    LEGS: '#f2a541',
    SHOULDER: '#5c7cfa',
    ARMS: '#d66efd',
    CORE: '#20a4a8',
    CARDIO: '#ff7a59',
    REST: '#8c9aa9',
    strength: '#ef6f6c',
    hypertrophy: '#f2a541',
    endurance: '#2f9e75',
    cardio: '#ff7a59',
    flexibility: '#5c7cfa'
  }
  return map[type || ''] || '#607d8b'
}

function formatDateKey(date: Date): string {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

function normalizeDateKey(value: string): string {
  if (!value) return ''
  return value.slice(0, 10)
}

function getMonthStart(year: number, month: number): string {
  return `${year}-${String(month).padStart(2, '0')}-01`
}

function getMonthEnd(year: number, month: number): string {
  return formatDateKey(new Date(year, month, 0))
}

const calendarRecordMap = computed(() => {
  const map = new Map<string, TrainingRecord[]>()
  calendarRecords.value.forEach(record => {
    const key = normalizeDateKey(record.recordDate)
    if (!key) return
    const records = map.get(key) || []
    records.push(record)
    map.set(key, records)
  })
  return map
})

const calendarDays = computed<CalendarDay[]>(() => {
  const year = calendarYear.value
  const month = calendarMonth.value
  const first = new Date(year, month - 1, 1)
  const daysInMonth = new Date(year, month, 0).getDate()
  const leadingBlanks = (first.getDay() + 6) % 7
  const todayKey = formatDateKey(new Date())
  const days: CalendarDay[] = []

  for (let index = 0; index < leadingBlanks; index++) {
    days.push({
      key: `blank-${index}`,
      dayOfMonth: '',
      inMonth: false,
      isToday: false,
      records: []
    })
  }

  for (let day = 1; day <= daysInMonth; day++) {
    const key = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`
    days.push({
      key,
      dayOfMonth: day,
      inMonth: true,
      isToday: key === todayKey,
      records: calendarRecordMap.value.get(key) || []
    })
  }

  const trailingBlanks = (7 - (days.length % 7)) % 7
  for (let index = 0; index < trailingBlanks; index++) {
    days.push({
      key: `tail-${index}`,
      dayOfMonth: '',
      inMonth: false,
      isToday: false,
      records: []
    })
  }

  return days
})

const calendarLegend = computed(() => {
  return trainingTypeOptions.value.map(item => ({
    value: item.value,
    label: item.label,
    color: getTrainingTypeColor(item.value)
  }))
})

function parseTimeToMinutes(time: string | null): number | null {
  if (!time) return null
  const parts = time.split(':').map(Number)
  if (parts.length < 2 || parts.some(Number.isNaN)) return null
  return parts[0] * 60 + parts[1]
}

function calcDurationMinutes(startTime: string | null, endTime: string | null): number | null {
  const start = parseTimeToMinutes(startTime)
  const end = parseTimeToMinutes(endTime)
  if (start === null || end === null || end <= start) return null
  return end - start
}

function syncDurationFromTime() {
  const duration = calcDurationMinutes(trainingForm.startTime, trainingForm.endTime)
  if (duration !== null) {
    trainingForm.durationMinutes = duration
  }
}

// ==================== 数据获取 ====================

/** 获取训练类型字典 */
async function fetchTrainingTypes() {
  try {
    trainingTypeOptions.value = (await getDictOptions('training_type'))
      .filter(item => item.value !== 'REST')
  } catch (err: any) {
    ElMessage.error(err.message || '训练类型字典加载失败')
    trainingTypeOptions.value = []
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
    const month = calendarMonth.value
    const res = await getTrainings(getMonthStart(year, month), getMonthEnd(year, month)) as any
    calendarRecords.value = (Array.isArray(res) ? res : res?.records || res?.list || []) as TrainingRecord[]
  } catch {
    calendarRecords.value = []
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

async function fetchPlans() {
  planLoading.value = true
  try {
    const res = await getPlans() as any
    const list = (Array.isArray(res) ? res : res?.records || res?.list || []) as PlanOption[]
    planOptions.value = list
    if (!selectedPlanId.value) {
      const active = list.find(plan => plan.isActive === 1)
      selectedPlanId.value = active?.id || list[0]?.id || null
    }
    if (selectedPlanId.value) {
      await handlePlanSelect(selectedPlanId.value)
    }
  } catch (err: any) {
    planOptions.value = []
    planDays.value = []
    ElMessage.error(err.message || '训练计划加载失败')
  } finally {
    planLoading.value = false
  }
}

async function handlePlanSelect(planId: number | null) {
  selectedPlanDay.value = null
  planDays.value = []
  if (!planId) return

  planLoading.value = true
  try {
    const detail = await getPlanDetail(planId) as any
    const days = Array.isArray(detail?.trainingDays) ? detail.trainingDays : []
    planDays.value = days
      .filter((day: any) => day.dayType !== 'REST')
      .map((day: any) => ({
        id: day.id,
        name: getPlanDayLabel(day),
        dayNumber: day.dayOrder || day.dayNumber || 1,
        trainingType: normalizePlanTrainingType(day.trainingType),
        actions: (day.actions || []).map((action: any, index: number) => ({
          actionId: action.actionId ?? null,
          actionName: action.actionName || '',
          sets: action.minSets ?? action.sets ?? 3,
          weightKg: 0,
          sortOrder: action.sortOrder ?? index
        }))
      }))
    selectedPlanDay.value = planDays.value[0]?.id || null
  } catch (err: any) {
    planDays.value = []
    ElMessage.error(err.message || '训练计划详情加载失败')
  } finally {
    planLoading.value = false
  }
}

function getPlanDayLabel(day: any): string {
  const dayNumber = day.dayOrder || day.dayNumber || 1
  const typeText = getTrainingTypeLabel(normalizePlanTrainingType(day.trainingType))
  return typeText ? `第${dayNumber}天 · ${typeText}` : `第${dayNumber}天`
}

function normalizePlanTrainingType(value: any): string {
  if (Array.isArray(value)) return value[0] || ''
  if (typeof value !== 'string') return ''
  return value.split(',').map(item => item.trim()).filter(Boolean)[0] || ''
}

function getTrainingTypeLabel(value?: string): string {
  if (!value) return ''
  return trainingTypeOptions.value.find(item => item.value === value)?.label || value
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
async function openTrainingDialog(row?: TrainingRecord) {
  loadFromPlanDialog.value = false

  if (row) {
    isEditing.value = true
    editingId.value = row.id ?? null
    dialogVisible.value = true
    loadingPlan.value = true
    try {
      const detail = row.id ? await getTrainingDetail(row.id) as any : null
      const record = detail?.record || row
      const details = Array.isArray(detail?.details) ? detail.details : (row.details || [])
      fillTrainingForm(record, details)
    } catch (err: any) {
      ElMessage.error(err.message || '训练记录详情加载失败')
      fillTrainingForm(row, row.details || [])
    } finally {
      loadingPlan.value = false
    }
  } else {
    isEditing.value = false
    editingId.value = null
    resetTrainingFormData()
    dialogVisible.value = true
  }
}

function resetTrainingForm() {
  trainingFormRef.value?.resetFields()
}

function fillTrainingForm(record: TrainingRecord, details: ActionItem[]) {
  trainingForm.recordDate = record.recordDate
  trainingForm.planId = record.planId ?? null
  trainingForm.trainingDayId = record.trainingDayId ?? null
  trainingForm.trainingType = record.trainingType
  trainingForm.startTime = record.startTime || null
  trainingForm.endTime = record.endTime || null
  trainingForm.durationMinutes = record.durationMinutes ?? null
  trainingForm.note = record.note || ''
  trainingForm.details = details?.length
    ? details.map((a, index) => ({
        actionId: a.actionId,
        actionName: a.actionName || '',
        sets: a.sets || 3,
        weightKg: a.weightKg || 0,
        sortOrder: a.sortOrder ?? index
      }))
    : defaultActions()
}

function resetTrainingFormData() {
  trainingForm.recordDate = getTodayStr()
  trainingForm.planId = null
  trainingForm.trainingDayId = null
  trainingForm.trainingType = ''
  trainingForm.startTime = null
  trainingForm.endTime = null
  trainingForm.durationMinutes = null
  trainingForm.note = ''
  trainingForm.details = defaultActions()
}

/** 处理从计划加载 */
async function openLoadFromPlanDialog() {
  loadFromPlanDialog.value = true
  const preferredDayId = trainingForm.trainingDayId
  selectedPlanId.value = trainingForm.planId || selectedPlanId.value
  selectedPlanDay.value = trainingForm.trainingDayId || null
  if (planOptions.value.length === 0) {
    await fetchPlans()
  } else if (selectedPlanId.value) {
    await handlePlanSelect(selectedPlanId.value)
  }
  if (preferredDayId && planDays.value.some(day => day.id === preferredDayId)) {
    selectedPlanDay.value = preferredDayId
  }
}

async function handleLoadPlan() {
  if (!selectedPlanId.value) {
    ElMessage.warning('请选择训练计划')
    return
  }
  if (!selectedPlanDay.value) {
    ElMessage.warning('请选择训练日')
    return
  }
  // 从已选训练日加载动作
  const day = planDays.value.find(d => d.id === selectedPlanDay.value)
  if (day && day.actions?.length) {
    const hasExistingActions = trainingForm.details.some(a => a.actionId != null)
    if (hasExistingActions) {
      try {
        await ElMessageBox.confirm(
          '当前动作列表已有内容，从计划加载会覆盖现有动作，是否继续？',
          '确认覆盖',
          { type: 'warning' }
        )
      } catch {
        return
      }
    }
    trainingForm.details = day.actions.map(a => ({
      actionId: a.actionId,
      actionName: a.actionName || '',
      sets: a.sets || 3,
      weightKg: a.weightKg || (a as any).weight || 0,
      sortOrder: a.sortOrder
    }))
    trainingForm.planId = selectedPlanId.value
    trainingForm.trainingDayId = selectedPlanDay.value
    if (day.trainingType) {
      trainingForm.trainingType = day.trainingType
    }
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
  if (trainingForm.startTime && trainingForm.endTime) {
    const duration = calcDurationMinutes(trainingForm.startTime, trainingForm.endTime)
    if (duration === null) {
      ElMessage.warning('结束时间必须晚于开始时间')
      return
    }
    trainingForm.durationMinutes = duration
  }

  // 过滤无效动作（未选择动作的行）
  const validDetails = trainingForm.details.filter(a => a.actionId != null)

  saving.value = true
  try {
    const payload = {
      recordDate: trainingForm.recordDate,
      planId: trainingForm.planId,
      trainingDayId: trainingForm.trainingDayId,
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
  if (route.query.focus === 'calendar') {
    nextTick(() => {
      calendarSectionRef.value?.$el?.scrollIntoView({ behavior: 'smooth', block: 'start' })
    })
  }
})

watch(() => [trainingForm.startTime, trainingForm.endTime], syncDurationFromTime)
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

.training-calendar-card {
  scroll-margin-top: 16px;
}

.calendar-tools {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.calendar-legend {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.legend-item {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: #606266;
}

.legend-dot {
  width: 9px;
  height: 9px;
  border-radius: 50%;
}

.calendar-weekdays {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 8px;
  margin-bottom: 8px;
  color: #909399;
  font-size: 12px;
  text-align: center;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 8px;
}

.calendar-day {
  min-height: 92px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 8px;
  background: #ffffff;
  overflow: hidden;
}

.calendar-day--muted {
  background: #fafafa;
}

.calendar-day--today {
  border-color: #38b589;
  box-shadow: 0 0 0 1px rgba(56, 181, 137, 0.18);
}

.calendar-day-number {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  min-height: 18px;
  margin-bottom: 6px;
}

.calendar-records {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.calendar-record {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 6px;
  min-height: 24px;
  border-radius: 4px;
  padding: 4px 6px;
  color: #ffffff;
  font-size: 12px;
  line-height: 1.2;
}

.calendar-record-type,
.calendar-record-duration {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.calendar-record-type {
  min-width: 0;
}

.calendar-record-duration {
  flex: 0 0 auto;
  opacity: 0.92;
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
