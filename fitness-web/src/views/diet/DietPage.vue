<template>
  <!-- 饮食记录页面：按日期查看/按餐次分组展示饮食记录，新增/编辑/删除，支持从食物库导入 -->
  <div class="diet-page">
    <!-- ==================== 页面标题与操作栏 ==================== -->
    <div class="page-header">
      <h2>饮食记录</h2>
      <div class="header-actions">
        <el-date-picker
          v-model="selectedDate"
          type="date"
          placeholder="选择日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          @change="fetchDietList"
          style="width: 180px"
        />
        <el-button @click="$router.push('/diet/overview')">
          <el-icon><DataAnalysis /></el-icon>
          饮食概览
        </el-button>
        <el-button type="primary" @click="openDialog()">
          <el-icon><Plus /></el-icon>
          添加饮食
        </el-button>
      </div>
    </div>

    <!-- ==================== 加载骨架 ==================== -->
    <template v-if="loading">
      <el-card v-for="i in 3" :key="i" class="section">
        <el-skeleton :rows="4" animated />
      </el-card>
    </template>

    <!-- ==================== 错误状态 ==================== -->
    <template v-else-if="error">
      <el-result icon="error" title="数据加载失败" :sub-title="error">
        <template #extra>
          <el-button type="primary" @click="fetchDietList">重新加载</el-button>
        </template>
      </el-result>
    </template>

    <!-- ==================== 按餐次分组展示 ==================== -->
    <template v-else>
      <template v-if="Object.keys(groupedRecords).length > 0">
        <el-card v-for="(records, mealType) in groupedRecords" :key="mealType" class="section meal-group">
          <template #header>
            <div class="meal-header">
              <el-tag :type="mealTagType(mealType)" size="large">
                <el-icon><Food /></el-icon>
                {{ getMealLabel(mealType) }}
              </el-tag>
              <span class="meal-stats">
                {{ records.length }} 项 · 共 {{ calcMealCalories(records) }} kcal
              </span>
            </div>
          </template>

          <!-- 饮食记录卡片列表 -->
          <div class="diet-records">
            <div v-for="record in records" :key="record.id" class="diet-record-card">
              <!-- 食物图片缩略图 -->
              <div class="record-image" v-if="record.imageUrl">
                <el-image
                  :src="record.imageUrl"
                  fit="cover"
                  style="width: 80px; height: 80px; border-radius: 6px"
                  :preview-src-list="[record.imageUrl]"
                />
              </div>
              <div v-else class="record-image record-image-placeholder">
                <el-icon :size="32"><PictureFilled /></el-icon>
              </div>

              <!-- 记录详情 -->
              <div class="record-info">
                <div class="record-name">{{ record.foodName }}</div>
                <div class="record-nutrition">
                  <span class="nutrition-item">
                    <span class="label">碳水</span>
                    <span class="value">{{ formatNum(record.carbGrams) }}g</span>
                  </span>
                  <span class="nutrition-item">
                    <span class="label">蛋白质</span>
                    <span class="value">{{ formatNum(record.proteinGrams) }}g</span>
                  </span>
                  <span class="nutrition-item">
                    <span class="label">脂肪</span>
                    <span class="value">{{ formatNum(record.fatGrams) }}g</span>
                  </span>
                  <span class="nutrition-item nutrition-calories">
                    <span class="value">{{ formatNum(record.calories, 0) }} kcal</span>
                  </span>
                </div>
              </div>

              <!-- 操作按钮 -->
              <div class="record-actions">
                <el-button type="primary" link size="small" @click="openDialog(record)">
                  <el-icon><Edit /></el-icon>
                </el-button>
                <el-button type="danger" link size="small" @click="handleDelete(record)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </template>
      <el-empty v-else description="暂无饮食记录，点击上方按钮添加" :image-size="120" />
    </template>

    <!-- ==================== 添加/编辑饮食弹窗 ==================== -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditing ? '编辑饮食记录' : '添加饮食记录'"
      width="560px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <!-- 从食物库导入 -->
        <el-form-item label="从食物库导入">
          <div class="food-import-row">
            <el-select
              v-model="selectedFoodId"
              filterable
              remote
              reserve-keyword
              :remote-method="searchFoodsRemote"
              :loading="foodSearchLoading"
              placeholder="搜索食物..."
              clearable
              style="flex: 1"
              @change="onFoodSelected"
            >
              <el-option
                v-for="food in foodOptions"
                :key="food.id"
                :label="food.name"
                :value="food.id"
              />
            </el-select>
            <el-select
              v-model="selectedUnitType"
              placeholder="选择单位"
              style="width: 140px"
              :disabled="!selectedFoodDetail"
              @change="onUnitSelected"
            >
              <el-option
                v-for="unit in foodUnits"
                :key="unit.unitType"
                :label="getUnitTypeLabel(unit.unitType) + ' (' + unit.servingWeight + 'g)'"
                :value="unit.unitType"
              />
            </el-select>
          </div>
        </el-form-item>

        <el-form-item label="餐次" prop="mealType">
          <el-select v-model="form.mealType" placeholder="选择餐次" style="width: 100%">
            <el-option
              v-for="item in mealTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="食物名称" prop="foodName">
          <el-input v-model="form.foodName" placeholder="如：鸡胸肉" />
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="10">
            <el-form-item label="碳水(g)" prop="carbGrams">
              <el-input-number v-model="form.carbGrams" :min="0" :precision="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="10">
            <el-form-item label="蛋白质(g)" prop="proteinGrams">
              <el-input-number v-model="form.proteinGrams" :min="0" :precision="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="10">
            <el-form-item label="脂肪(g)" prop="fatGrams">
              <el-input-number v-model="form.fatGrams" :min="0" :precision="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="食物图片">
          <ImageUpload
            v-model="form.imageUrls"
            :limit="1"
            :multiple="false"
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
 * 饮食记录页面
 * 支持按日期查看饮食记录，按餐次(早餐/午餐/晚餐/加餐)分组展示，
 * 新增/编辑/删除饮食记录，支持从食物库导入营养数据
 */
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Food, DataAnalysis, Edit, Delete, PictureFilled } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { getDietList, createDiet, updateDiet, deleteDiet } from '@/api/diet'
import { getDictOptions, type DictOption } from '@/api/dict'
import { searchFoods, getFoodDetail } from '@/api/food'
import ImageUpload from '@/components/common/ImageUpload.vue'

