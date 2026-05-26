<template>
  <!-- 健身计划编辑器：支持创建/编辑，包含基本信息、训练日安排（嵌套动作）、餐次配置（训练日/休息日各7餐） -->
  <div class="plan-editor-page">
    <!-- ==================== 页面标题栏 ==================== -->
    <div class="page-header">
      <h2>{{ isEdit ? '编辑健身计划' : '创建健身计划' }}</h2>
      <el-button @click="$router.push('/plans')">
        <el-icon><ArrowLeft /></el-icon>
        返回计划列表
      </el-button>
    </div>

    <!-- ==================== 加载骨架 ==================== -->
    <template v-if="loading">
      <el-card class="section"><el-skeleton :rows="6" animated /></el-card>
      <el-card class="section"><el-skeleton :rows="8" animated /></el-card>
      <el-card class="section"><el-skeleton :rows="5" animated /></el-card>
    </template>

    <!-- ==================== 错误状态 ==================== -->
    <template v-else-if="error">
      <el-result icon="error" title="数据加载失败" :sub-title="error">
        <template #extra>
          <el-button type="primary" @click="loadPageData">重新加载</el-button>
        </template>
      </el-result>
    </template>

    <!-- ==================== 编辑表单 ==================== -->
    <template v-else>
      <!-- 第1部分：基本信息 -->
      <el-card class="section">
        <template #header>
          <div class="section-header"><span class="section-title">基本信息</span></div>
        </template>

        <el-form :model="formData" label-width="100px">
          <el-row :gutter="20">
            <el-col :xs="24" :sm="8">
              <el-form-item label="计划名称" required>
                <el-input v-model="formData.planName" placeholder="请输入计划名称" maxlength="50" show-word-limit />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="8">
              <el-form-item label="计划类型" required>
                <el-select v-model="formData.planType" style="width: 100%">
                  <el-option
                    v-for="item in planTypeOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="8">
              <el-form-item label="分化方式" required>
                <el-select v-model="formData.splitType" style="width: 100%">
                  <el-option
                    v-for="item in splitTypeOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-card>

      <!-- 第2部分：训练日安排 -->
      <el-card class="section">
        <template #header>
          <div class="section-header">
            <span class="section-title">训练日安排</span>
            <div class="section-header-actions">
              <el-button type="primary" @click="addTrainingDay">
                <el-icon><Plus /></el-icon>
                添加训练日
              </el-button>
              <el-button @click="addRestDay">
                <el-icon><Plus /></el-icon>
                添加休息日
              </el-button>
            </div>
          </div>
        </template>

        <template v-if="formData.trainingDays.length === 0">
          <el-empty description="还没有安排训练日，请点击上方按钮添加" :image-size="80" />
        </template>

        <div
          v-for="(day, dayIndex) in formData.trainingDays"
          :key="day._key"
          class="day-card"
        >
          <!-- 日卡片头部：天数 + 类型切换 + 删除 -->
          <div class="day-header">
            <span class="day-title">第{{ day.dayOrder }}天</span>
            <el-radio-group
              v-model="day.dayType"
              size="small"
              @change="() => onDayTypeChange(day)"
            >
              <el-radio-button value="TRAINING">训练日</el-radio-button>
              <el-radio-button value="REST">休息日</el-radio-button>
            </el-radio-group>
            <div class="day-header-right">
              <el-button
                size="small"
                @click="moveDay(dayIndex, -1)"
                :disabled="dayIndex === 0"
                :icon="ArrowUp"
              />
              <el-button
                size="small"
                @click="moveDay(dayIndex, 1)"
                :disabled="dayIndex === formData.trainingDays.length - 1"
                :icon="ArrowDown"
              />
              <el-button type="danger" link @click="removeDay(dayIndex)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
          </div>

          <!-- 日卡片内容 -->
          <div class="day-body">
            <el-row :gutter="16">
              <!-- 训练部位：仅训练日可见 -->
              <el-col :xs="24" :sm="6" v-if="day.dayType === 'TRAINING'">
                <el-form-item label="训练部位" label-width="80px">
                  <el-select
                    v-model="day.trainingType"
                    multiple
                    collapse-tags
                    collapse-tags-tooltip
                    style="width: 100%"
                  >
                    <el-option
                      v-for="item in trainingTypeOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <!-- 营养倍数 -->
              <el-col :xs="8" :sm="6">
                <el-form-item label="碳水倍数" label-width="80px">
                  <el-input-number
                    v-model="day.carbMultiplier"
                    :min="0"
                    :step="0.1"
                    :precision="1"
                    size="small"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
              <el-col :xs="8" :sm="6">
                <el-form-item label="蛋白倍数" label-width="80px">
                  <el-input-number
                    v-model="day.proteinMultiplier"
                    :min="0"
                    :step="0.1"
                    :precision="1"
                    size="small"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
              <el-col :xs="8" :sm="6">
                <el-form-item label="脂肪倍数" label-width="80px">
                  <el-input-number
                    v-model="day.fatMultiplier"
                    :min="0"
                    :step="0.1"
                    :precision="1"
                    size="small"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <!-- 动作列表：仅训练日可见 -->
            <template v-if="day.dayType === 'TRAINING'">
              <div class="sub-section">
                <div class="sub-section-header">
                  <span class="sub-section-title">动作列表</span>
                  <el-button size="small" type="primary" @click="addAction(dayIndex)">
                    <el-icon><Plus /></el-icon>
                    添加动作
                  </el-button>
                </div>

                <el-table
                  v-if="day.actions.length > 0"
                  :data="day.actions"
                  border
                  size="small"
                  class="action-table"
                >
                  <el-table-column label="动作名称" min-width="200">
                    <template #default="{ row, $index }">
                      <div class="action-name-cell">
                        <span :class="{ 'action-empty': !row.actionName }">{{ row.actionName || '未选择动作' }}</span>
                        <el-button link type="primary" size="small" @click="openActionPicker(dayIndex, $index)">
                          选择
                        </el-button>
                      </div>
                    </template>
                  </el-table-column>
                  <el-table-column label="最小组数" width="100">
                    <template #default="{ row }">
                      <el-input-number
                        v-model="row.minSets"
                        :min="1"
                        :max="10"
                        size="small"
                        style="width: 100%"
                      />
                    </template>
                  </el-table-column>
                  <el-table-column label="最大组数" width="100">
                    <template #default="{ row }">
                      <el-input-number
                        v-model="row.maxSets"
                        :min="1"
                        :max="10"
                        size="small"
                        style="width: 100%"
                      />
                    </template>
                  </el-table-column>
                  <el-table-column label="休息(分)" width="100">
                    <template #default="{ row }">
                      <el-input-number
                        v-model="row.restMinutes"
                        :min="0"
                        :max="10"
                        :step="0.5"
                        :precision="1"
                        size="small"
                        style="width: 100%"
                      />
                    </template>
                  </el-table-column>
                  <el-table-column label="排序" width="80">
                    <template #default="{ row }">
                      <el-input-number
                        v-model="row.sortOrder"
                        :min="0"
                        size="small"
                        style="width: 100%"
                        controls-position="right"
                      />
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="60" fixed="right">
                    <template #default="{ $index }">
                      <el-button
                        type="danger"
                        link
                        size="small"
                        @click="removeAction(dayIndex, $index)"
                      >
                        <el-icon><Delete /></el-icon>
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>

                <el-empty
                  v-else
                  description="还没有添加动作"
                  :image-size="60"
                  style="padding: 16px 0"
                />
              </div>
            </template>
          </div>
        </div>
      </el-card>

      <!-- 第3部分：餐次配置 -->
      <el-card class="section">
        <template #header>
          <div class="section-header">
            <span class="section-title">餐次配置</span>
          </div>
        </template>

        <el-tabs v-model="activeMealTab" type="border-card">
          <!-- 训练日餐次 -->
          <el-tab-pane label="训练日餐次" name="TRAINING">
            <div class="meal-config-list">
              <div
                v-for="config in trainingMealConfigs"
                :key="'train-' + config.mealType"
                class="meal-config-row"
              >
                <!-- 餐次行头部 -->
                <div class="meal-config-header">
                  <el-tag type="primary" effect="dark" size="default">
                    {{ getMealLabel(config.mealType) }}
                  </el-tag>
                </div>

                <!-- 餐次比率输入 -->
                <el-row :gutter="12" align="middle">
                  <el-col :xs="8" :sm="6">
                    <el-form-item label="碳水比例" label-width="80px">
                      <el-input-number
                        :model-value="config.carbRatio"
                        @update:model-value="(v: number | undefined) => config.carbRatio = v ?? 0"
                        :min="0"
                        :max="100"
                        :step="1"
                        :precision="0"
                        size="small"
                        style="width: 100%"
                      />
                      <span class="unit-suffix">%</span>
                    </el-form-item>
                  </el-col>
                  <el-col :xs="8" :sm="6">
                    <el-form-item label="蛋白比例" label-width="80px">
                      <el-input-number
                        :model-value="config.proteinRatio"
                        @update:model-value="(v: number | undefined) => config.proteinRatio = v ?? 0"
                        :min="0"
                        :max="100"
                        :step="1"
                        :precision="0"
                        size="small"
                        style="width: 100%"
                      />
                      <span class="unit-suffix">%</span>
                    </el-form-item>
                  </el-col>
                  <el-col :xs="8" :sm="6">
                    <el-form-item label="脂肪比例" label-width="80px">
                      <el-input-number
                        :model-value="config.fatRatio"
                        @update:model-value="(v: number | undefined) => config.fatRatio = v ?? 0"
                        :min="0"
                        :max="100"
                        :step="1"
                        :precision="0"
                        size="small"
                        style="width: 100%"
                      />
                      <span class="unit-suffix">%</span>
                    </el-form-item>
                  </el-col>
                  <el-col :xs="24" :sm="6">
                    <el-button
                      link
                      type="primary"
                      @click="toggleFoodExpanded(config)"
                      style="margin-left: 12px"
                    >
                      {{ config._expanded ? '收起推荐食物' : '展开推荐食物' }}
                      ({{ config.foods.length }})
                    </el-button>
                  </el-col>
                </el-row>

                <!-- 展开推荐食物列表 -->
                <div v-if="config._expanded" class="food-section">
                  <div
                    v-for="(food, foodIdx) in config.foods"
                    :key="foodIdx"
                    class="food-row"
                  >
                    <el-select
                      :model-value="food.foodId"
                      filterable
                      remote
                      reserve-keyword
                      placeholder="搜索食物"
                      :remote-method="(kw: string) => searchFoodsRemote(kw)"
                      :loading="foodSearchLoading"
                      style="width: 260px"
                      @update:model-value="(val: number) => onFoodSelect(val, config, foodIdx)"
                      @change="(val: number) => onFoodSelect(val, config, foodIdx)"
                    >
                      <el-option
                        v-for="item in foodSearchResults"
                        :key="item.id"
                        :label="item.foodName"
                        :value="item.id"
                      />
                    </el-select>
                    <el-input-number
                      v-model="food.suggestedAmountG"
                      :min="0"
                      :step="10"
                      placeholder="建议克数"
                      size="small"
                      style="width: 140px; margin-left: 8px"
                    />
                    <span style="margin-left: 4px; color: #909399; font-size: 13px">g</span>
                    <el-button
                      type="danger"
                      link
                      size="small"
                      @click="removeFood(config, foodIdx)"
                      style="margin-left: 8px"
                    >
                      <el-icon><Delete /></el-icon>
                    </el-button>
                  </div>
                  <el-button size="small" type="primary" link @click="addFood(config)">
                    <el-icon><Plus /></el-icon>
                    添加食物
                  </el-button>
                </div>
              </div>

              <!-- 训练日比率汇总 -->
              <div class="meal-summary">
                <span :class="{ 'ratio-ok': trainingCarbOk, 'ratio-warn': !trainingCarbOk }">
                  碳水合计: {{ trainingCarbTotal }}%
                </span>
                <span :class="{ 'ratio-ok': trainingProteinOk, 'ratio-warn': !trainingProteinOk }">
                  蛋白质合计: {{ trainingProteinTotal }}%
                </span>
                <span :class="{ 'ratio-ok': trainingFatOk, 'ratio-warn': !trainingFatOk }">
                  脂肪合计: {{ trainingFatTotal }}%
                </span>
              </div>
            </div>
          </el-tab-pane>

          <!-- 休息日餐次 -->
          <el-tab-pane label="休息日餐次" name="REST">
            <div class="meal-config-list">
              <div
                v-for="config in restMealConfigs"
                :key="'rest-' + config.mealType"
                class="meal-config-row"
              >
                <div class="meal-config-header">
                  <el-tag type="info" effect="dark" size="default">
                    {{ getMealLabel(config.mealType) }}
                  </el-tag>
                </div>

                <el-row :gutter="12" align="middle">
                  <el-col :xs="8" :sm="6">
                    <el-form-item label="碳水比例" label-width="80px">
                      <el-input-number
                        :model-value="config.carbRatio"
                        @update:model-value="(v: number | undefined) => config.carbRatio = v ?? 0"
                        :min="0"
                        :max="100"
                        :step="1"
                        :precision="0"
                        size="small"
                        style="width: 100%"
                      />
                      <span class="unit-suffix">%</span>
                    </el-form-item>
                  </el-col>
                  <el-col :xs="8" :sm="6">
                    <el-form-item label="蛋白比例" label-width="80px">
                      <el-input-number
                        :model-value="config.proteinRatio"
                        @update:model-value="(v: number | undefined) => config.proteinRatio = v ?? 0"
                        :min="0"
                        :max="100"
                        :step="1"
                        :precision="0"
                        size="small"
                        style="width: 100%"
                      />
                      <span class="unit-suffix">%</span>
                    </el-form-item>
                  </el-col>
                  <el-col :xs="8" :sm="6">
                    <el-form-item label="脂肪比例" label-width="80px">
                      <el-input-number
                        :model-value="config.fatRatio"
                        @update:model-value="(v: number | undefined) => config.fatRatio = v ?? 0"
                        :min="0"
                        :max="100"
                        :step="1"
                        :precision="0"
                        size="small"
                        style="width: 100%"
                      />
                      <span class="unit-suffix">%</span>
                    </el-form-item>
                  </el-col>
                  <el-col :xs="24" :sm="6">
                    <el-button
                      link
                      type="primary"
                      @click="toggleFoodExpanded(config)"
                      style="margin-left: 12px"
                    >
                      {{ config._expanded ? '收起推荐食物' : '展开推荐食物' }}
                      ({{ config.foods.length }})
                    </el-button>
                  </el-col>
                </el-row>

                <div v-if="config._expanded" class="food-section">
                  <div
                    v-for="(food, foodIdx) in config.foods"
                    :key="foodIdx"
                    class="food-row"
                  >
                    <el-select
                      :model-value="food.foodId"
                      filterable
                      remote
                      reserve-keyword
                      placeholder="搜索食物"
                      :remote-method="(kw: string) => searchFoodsRemote(kw)"
                      :loading="foodSearchLoading"
                      style="width: 260px"
                      @update:model-value="(val: number) => onFoodSelect(val, config, foodIdx)"
                      @change="(val: number) => onFoodSelect(val, config, foodIdx)"
                    >
                      <el-option
                        v-for="item in foodSearchResults"
                        :key="item.id"
                        :label="item.foodName"
                        :value="item.id"
                      />
                    </el-select>
                    <el-input-number
                      v-model="food.suggestedAmountG"
                      :min="0"
                      :step="10"
                      placeholder="建议克数"
                      size="small"
                      style="width: 140px; margin-left: 8px"
                    />
                    <span style="margin-left: 4px; color: #909399; font-size: 13px">g</span>
                    <el-button
                      type="danger"
                      link
                      size="small"
                      @click="removeFood(config, foodIdx)"
                      style="margin-left: 8px"
                    >
                      <el-icon><Delete /></el-icon>
                    </el-button>
                  </div>
                  <el-button size="small" type="primary" link @click="addFood(config)">
                    <el-icon><Plus /></el-icon>
                    添加食物
                  </el-button>
                </div>
              </div>

              <!-- 休息日比率汇总 -->
              <div class="meal-summary">
                <span :class="{ 'ratio-ok': restCarbOk, 'ratio-warn': !restCarbOk }">
                  碳水合计: {{ restCarbTotal }}%
                </span>
                <span :class="{ 'ratio-ok': restProteinOk, 'ratio-warn': !restProteinOk }">
                  蛋白质合计: {{ restProteinTotal }}%
                </span>
                <span :class="{ 'ratio-ok': restFatOk, 'ratio-warn': !restFatOk }">
                  脂肪合计: {{ restFatTotal }}%
                </span>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-card>

      <!-- 底部操作栏 -->
      <div class="form-footer">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit" size="large">
          {{ isEdit ? '更新计划' : '保存计划' }}
        </el-button>
      </div>

      <el-dialog
        v-model="actionPickerVisible"
        title="选择训练动作"
        width="860px"
        :close-on-click-modal="false"
      >
        <div class="action-picker">
          <aside class="muscle-filter">
            <div class="picker-title">肌群</div>
            <el-checkbox-group v-model="selectedMuscleCodes">
              <el-checkbox
                v-for="item in muscleGroupOptions"
                :key="item.value"
                :label="item.value"
                :value="item.value"
              >
                {{ item.label }}
              </el-checkbox>
            </el-checkbox-group>
          </aside>
          <section class="action-picker-main">
            <el-input
              v-model="actionPickerKeyword"
              placeholder="搜索动作名称"
              clearable
              @keyup.enter="searchActionsRemote"
              @clear="searchActionsRemote"
            >
              <template #append>
                <el-button @click="searchActionsRemote">搜索</el-button>
              </template>
            </el-input>
            <el-table
              v-loading="actionSearchLoading"
              :data="filteredActionResults"
              row-key="id"
              border
              height="360"
              style="width: 100%; margin-top: 12px"
              @selection-change="onPickerSelectionChange"
            >
              <el-table-column type="selection" width="48" />
              <el-table-column prop="actionName" label="动作名称" min-width="160" />
              <el-table-column label="主要肌群" min-width="180">
                <template #default="{ row }">
                  <el-tag
                    v-for="code in row.primaryMuscles"
                    :key="code"
                    size="small"
                    type="danger"
                    effect="plain"
                    class="picker-tag"
                  >
                    {{ getMuscleLabel(code) }}
                  </el-tag>
                  <span v-if="!row.primaryMuscles?.length" class="muted">--</span>
                </template>
              </el-table-column>
              <el-table-column label="辅助肌群" min-width="180">
                <template #default="{ row }">
                  <el-tag
                    v-for="code in row.secondaryMuscles"
                    :key="code"
                    size="small"
                    type="warning"
                    effect="plain"
                    class="picker-tag"
                  >
                    {{ getMuscleLabel(code) }}
                  </el-tag>
                  <span v-if="!row.secondaryMuscles?.length" class="muted">--</span>
                </template>
              </el-table-column>
            </el-table>
          </section>
        </div>

        <template #footer>
          <el-button @click="actionPickerVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmActionPicker">添加到训练日</el-button>
        </template>
      </el-dialog>
    </template>
  </div>
