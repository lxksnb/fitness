<template>
  <!-- 组间休息计时器组件：圆形倒计时显示，支持开始/暂停/重置，到时音频提醒 -->
  <div class="rest-timer" :class="{ compact }">
    <!-- 圆形进度环 -->
    <div class="timer-ring" @click="toggle">
      <svg :width="size" :height="size" viewBox="0 0 120 120">
        <!-- 背景圆环 -->
        <circle
          cx="60" cy="60" :r="radius"
          fill="none"
          :stroke="bgColor"
          :stroke-width="strokeWidth"
        />
        <!-- 进度圆环 -->
        <circle
          cx="60" cy="60" :r="radius"
          fill="none"
          :stroke="progressColor"
          :stroke-width="strokeWidth"
          :stroke-dasharray="circumference"
          :stroke-dashoffset="dashOffset"
          stroke-linecap="round"
          transform="rotate(-90 60 60)"
          class="progress-ring"
        />
        <!-- 中心文字：剩余时间 -->
        <text
          x="60" y="52"
          text-anchor="middle"
          :font-size="timeFontSize"
          font-weight="700"
          :fill="textColor"
        >
          {{ displayTime }}
        </text>
        <!-- 状态文字 -->
        <text
          x="60" y="72"
          text-anchor="middle"
          font-size="12"
          fill="#909399"
        >
          {{ statusText }}
        </text>
      </svg>
    </div>

    <!-- 控制按钮组 -->
    <div class="timer-controls" :class="{ column: compact }">
      <el-button-group>
        <el-button
          :type="isRunning ? 'warning' : 'success'"
          size="small"
          @click="toggle"
          :icon="isRunning ? VideoPause : VideoPlay"
        >
          {{ isRunning ? '暂停' : '开始' }}
        </el-button>
        <el-button
          size="small"
          @click="reset"
          :disabled="remaining === totalMinutes * 60 && !isRunning"
          :icon="Refresh"
        >
          重置
        </el-button>
      </el-button-group>

      <!-- 时长调节 -->
      <div class="time-preset">
        <span class="preset-label">时长(min):</span>
        <el-input-number
          v-model="customMinutes"
          :min="0.5"
          :max="30"
          :step="0.5"
          :precision="1"
          size="small"
          controls-position="right"
          style="width: 120px"
          @change="onMinutesChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 组间休息计时器组件
 * 圆形倒计时展示，支持开始/暂停/重置，到达0秒时播放音频提醒
 * Props: defaultMinutes - 默认休息时长(分钟)
 * Emits: complete - 倒计时结束事件
 */
import { ref, computed, watch, onBeforeUnmount } from 'vue'
import { VideoPlay, VideoPause, Refresh } from '@element-plus/icons-vue'

// ==================== Props ====================

const props = withDefaults(defineProps<{
  /** 默认休息时长(分钟)，通常从训练计划获取 */
  defaultMinutes?: number
  /** SVG尺寸 */
  size?: number
  /** 紧凑模式 */
  compact?: boolean
}>(), {
  defaultMinutes: 1.5,
  size: 200,
  compact: false
})

// ==================== Emits ====================

const emit = defineEmits<{
  /** 倒计时完成事件 */
  (e: 'complete'): void
}>()

// ==================== 状态 ====================

/** 自定义分钟数（双向绑定到输入框） */
const customMinutes = ref(props.defaultMinutes)
/** 总秒数 */
const totalMinutes = ref(props.defaultMinutes)
/** 剩余秒数 */
const remaining = ref(props.defaultMinutes * 60)
/** 计时器是否运行中 */
const isRunning = ref(false)
/** 是否已完成一次倒计时 */
const hasCompleted = ref(false)

/** 计时器 interval ID */
let timer: ReturnType<typeof setInterval> | null = null

// ==================== SVG 计算属性 ====================

const radius = 50
const strokeWidth = 8
const circumference = 2 * Math.PI * radius

/** 进度条偏移量：剩余越少，偏移越多 */
const dashOffset = computed(() => {
  const progress = remaining.value / (totalMinutes.value * 60)
  return circumference * (1 - progress)
})