const router = useRouter()

// ==================== 类型定义 ====================

/** 饮食记录数据项 */
interface DietRecord {
  id?: number
  mealType: string
  foodName: string
  carbGrams: number
  proteinGrams: number
  fatGrams: number
  calories: number
  imageUrl?: string
}

/** 食物搜索选项 */
interface FoodOption {
  id: number
  name: string
}

/** 食物营养单位 */
interface FoodUnit {
  unitType: string
  servingWeight: number
  carbGrams: number
  proteinGrams: number
  fatGrams: number
}

// ==================== 状态 ====================

const loading = ref(true)
const error = ref('')
const saving = ref(false)
const dialogVisible = ref(false)
const isEditing = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const selectedDate = ref(getTodayStr())
const foodSearchLoading = ref(false)

/** 餐次字典选项 */
const mealTypeOptions = ref<DictOption[]>([])
const unitTypeOptions = ref<DictOption[]>([])

/** 全部饮食记录 */
const dietRecords = ref<DietRecord[]>([])

/** 食物库搜索选项 */
const foodOptions = ref<FoodOption[]>([])
const selectedFoodId = ref<number | null>(null)
const selectedFoodDetail = ref<any>(null)
const foodUnits = ref<FoodUnit[]>([])
const selectedUnitType = ref<string>('')

// ==================== 表单 ====================

const form = reactive({
  mealType: '',
  foodName: '',
  carbGrams: 0,
  proteinGrams: 0,
  fatGrams: 0,
  imageUrls: [] as string[]
})

const formRules: FormRules = {
  mealType: [{ required: true, message: '请选择餐次', trigger: 'blur' }],
  foodName: [{ required: true, message: '请输入食物名称', trigger: 'blur' }],
  carbGrams: [{ required: true, message: '请输入碳水含量', trigger: 'blur' }],
  proteinGrams: [{ required: true, message: '请输入蛋白质含量', trigger: 'blur' }],
  fatGrams: [{ required: true, message: '请输入脂肪含量', trigger: 'blur' }]
}

// ==================== 计算属性 ====================