</template>

<script setup lang="ts">
/**
 * 健身计划编辑器
 * 支持创建和编辑模式，包含：
 * - 基本信息：计划名称、类型、分化方式
 * - 训练日安排：动态添加/删除训练日和休息日，嵌套动作列表
 * - 餐次配置：训练日和休息日各7餐的营养素比例分配，可展开推荐食物
 * - 前端校验：比例总和约100%、必须包含训练日
 */
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft, Plus, Delete, ArrowUp, ArrowDown
} from '@element-plus/icons-vue'
import { createPlan, updatePlan, getPlanDetail } from '@/api/plan'
import { searchActions } from '@/api/action'
import { searchFoods } from '@/api/food'
import { getDictOptions, type DictOption } from '@/api/dict'

const router = useRouter()
const route = useRoute()

// ==================== 模式判断 ====================

/** 是否为编辑模式（路由存在 :id 参数） */
const isEdit = computed(() => !!route.params.id)

/** 编辑模式下的计划ID */
const editId = computed(() => Number(route.params.id))

// ==================== 字典选项 ====================

interface MealTypeOption extends DictOption {
  type: string
  sortOrder: number
}

const planTypeOptions = ref<DictOption[]>([])
const splitTypeOptions = ref<DictOption[]>([])
const trainingTypeOptions = ref<DictOption[]>([])
const muscleGroupOptions = ref<DictOption[]>([])
const mealTypeOptions = ref<MealTypeOption[]>([])

