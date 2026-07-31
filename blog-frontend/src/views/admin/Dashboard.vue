<template>
  <div class="dashboard-page">
    <div class="page-head">
      <div>
        <span class="page-eyebrow">概览</span>
        <h2>仪表盘</h2>
      </div>
      <button class="tool-button" type="button" :disabled="loading" @click="loadDashboard">
        {{ loading ? '刷新中' : '刷新' }}
      </button>
    </div>

    <el-alert
      v-if="loadError"
      class="dashboard-alert"
      type="warning"
      :title="loadError"
      show-icon
      :closable="false"
    />

    <div class="signal-strip" v-loading="loading">
      <section class="signal-primary">
        <div>
          <span class="section-eyebrow">内容总览</span>
          <h3>当前内容池</h3>
          <p class="panel-caption">文章、用户和资源状态在这里汇总。</p>
        </div>
        <strong class="big-number">{{ stats.metrics.totalArticles }}</strong>
      </section>

      <section v-for="card in quickStats" :key="card.label" class="signal-stat">
        <span class="tiny-label">{{ card.label }}</span>
        <strong>{{ card.value }}</strong>
        <div class="row-meta">{{ card.note }}</div>
      </section>
    </div>

    <div class="dashboard-grid">
      <section class="surface" v-loading="loading">
        <div class="panel-head">
          <div>
            <span class="section-eyebrow">内容流转</span>
            <h3>文章状态分布</h3>
          </div>
          <span class="chip">{{ stats.metrics.totalArticles }} 篇</span>
        </div>

        <div class="status-ledger" aria-label="文章状态分布">
          <div v-for="item in stats.articleStatus" :key="item.key" class="ledger-row">
            <span>{{ item.label }}</span>
            <div class="track" :class="trackClass(item.tone)">
              <span :style="{ width: `${Math.max(item.percent, item.count ? 6 : 0)}%` }"></span>
            </div>
            <strong>{{ item.percent }}%</strong>
          </div>
        </div>

        <div class="content-preview">
          <div class="panel-head">
            <div>
              <span class="section-eyebrow">标签概览</span>
              <h3>内容归类</h3>
            </div>
            <button class="tool-button is-text" type="button" @click="router.push('/admin/resources')">管理资源</button>
          </div>
          <div v-if="stats.tagSummary.items.length" class="chip-row">
            <span v-for="tag in stats.tagSummary.items" :key="tag.id || tag.name" class="chip">{{ tag.name }}</span>
          </div>
          <div v-else class="empty-state">暂无标签</div>
        </div>
      </section>

      <section class="surface" v-loading="loading">
        <div class="panel-head">
          <div>
            <span class="section-eyebrow">工作队列</span>
            <h3>优先处理</h3>
          </div>
          <span class="chip" :class="{ 'is-warning': hasWork, 'is-success': !hasWork }">
            {{ hasWork ? '有待处理' : '已清空' }}
          </span>
        </div>

        <div class="review-list">
          <div class="review-item">
            <div class="review-title">
              <span>文章审核</span>
              <span>{{ stats.metrics.pendingArticles }}</span>
            </div>
            <div v-if="stats.pendingArticles.length" class="queue-lines">
              <button
                v-for="article in stats.pendingArticles"
                :key="article.id"
                class="queue-line"
                type="button"
                @click="goArticle(article)"
              >
                <span>{{ article.title }}</span>
                <small>{{ article.createdBy || '未知作者' }}</small>
              </button>
            </div>
            <div v-else class="row-meta">没有需要审核的文章。</div>
          </div>

          <div class="review-item">
            <div class="review-title">
              <span>用户审核</span>
              <span>{{ stats.metrics.pendingUsers }}</span>
            </div>
            <div v-if="stats.pendingUsers.length" class="queue-lines">
              <button
                v-for="user in stats.pendingUsers"
                :key="user.id"
                class="queue-line"
                type="button"
                @click="goUsers"
              >
                <span>{{ user.nickname || user.username }}</span>
                <small>{{ user.email || user.username }}</small>
              </button>
            </div>
            <div v-else class="row-meta">没有等待通过的账号。</div>
          </div>

          <div class="review-item">
            <div class="review-title">
              <span>资源维护</span>
              <span>{{ stats.tagSummary.total }}</span>
            </div>
            <div class="row-meta">标签和图片已合并到资源管理。</div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getAdminArticles } from '../../api/article'