/** 按餐次分组排序的饮食记录 */
const groupedRecords = computed(() => {
  const groups: Record<string, DietRecord[]> = {}

  const mealOrder = mealTypeOptions.value.map(item => item.value)

  for (const meal of mealOrder) {
    const records = dietRecords.value.filter(r => r.mealType === meal)
    if (records.length > 0) {
      groups[meal] = records
    }
  }

  // 追加未定义的餐次
  for (const record of dietRecords.value) {
    if (!mealOrder.includes(record.mealType)) {
      if (!groups[record.mealType]) {
        groups[record.mealType] = []
      }
      groups[record.mealType].push(record)
    }
  }

  return groups
})

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

/** 将后端餐食类型映射为中文 */
function getMealLabel(type: string): string {
  return mealTypeOptions.value.find(item => item.value === type)?.label || type
}

/** 将食物单位类型映射为字典标签 */
function getUnitTypeLabel(type: string): string {
  return unitTypeOptions.value.find(item => item.value === type)?.label || type
}

/** 根据餐食类型返回 el-tag 的 type */
function mealTagType(type: string): string {
  const map: Record<string, string> = {
    BREAKFAST: 'success',
    LUNCH: 'warning',
    DINNER: 'danger',
    SUPPER: 'info',
    PRE_WORKOUT: 'primary',
    POST_WORKOUT: 'success',
    OTHER: 'info',
    breakfast: 'success',
    lunch: 'warning',
    dinner: 'danger',
    snack: 'info'
  }
  return map[type] || ''
}

/** 计算某个餐次的总热量 */
function calcMealCalories(records: DietRecord[]): string {
  const total = records.reduce((sum, r) => sum + (r.calories || 0), 0)
  return formatNum(total, 0)
}

// ==================== 数据获取 ====================

/** 加载餐次字典 */
async function fetchMealTypes() {
  try {
    const [mealTypes, unitTypes] = await Promise.all([
      getDictOptions('meal_type'),
      getDictOptions('food_unit_type')
    ])
    mealTypeOptions.value = mealTypes
    unitTypeOptions.value = unitTypes
    if (!form.mealType) {
      form.mealType = mealTypeOptions.value[0]?.value || ''
    }
  } catch (err: any) {
    ElMessage.error(err.message || '餐次字典加载失败')
    mealTypeOptions.value = []
    unitTypeOptions.value = []
  }
}