/** 训练日默认营养素分配比例（百分比，合计约100%） */
const DEFAULT_TRAINING_RATIOS: Record<string, { carb: number; protein: number; fat: number }> = {
  BREAKFAST:     { carb: 20, protein: 15, fat: 15 },
  LUNCH:         { carb: 30, protein: 25, fat: 20 },
  DINNER:        { carb: 25, protein: 20, fat: 25 },
  SUPPER:        { carb: 0,  protein: 5,  fat: 10 },
  PRE_WORKOUT:   { carb: 15, protein: 15, fat: 5  },
  POST_WORKOUT:  { carb: 10, protein: 15, fat: 5  },
  OTHER:         { carb: 0,  protein: 5,  fat: 20 }
}

/** 休息日默认营养素分配比例（百分比，合计约100%） */
const DEFAULT_REST_RATIOS: Record<string, { carb: number; protein: number; fat: number }> = {
  BREAKFAST:     { carb: 15, protein: 20, fat: 15 },
  LUNCH:         { carb: 25, protein: 25, fat: 20 },
  DINNER:        { carb: 20, protein: 20, fat: 20 },
  SUPPER:        { carb: 5,  protein: 10, fat: 15 },
  PRE_WORKOUT:   { carb: 10, protein: 10, fat: 5  },
  POST_WORKOUT:  { carb: 10, protein: 10, fat: 5  },
  OTHER:         { carb: 15, protein: 5,  fat: 20 }
}

