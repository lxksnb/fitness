<template>
  <!-- 管理后台 - 字典管理页面 -->
  <div class="admin-dict-page">
    <div class="page-header">
      <h2>字典管理</h2>
    </div>

    <!-- ==================== 左右双面板布局 ==================== -->
    <div class="dict-panels">
      <!-- ========== 左侧：字典类型 ========== -->
      <div class="panel panel-left">
        <div class="panel-header">
          <span class="panel-title">字典类型</span>
          <el-button type="primary" size="small" @click="openTypeDialog()">
            <el-icon><Plus /></el-icon>
            新增类型
          </el-button>
        </div>

        <!-- 加载状态 -->
        <div v-if="typeLoading" class="panel-loading">
          <el-skeleton :rows="6" animated />
        </div>

        <!-- 错误状态 -->
        <div v-else-if="typeError" class="panel-error">
          <el-result icon="error" title="加载失败" :sub-title="typeError">
            <template #extra>
              <el-button size="small" @click="fetchDictTypes">重试</el-button>
            </template>
          </el-result>
        </div>

        <!-- 空状态 -->
        <el-empty v-else-if="dictTypes.length === 0" description="暂无字典类型" :image-size="80" />

        <!-- 类型列表 -->
        <div v-else class="type-list">
          <div
            v-for="item in dictTypes"
            :key="item.id"
            class="type-item"
            :class="{ active: selectedTypeId === item.id }"
            @click="selectType(item)"
          >
            <div class="type-info">
              <span class="type-code">{{ item.dictCode }}</span>
              <span class="type-name">{{ item.dictName }}</span>
            </div>
            <div class="type-actions" @click.stop>
              <el-button type="primary" link size="small" @click="openTypeDialog(item)">
                <el-icon><Edit /></el-icon>
              </el-button>
              <el-button type="danger" link size="small" @click="handleDeleteType(item)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- ========== 右侧：字典数据 ========== -->
      <div class="panel panel-right">
        <div class="panel-header">
          <span class="panel-title">
            {{ selectedType ? `${selectedType.dictName} - 字典数据` : '字典数据' }}
          </span>
          <el-button
            v-if="selectedTypeId"
            type="primary"
            size="small"
            @click="openDataDialog()"
          >
            <el-icon><Plus /></el-icon>
            新增数据
          </el-button>
        </div>

        <!-- 未选择提示 -->
        <div v-if="!selectedTypeId" class="panel-placeholder">
          <el-icon :size="48" style="color: #c0c4cc"><ArrowLeft /></el-icon>
          <p>请在左侧选择一个字典类型</p>
        </div>

        <!-- 加载状态 -->
        <div v-else-if="dataLoading" class="panel-loading">
          <el-skeleton :rows="6" animated />
        </div>

        <!-- 错误状态 -->
        <div v-else-if="dataError" class="panel-error">
          <el-result icon="error" title="加载失败" :sub-title="dataError">
            <template #extra>
              <el-button size="small" @click="fetchDictData">重试</el-button>
            </template>
          </el-result>
        </div>

        <!-- 空状态 -->
        <el-empty v-else-if="dictDataList.length === 0" description="该类型下暂无字典数据" :image-size="80" />

        <!-- 数据表格 -->
        <el-table
          v-else
          :data="dictDataList"
          border
          stripe
          size="small"
          style="width: 100%"
        >
          <el-table-column prop="dictLabel" label="字典标签" min-width="120" />
          <el-table-column prop="dictValue" label="字典值" min-width="120" />
          <el-table-column prop="sort" label="排序" width="80" align="center" />
          <el-table-column prop="status" label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">
                {{ row.status === 'ACTIVE' ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="openDataDialog(row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button type="danger" link size="small" @click="handleDeleteData(row)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- ==================== 字典类型弹窗 ==================== -->
    <el-dialog
      v-model="typeDialogVisible"
      :title="typeEditing ? '编辑字典类型' : '新增字典类型'"
      width="480px"
      :close-on-click-modal="false"
      @closed="resetTypeForm"
    >
      <el-form ref="typeFormRef" :model="typeForm" :rules="typeFormRules" label-width="100px">
        <el-form-item label="字典编码" prop="dictCode">
          <el-input v-model="typeForm.dictCode" placeholder="如：training_type" />
        </el-form-item>
        <el-form-item label="字典名称" prop="dictName">
          <el-input v-model="typeForm.dictName" placeholder="如：训练类型" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="typeForm.status">
            <el-radio value="ACTIVE">启用</el-radio>
            <el-radio value="DISABLED">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="typeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="typeSaving" @click="handleSaveType">保存</el-button>
      </template>
    </el-dialog>

    <!-- ==================== 字典数据弹窗 ==================== -->
    <el-dialog
      v-model="dataDialogVisible"
      :title="dataEditing ? '编辑字典数据' : '新增字典数据'"
      width="480px"
      :close-on-click-modal="false"
      @closed="resetDataForm"
    >
      <el-form ref="dataFormRef" :model="dataForm" :rules="dataFormRules" label-width="100px">
        <el-form-item label="字典标签" prop="dictLabel">
          <el-input v-model="dataForm.dictLabel" placeholder="如：练胸" />
        </el-form-item>
        <el-form-item label="字典值" prop="dictValue">
          <el-input v-model="dataForm.dictValue" placeholder="如：CHEST" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="dataForm.sort" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="dataForm.status">
            <el-radio value="ACTIVE">启用</el-radio>
            <el-radio value="DISABLED">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dataDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="dataSaving" @click="handleSaveData">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
/**
 * 管理后台 - 字典管理页面
 * 左右双面板布局：左侧管理字典类型，右侧管理选中类型下的字典数据
 * 支持字典类型和数据的增删改操作
 */
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, ArrowLeft } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getDictTypes,
  createDictType,
  updateDictType,
  deleteDictType,
  getDictData,
  createDictData,
  updateDictData,
  deleteDictData
} from '@/api/admin'

