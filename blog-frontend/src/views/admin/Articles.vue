<template>
  <div>
    <div class="page-header">
      <h2>文章管理</h2>
    </div>

    <div class="admin-toolbar">
      <label class="filter-field">
        <span>筛选状态</span>
        <el-select v-model="status" class="status-filter" placeholder="全部状态" clearable @change="handleStatusChange">
          <el-option label="全部状态" value="" />
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </label>
      <label class="filter-field">
        <span>筛选可见性</span>
        <el-select
          v-model="visibility"
          class="visibility-filter"
          placeholder="全部可见性"
          clearable
          @change="handleVisibilityChange"
        >
          <el-option label="全部可见性" value="" />
          <el-option label="公开" value="public" />
          <el-option label="仅自己可见" value="private" />
        </el-select>
      </label>
    </div>

    <el-table :data="articles" stripe class="admin-table">
      <el-table-column prop="title" label="标题" min-width="200" />
      <el-table-column prop="createdBy" label="作者" width="120" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">
            {{ statusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="visibility" label="可见性" width="130">
        <template #default="{ row }">
          <el-tag :type="articleVisibilityType(row.visibility)" effect="plain">
            {{ articleVisibilityText(row.visibility) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="reviewReason" label="驳回原因" min-width="160">
        <template #default="{ row }">{{ row.reviewReason || '-' }}</template>
      </el-table-column>
      <el-table-column prop="viewCount" label="阅读" width="80" />
      <el-table-column label="时间" width="180">
        <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button size="small" :disabled="isBusy(row.id)" @click="$router.push(`/admin/articles/edit/${row.id}`)">
            编辑
          </el-button>
          <el-button
            v-if="row.status === 'pending'"
            size="small"
            type="success"
            :loading="isBusy(row.id)"
            :disabled="isBusy(row.id)"
            @click="handleApprove(row.id)"
          >
            通过
          </el-button>
          <el-button
            v-if="row.status === 'pending'"
            size="small"
            type="warning"
            :loading="isBusy(row.id)"
            :disabled="isBusy(row.id)"
            @click="handleReject(row.id)"
          >
            驳回
          </el-button>
          <el-button
            size="small"
            type="danger"
            :loading="isBusy(row.id)"
            :disabled="isBusy(row.id)"
            @click="handleDelete(row.id)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-if="total > size"
      v-model:current-page="page"
      :total="total"
      :page-size="size"
      layout="prev,pager,next"
      class="admin-pagination"
      @current-change="load"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { approveArticle, deleteAdminArticle, getAdminArticles, rejectArticle } from '../../api/article'
import { ElMessage, ElMessageBox } from 'element-plus'
import { articleVisibilityText, articleVisibilityType } from '../../utils/articleVisibility'
import { formatDate } from '../../utils'

const statusMap = {
  draft: { text: '草稿', type: 'info' },
  pending: { text: '待审核', type: 'warning' },
  published: { text: '已发布', type: 'success' },
  rejected: { text: '已驳回', type: 'danger' },
}

const statusOptions = Object.entries(statusMap).map(([value, item]) => ({
  value,
  label: item.text,
}))

const articles = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const status = ref('')
const visibility = ref('')
const busyIds = ref(new Set())

onMounted(() => load())

function statusText(value) {
  return statusMap[value]?.text || value || '-'
}

function statusType(value) {
  return statusMap[value]?.type || 'info'
}

function isBusy(id) {
  return busyIds.value.has(id)
}

function setBusy(id, busy) {
  const next = new Set(busyIds.value)
  if (busy) {
    next.add(id)
  } else {
    next.delete(id)
  }
  busyIds.value = next
}

async function load() {
  const params = { page: page.value, size: size.value }
  if (status.value) params.status = status.value
  if (visibility.value) params.visibility = visibility.value
  const r = await getAdminArticles(params)
  articles.value = r.data || []
  total.value = r.total || 0
}

function handleStatusChange() {
  page.value = 1
  load()
}

function handleVisibilityChange() {
  page.value = 1
  load()
}

async function handleApprove(id) {
  if (isBusy(id)) return
  setBusy(id, true)
  try {
    await approveArticle(id)
    ElMessage.success('审核通过')
    await load()
  } finally {
    setBusy(id, false)
  }
}

async function handleReject(id) {
  if (isBusy(id)) return
  setBusy(id, true)
  try {
    const reason = await ElMessageBox.prompt('请输入驳回原因', '驳回文章', {
      confirmButtonText: '驳回',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputPlaceholder: '驳回原因',
      inputValidator: value => {
        if (!value || !value.trim()) return false
        return true
      },
      inputErrorMessage: '驳回原因不能为空',
    })
    await rejectArticle(id, reason.value.trim())
    ElMessage.success('已驳回')
    await load()
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    throw error
  } finally {
    setBusy(id, false)
  }
}

async function handleDelete(id) {
  if (isBusy(id)) return
  setBusy(id, true)
  try {
    await ElMessageBox.confirm('确定删除？', '提示')
    await deleteAdminArticle(id)
    ElMessage.success('删除成功')
    await load()
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    throw error
  } finally {
    setBusy(id, false)
  }
}
</script>

<style scoped>
.admin-toolbar {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 12px;
}

.filter-field {
  display: grid;
  gap: 6px;
  color: var(--muted-text-color);
  font-size: 12px;
  font-weight: 700;
}

.status-filter,
.visibility-filter {
  width: 160px;
}

.admin-table,
.admin-pagination {
  margin-top: 16px;
}
</style>