// ==================== 类型定义 ====================

/** 动作项 */
interface ActionItem {
  actionId: number | null
  actionName: string
  minSets: number
  maxSets: number
  restMinutes: number
  sortOrder: number
}

/** 推荐食物项 */
interface FoodItem {
  foodId: number | null
  foodName: string
  suggestedAmountG: number
}

/** 训练日项 */
interface TrainingDayItem {
  _key: string
  dayOrder: number
  dayType: 'TRAINING' | 'REST'
  trainingType: string[]
  carbMultiplier: number
  proteinMultiplier: number
  fatMultiplier: number
  actions: ActionItem[]
}

/** 餐次配置项 */
interface MealConfigItem {
  dayType: 'TRAINING' | 'REST'
  mealType: string
  carbRatio: number
  proteinRatio: number
  fatRatio: number
  sortOrder: number
  foods: FoodItem[]
  _expanded: boolean
}

// ==================== 表单状态 ====================

const loading = ref(false)
const error = ref('')
const submitting = ref(false)
const activeMealTab = ref('TRAINING')

/** 表单数据 */
const formData = reactive<{
  planName: string
  planType: string
  splitType: string
  trainingDays: TrainingDayItem[]
  mealConfigs: MealConfigItem[]
}>({
  planName: '',
  planType: 'BULK',
  splitType: 'FOUR_DAY',
  trainingDays: [],
  mealConfigs: []
})

/** 全局唯一递增key，用于列表渲染 */
let uidCounter = 0
function nextUid(): string {
  return 'uid_' + (++uidCounter)
}

interface ActionSearchItem {
  id: number
  actionName: string
  primaryMuscles?: string[]
  secondaryMuscles?: string[]
  suitableFor?: string[] | string
}

function getPreferredOptionValue(options: DictOption[], preferred: string): string {
  return options.find(item => item.value === preferred)?.value || options[0]?.value || ''
}