// ==================== 类型定义 ====================

/** 字典类型 */
interface DictType {
  id: number
  dictCode: string
  dictName: string
  status: string
}

/** 字典数据 */
interface DictData {
  id: number
  dictTypeId: number
  dictLabel: string
  dictValue: string
  sort: number
  status: string
}

// ==================== 字典类型状态 ====================

const dictTypes = ref<DictType[]>([])
const typeLoading = ref(true)
const typeError = ref('')
const typeSaving = ref(false)
const typeDialogVisible = ref(false)
const typeEditing = ref(false)
const typeEditingId = ref<number | null>(null)
const typeFormRef = ref<FormInstance>()
const selectedTypeId = ref<number | null>(null)

/** 当前选中的字典类型 */
const selectedType = computed(() => {
  return dictTypes.value.find(t => t.id === selectedTypeId.value) || null
})

const typeForm = reactive({
  dictCode: '',
  dictName: '',
  status: 'ACTIVE'
})

const typeFormRules: FormRules = {
  dictCode: [
    { required: true, message: '请输入字典编码', trigger: 'blur' },
    { max: 50, message: '编码不超过50个字符', trigger: 'blur' }
  ],
  dictName: [
    { required: true, message: '请输入字典名称', trigger: 'blur' },
    { max: 50, message: '名称不超过50个字符', trigger: 'blur' }
  ],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

// ==================== 字典数据状态 ====================

const dictDataList = ref<DictData[]>([])
const dataLoading = ref(false)
const dataError = ref('')
const dataSaving = ref(false)
const dataDialogVisible = ref(false)
const dataEditing = ref(false)
const dataEditingId = ref<number | null>(null)
const dataFormRef = ref<FormInstance>()

const dataForm = reactive({
  dictLabel: '',
  dictValue: '',
  sort: 0,
  status: 'ACTIVE'
})

const dataFormRules: FormRules = {
  dictLabel: [
    { required: true, message: '请输入字典标签', trigger: 'blur' },
    { max: 100, message: '标签不超过100个字符', trigger: 'blur' }
  ],
  dictValue: [
    { required: true, message: '请输入字典值', trigger: 'blur' },
    { max: 100, message: '值不超过100个字符', trigger: 'blur' }
  ],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

// ==================== 字典类型操作 ====================

/** 获取所有字典类型 */
async function fetchDictTypes() {
  typeLoading.value = true
  typeError.value = ''
  try {
    const res = await getDictTypes() as any
    dictTypes.value = Array.isArray(res) ? res : (res?.records || res?.list || [])
  } catch (err: any) {
    typeError.value = err.message || '加载字典类型失败'
  } finally {
    typeLoading.value = false
  }
}

/** 选择字典类型 */
function selectType(item: DictType) {
  selectedTypeId.value = item.id
  fetchDictData()
}

/** 打开新增/编辑类型弹窗 */
function openTypeDialog(item?: DictType) {
  if (item) {
    typeEditing.value = true
    typeEditingId.value = item.id
    typeForm.dictCode = item.dictCode
    typeForm.dictName = item.dictName
    typeForm.status = item.status
  } else {
    typeEditing.value = false
    typeEditingId.value = null
    resetTypeFormData()
  }
  typeDialogVisible.value = true
}

/** 重置类型表单 */
function resetTypeFormData() {
  typeForm.dictCode = ''
  typeForm.dictName = ''
  typeForm.status = 'ACTIVE'
}

function resetTypeForm() {
  typeFormRef.value?.resetFields()
  resetTypeFormData()
}

/** 保存字典类型 */
async function handleSaveType() {
  const valid = await typeFormRef.value?.validate().catch(() => false)
  if (!valid) return

  typeSaving.value = true
  try {
    const payload = {
      dictCode: typeForm.dictCode,
      dictName: typeForm.dictName,
      status: typeForm.status
    }

    if (typeEditing.value && typeEditingId.value) {
      await updateDictType(typeEditingId.value, payload)
      ElMessage.success('字典类型已更新')
    } else {
      await createDictType(payload)
      ElMessage.success('字典类型已添加')
    }

    typeDialogVisible.value = false
    await fetchDictTypes()
  } catch (err: any) {
    ElMessage.error(err.message || '保存失败')
  } finally {
    typeSaving.value = false
  }
}

/** 删除字典类型 */
async function handleDeleteType(item: DictType) {
  try {
    await ElMessageBox.confirm(
      `确定要删除字典类型"${item.dictName}"吗？该类型下的所有字典数据也将被删除。此操作不可撤销。`,
      '删除确认',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    await deleteDictType(item.id)
    ElMessage.success('字典类型已删除')
    if (selectedTypeId.value === item.id) {
      selectedTypeId.value = null
      dictDataList.value = []
    }
    await fetchDictTypes()
  } catch (err: any) {
    if (err !== 'cancel' && err !== 'close') {
      ElMessage.error(err.message || '删除失败')
    }
  }
}

// ==================== 字典数据操作 ====================

/** 获取选中类型的字典数据 */
async function fetchDictData() {
  if (!selectedTypeId.value) return
  dataLoading.value = true
  dataError.value = ''
  try {
    const res = await getDictData(selectedTypeId.value) as any
    dictDataList.value = Array.isArray(res) ? res : (res?.records || res?.list || [])
  } catch (err: any) {
    dataError.value = err.message || '加载字典数据失败'
  } finally {
    dataLoading.value = false
  }
}

/** 打开新增/编辑数据弹窗 */
function openDataDialog(item?: DictData) {
  if (item) {
    dataEditing.value = true
    dataEditingId.value = item.id
    dataForm.dictLabel = item.dictLabel
    dataForm.dictValue = item.dictValue
    dataForm.sort = item.sort
    dataForm.status = item.status
  } else {
    dataEditing.value = false
    dataEditingId.value = null
    resetDataFormData()
  }
  dataDialogVisible.value = true
}

/** 重置数据表单 */
function resetDataFormData() {
  dataForm.dictLabel = ''
  dataForm.dictValue = ''
  dataForm.sort = 0
  dataForm.status = 'ACTIVE'
}

function resetDataForm() {
  dataFormRef.value?.resetFields()
  resetDataFormData()
}

/** 保存字典数据 */
async function handleSaveData() {
  if (!selectedTypeId.value) return
  const valid = await dataFormRef.value?.validate().catch(() => false)
  if (!valid) return

  dataSaving.value = true
  try {
    const payload = {
      dictLabel: dataForm.dictLabel,
      dictValue: dataForm.dictValue,
      sort: dataForm.sort,
      status: dataForm.status
    }

    if (dataEditing.value && dataEditingId.value) {
      await updateDictData(dataEditingId.value, payload)
      ElMessage.success('字典数据已更新')
    } else {
      await createDictData(selectedTypeId.value, payload)
      ElMessage.success('字典数据已添加')
    }

    dataDialogVisible.value = false
    await fetchDictData()
  } catch (err: any) {
    ElMessage.error(err.message || '保存失败')
  } finally {
    dataSaving.value = false
  }
}

/** 删除字典数据 */
async function handleDeleteData(item: DictData) {
  try {
    await ElMessageBox.confirm(
      `确定要删除字典数据"${item.dictLabel}"吗？此操作不可撤销。`,
      '删除确认',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    await deleteDictData(item.id)
    ElMessage.success('字典数据已删除')
    await fetchDictData()
  } catch (err: any) {
    if (err !== 'cancel' && err !== 'close') {
      ElMessage.error(err.message || '删除失败')
    }
  }
}

// ==================== 生命周期 ====================

onMounted(() => {
  fetchDictTypes()
})
</script>

<style scoped lang="scss">
/**
 * 管理后台 - 字典管理页面样式
 */

.admin-dict-page {
  padding: 4px;
}

/* ==================== 页面标题栏 ==================== */

.page-header {
  margin-bottom: 16px;

  h2 {
    margin: 0;
    font-size: 22px;
    font-weight: 600;
    color: #303133;
  }
}

/* ==================== 双面板布局 ==================== */

.dict-panels {
  display: flex;
  gap: 16px;
  min-height: 480px;
}

.panel {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #fff;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.panel-left {
  width: 320px;
  flex-shrink: 0;
}

.panel-right {
  flex: 1;
  min-width: 0;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
  background: #fafafa;

  .panel-title {
    font-size: 14px;
    font-weight: 600;
    color: #303133;
  }
}

/* ==================== 加载/错误/占位 ==================== */

.panel-loading {
  padding: 16px;
}

.panel-error {
  padding: 16px;
}

.panel-placeholder {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  gap: 12px;

  p {
    margin: 0;
    font-size: 14px;
  }
}

/* ==================== 类型列表 ==================== */

.type-list {
  flex: 1;
  overflow-y: auto;
  padding: 4px 0;
}

.type-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  cursor: pointer;
  transition: background 0.15s;
  border-left: 3px solid transparent;

  &:hover {
    background: #f5f7fa;
  }

  &.active {
    background: #ecf5ff;
    border-left-color: #409eff;
  }

  .type-info {
    display: flex;
    flex-direction: column;
    gap: 2px;
    overflow: hidden;

    .type-code {
      font-size: 12px;
      color: #909399;
      font-family: monospace;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .type-name {
      font-size: 14px;
      color: #303133;
      font-weight: 500;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .type-actions {
    flex-shrink: 0;
    opacity: 0;
    transition: opacity 0.15s;
  }

  &:hover .type-actions {
    opacity: 1;
  }
}
</style>