/** 背景色 */
const bgColor = '#e9ecef'
/** 进度色：根据剩余时间变色 */
const progressColor = computed(() => {
  const ratio = remaining.value / (totalMinutes.value * 60)
  if (ratio < 0.2) return '#f56c6c'  // 快结束时红色
  if (ratio < 0.5) return '#e6a23c'  // 过半时橙色
  return '#409eff'                     // 正常蓝色
})
/** 文字颜色 */
const textColor = computed(() => progressColor.value)
/** 时间字体大小：紧凑模式更小 */
const timeFontSize = computed(() => props.compact ? '22' : '26')

/** 格式化显示时间 mm:ss */
const displayTime = computed(() => {
  const m = Math.floor(remaining.value / 60)
  const s = Math.floor(remaining.value % 60)
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
})

/** 状态文字 */
const statusText = computed(() => {
  if (!isRunning.value && hasCompleted.value) return '完成!'
  if (!isRunning.value && remaining.value === totalMinutes.value * 60) return '准备'
  if (!isRunning.value) return '已暂停'
  return '进行中'
})

// ==================== 计时逻辑 ====================

/** 开始 / 暂停切换 */
function toggle() {
  if (isRunning.value) {
    pause()
  } else {
    start()
  }
}

/** 开始计时 */
function start() {
  if (remaining.value <= 0) {
    reset()
  }
  isRunning.value = true
  hasCompleted.value = false

  timer = setInterval(() => {
    if (remaining.value > 0) {
      remaining.value--
    }
    if (remaining.value <= 0) {
      handleComplete()
    }
  }, 1000)
}

/** 暂停计时 */
function pause() {
  isRunning.value = false
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

/** 重置计时器 */
function reset() {
  pause()
  remaining.value = totalMinutes.value * 60
  hasCompleted.value = false
}

/** 处理倒计时完成 */
function handleComplete() {
  pause()
  hasCompleted.value = true
  remaining.value = 0
  // 播放音频提醒
  playAlert()
  emit('complete')
}

/** 播放提示音（使用 Web Audio API） */
function playAlert() {
  try {
    const ctx = new (window.AudioContext || (window as any).webkitAudioContext)()
    // 播放三段短促的蜂鸣声
    const beeps = [0, 0.2, 0.4]
    beeps.forEach((delay) => {
      const osc = ctx.createOscillator()
      const gain = ctx.createGain()
      osc.connect(gain)
      gain.connect(ctx.destination)
      osc.frequency.value = 800
      osc.type = 'sine'
      gain.gain.setValueAtTime(0.3, ctx.currentTime + delay)
      gain.gain.exponentialRampToValueAtTime(0.01, ctx.currentTime + delay + 0.15)
      osc.start(ctx.currentTime + delay)
      osc.stop(ctx.currentTime + delay + 0.15)
    })
  } catch {
    // Web Audio API 不可用，静默处理
  }
}

/** 手动调整时长 */
function onMinutesChange(val: number | undefined) {
  if (val == null) return
  totalMinutes.value = val
  remaining.value = val * 60
  pause()
  hasCompleted.value = false
}

// ==================== 监听 ====================

watch(() => props.defaultMinutes, (val) => {
  if (val != null && val > 0) {
    customMinutes.value = val
    totalMinutes.value = val
    remaining.value = val * 60
    pause()
    hasCompleted.value = false
  }
})

// ==================== 生命周期 ====================

onBeforeUnmount(() => {
  pause()
})
</script>

<style scoped lang="scss">
/**
 * 组间休息计时器样式
 * 圆形进度环 + 控制按钮，支持紧凑模式
 */

.rest-timer {
  display: flex;
  align-items: center;
  gap: 16px;

  &.compact {
    flex-direction: row;
    gap: 12px;

    .timer-ring {
      cursor: pointer;
    }
  }

  .timer-ring {
    cursor: pointer;
    user-select: none;
    flex-shrink: 0;

    svg {
      display: block;
    }

    .progress-ring {
      transition: stroke-dashoffset 0.9s linear, stroke 0.5s ease;
    }
  }

  .timer-controls {
    display: flex;
    flex-direction: column;
    gap: 10px;

    &.column {
      flex-direction: column;
    }

    .time-preset {
      display: flex;
      align-items: center;
      gap: 6px;

      .preset-label {
        font-size: 13px;
        color: #606266;
        white-space: nowrap;
      }
    }
  }
}
</style>