async function fetchDictData(): Promise<void> {
  const [planTypes, splitTypes, trainingTypes, muscleGroups, mealTypes] = await Promise.all([
    getDictOptions('plan_type'),
    getDictOptions('split_type'),
    getDictOptions('training_type'),
    getDictOptions('muscle_group'),
    getDictOptions('meal_type')
  ])

  planTypeOptions.value = planTypes
  splitTypeOptions.value = splitTypes
  trainingTypeOptions.value = trainingTypes.filter(item => item.value !== 'REST')
  muscleGroupOptions.value = muscleGroups
  mealTypeOptions.value = mealTypes.map(item => ({
    ...item,
    type: item.value,
    sortOrder: item.sort || 0
  }))

  if (!planTypeOptions.value.length || !splitTypeOptions.value.length ||
      !trainingTypeOptions.value.length || !muscleGroupOptions.value.length || !mealTypeOptions.value.length) {
    throw new Error('基础字典数据为空，请检查字典配置')
  }
}

// ==================== 表单初始化 ====================

/** 创建默认训练日 */
function createDefaultTrainingDay(order: number): TrainingDayItem {
  return {
    _key: nextUid(),
    dayOrder: order,
    dayType: 'TRAINING',
    trainingType: [getPreferredOptionValue(trainingTypeOptions.value, 'CHEST')].filter(Boolean),
    carbMultiplier: 4.0,
    proteinMultiplier: 2.0,
    fatMultiplier: 1.0,
    actions: []
  }
}

/** 创建默认休息日 */
function createDefaultRestDay(order: number): TrainingDayItem {
  return {
    _key: nextUid(),
    dayOrder: order,
    dayType: 'REST',
    trainingType: [],
    carbMultiplier: 1.0,
    proteinMultiplier: 1.5,
    fatMultiplier: 1.2,
    actions: []
  }
}

/** 初始化14个餐次配置项（训练日7 + 休息日7） */
function initMealConfigs(): MealConfigItem[] {
  const configs: MealConfigItem[] = []

  for (const mt of mealTypeOptions.value) {
    const defaults = DEFAULT_TRAINING_RATIOS[mt.type]
    configs.push({
      dayType: 'TRAINING',
      mealType: mt.type,
      carbRatio: defaults?.carb ?? 0,
      proteinRatio: defaults?.protein ?? 0,
      fatRatio: defaults?.fat ?? 0,
      sortOrder: mt.sortOrder,
      foods: [],
      _expanded: false
    })
  }

  for (const mt of mealTypeOptions.value) {
    const defaults = DEFAULT_REST_RATIOS[mt.type]
    configs.push({
      dayType: 'REST',
      mealType: mt.type,
      carbRatio: defaults?.carb ?? 0,
      proteinRatio: defaults?.protein ?? 0,
      fatRatio: defaults?.fat ?? 0,
      sortOrder: mt.sortOrder,
      foods: [],
      _expanded: false
    })
  }

  return configs
}

/** 初始化表单数据（新创建模式） */
function initFormData(): void {
  formData.planName = ''
  formData.planType = getPreferredOptionValue(planTypeOptions.value, 'BULK')
  formData.splitType = getPreferredOptionValue(splitTypeOptions.value, 'FOUR_DAY')
  formData.trainingDays = [createDefaultTrainingDay(1)]
  formData.mealConfigs = initMealConfigs()
  uidCounter = 0
}

// ==================== 计算属性：餐次配置拆分 ====================

/** 训练日餐次配置（按排序字段排列） */
const trainingMealConfigs = computed(() =>
  formData.mealConfigs
    .filter(c => c.dayType === 'TRAINING')
    .sort((a, b) => a.sortOrder - b.sortOrder)
)

/** 休息日餐次配置 */
const restMealConfigs = computed(() =>
  formData.mealConfigs
    .filter(c => c.dayType === 'REST')
    .sort((a, b) => a.sortOrder - b.sortOrder)
)

// ==================== 计算属性：比率汇总 ====================

function sumField(configs: MealConfigItem[], field: 'carbRatio' | 'proteinRatio' | 'fatRatio'): number {
  return configs.reduce((sum, c) => sum + c[field], 0)
}

const trainingCarbTotal = computed(() => sumField(trainingMealConfigs.value, 'carbRatio'))
const trainingProteinTotal = computed(() => sumField(trainingMealConfigs.value, 'proteinRatio'))
const trainingFatTotal = computed(() => sumField(trainingMealConfigs.value, 'fatRatio'))

const restCarbTotal = computed(() => sumField(restMealConfigs.value, 'carbRatio'))
const restProteinTotal = computed(() => sumField(restMealConfigs.value, 'proteinRatio'))
const restFatTotal = computed(() => sumField(restMealConfigs.value, 'fatRatio'))

const RATIO_TOLERANCE = 5 // 允许±5%容差

function isRatioValid(total: number): boolean {
  return total >= 100 - RATIO_TOLERANCE && total <= 100 + RATIO_TOLERANCE
}

const trainingCarbOk = computed(() => isRatioValid(trainingCarbTotal.value))
const trainingProteinOk = computed(() => isRatioValid(trainingProteinTotal.value))
const trainingFatOk = computed(() => isRatioValid(trainingFatTotal.value))

const restCarbOk = computed(() => isRatioValid(restCarbTotal.value))
const restProteinOk = computed(() => isRatioValid(restProteinTotal.value))
const restFatOk = computed(() => isRatioValid(restFatTotal.value))

/** 训练日所有比例是否都合理 */
const isTrainingRatioValid = computed(() =>
  trainingCarbOk.value && trainingProteinOk.value && trainingFatOk.value
)

/** 休息日所有比例是否都合理 */
const isRestRatioValid = computed(() =>
  restCarbOk.value && restProteinOk.value && restFatOk.value
)

// ==================== 远程搜索 ====================

const actionSearchResults = ref<ActionSearchItem[]>([])
const actionSearchLoading = ref(false)
const actionPickerVisible = ref(false)
const actionPickerDayIndex = ref<number | null>(null)
const actionPickerActionIndex = ref<number | null>(null)
const actionPickerKeyword = ref('')
const selectedMuscleCodes = ref<string[]>([])
const selectedPickerActions = ref<ActionSearchItem[]>([])

