<template>
  <!-- 食物库页面：搜索、卡片网格展示、展开查看营养变体、新增/编辑/删除食物 -->
  <div class="food-list-page">
    <!-- ==================== 页面标题与搜索栏 ==================== -->
    <div class="page-header">
      <h2>食物库</h2>
      <div class="header-actions">
        <el-input
          v-model="keyword"
          placeholder="搜索食物名称..."
          clearable
          @clear="fetchFoods"
          style="width: 260px"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="openDialog()">
          <el-icon><Plus /></el-icon>
          添加食物
        </el-button>
      </div>
    </div>

    <el-tabs v-model="activeScope" class="scope-tabs" @tab-change="() => fetchFoods()">
      <el-tab-pane label="全部食物" name="ALL" />
      <el-tab-pane label="系统食物" name="SYSTEM" />
      <el-tab-pane label="我的食物" name="USER" />
    </el-tabs>

    <!-- ==================== 加载骨架 ==================== -->
    <template v-if="loading">
      <el-row :gutter="16">
        <el-col v-for="i in 6" :key="i" :span="8" style="margin-bottom: 16px">
          <el-card>
            <el-skeleton :rows="4" animated />
          </el-card>
        </el-col>
      </el-row>
    </template>

    <!-- ==================== 错误状态 ==================== -->
    <template v-else-if="error">
      <el-result icon="error" title="数据加载失败" :sub-title="error">
        <template #extra>
          <el-button type="primary" @click="fetchFoods">重新加载</el-button>
        </template>
      </el-result>
    </template>

    <!-- ==================== 食物卡片网格 ==================== -->
    <template v-else>
      <template v-if="foodList.length > 0">
        <el-row :gutter="16">
          <el-col v-for="food in foodList" :key="food.id" :xs="24" :sm="12" :md="8" :lg="6" style="margin-bottom: 16px">
            <el-card shadow="hover" class="food-card" :class="{ expanded: expandedId === food.id }">
              <!-- 卡片头部：图片 + 名称 -->
              <div class="food-card-header" @click="toggleExpand(food)">
                <div class="food-image">
                  <el-image
                    v-if="food.imageUrl"
                    :src="food.imageUrl"
                    fit="cover"
                    style="width: 100%; height: 140px; border-radius: 6px 6px 0 0"
                  />
                  <div v-else class="food-image-placeholder">
                    <el-icon :size="40"><PictureFilled /></el-icon>
                  </div>
                </div>
                <div class="food-name-row">
                  <span class="food-name">{{ food.foodName }}</span>
                  <el-tag v-if="food.isSystem" size="small" type="info">系统</el-tag>
                </div>
              </div>

              <!-- 展开的营养变体详情 -->
              <div v-if="expandedId === food.id" class="food-detail">
                <div v-if="foodDetailLoading" class="detail-loading">
                  <el-skeleton :rows="3" animated />
                </div>
                <div v-else-if="foodDetail" class="nutrition-entries">
                  <div
                    v-for="(entry, idx) in foodDetail.nutritions"
                    :key="idx"
                    class="nutrition-entry-item"
                  >
                    <el-tag size="small" effect="plain" type="success">{{ entry.unitType || '标准' }}</el-tag>
                    <span class="entry-serving">每{{ entry.servingWeightG || 100 }}g</span>
                    <div class="entry-macros">
                      <span>碳水 {{ formatNum(entry.carbGrams) }}g</span>
                      <span>蛋白质 {{ formatNum(entry.proteinGrams) }}g</span>
                      <span>脂肪 {{ formatNum(entry.fatGrams) }}g</span>
                    </div>
                    <el-image
                      v-if="entry.imageUrl"
                      :src="entry.imageUrl"
                      fit="cover"
                      style="width: 40px; height: 40px; border-radius: 4px"
                      :preview-src-list="[entry.imageUrl]"
                    />
                  </div>
                </div>
                <el-empty v-else description="暂无详细营养数据" :image-size="60" />
              </div>

              <!-- 操作按钮 -->
              <div class="food-card-actions" v-if="!food.isSystem">
                <el-button type="primary" link size="small" @click="openDialog(food)">
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button type="danger" link size="small" @click="handleDelete(food)">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </div>
              <div v-else class="food-card-actions">
                <span class="system-hint">系统数据，不可编辑</span>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </template>
      <el-empty v-else description="暂无食物数据，点击上方按钮添加" :image-size="120">
        <el-button type="primary" @click="openDialog()">添加食物</el-button>
      </el-empty>
    </template>

    <!-- ==================== 添加/编辑食物弹窗 ==================== -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditing ? '编辑食物' : '添加食物'"
      width="640px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="食物名称" prop="name">
          <el-input v-model="form.name" placeholder="如：鸡胸肉" />
        </el-form-item>

        <el-form-item label="食物图片">
          <ImageUpload
            v-model="form.imageUrls"
            :limit="1"
            :multiple="false"
          />
        </el-form-item>

        <!-- 营养单位列表（动态增删） -->
        <el-divider content-position="left">营养单位</el-divider>

        <div
          v-for="(entry, index) in form.nutritionEntries"
          :key="index"
          class="nutrition-entry-form"
        >
          <div class="entry-header">
            <span>营养单位 #{{ index + 1 }}</span>
            <el-button
              v-if="form.nutritionEntries.length > 1"
              type="danger"
              link
              size="small"
              @click="removeNutritionEntry(index)"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>

          <el-row :gutter="16" style="margin-bottom: 15px">
            <el-col :span="12">
              <el-form-item label="单位类型" :prop="`nutritionEntries.${index}.unitType`" :rules="[{ required: true, message: '请选择', trigger: 'change' }]" style="margin-bottom: 0">
                <el-select v-model="entry.unitType" placeholder="选择单位类型" style="width: 100%" @change="onUnitTypeChange(index)">
                  <el-option
                    v-for="item in unitTypeOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="10" v-if="entry.unitType !== 'PER_100G'">
              <el-form-item label="单位重量(g)" :prop="`nutritionEntries.${index}.servingWeightG`" :rules="[{ required: true, message: '必填', trigger: 'blur' }]" style="margin-bottom: 0">
                <el-input-number v-model="entry.servingWeightG" :min="1" :controls="false" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20" style="margin-bottom: 25px">
            <el-col :span="8">
              <el-form-item label="碳水(g)" style="margin-bottom: 0">
                <el-input-number v-model="entry.carbGrams" :min="0" :precision="1" :controls="false" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="蛋白(g)" style="margin-bottom: 0">
                <el-input-number v-model="entry.proteinGrams" :min="0" :precision="1" :controls="false" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="脂肪(g)" style="margin-bottom: 0">
                <el-input-number v-model="entry.fatGrams" :min="0" :precision="1" :controls="false" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>

          <!-- 营养单位图片 -->
          <el-form-item label="参考图片" class="entry-image-item">
            <ImageUpload
              :model-value="entry.imageUrl ? [entry.imageUrl] : []"
              @update:model-value="(urls: string[]) => { entry.imageUrl = urls[0] || '' }"
              :limit="1"
              :multiple="false"
              :button-text="'上传参考图'"
            />
          </el-form-item>
        </div>

        <el-button type="success" plain size="small" @click="addNutritionEntry" style="margin-top: 8px">
          <el-icon><Plus /></el-icon>
          添加营养单位
        </el-button>
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
 * 食物库页面
 * 支持搜索食物、卡片网格展示、点击展开营养变体详情、
 * 新增/编辑/删除自定义食物（系统食物不可编辑）
 */
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Edit, Delete, PictureFilled } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { searchFoods, getFoodDetail, createFood, updateFood, deleteFood } from '@/api/food'
import { getDictOptions, type DictOption } from '@/api/dict'
import ImageUpload from '@/components/common/ImageUpload.vue'