/** 查询饮食记录列表 */
async function fetchDietList() {
  loading.value = true
  error.value = ''
  try {
    const res = await getDietList(selectedDate.value) as any
    dietRecords.value = (Array.isArray(res) ? res : (res?.records || res?.list || [])) as DietRecord[]
  } catch (err: any) {
    error.value = err.message || '数据加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

/** 远程搜索食物库 */
async function searchFoodsRemote(keyword: string) {
  if (!keyword) {
    foodOptions.value = []
    return
  }
  foodSearchLoading.value = true
  try {
    const res = await searchFoods(keyword) as any
    foodOptions.value = (Array.isArray(res) ? res : (res?.records || res?.list || [])).map((item: any) => ({
      id: item.id,
      name: item.foodName || item.name
    }))
  } catch {
    foodOptions.value = []
  } finally {
    foodSearchLoading.value = false
  }
}

/** 选中食物库中的食物 */
async function onFoodSelected(foodId: number | null) {
  selectedFoodDetail.value = null
  foodUnits.value = []
  selectedUnitType.value = ''

  if (!foodId) return

  try {
    const res = await getFoodDetail(foodId) as any
    selectedFoodDetail.value = res
    // 提取营养单位列表
    const units = res?.nutritions || res?.nutritionEntries || res?.units || []
    foodUnits.value = Array.isArray(units) ? units : []

    // 填充食物名称
    form.foodName = res?.foodName || res?.name || form.foodName

    // 如果只有一个单位，自动选择
    if (foodUnits.value.length === 1) {
      selectedUnitType.value = foodUnits.value[0].unitType
      fillNutritionFromUnit(foodUnits.value[0])
    }
  } catch (err: any) {
    ElMessage.error('获取食物详情失败')
  }
}

/** 选择营养单位后自动填充营养值 */
function onUnitSelected(unitType: string) {
  if (!unitType) return
  const unit = foodUnits.value.find((u: FoodUnit) => u.unitType === unitType)
  if (unit) {
    fillNutritionFromUnit(unit)
  }
}

/** 根据营养单位自动填充碳水/蛋白质/脂肪 */
function fillNutritionFromUnit(unit: FoodUnit) {
  form.carbGrams = unit.carbGrams || 0
  form.proteinGrams = unit.proteinGrams || 0
  form.fatGrams = unit.fatGrams || 0
}

// ==================== 弹窗操作 ====================

/** 打开新增/编辑弹窗 */
function openDialog(record?: DietRecord) {
  if (record) {
    isEditing.value = true
    editingId.value = record.id || null
    form.mealType = record.mealType
    form.foodName = record.foodName
    form.carbGrams = record.carbGrams
    form.proteinGrams = record.proteinGrams
    form.fatGrams = record.fatGrams
    form.imageUrls = record.imageUrl ? [record.imageUrl] : []
  } else {
    isEditing.value = false
    editingId.value = null
    resetFormData()
  }
  // 清除食物库选择
  selectedFoodId.value = null
  selectedFoodDetail.value = null
  foodUnits.value = []
  selectedUnitType.value = ''
  dialogVisible.value = true
}

/** 重置表单数据 */
function resetFormData() {
  form.mealType = mealTypeOptions.value[0]?.value || ''
  form.foodName = ''
  form.carbGrams = 0
  form.proteinGrams = 0
  form.fatGrams = 0
  form.imageUrls = []
}

/** 关闭弹窗后重置表单 */
function resetForm() {
  formRef.value?.resetFields()
  resetFormData()
}

/** 保存饮食记录 */
async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const payload = {
      recordDate: selectedDate.value,
      mealType: form.mealType,
      foodName: form.foodName,
      carbGrams: form.carbGrams,
      proteinGrams: form.proteinGrams,
      fatGrams: form.fatGrams,
      imageUrl: form.imageUrls.length > 0 ? form.imageUrls[0] : undefined
    }

    if (isEditing.value && editingId.value) {
      await updateDiet(editingId.value, payload)
      ElMessage.success('饮食记录已更新')
    } else {
      await createDiet(payload)
      ElMessage.success('饮食记录已添加')
    }

    dialogVisible.value = false
    await fetchDietList()
  } catch (err: any) {
    ElMessage.error(err.message || '保存失败，请重试')
  } finally {
    saving.value = false
  }
}

/** 删除饮食记录 */
async function handleDelete(record: DietRecord) {
  try {
    await ElMessageBox.confirm(
      `确定要删除"${record.foodName}"吗？此操作不可撤销。`,
      '删除确认',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    await deleteDiet(record.id!)
    ElMessage.success('已删除')
    await fetchDietList()
  } catch (err: any) {
    if (err !== 'cancel' && err !== 'close') {
      ElMessage.error(err.message || '删除失败')
    }
  }
}

// ==================== 生命周期 ====================

onMounted(() => {
  fetchMealTypes()
  fetchDietList()
})
</script>

<style scoped lang="scss">
/**
 * 饮食记录页面样式
 */

.diet-page {
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

/* ==================== 餐次分组卡片 ==================== */
.meal-group {
  :deep(.el-card__header) {
    padding: 14px 20px;
    background: #fafafa;
    border-bottom: 1px solid #ebeef5;
  }
}

.meal-header {
  display: flex;
  align-items: center;
  gap: 12px;

  .meal-stats {
    font-size: 13px;
    color: #909399;
  }
}

/* ==================== 饮食记录卡片 ==================== */
.diet-records {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.diet-record-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  transition: box-shadow 0.2s;

  &:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  }
}

.record-image {
  flex-shrink: 0;
  width: 80px;
  height: 80px;
  border-radius: 6px;
  overflow: hidden;

  &-placeholder {
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f5f7fa;
    color: #c0c4cc;
  }
}

.record-info {
  flex: 1;
  min-width: 0;

  .record-name {
    font-size: 15px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 8px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .record-nutrition {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
  }
}

.nutrition-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;

  .label {
    color: #909399;
  }

  .value {
    color: #606266;
    font-weight: 500;
  }

  &.nutrition-calories {
    .value {
      color: #e6a23c;
      font-weight: 600;
    }
  }
}

.record-actions {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

/* ==================== 食物导入行 ==================== */
.food-import-row {
  display: flex;
  gap: 8px;
  width: 100%;
}
</style>