function normalizeTrainingTypes(value: unknown): string[] {
  if (Array.isArray(value)) return value.map(String).filter(Boolean)
  if (typeof value === 'string') return value.split(',').filter(Boolean)
  return []
}

function normalizeCodeList(value: unknown): string[] {
  if (Array.isArray(value)) return value.map(String).filter(Boolean)
  if (typeof value === 'string') return value.split(',').filter(Boolean)
  return []
}

function normalizeActionSearchItem(item: any): ActionSearchItem {
  const primaryMuscles = normalizeCodeList(item.primaryMuscles)
  return {
    ...item,
    primaryMuscles: primaryMuscles.length > 0 ? primaryMuscles : normalizeCodeList(item.suitableFor),
    secondaryMuscles: normalizeCodeList(item.secondaryMuscles)
  }
}

const filteredActionResults = computed(() => {
  if (!selectedMuscleCodes.value.length) return actionSearchResults.value
  const selected = new Set(selectedMuscleCodes.value)
  return actionSearchResults.value.filter(action => {
    const muscles = [
      ...(action.primaryMuscles || []),
      ...(action.secondaryMuscles || [])
    ]
    return muscles.some(code => selected.has(code))
  })
})

function getMuscleLabel(code: string): string {
  return muscleGroupOptions.value.find(item => item.value === code)?.label || code
}

async function searchActionsRemote(keyword?: string) {
  actionSearchLoading.value = true
  try {
    const kw = (typeof keyword === 'string' ? keyword : actionPickerKeyword.value).trim()
    const res = await searchActions(kw || undefined) as any
    const list = Array.isArray(res) ? res : (res?.records || res?.list || [])
    actionSearchResults.value = list.map(normalizeActionSearchItem)
  } catch {
    actionSearchResults.value = []
  } finally {
    actionSearchLoading.value = false
  }
}

const foodSearchResults = ref<any[]>([])
const foodSearchLoading = ref(false)

async function searchFoodsRemote(keyword: string) {
  if (!keyword || keyword.trim().length === 0) {
    foodSearchResults.value = []
    return
  }
  foodSearchLoading.value = true
  try {
    const res = await searchFoods(keyword.trim()) as any
    foodSearchResults.value = Array.isArray(res) ? res : (res?.records || res?.list || [])
  } catch {
    foodSearchResults.value = []
  } finally {
    foodSearchLoading.value = false
  }
}

// ==================== 选择事件处理 ====================

function openActionPicker(dayIndex: number, actionIndex?: number) {
  actionPickerDayIndex.value = dayIndex
  actionPickerActionIndex.value = typeof actionIndex === 'number' ? actionIndex : null
  selectedPickerActions.value = []
  actionPickerVisible.value = true
  searchActionsRemote()
}

function onPickerSelectionChange(selection: ActionSearchItem[]) {
  selectedPickerActions.value = selection
}

function createPlanAction(action: ActionSearchItem, sortOrder: number): ActionItem {
  return {
    actionId: action.id,
    actionName: action.actionName,
    minSets: 3,
    maxSets: 5,
    restMinutes: 2,
    sortOrder
  }
}

function confirmActionPicker() {
  if (actionPickerDayIndex.value === null) return
  if (!selectedPickerActions.value.length) {
    ElMessage.warning('请至少选择一个动作')
    return
  }

  const day = formData.trainingDays[actionPickerDayIndex.value]
  if (actionPickerActionIndex.value !== null && selectedPickerActions.value.length === 1) {
    day.actions[actionPickerActionIndex.value] = {
      ...day.actions[actionPickerActionIndex.value],
      ...createPlanAction(selectedPickerActions.value[0], actionPickerActionIndex.value)
    }
  } else {
    selectedPickerActions.value.forEach(action => {
      const exists = day.actions.some(item => item.actionId === action.id)
      if (!exists) {
        day.actions.push(createPlanAction(action, day.actions.length))
      }
    })
  }

  day.actions.forEach((action, index) => {
    action.sortOrder = index
  })
  actionPickerVisible.value = false
}

/** 食物选中：从搜索结果中获取名称赋值 */
function onFoodSelect(val: number, config: MealConfigItem, foodIndex: number) {
  const selected = foodSearchResults.value.find((item: any) => item.id === val)
  if (selected) {
    config.foods[foodIndex].foodId = selected.id
    config.foods[foodIndex].foodName = selected.foodName || selected.name || ''
  }
}

// ==================== 训练日操作 ====================

/** 添加训练日 */
function addTrainingDay() {
  const newOrder = formData.trainingDays.length + 1
  formData.trainingDays.push(createDefaultTrainingDay(newOrder))
}

/** 添加休息日 */
function addRestDay() {
  const newOrder = formData.trainingDays.length + 1
  formData.trainingDays.push(createDefaultRestDay(newOrder))
}

/** 删除训练日 */
function removeDay(index: number) {
  formData.trainingDays.splice(index, 1)
  // 重新编排dayOrder
  formData.trainingDays.forEach((day, i) => {
    day.dayOrder = i + 1
  })
}

/** 上移/下移训练日 */
function moveDay(index: number, direction: number) {
  const target = index + direction
  if (target < 0 || target >= formData.trainingDays.length) return
  const temp = formData.trainingDays[index]
  formData.trainingDays[index] = formData.trainingDays[target]
  formData.trainingDays[target] = temp
  // 重新编排dayOrder
  formData.trainingDays.forEach((day, i) => {
    day.dayOrder = i + 1
  })
}

/** 切换训练日/休息日类型 */
function onDayTypeChange(day: TrainingDayItem) {
  // 切换为休息日时清空动作列表
  if (day.dayType === 'REST') {
    day.actions = []
  }
}

// ==================== 动作操作 ====================

/** 添加动作到指定训练日 */
function addAction(dayIndex: number) {
  openActionPicker(dayIndex)
}

/** 删除指定训练日的动作 */
function removeAction(dayIndex: number, actionIndex: number) {
  formData.trainingDays[dayIndex].actions.splice(actionIndex, 1)
  // 重新编排sortOrder
  formData.trainingDays[dayIndex].actions.forEach((a, i) => {
    a.sortOrder = i
  })
}

// ==================== 餐次与食物操作 ====================

