<template>
  <div class="articles-page">
    <div class="page-head">
      <div>
        <span class="page-eyebrow">内容审核</span>
        <h2>文章管理</h2>
      </div>
      <button class="tool-button is-primary" type="button" @click="$router.push('/admin/articles/create')">
        新建文章
      </button>
    </div>

    <section class="surface article-board" v-loading="loading">
      <div class="filter-bar">
        <div>
          <span class="section-eyebrow">文章管理</span>
          <div class="help-heading">
            <h3>内容审核台</h3>
            <span class="help-popover" :class="{ 'is-open': helpOpen }">
              <button
                class="help-trigger"
                type="button"
                aria-label="查看审核规则"
                :aria-expanded="helpOpen"
                @click="helpOpen = !helpOpen"
              >
                ?
              </button>
              <span class="help-card" role="tooltip">
                通过后进入对应可见范围。驳回时必须填写原因，原因会同步回创作者工作台。
              </span>
            </span>
          </div>
        </div>

        <div class="filter-group" aria-label="筛选条件">
          <label class="filter-field">
            状态
            <select v-model="status" class="status-filter" aria-label="状态" @change="handleFilterChange">
              <option value="">全部状态</option>
              <option v-for="item in statusOptions" :key="item.value" :value="item.value">
                {{ item.label }}
              </option>
            </select>
          </label>
          <label class="filter-field">
            可见性
            <select v-model="visibility" class="visibility-filter" aria-label="可见性" @change="handleFilterChange">
              <option value="">全部</option>
              <option value="public">公开</option>
              <option value="private">仅自己</option>
            </select>
          </label>
          <label class="filter-field">
            搜索
            <input
              v-model.trim="keyword"
              aria-label="搜索文章"
              placeholder="标题或摘要"
              @keyup.enter="handleFilterChange"
            >
          </label>
          <button class="tool-button" type="button" @click="handleFilterChange">筛选</button>
        </div>
      </div>

      <div class="article-table-head">
        <span>文章</span>
        <span>状态</span>
        <span>可见性</span>
        <span>操作</span>
      </div>

      <div v-if="articles.length" class="article-list">
        <article v-for="article in articles" :key="article.id" class="article-row">
          <div>
            <div class="article-title">{{ article.title }}</div>
            <div class="row-meta">{{ articleMeta(article) }}</div>
          </div>
          <span class="article-state" :class="articleStatusClass(article.status)">
            <span class="state-dot" aria-hidden="true"></span>
            {{ statusText(article.status) }}
          </span>
          <span class="visibility-mark" :class="visibilityClass(article.visibility)">
            <svg v-if="isPublicArticle(article.visibility)" class="visibility-icon" viewBox="0 0 24 24" aria-hidden="true">
              <path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7-10-7-10-7Z" />
              <circle cx="12" cy="12" r="3" />
            </svg>
            <svg v-else class="visibility-icon" viewBox="0 0 24 24" aria-hidden="true">
              <rect x="5" y="11" width="14" height="10" rx="2" />
              <path d="M8 11V8a4 4 0 0 1 8 0v3" />
            </svg>
            {{ visibilityText(article.visibility) }}
          </span>
          <div class="row-actions">
            <button
              class="row-action-button"
              type="button"
              :disabled="isBusy(article.id)"
              @click="$router.push(`/admin/articles/edit/${article.id}`)"
            >
              编辑
            </button>
            <button
              v-if="article.status === 'pending'"
              class="row-action-button is-success"
              type="button"
              :disabled="isBusy(article.id)"
              @click="handleApprove(article.id)"
            >
              通过
            </button>
            <button
              v-if="article.status === 'pending'"
              class="row-action-button is-warning"
              type="button"
              :disabled="isBusy(article.id)"
              @click="handleReject(article.id)"
            >
              驳回
            </button>
            <button
              class="row-action-button is-danger"
              type="button"
              :disabled="isBusy(article.id)"
              @click="handleDelete(article.id)"
            >
              删除
            </button>
          </div>
        </article>
      </div>
      <div v-else class="empty-state">暂无符合条件的文章</div>
    </section>

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
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { approveArticle, deleteAdminArticle, getAdminArticles, rejectArticle } from '../../api/article'
import { articleVisibilityText, normalizeArticleVisibility } from '../../utils/articleVisibility'
import { formatDate } from '../../utils'

const statusMap = {
  draft: { text: '草稿', tone: 'neutral' },
  pending: { text: '待审核', tone: 'warning' },
  published: { text: '已发布', tone: 'success' },
  rejected: { text: '已驳回', tone: 'danger' },
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
const keyword = ref('')
const loading = ref(false)
const busyIds = ref(new Set())
const helpOpen = ref(false)

onMounted(() => load())

function statusText(value) {
  return statusMap[value]?.text || value || '-'
}

function articleStatusClass(value) {
  const tone = statusMap[value]?.tone
  return tone ? `is-${tone}` : ''
}

function visibilityText(value) {
  return normalizeArticleVisibility(value) === 'private' ? '仅自己' : articleVisibilityText(value)
}

function visibilityClass(value) {
  return normalizeArticleVisibility(value) === 'private' ? 'is-private' : 'is-public'
}

function isPublicArticle(value) {
  return normalizeArticleVisibility(value) === 'public'
}

function articleMeta(article) {
  const author = article.createdBy || '未知作者'
  const time = formatDate(article.updatedAt || article.createdAt) || '暂无时间'
  if (article.reviewReason) return `${author} / ${time} / 驳回原因：${article.reviewReason}`
  return `${author} / ${time} / 阅读 ${article.viewCount || 0}`
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
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (status.value) params.status = status.value
    if (visibility.value) params.visibility = visibility.value
    if (keyword.value) params.keyword = keyword.value
    const result = await getAdminArticles(params)
    articles.value = result.data || []
    total.value = result.total || 0
  } finally {
    loading.value = false
  }
}

function handleFilterChange() {
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
      inputValidator: value => !!value?.trim(),
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
    await ElMessageBox.confirm('确定删除这篇文章？', '提示')
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
.articles-page {
  display: grid;
  gap: 16px;
}

.article-list {
  display: grid;
}

@media (max-width: 980px) {
  .filter-group {
    width: 100%;
  }
}
</style>
