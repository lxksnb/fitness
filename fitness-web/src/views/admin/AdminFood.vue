<template>
  <!-- 管理后台 - 系统食物库管理 -->
  <div class="admin-food-page">
    <!-- ==================== 页面标题与搜索栏 ==================== -->
    <div class="page-header">
      <h2>食物库管理</h2>
      <div class="header-actions">
        <el-input
          v-model="keyword"
          placeholder="搜索食物名称..."
          clearable
          @keyup.enter="fetchList"
          @clear="fetchList"
          style="width: 260px"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="openDialog()">
          <el-icon><Plus /></el-icon>
          添加系统食物
        </el-button>
      </div>
    </div>

    <!-- ==================== 数据表格 ==================== -->
    <el-table
      v-loading="loading"
      :data="tableData"
      border
      stripe
      style="width: 100%"
      empty-text="暂无系统食物数据"
    >
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column label="图片" width="90" align="center">
        <template #default="{ row }">
          <el-image
            v-if="row.imageUrl"
            :src="row.imageUrl"
            fit="cover"
            style="width: 50px; height: 50px; border-radius: 4px"
            :preview-src-list="[row.imageUrl]"
          />
          <span v-else class="no-image">--</span>
        </template>
      </el-table-column>
      <el-table-column prop="foodName" label="食物名称" min-width="180" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 'DELETED' ? 'danger' : 'success'" size="small">
            {{ row.status === 'DELETED' ? '已删除' : '正常' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="180" align="center">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="openDialog(row)">
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
          <el-button
            v-if="row.status !== 'DELETED'"
            type="danger"
            link
            size="small"
            @click="handleDelete(row)"
          >
            <el-icon><Delete /></el-icon>
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- ==================== 分页 ==================== -->
    <div class="pagination-wrapper" v-if="total > pageSize">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchList"
      />
    </div>

    <!-- ==================== 错误状态 ==================== -->
    <template v-if="!loading && error">
      <el-result icon="error" title="数据加载失败" :sub-title="error">
        <template #extra>
          <el-button type="primary" @click="fetchList">重新加载</el-button>
        </template>
      </el-result>
    </template>

    <!-- ==================== 添加/编辑弹窗 ==================== -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditing ? '编辑系统食物' : '添加系统食物'"
      width="680px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="食物名称" prop="name">
          <el-input v-model="form.name" placeholder="如：鸡胸肉" maxlength="50" show-word-limit />
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
          <el-row :gutter="16" style="margin-bottom: 25px">
            <el-col :span="7">
              <el-form-item label="碳水(g)" style="margin-bottom: 0">
                <el-input-number v-model="entry.carbGrams" :min="0" :precision="1" :controls="false" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="7">
              <el-form-item label="蛋白(g)" style="margin-bottom: 0">
                <el-input-number v-model="entry.proteinGrams" :min="0" :precision="1" :controls="false" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="7">
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
 * 管理后台 - 系统食物库管理页面
 * 管理员管理系统级别的食物库，支持搜索、新增、编辑、删除（软删除）
 * 所有管理页面仅 ADMIN 角色可访问（路由守卫控制）
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Edit, Delete } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { getAdminFoods, createAdminFood, updateAdminFood, deleteAdminFood } from '@/api/admin'
import { getDictOptions, type DictOption } from '@/api/dict'
import ImageUpload from '@/components/common/ImageUpload.vue'

// ==================== 类型定义 ====================

/** 食物列表项 */
interface FoodItem {
  id: number
  foodName: string
  imageUrl?: string
  status?: string
  scope?: string
  createdAt?: string
}

/** 营养单位 */
interface NutritionEntry {
  unitType: string
  servingWeightG: number
  carbGrams: number
  proteinGrams: number
  fatGrams: number
  imageUrl?: string
}

/** 食物详情（用于编辑时回填） */
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
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

/** 表格数据 */
const tableData = ref<FoodItem[]>([])

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

/** 格式化日期 */
function formatDate(dateStr?: string): string {
  if (!dateStr) return '--'
  const d = new Date(dateStr)
  if (isNaN(d.getTime())) return dateStr
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

// ==================== 数据获取 ====================

/** 获取食物列表 */
async function fetchList() {
  loading.value = true
  error.value = ''
  try {
    const kw = keyword.value.trim() || undefined
    const res = await getAdminFoods({ keyword: kw }) as any
    // 兼容分页和列表两种响应格式
    if (res && (res.records || res.list)) {
      const list = res.records || res.list || []
      tableData.value = list
      total.value = res.total || list.length
    } else if (Array.isArray(res)) {
      tableData.value = res
      total.value = res.length
    } else {
      tableData.value = []
      total.value = 0
    }
  } catch (err: any) {
    error.value = err.message || '数据加载失败，请稍后重试'
    tableData.value = []
  } finally {
    loading.value = false
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

    // 尝试加载详情以获取营养单位列表
    try {
      // 用 admin API 获取详情（复用列表中的接口可能已包含详情）
      // 如果后端单独提供了详情接口，可在此调用
      // 这里使用 food 对象自身携带的信息
      const detail = food as any
      const entries = detail.nutritions || detail.nutritionEntries
      if (entries && Array.isArray(entries) && entries.length > 0) {
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
      })),
      scope: 'SYSTEM'
    }

    if (isEditing.value && editingId.value) {
      await updateAdminFood(editingId.value, payload)
      ElMessage.success('系统食物已更新')
    } else {
      await createAdminFood(payload)
      ElMessage.success('系统食物已添加')
    }

    dialogVisible.value = false
    await fetchList()
  } catch (err: any) {
    ElMessage.error(err.message || '保存失败，请重试')
  } finally {
    saving.value = false
  }
}

/** 删除食物（软删除） */
async function handleDelete(food: FoodItem) {
  try {
    await ElMessageBox.confirm(
      `确定要删除"${food.foodName}"吗？此操作将软删除该食物，不会影响已有记录。`,
      '删除确认',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    await deleteAdminFood(food.id)
    ElMessage.success('已删除')
    await fetchList()
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
  fetchList()
  loadUnitTypeOptions()
})
</script>

<style scoped lang="scss">
/**
 * 管理后台 - 系统食物库管理页面样式
 */

.admin-food-page {
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

/* ==================== 分页 ==================== */
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

/* ==================== 无图片占位 ==================== */
.no-image {
  color: #c0c4cc;
  font-size: 12px;
}

/* ==================== 弹窗表单中的营养单位 ==================== */
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