/** 展开/收起推荐食物 */
function toggleFoodExpanded(config: MealConfigItem) {
  config._expanded = !config._expanded
}

/** 添加食物到指定餐次配置 */
function addFood(config: MealConfigItem) {
  config.foods.push({
    foodId: null,
    foodName: '',
    suggestedAmountG: 100
  })
}

/** 删除指定餐次配置中的食物 */
function removeFood(config: MealConfigItem, foodIndex: number) {
  config.foods.splice(foodIndex, 1)
}

// ==================== 工具函数 ====================

/** 获取餐次中文名称 */
function getMealLabel(mealType: string): string {
  const found = mealTypeOptions.value.find(m => m.type === mealType)
  return found ? found.label : mealType
}

// ==================== 表单校验 ====================

function validateForm(): { valid: boolean; message: string } {
  // 1. 计划名称不能为空
  if (!formData.planName.trim()) {
    return { valid: false, message: '请输入计划名称' }
  }

  // 2. 至少包含一个训练日
  const hasTrainingDay = formData.trainingDays.some(d => d.dayType === 'TRAINING')
  if (!hasTrainingDay) {
    return { valid: false, message: '请至少添加一个训练日' }
  }

  // 3. 校验每个训练日的动作是否选择了动作名称
  for (let i = 0; i < formData.trainingDays.length; i++) {
    const day = formData.trainingDays[i]
    if (day.dayType === 'TRAINING') {
      for (let j = 0; j < day.actions.length; j++) {
        const action = day.actions[j]
        if (!action.actionId) {
          return { valid: false, message: `第${day.dayOrder}天第${j + 1}个动作未选择` }
        }
        if (action.minSets > action.maxSets) {
          return { valid: false, message: `第${day.dayOrder}天"${action.actionName}"的最小组数不能大于最大组数` }
        }
      }
    }
  }

  // 4. 训练日餐次比例校验
  if (!isTrainingRatioValid.value) {
    return {
      valid: false,
      message: '训练日营养素比例合计应接近100%，当前碳水' + trainingCarbTotal.value +
        '%、蛋白质' + trainingProteinTotal.value + '%、脂肪' + trainingFatTotal.value + '%'
    }
  }

  // 5. 休息日餐次比例校验
  if (!isRestRatioValid.value) {
    return {
      valid: false,
      message: '休息日营养素比例合计应接近100%，当前碳水' + restCarbTotal.value +
        '%、蛋白质' + restProteinTotal.value + '%、脂肪' + restFatTotal.value + '%'
    }
  }

  return { valid: true, message: '' }
}

// ==================== 构建提交数据 ====================

/** 将表单数据转为API DTO格式（百分比 → 小数） */
function buildPayload(): any {
  return {
    planName: formData.planName.trim(),
    planType: formData.planType,
    splitType: formData.splitType,
    trainingDays: formData.trainingDays.map(day => ({
      dayOrder: day.dayOrder,
      dayType: day.dayType,
      trainingType: day.dayType === 'TRAINING' ? day.trainingType : undefined,
      carbMultiplier: day.carbMultiplier,
      proteinMultiplier: day.proteinMultiplier,
      fatMultiplier: day.fatMultiplier,
      actions: day.dayType === 'TRAINING'
        ? day.actions.map(a => ({
            actionId: a.actionId,
            actionName: a.actionName,
            minSets: a.minSets,
            maxSets: a.maxSets,
            restMinutes: a.restMinutes,
            sortOrder: a.sortOrder
          }))
        : []
    })),
    mealConfigs: formData.mealConfigs.map(config => ({
      dayType: config.dayType,
      mealType: config.mealType,
      carbRatio: config.carbRatio / 100,
      proteinRatio: config.proteinRatio / 100,
      fatRatio: config.fatRatio / 100,
      sortOrder: config.sortOrder,
      foods: config.foods
        .filter(f => f.foodId !== null)
        .map(f => ({
          foodId: f.foodId,
          foodName: f.foodName,
          suggestedAmountG: f.suggestedAmountG
        }))
    }))
  }
}

// ==================== 提交 ====================

