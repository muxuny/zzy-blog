<template>
  <div class="layout">
    <AppHeader />
    <el-main class="main">
      <div class="page-header">
        <div>
          <span class="page-kicker">创作中心</span>
          <h1>我的文章</h1>
        </div>
        <el-button type="primary" @click="$router.push('/creator/articles/create')">写文章</el-button>
      </div>

      <div class="filters">
        <el-select v-model="status" placeholder="全部状态" clearable @change="handleStatusChange">
          <el-option label="草稿" value="draft" />
          <el-option label="待审核" value="pending" />
          <el-option label="已发布" value="published" />
          <el-option label="已驳回" value="rejected" />
        </el-select>
      </div>

      <el-table v-loading="loading" :data="articles" stripe class="article-table">
        <el-table-column prop="title" label="标题" min-width="220" />
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reviewReason" label="审核反馈" min-width="180">
          <template #default="{ row }">{{ row.reviewReason || '-' }}</template>
        </el-table-column>
        <el-table-column label="更新时间" width="180">
          <template #default="{ row }">{{ formatDate(row.updatedAt || row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="320">
          <template #default="{ row }">
            <el-button
              v-if="row.status !== 'pending'"
              size="small"
              :disabled="isRowBusy(row.id)"
              @click="$router.push(`/creator/articles/edit/${row.id}`)"
            >
              编辑
            </el-button>
            <el-button
              v-if="row.status === 'draft' || row.status === 'rejected'"
              size="small"
              type="success"
              :loading="rowAction[row.id] === 'submit'"
              :disabled="isRowBusy(row.id)"
              @click="submit(row.id)"
            >
              提交审核
            </el-button>
            <el-button
              v-if="row.status === 'pending'"
              size="small"
              :loading="rowAction[row.id] === 'withdraw'"
              :disabled="isRowBusy(row.id)"
              @click="withdraw(row.id)"
            >
              撤回
            </el-button>
            <el-button
              v-if="row.status === 'published'"
              size="small"
              :disabled="isRowBusy(row.id)"
              @click="$router.push(`/article/${row.id}`)"
            >
              查看已发布
            </el-button>
            <el-button
              size="small"
              type="danger"
              :loading="rowAction[row.id] === 'delete'"
              :disabled="isRowBusy(row.id)"
              @click="remove(row.id)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!articles.length && !loading" description="暂无文章" />
      <el-pagination
        v-if="total > size"
        v-model:current-page="page"
        :total="total"
        :page-size="size"
        layout="prev,pager,next"
        class="article-pagination"
        @current-change="load"
      />
    </el-main>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import AppHeader from '../../components/AppHeader.vue'
import { deleteMyArticle, getMyArticles, submitMyArticle, withdrawMyArticle } from '../../api/myArticle'
import { formatDate } from '../../utils'

const articles = ref([])
const loading = ref(false)
const status = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const rowAction = ref({})

const statusMap = {
  draft: { text: '草稿', type: 'info' },
  pending: { text: '待审核', type: 'warning' },
  published: { text: '已发布', type: 'success' },
  rejected: { text: '已驳回', type: 'danger' }
}

onMounted(load)

function statusText(value) {
  return statusMap[value]?.text || value
}

function statusType(value) {
  return statusMap[value]?.type || 'info'
}

async function load() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (status.value) params.status = status.value
    const result = await getMyArticles(params)
    articles.value = result.data || []
    total.value = result.total || 0
  } finally {
    loading.value = false
  }
}

function handleStatusChange() {
  page.value = 1
  load()
}

function isRowBusy(id) {
  return !!rowAction.value[id]
}

async function runRowAction(id, action, request) {
  if (isRowBusy(id)) return
  rowAction.value = { ...rowAction.value, [id]: action }
  try {
    await request()
    await load()
  } finally {
    const next = { ...rowAction.value }
    delete next[id]
    rowAction.value = next
  }
}

async function submit(id) {
  await runRowAction(id, 'submit', async () => {
    await submitMyArticle(id)
    ElMessage.success('已提交审核')
  })
}

async function withdraw(id) {
  await runRowAction(id, 'withdraw', async () => {
    await withdrawMyArticle(id)
    ElMessage.success('已撤回为草稿')
  })
}

async function remove(id) {
  await ElMessageBox.confirm('确定删除这篇文章吗？', '提示')
  await runRowAction(id, 'delete', async () => {
    await deleteMyArticle(id)
    ElMessage.success('删除成功')
  })
}
</script>

<style scoped>
.main {
  width: min(100%, var(--content-width));
  margin: 0 auto;
  padding: 32px 24px 64px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
  margin-bottom: 18px;
}

.page-kicker {
  color: var(--accent-color);
  font-size: 12px;
  font-weight: 800;
}

.page-header h1 {
  margin: 4px 0 0;
  color: var(--text-color);
  font-size: 30px;
}

.filters {
  margin-bottom: 16px;
}

.filters .el-select {
  width: 180px;
}

.article-table {
  margin-top: 12px;
}

.article-pagination {
  margin-top: 16px;
}

@media (max-width: 760px) {
  .main {
    padding: 24px 14px 48px;
  }

  .page-header {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