// ==================== 类型定义 ====================

/** 食物列表项 */
interface FoodItem {
  id: number
  foodName: string
  imageUrl?: string
  isSystem?: boolean
  scope?: string
}

/** 营养单位项 */
interface NutritionEntry {
  unitType: string
  servingWeightG: number
  carbGrams: number
  proteinGrams: number
  fatGrams: number
  imageUrl?: string
}

/** 食物详情 */
interface FoodDetail {
  id: number
  foodName: string
  imageUrl?: string
  nutritions: NutritionEntry[]
}

// ==================== 状态 ====================

const loading = ref(true)
const error = ref('')
const saving = ref(false)
const dialogVisible = ref(false)
const isEditing = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const keyword = ref('')
const activeScope = ref<'ALL' | 'SYSTEM' | 'USER'>('ALL')
let searchTimer: ReturnType<typeof setTimeout> | null = null

/** 食物列表 */
const foodList = ref<FoodItem[]>([])

/** 展开状态 */
const expandedId = ref<number | null>(null)
const foodDetail = ref<FoodDetail | null>(null)
const foodDetailLoading = ref(false)

/** 单位类型下拉选项（从字典 food_unit_type 加载） */
const unitTypeOptions = ref<DictOption[]>([])

// ==================== 表单 ====================