async function handleSubmit() {
  const validation = validateForm()
  if (!validation.valid) {
    ElMessage.warning(validation.message)
    return
  }

  submitting.value = true
  try {
    const payload = buildPayload()

    if (isEdit.value) {
      await updatePlan(editId.value, payload)
      ElMessage.success('计划已更新')
    } else {
      await createPlan(payload)
      ElMessage.success('计划创建成功')
    }

    router.push('/plans')
  } catch (err: any) {
    ElMessage.error(err.message || '保存失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

/** 取消编辑 */
function handleCancel() {
  router.push('/plans')
}

// ==================== 加载编辑数据 ====================

/** 从后端数据填充表单 */
function populateForm(detail: any) {
  formData.planName = detail.planName || ''
  formData.planType = detail.planType || getPreferredOptionValue(planTypeOptions.value, 'BULK')
  formData.splitType = detail.splitType || getPreferredOptionValue(splitTypeOptions.value, 'FOUR_DAY')

  // 填充训练日
  if (detail.trainingDays && Array.isArray(detail.trainingDays)) {
    formData.trainingDays = detail.trainingDays.map((day: any) => ({
      _key: nextUid(),
      dayOrder: day.dayOrder || 1,
      dayType: day.dayType || 'TRAINING',
      trainingType: normalizeTrainingTypes(day.trainingType).length > 0
        ? normalizeTrainingTypes(day.trainingType)
        : [getPreferredOptionValue(trainingTypeOptions.value, 'CHEST')].filter(Boolean),
      carbMultiplier: day.carbMultiplier ?? 4.0,
      proteinMultiplier: day.proteinMultiplier ?? 2.0,
      fatMultiplier: day.fatMultiplier ?? 1.0,
      actions: (day.actions || []).map((action: any, idx: number) => ({
        actionId: action.actionId || null,
        actionName: action.actionName || '',
        minSets: action.minSets ?? 3,
        maxSets: action.maxSets ?? 5,
        restMinutes: action.restMinutes ?? 2,
        sortOrder: action.sortOrder ?? idx
      }))
    }))
  } else {
    formData.trainingDays = [createDefaultTrainingDay(1)]
  }

  // 填充餐次配置（后端为小数，前端为百分比需 ×100）
  formData.mealConfigs = initMealConfigs() // 先用默认值初始化结构
  if (detail.mealConfigs && Array.isArray(detail.mealConfigs)) {
    // 用后端数据覆盖默认值
    const customConfigs: MealConfigItem[] = []
    // 复用默认结构中未提供的餐次
    const providedMap = new Map<string, boolean>()

    for (const mc of detail.mealConfigs) {
      const key = mc.dayType + '|' + mc.mealType
      providedMap.set(key, true)
      customConfigs.push({
        dayType: mc.dayType,
        mealType: mc.mealType,
        carbRatio: Math.round((mc.carbRatio || 0) * 100),
        proteinRatio: Math.round((mc.proteinRatio || 0) * 100),
        fatRatio: Math.round((mc.fatRatio || 0) * 100),
        sortOrder: mc.sortOrder || 0,
        foods: (mc.foods || []).map((f: any) => ({
          foodId: f.foodId || null,
          foodName: f.foodName || '',
          suggestedAmountG: f.suggestedAmountG ?? 100
        })),
        _expanded: false
      })
    }

    // 补充后端未返回的餐次类型（用默认值）
    for (const dt of ['TRAINING', 'REST'] as const) {
      for (const mt of mealTypeOptions.value) {
        const key = dt + '|' + mt.type
        if (!providedMap.has(key)) {
          const defaults = dt === 'TRAINING' ? DEFAULT_TRAINING_RATIOS[mt.type] : DEFAULT_REST_RATIOS[mt.type]
          customConfigs.push({
            dayType: dt,
            mealType: mt.type,
            carbRatio: defaults?.carb ?? 0,
            proteinRatio: defaults?.protein ?? 0,
            fatRatio: defaults?.fat ?? 0,
            sortOrder: mt.sortOrder,
            foods: [],
            _expanded: false
          })
        }
      }
    }

    formData.mealConfigs = customConfigs
  }
}

async function loadPageData() {
  loading.value = true
  error.value = ''
  try {
    await fetchDictData()
    if (isEdit.value) {
      const detail = await getPlanDetail(editId.value) as any
      populateForm(detail)
    } else {
      initFormData()
    }
  } catch (err: any) {
    error.value = err.message || '数据加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

// ==================== 生命周期 ====================

onMounted(() => {
  loadPageData()
})
</script>

<style scoped lang="scss">
/**
 * 健身计划编辑器样式
 */

.plan-editor-page {
  padding: 4px;
  max-width: 1200px;
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

/* ==================== 分区卡片 ==================== */
.section {
  margin-bottom: 16px;

  :deep(.el-card__header) {
    padding: 12px 20px;
    background: #fafafa;
    border-bottom: 1px solid #ebeef5;
  }

  :deep(.el-card__body) {
    padding: 20px;
  }
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;

  .section-title {
    font-size: 15px;
    font-weight: 600;
    color: #303133;
  }

  .section-header-actions {
    display: flex;
    gap: 8px;
  }
}

/* ==================== 训练日卡片 ==================== */
.day-card {
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  margin-bottom: 14px;
  overflow: hidden;
}

.day-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;

  .day-title {
    font-size: 14px;
    font-weight: 600;
    color: #303133;
    min-width: 60px;
  }

  .day-header-right {
    margin-left: auto;
    display: flex;
    align-items: center;
    gap: 4px;
  }
}

.day-body {
  padding: 16px;
}

.day-form {
  :deep(.el-form-item) {
    margin-bottom: 8px;
  }
}

/* ==================== 动作子区域 ==================== */
.sub-section {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #e4e7ed;

  .sub-section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;

    .sub-section-title {
      font-size: 13px;
      font-weight: 600;
      color: #606266;
    }
  }
}

.action-table {
  :deep(.el-input-number) {
    width: 100%;
  }
}

.action-name-cell {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.action-empty,
.muted {
  color: #909399;
}

.action-picker {
  display: grid;
  grid-template-columns: 180px 1fr;
  gap: 16px;
}

.muscle-filter {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 12px;
  max-height: 420px;
  overflow-y: auto;

  :deep(.el-checkbox) {
    display: flex;
    margin-right: 0;
    height: 28px;
  }
}

.picker-title {
  font-size: 13px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 8px;
}

.picker-tag {
  margin-right: 4px;
  margin-bottom: 4px;
}

/* ==================== 餐次配置 ==================== */
.meal-config-list {
  padding: 4px 0;
}

.meal-config-row {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 14px 16px;
  margin-bottom: 10px;
  background: #fff;

  &:last-child {
    margin-bottom: 12px;
  }

  .meal-config-header {
    margin-bottom: 10px;
  }

  :deep(.el-form-item) {
    margin-bottom: 0;
    display: flex;
    align-items: center;
  }

  :deep(.el-form-item__label) {
    font-size: 12px;
    padding-right: 6px;
  }
}

.unit-suffix {
  margin-left: 4px;
  font-size: 13px;
  color: #909399;
}

/* ==================== 食物展开区域 ==================== */
.food-section {
  margin-top: 10px;
  padding-top: 10px;
  padding-left: 12px;
  border-top: 1px dashed #e4e7ed;
}

.food-row {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

/* ==================== 比率汇总行 ==================== */
.meal-summary {
  display: flex;
  gap: 24px;
  padding: 10px 16px;
  background: #f5f7fa;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;

  .ratio-ok {
    color: #67c23a;
  }

  .ratio-warn {
    color: #f56c6c;
  }
}

/* ==================== 底部操作栏 ==================== */
.form-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 0;
  margin-top: 8px;
}

/* ==================== 响应式调整 ==================== */
@media (max-width: 768px) {
  .plan-editor-page {
    padding: 2px;
  }

  .section {
    :deep(.el-card__body) {
      padding: 12px;
    }
  }

  .day-body {
    padding: 10px;
  }

  .meal-config-row {
    padding: 10px 12px;
  }

  .food-row {
    flex-wrap: wrap;
  }
}
</style>
