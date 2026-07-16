<template>
  <div class="admin-page dashboard-page">
    <div class="page-header dashboard-header">
      <div>
        <span class="page-kicker">概览</span>
        <h2>仪表盘</h2>
      </div>
      <el-button :loading="loading" @click="loadDashboard">刷新</el-button>
    </div>

    <el-alert
      v-if="loadError"
      class="dashboard-alert"
      type="warning"
      :title="loadError"
      show-icon
      :closable="false"
    />

    <div class="stat-grid">
      <section
        v-for="card in metricCards"
        :key="card.label"
        class="stat-card"
        :class="`tone-${card.tone}`"
      >
        <span>{{ card.label }}</span>
        <strong>{{ card.value }}</strong>
        <small>{{ card.note }}</small>
      </section>
    </div>

    <div class="dashboard-grid">
      <section v-loading="loading" class="panel status-panel">
        <div class="panel-head">
          <div>
            <span class="panel-kicker">内容流转</span>
            <h3>文章状态分布</h3>
          </div>
          <strong>{{ stats.metrics.totalArticles }}</strong>
        </div>

        <div class="status-track" aria-label="文章状态分布">
          <template v-for="item in stats.articleStatus" :key="item.key">
            <span
              v-if="item.percent > 0"
              :class="`status-segment tone-${item.tone}`"
              :style="{ width: `${item.percent}%` }"
              :title="`${item.label} ${item.count}`"
            />
          </template>
        </div>

        <div class="status-list">
          <div v-for="item in stats.articleStatus" :key="item.key" class="status-row">
            <span class="status-dot" :class="`tone-${item.tone}`" />
            <span>{{ item.label }}</span>
            <strong>{{ item.count }}</strong>
            <em>{{ item.percent }}%</em>
          </div>
        </div>
      </section>

      <section v-loading="loading" class="panel queue-panel">
        <div class="panel-head">
          <div>
            <span class="panel-kicker">待处理</span>
            <h3>审核队列</h3>
          </div>
          <el-tag :type="hasWork ? 'warning' : 'success'">
            {{ hasWork ? '有待处理' : '已清空' }}
          </el-tag>
        </div>

        <div class="queue-group">
          <div class="queue-title">待审核文章</div>
          <button
            v-for="article in stats.pendingArticles"
            :key="article.id"
            class="queue-item"
            type="button"
            @click="goArticle(article)"
          >
            <span>{{ article.title }}</span>
            <small>{{ article.createdBy || '未知作者' }}</small>
          </button>
          <el-empty v-if="!stats.pendingArticles.length" description="暂无待审核文章" :image-size="64" />
        </div>

        <div class="queue-group">
          <div class="queue-title">待审核用户</div>
          <button
            v-for="user in stats.pendingUsers"
            :key="user.id"
            class="queue-item"
            type="button"
            @click="goUsers"
          >
            <span>{{ user.nickname || user.username }}</span>
            <small>{{ user.email || user.username }}</small>
          </button>
          <el-empty v-if="!stats.pendingUsers.length" description="暂无待审核用户" :image-size="64" />
        </div>
      </section>
    </div>

    <section v-loading="loading" class="panel tag-panel">
      <div class="panel-head">
        <div>
          <span class="panel-kicker">内容组织</span>
          <h3>标签概览</h3>
        </div>
        <strong>{{ stats.tagSummary.total }}</strong>
      </div>

      <div v-if="stats.tagSummary.items.length" class="tag-cloud">
        <el-tag v-for="tag in stats.tagSummary.items" :key="tag.id || tag.name" effect="plain">
          {{ tag.name }}
        </el-tag>
      </div>
      <el-empty v-else description="暂无标签" :image-size="64" />
    </section>
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