import { getTags } from '../../api/tag'
import { getUsers } from '../../api/user'
import { buildDashboardStats } from '../../utils/dashboardStats'

const router = useRouter()
const loading = ref(true)
const loadError = ref('')
const stats = ref(buildDashboardStats())

const quickStats = computed(() => [
  { label: '已发布', value: stats.value.metrics.publishedArticles, note: '公开和私密内容' },
  { label: '公开可见', value: stats.value.metrics.publicPublishedArticles, note: '展示在前台' },
  { label: '待处理', value: stats.value.metrics.pendingArticles + stats.value.metrics.pendingUsers, note: '文章和账号' },
])

const hasWork = computed(() => stats.value.pendingArticles.length > 0 || stats.value.pendingUsers.length > 0)

async function safeRequest(task) {
  try {
    return await task()
  } catch (error) {
    loadError.value = '部分数据加载失败'
    return null
  }
}

async function loadDashboard() {
  loading.value = true
  loadError.value = ''

  const [allArticles, pendingArticles, tags, users] = await Promise.all([
    safeRequest(() => getAdminArticles({ page: 1, size: 200 })),
    safeRequest(() => getAdminArticles({ page: 1, size: 5, status: 'pending' })),
    safeRequest(() => getTags()),
    safeRequest(() => getUsers({ page: 1, size: 100 }))
  ])

  stats.value = buildDashboardStats({
    allArticles: allArticles?.data || [],
    tags: tags?.data || [],
    users: users?.data || []
  })

  if (pendingArticles?.data?.length) {
    stats.value.pendingArticles = pendingArticles.data
  }

  loading.value = false
}

function trackClass(tone) {
  if (tone === 'warning') return 'is-warning'
  if (tone === 'danger') return 'is-danger'
  return ''
}

function goArticle(article) {
  router.push(`/admin/articles/edit/${article.id}`)
}

function goUsers() {
  router.push('/admin/users')
}

onMounted(loadDashboard)
</script>

<style scoped>
.dashboard-page {
  display: grid;
  gap: 16px;
}

.dashboard-alert {
  border-radius: var(--radius-md);
}

.signal-strip {
  display: grid;
  grid-template-columns: 1.34fr repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.signal-primary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px;
  background:
    linear-gradient(135deg, color-mix(in srgb, var(--primary-color) 12%, transparent), transparent 66%),
    color-mix(in srgb, var(--panel-bg) 96%, var(--bg-color));
}

.big-number {
  color: var(--text-color);
  font-family: Georgia, "Times New Roman", serif;
  font-size: 48px;
  font-weight: 500;
  line-height: 1;
}

.signal-stat {
  padding: 16px;
}

.signal-stat strong {
  display: block;
  margin-top: 8px;
  color: var(--text-color);
  font-size: 24px;
  font-weight: 760;
  line-height: 1.15;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(290px, 0.72fr);
  gap: 16px;
}

.content-preview {
  margin-top: 20px;
  padding-top: 18px;
  border-top: 1px solid var(--soft-border-color);
}

.queue-lines {
  display: grid;
  gap: 6px;
  margin-top: 8px;
}

.queue-line {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 8px 0;
  border: 0;
  border-top: 1px solid color-mix(in srgb, var(--soft-border-color) 72%, transparent);
  color: var(--text-color);
  background: transparent;
  text-align: left;
}

.queue-line span,
.queue-line small {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.queue-line span {
  font-weight: 700;
}

.queue-line small {
  color: var(--muted-text-color);
  font-size: 12px;
}

@media (max-width: 1080px) {
  .signal-strip {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .dashboard-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .signal-strip {
    grid-template-columns: 1fr;
  }
}
</style>