const form = reactive({
  name: '',
  imageUrls: [] as string[],
  nutritionEntries: [
    {
      unitType: 'PER_100G',
      servingWeightG: 100,
      carbGrams: 0,
      proteinGrams: 0,
      fatGrams: 0,
      imageUrl: ''
    }
  ] as NutritionEntry[]
})

const formRules: FormRules = {
  name: [
    { required: true, message: '请输入食物名称', trigger: 'blur' },
    { max: 50, message: '食物名称不超过50个字符', trigger: 'blur' }
  ]
}

// ==================== 工具函数 ====================

/** 格式化数字 */
function formatNum(val: number | null | undefined, decimals = 1): string {
  if (val == null || isNaN(val)) return '0'
  return Number(val).toFixed(decimals)
}

function scheduleFetchFoods() {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    fetchFoods()
  }, 300)
}

// ==================== 数据获取 ====================

/** 搜索食物列表 */
async function fetchFoods() {
  loading.value = true
  error.value = ''
  expandedId.value = null
  foodDetail.value = null
  try {
    const kw = keyword.value.trim() || undefined
    const scope = activeScope.value === 'ALL' ? undefined : activeScope.value
    const res = await searchFoods(kw, scope) as any
    const list = (Array.isArray(res) ? res : (res?.records || res?.list || [])) as any[]
    foodList.value = list.map(item => ({
      ...item,
      isSystem: item.isSystem || item.scope === 'SYSTEM'
    })) as FoodItem[]
  } catch (err: any) {
    error.value = err.message || '数据加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

/** 展开/折叠食物详情 */
async function toggleExpand(food: FoodItem) {
  if (expandedId.value === food.id) {
    // 折叠
    expandedId.value = null
    foodDetail.value = null
    return
  }

  expandedId.value = food.id
  foodDetail.value = null
  foodDetailLoading.value = true

  try {
    const res = await getFoodDetail(food.id) as any
    foodDetail.value = {
      id: res?.id || food.id,
      foodName: res?.foodName || food.foodName,
      imageUrl: res?.imageUrl,
      nutritions: Array.isArray(res?.nutritions)
        ? res.nutritions
        : (Array.isArray(res?.nutritionEntries) ? res.nutritionEntries : (Array.isArray(res?.units) ? res.units : []))
    }
  } catch {
    foodDetail.value = {
      id: food.id,
      foodName: food.foodName,
      nutritions: []
    }
  } finally {
    foodDetailLoading.value = false
  }
}

// ==================== 营养单位操作 ====================

/** 添加营养单位行 */
function addNutritionEntry() {
  form.nutritionEntries.push({
    unitType: 'PER_100G',
    servingWeightG: 100,
    carbGrams: 0,
    proteinGrams: 0,
    fatGrams: 0,
    imageUrl: ''
  })
}

/** 移除营养单位行 */
function removeNutritionEntry(index: number) {
  if (form.nutritionEntries.length <= 1) {
    ElMessage.warning('至少保留一个营养单位')
    return
  }
  form.nutritionEntries.splice(index, 1)
}

/** 单位类型变更: 选"每100g"时自动固定重量为100并隐藏输入框 */
function onUnitTypeChange(index: number) {
  if (form.nutritionEntries[index].unitType === 'PER_100G') {
    form.nutritionEntries[index].servingWeightG = 100
  }
}

// ==================== 弹窗操作 ====================

/** 打开新增/编辑弹窗 */
async function openDialog(food?: FoodItem) {
  if (food) {
    isEditing.value = true
    editingId.value = food.id
    form.name = food.foodName
    form.imageUrls = food.imageUrl ? [food.imageUrl] : []

    // 加载详情以获取营养单位列表
    try {
      const res = await getFoodDetail(food.id) as any
      const entries = Array.isArray(res?.nutritions)
        ? res.nutritions
        : (Array.isArray(res?.nutritionEntries) ? res.nutritionEntries : (Array.isArray(res?.units) ? res.units : []))
      if (entries.length > 0) {
        form.nutritionEntries = entries.map((e: any) => ({
          unitType: e.unitType || 'PER_100G',
          servingWeightG: e.servingWeightG || e.servingWeight || 100,
          carbGrams: e.carbGrams || 0,
          proteinGrams: e.proteinGrams || 0,
          fatGrams: e.fatGrams || 0,
          imageUrl: e.imageUrl || ''
        }))
      } else {
        form.nutritionEntries = [
          { unitType: 'PER_100G', servingWeightG: 100, carbGrams: 0, proteinGrams: 0, fatGrams: 0, imageUrl: '' }
        ]
      }
    } catch {
      form.nutritionEntries = [
        { unitType: 'PER_100G', servingWeightG: 100, carbGrams: 0, proteinGrams: 0, fatGrams: 0, imageUrl: '' }
      ]
    }
  } else {
    isEditing.value = false
    editingId.value = null
    resetFormData()
  }
  dialogVisible.value = true
}

/** 重置表单数据 */
function resetFormData() {
  form.name = ''
  form.imageUrls = []
  form.nutritionEntries = [
    { unitType: 'PER_100G', servingWeightG: 100, carbGrams: 0, proteinGrams: 0, fatGrams: 0, imageUrl: '' }
  ]
}

/** 关闭弹窗后重置 */
function resetForm() {
  formRef.value?.resetFields()
  resetFormData()
}

/** 保存食物 */
async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const payload = {
      foodName: form.name,
      imageUrl: form.imageUrls.length > 0 ? form.imageUrls[0] : undefined,
      nutritions: form.nutritionEntries.map(e => ({
        unitType: e.unitType,
        servingWeightG: e.servingWeightG,
        carbGrams: e.carbGrams,
        proteinGrams: e.proteinGrams,
        fatGrams: e.fatGrams,
        imageUrl: e.imageUrl || undefined
      }))
    }

    if (isEditing.value && editingId.value) {
      await updateFood(editingId.value, payload)
      ElMessage.success('食物已更新')
    } else {
      await createFood(payload)
      ElMessage.success('食物已添加')
    }

    dialogVisible.value = false
    await fetchFoods()
  } catch (err: any) {
    ElMessage.error(err.message || '保存失败，请重试')
  } finally {
    saving.value = false
  }
}

