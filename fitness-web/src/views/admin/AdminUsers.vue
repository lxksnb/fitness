<template>
  <!-- 管理后台 - 用户管理 -->
  <div class="admin-users-page">
    <!-- ==================== 页面标题 ==================== -->
    <div class="page-header">
      <h2>用户管理</h2>
    </div>

    <!-- ==================== 数据表格 ==================== -->
    <el-table
      v-loading="loading"
      :data="tableData"
      border
      stripe
      style="width: 100%"
      empty-text="暂无用户数据"
    >
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="username" label="用户名" min-width="120" />
      <el-table-column label="昵称" min-width="120">
        <template #default="{ row }">
          {{ row.nickname || '--' }}
        </template>
      </el-table-column>
      <el-table-column label="邮箱" min-width="180">
        <template #default="{ row }">
          {{ row.email || '--' }}
        </template>
      </el-table-column>
      <el-table-column label="角色" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'info'" size="small">
            {{ row.role === 'ADMIN' ? '管理员' : '用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'" size="small">
            {{ row.status === 'ACTIVE' ? '正常' : '已禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="注册时间" width="180" align="center">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" align="center" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 'ACTIVE'"
            type="warning"
            link
            size="small"
            @click="handleToggleStatus(row)"
          >
            <el-icon><Lock /></el-icon>
            禁用
          </el-button>
          <el-button
            v-else
            type="success"
            link
            size="small"
            @click="handleToggleStatus(row)"
          >
            <el-icon><Unlock /></el-icon>
            启用
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
  </div>
</template>

<script setup lang="ts">
/**
 * 管理后台 - 用户管理页面
 * 管理员查看用户列表，支持启用/禁用用户状态切换
 * 不提供创建和编辑用户功能（用户通过正常注册流程创建）
 */
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Lock, Unlock } from '@element-plus/icons-vue'
import { getAdminUsers, updateUserStatus } from '@/api/admin'

// ==================== 类型定义 ====================

/** 用户列表项 */
interface UserItem {
  id: number
  username: string
  nickname?: string
  email?: string
  role: string
  status: string
  createdAt?: string
}

// ==================== 状态 ====================

const loading = ref(true)
const error = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

/** 表格数据 */
const tableData = ref<UserItem[]>([])

// ==================== 工具函数 ====================

/** 格式化日期 */
function formatDate(dateStr?: string): string {
  if (!dateStr) return '--'
  const d = new Date(dateStr)
  if (isNaN(d.getTime())) return dateStr
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

// ==================== 数据获取 ====================

/** 获取用户列表 */
async function fetchList() {
  loading.value = true
  error.value = ''
  try {
    const res = await getAdminUsers() as any
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

// ==================== 操作 ====================

/** 切换用户状态（启用/禁用） */
async function handleToggleStatus(user: UserItem) {
  const newStatus = user.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
  const actionText = newStatus === 'DISABLED' ? '禁用' : '启用'

  try {
    await ElMessageBox.confirm(
      `确定要${actionText}用户"${user.username}"吗？`,
      `${actionText}确认`,
      {
        confirmButtonText: `确定${actionText}`,
        cancelButtonText: '取消',
        type: newStatus === 'DISABLED' ? 'warning' : 'info'
      }
    )

    await updateUserStatus(user.id, newStatus)
    ElMessage.success(`用户已${actionText}`)

    // 更新本地列表状态，避免重新请求
    user.status = newStatus
  } catch (err: any) {
    if (err !== 'cancel' && err !== 'close') {
      ElMessage.error(err.message || '操作失败')
    }
  }
}

// ==================== 生命周期 ====================

onMounted(() => {
  fetchList()
})
</script>

<style scoped lang="scss">
/**
 * 管理后台 - 用户管理页面样式
 */

.admin-users-page {
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
}

/* ==================== 分页 ==================== */
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