const metricCards = computed(() => [
  { label: '文章总数', value: stats.value.metrics.totalArticles, note: '全部状态内容', tone: 'primary' },
  { label: '已发布', value: stats.value.metrics.publishedArticles, note: '包含公开和私密', tone: 'success' },
  { label: '公开可见', value: stats.value.metrics.publicPublishedArticles, note: '会展示在博客前台', tone: 'public' },
  { label: '仅自己可见', value: stats.value.metrics.privateArticles, note: '作者私密保留', tone: 'private' },
  { label: '待审核文章', value: stats.value.metrics.pendingArticles, note: '需要管理员处理', tone: 'warning' },
  { label: '待审核用户', value: stats.value.metrics.pendingUsers, note: '注册后等待通过', tone: 'danger' }
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
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.dashboard-header {
  align-items: center;
}

.page-kicker,
.panel-kicker {
  display: inline-block;
  margin-bottom: 4px;
  color: var(--accent-color);
  font-size: 12px;
  font-weight: 800;
}

.dashboard-alert {
  border-radius: 8px;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 14px;
}

.stat-card,
.panel {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  box-shadow: 0 14px 36px var(--shadow-color);
}

.stat-card {
  position: relative;
  min-height: 132px;
  overflow: hidden;
  padding: 18px;
}

.stat-card::before {
  content: "";
  position: absolute;
  inset: 0 auto 0 0;
  width: 4px;
  background: var(--tone-color, var(--accent-color));
}

.stat-card span,
.stat-card small {
  display: block;
  color: var(--muted-text-color);
}

.stat-card span {
  font-size: 13px;
  font-weight: 750;
}

.stat-card strong {
  display: block;
  margin-top: 12px;
  color: var(--text-color);
  font-size: 36px;
  line-height: 1;
}

.stat-card small {
  margin-top: 12px;
  font-size: 12px;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(340px, 0.85fr);
  gap: 18px;
}

.panel {
  padding: 18px;
}

.panel-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.panel-head h3 {
  margin: 0;
  color: var(--text-color);
  font-size: 18px;
}

.panel-head strong {
  color: var(--text-color);
  font-size: 28px;
  line-height: 1;
}

.status-track {
  display: flex;
  width: 100%;
  height: 18px;
  overflow: hidden;
  background: var(--hover-bg);
  border: 1px solid var(--border-color);
  border-radius: 999px;
}

.status-segment {
  min-width: 0;
  height: 100%;
  background: var(--tone-color);
}

.status-list {
  display: grid;
  gap: 12px;
  margin-top: 18px;
}

.status-row {
  display: grid;
  grid-template-columns: 14px 1fr auto 48px;
  align-items: center;
  gap: 10px;
  color: var(--text-color);
  font-size: 14px;
}

.status-row em {
  color: var(--muted-text-color);
  font-style: normal;
  text-align: right;
}

.status-dot {
  width: 9px;
  height: 9px;
  border-radius: 999px;
  background: var(--tone-color);
}

.queue-panel {
  display: grid;
  gap: 14px;
}

.queue-group {
  display: grid;
  gap: 8px;
}

.queue-title {
  color: var(--muted-text-color);
  font-size: 13px;
  font-weight: 750;
}

.queue-item {
  display: grid;
  gap: 4px;
  width: 100%;
  padding: 10px 12px;
  color: var(--text-color);
  text-align: left;
  background: var(--hover-bg);
  border: 1px solid transparent;
  border-radius: 8px;
  cursor: pointer;
}

.queue-item:hover,
.queue-item:focus-visible {
  border-color: var(--accent-color);
  outline: none;
}

.queue-item span {
  overflow: hidden;
  font-size: 14px;
  font-weight: 750;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.queue-item small {
  overflow: hidden;
  color: var(--muted-text-color);
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tone-primary {
  --tone-color: var(--accent-color);
}

.tone-success {
  --tone-color: #22a06b;
}

.tone-public {
  --tone-color: #2877d9;
}

.tone-private {
  --tone-color: #8a6be8;
}

.tone-warning {
  --tone-color: #d9971a;
}

.tone-danger {
  --tone-color: #d84c4c;
}

.tone-neutral {
  --tone-color: #7b8794;
}

@media (max-width: 1080px) {
  .stat-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .dashboard-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 560px) {
  .stat-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .panel-head {
    align-items: stretch;
    flex-direction: column;
  }

  .status-row {
    grid-template-columns: 14px 1fr auto;
  }

  .status-row em {
    display: none;
  }
}
</style>