/** 删除食物 */
async function handleDelete(food: FoodItem) {
  try {
    await ElMessageBox.confirm(
      `确定要删除"${food.foodName}"吗？此操作不可撤销。`,
      '删除确认',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    await deleteFood(food.id)
    ElMessage.success('已删除')
    await fetchFoods()
  } catch (err: any) {
    if (err !== 'cancel' && err !== 'close') {
      ElMessage.error(err.message || '删除失败')
    }
  }
}

// ==================== 字典数据 ====================

/** 加载食物单位类型字典 */
async function loadUnitTypeOptions() {
  try {
    unitTypeOptions.value = await getDictOptions('food_unit_type')
  } catch {
    unitTypeOptions.value = []
  }
}

// ==================== 生命周期 ====================

onMounted(() => {
  fetchFoods()
  loadUnitTypeOptions()
})

watch(keyword, scheduleFetchFoods)
</script>

<style scoped lang="scss">
/**
 * 食物库页面样式
 */

.food-list-page {
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

.scope-tabs {
  margin-bottom: 12px;
}

/* ==================== 食物卡片 ==================== */
.food-card {
  transition: box-shadow 0.2s;

  &.expanded {
    border-color: #409eff;
    box-shadow: 0 2px 12px rgba(64, 158, 255, 0.15);
  }

  :deep(.el-card__body) {
    padding: 0;
  }
}

.food-card-header {
  cursor: pointer;
}

.food-image {
  .food-image-placeholder {
    width: 100%;
    height: 140px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f5f7fa;
    color: #c0c4cc;
  }
}

.food-name-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px 6px;

  .food-name {
    font-size: 15px;
    font-weight: 600;
    color: #303133;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    flex: 1;
    margin-right: 8px;
  }
}

/* ==================== 展开详情 ==================== */
.food-detail {
  padding: 8px 14px 12px;
  border-top: 1px solid #f5f5f5;
  background: #fafafa;
}

.detail-loading {
  padding: 8px 0;
}

.nutrition-entries {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.nutrition-entry-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 0;

  .entry-serving {
    font-size: 12px;
    color: #909399;
    white-space: nowrap;
  }

  .entry-macros {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    font-size: 12px;
    color: #606266;

    span {
      white-space: nowrap;
    }
  }
}

/* ==================== 卡片底部操作 ==================== */
.food-card-actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 4px;
  padding: 8px 14px 12px;
  border-top: 1px solid #f5f5f5;
}

.system-hint {
  font-size: 12px;
  color: #c0c4cc;
}

/* ==================== 表单中的营养单位 ==================== */
.nutrition-entry-form {
  padding: 12px;
  margin-bottom: 12px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #fafafa;
}

.entry-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;

  span {
    font-size: 14px;
    font-weight: 500;
    color: #606266;
  }
}

.entry-image-item {
  margin-top: -8px;
}
</style>
