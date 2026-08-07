<template>
  <div class="dashboard-page">
    <div class="page-head">
      <div>
        <span class="page-eyebrow">{{ pageCopy.eyebrow }}</span>
        <h2>{{ pageCopy.title }}</h2>
        <p v-if="pageCopy.description" class="page-description">{{ pageCopy.description }}</p>
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

    <div class="tech-board" v-loading="loading">
      <div class="signal-row">
        <section class="signal-card">
          <div class="signal-topline">
            <span class="signal-icon">文</span>
            <span class="signal-code">A-01</span>
          </div>
          <small>全站文章</small>
          <strong>{{ stats.metrics.totalArticles }}</strong>
          <span>已发布 {{ stats.metrics.publishedArticles }} / 待审核 {{ stats.metrics.pendingArticles }}</span>
        </section>

        <section class="signal-card">
          <div class="signal-topline">
            <span class="signal-icon">审</span>
            <span class="signal-code">Q-02</span>
          </div>
          <small>待处理队列</small>
          <strong>{{ pendingTotal }}</strong>
          <span>文章 {{ stats.metrics.pendingArticles }} / 用户 {{ stats.metrics.pendingUsers }}</span>
        </section>

        <section class="signal-card">
          <div class="signal-topline">
            <span class="signal-icon">览</span>
            <span class="signal-code">V-03</span>
          </div>
          <small>全站浏览</small>
          <strong>{{ formatNumber(stats.trafficSummary.totalViews) }}</strong>
          <span>平均 {{ formatNumber(stats.trafficSummary.averageViews) }} / 篇</span>
        </section>

        <section class="signal-card">
          <div class="signal-topline">
            <span class="signal-icon">读</span>
            <span class="signal-code">R-04</span>
          </div>
          <small>30 日阅读活跃</small>
          <strong>{{ stats.readingSummary.recent30Days }}</strong>
          <span>登录用户阅读记录</span>
        </section>

        <section class="signal-card">
          <div class="signal-topline">
            <span class="signal-icon">源</span>
            <span class="signal-code">S-05</span>
          </div>
          <small>资源体量</small>
          <strong>{{ formatBytes(stats.resourceSummary.totalImageSize) }}</strong>
          <span>图片 {{ stats.resourceSummary.totalImages }} / 标签 {{ stats.resourceSummary.totalTags }}</span>
        </section>
      </div>

      <div class="dashboard-core">
        <section class="dash-panel content-structure-panel">
          <div class="panel-head">
            <div>
              <span class="panel-kicker">内容结构</span>
              <h3>状态与可见性</h3>
            </div>
            <span class="micro-pill">全站</span>
          </div>

          <div class="ring-stack">
            <div class="ring-block">
              <div class="ring" :style="articleRingStyle" data-center="128" aria-label="文章状态分布"></div>
              <div class="legend-list">
                <div v-for="item in stats.articleStatus" :key="item.key" class="legend-row">
                  <span><i class="legend-dot" :style="{ background: statusColor(item.key) }"></i>{{ item.label }}</span>
                  <strong>{{ item.percent }}%</strong>
                </div>
              </div>
            </div>

            <div class="ring-block">
              <div class="ring is-visibility" :style="visibilityRingStyle" data-center="可见"></div>
              <div class="legend-list">
                <div class="legend-row"><span><i class="legend-dot" style="background:var(--dash-teal)"></i>公开文章</span><strong>{{ visibilityPercent.public }}%</strong></div>
                <div class="legend-row"><span><i class="legend-dot" style="background:var(--dash-plum)"></i>私密文章</span><strong>{{ visibilityPercent.private }}%</strong></div>
                <div class="legend-row"><span><i class="legend-dot" style="background:var(--dash-amber)"></i>内容维护压力</span><strong>{{ maintenanceLevel }}</strong></div>
              </div>
            </div>
          </div>

          <div class="maintenance-summary">
            <p>当前需关注：{{ maintenanceMessage }}</p>
            <div class="summary-grid">
              <div v-for="item in maintenanceItems" :key="item.label" class="summary-cell">
                <span class="mini-label">{{ item.label }}</span>
                <strong>{{ item.value }}</strong>
              </div>
            </div>
          </div>
        </section>

        <section class="dash-panel reading-activity-panel">
          <div class="panel-head">
            <div>
              <span class="panel-kicker">阅读活跃中枢</span>
              <h3>近 30 天阅读记录分布</h3>
            </div>
            <span class="micro-pill">阅读历史</span>
          </div>

          <div class="activity-grid">
            <div class="chart-box">
              <div v-if="sampleRows.length" class="chart-bars">
                <span
                  v-for="bar in sampleRows"
                  :key="bar.date"
                  class="bar-cell"
                  :title="`${bar.date}：${bar.count} 次`"
                >
                  <span class="bar" :style="{ height: `${bar.height}%` }">
                    <span class="bar-tip" aria-hidden="true">{{ bar.date }} · {{ bar.count }} 次</span>
                  </span>
                </span>
              </div>
              <div v-else class="chart-empty">暂无阅读记录</div>
              <div class="axis-labels">
                <span v-for="(label, index) in dailyAxis" :key="index">{{ label }}</span>
              </div>
            </div>

            <div class="activity-side">
              <div class="small-stat">
                <span class="mini-label">近 7 天</span>
                <strong>{{ stats.readingSummary.recent7Days }}</strong>
                <small>阅读记录</small>
              </div>
              <div class="small-stat">
                <span class="mini-label">活跃读者</span>
                <strong>{{ stats.readingSummary.activeReaders30Days }}</strong>
                <small>登录用户</small>
              </div>
              <div class="small-stat">
                <span class="mini-label">平均进度</span>
                <strong>{{ stats.readingSummary.averageProgress }}%</strong>
                <small>最近阅读</small>
              </div>
            </div>
          </div>

          <div class="progress-lanes">
            <div v-for="bucket in stats.readingSummary.progressBuckets" :key="bucket.label" class="lane">
              <span>{{ bucket.label }}</span>
              <div class="lane-track"><span :style="{ width: `${bucket.percent}%` }"></span></div>
              <strong>{{ bucket.percent }}%</strong>
            </div>
          </div>
        </section>

        <section class="dash-panel heat-panel">
          <div class="panel-head">
            <div>
              <span class="panel-kicker">内容热度</span>
              <h3>浏览最高文章</h3>
            </div>
            <span class="micro-pill">Top 5</span>
          </div>

          <div v-if="stats.trafficSummary.topArticles.length" class="rank-list">
            <div v-for="(item, index) in stats.trafficSummary.topArticles" :key="item.id || index" class="rank-item">
              <span class="rank-no">{{ pad(index + 1) }}</span>
              <div>
                <strong>{{ item.title }}</strong>
                <small>浏览 {{ formatCompact(item.viewCount) }}</small>
              </div>
              <span class="rank-value">{{ formatCompact(item.viewCount) }}</span>
            </div>
          </div>
          <div v-else class="empty-state">暂无浏览数据</div>

          <div class="heat-summary">
            <div class="heat-cell"><span class="mini-label">总浏览</span><strong>{{ formatCompact(stats.trafficSummary.totalViews) }}</strong></div>
            <div class="heat-cell"><span class="mini-label">平均</span><strong>{{ formatCompact(stats.trafficSummary.averageViews) }}</strong></div>
            <div class="heat-cell"><span class="mini-label">低浏览</span><strong>{{ stats.trafficSummary.lowViewArticles }} 篇</strong></div>
          </div>
        </section>
      </div>

      <div class="lower-grid">
        <section class="dash-panel favorite-feedback-panel">
          <div class="panel-head">
            <div>
              <span class="panel-kicker">收藏反馈</span>
              <h3>读者留存信号</h3>
            </div>
            <span class="micro-pill">收藏</span>
          </div>

          <div class="feedback-chart">
            <div class="feedback-ring" :style="favoriteRingStyle"></div>
            <div>
              <div class="small-stat">
                <span class="mini-label">总收藏</span>
                <strong>{{ stats.favoriteSummary.total }}</strong>
                <small>近 7 天新增 {{ stats.favoriteSummary.recent7Days }}</small>
              </div>
              <div v-if="hasFavoriteTrend" class="sparkline">
                <span
                  v-for="(item, index) in favoriteSpark"
                  :key="item.date || index"
                  :style="{ height: `${item.height}%` }"
                  :title="`${item.date}：${item.count} 次`"
                ></span>
              </div>
              <div v-else class="empty-state">暂无收藏趋势</div>
            </div>
          </div>
        </section>

        <section class="dash-panel resource-health-panel">
          <div class="panel-head">
            <div>
              <span class="panel-kicker">资源健康</span>
              <h3>图片与标签资产</h3>
            </div>
            <span class="micro-pill">资源管理</span>
          </div>

          <div class="resource-grid">
            <div class="resource-cell"><span class="mini-label">图片数</span><strong>{{ stats.resourceSummary.totalImages }}</strong></div>
            <div class="resource-cell"><span class="mini-label">存储占用</span><strong>{{ formatBytes(stats.resourceSummary.totalImageSize) }}</strong></div>
            <div class="resource-cell"><span class="mini-label">7 天上传</span><strong>{{ stats.resourceSummary.recent7DaysImages }}</strong></div>
          </div>
          <div v-if="stats.tagSummary.items.length" class="tag-strip">
            <span v-for="tag in stats.tagSummary.items" :key="tag.id || tag.name">{{ tag.name }}</span>
          </div>
          <div v-else class="empty-state">暂无标签</div>
        </section>

        <section class="dash-panel priority-queue-panel">
          <div class="panel-head">
            <div>
              <span class="panel-kicker">工作队列</span>
              <h3>优先处理</h3>
            </div>
            <span class="micro-pill">{{ pendingTotal }} 项</span>
          </div>

          <div v-if="hasWork" class="queue-list">
            <button
              v-for="article in stats.pendingArticles"
              :key="article.id"
              class="queue-item"
              type="button"
              @click="goArticle(article)"
            >
              <strong>{{ article.title }}</strong>
              <span>文章审核</span>
            </button>
            <button
              v-for="user in stats.pendingUsers"
              :key="user.id"
              class="queue-item"
              type="button"
              @click="goUsers"
            >
              <strong>{{ user.nickname || user.username }}</strong>
              <span>用户审核</span>
            </button>
          </div>
          <div v-else class="empty-state">暂无待处理项</div>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getAdminDashboardOverview } from '../../api/dashboard'
import { buildDashboardStats } from '../../utils/dashboardStats'
import { usePageCopyStore } from '../../stores/pageCopy'

const router = useRouter()
const pageCopyStore = usePageCopyStore()
const loading = ref(true)
const loadError = ref('')
const stats = ref(buildDashboardStats())

const pageCopy = computed(() => pageCopyStore.resolveCopy('admin.dashboard'))
const pendingTotal = computed(
  () => stats.value.metrics.pendingArticles + stats.value.metrics.pendingUsers
)
const hasWork = computed(
  () => stats.value.pendingArticles.length > 0 || stats.value.pendingUsers.length > 0
)

const rejectedArticles = computed(() => stats.value.metrics.rejectedArticles)
const maintenanceLevel = computed(() => {
  const pending = stats.value.metrics.pendingArticles
  const rejected = rejectedArticles.value
  if (pending > 20 || rejected > 10) return '高'
  if (pending > 5 || rejected > 5) return '中'
  return '低'
})
const maintenanceMessage = computed(() => {
  const pending = stats.value.metrics.pendingArticles
  const rejected = rejectedArticles.value
  if (!pending && !rejected) return '待审核和驳回均已清空'
  return pending >= rejected
    ? `待审核 ${pending} 篇，建议优先处理审核队列`
    : `驳回 ${rejected} 篇，需要作者回看`
})
const maintenanceItems = computed(() => [
  { label: '待审核', value: `${stats.value.metrics.pendingArticles} 篇` },
  { label: '驳回', value: `${rejectedArticles.value} 篇` },
  { label: '私密', value: `${stats.value.metrics.privateArticles} 篇` }
])

const visibilityPercent = computed(() => {
  const publicCount = stats.value.metrics.publicPublishedArticles
  const privateCount = stats.value.metrics.privateArticles
  const total = publicCount + privateCount
  return {
    public: percent(publicCount, total),
    private: percent(privateCount, total)
  }
})

const articleRingStyle = computed(() => {
  let cursor = 0
  const segments = stats.value.articleStatus.map(item => {
    const start = cursor
    cursor += Math.max(item.percent, item.count ? 1 : 0)
    return `${statusColor(item.key)} ${start}% ${cursor}%`
  })
  if (!cursor) {
    return {
      background:
        'radial-gradient(circle at center, var(--panel-bg) 0 53%, transparent 54%), conic-gradient(var(--dash-steel) 0 100%)'
    }
  }
  return {
    background:
      `radial-gradient(circle at center, var(--panel-bg) 0 53%, transparent 54%), conic-gradient(${segments.join(', ')})`
  }
})

const visibilityRingStyle = computed(() => {
  const publicCount = stats.value.metrics.publicPublishedArticles
  const privateCount = stats.value.metrics.privateArticles
  const total = publicCount + privateCount
  if (!total) {
    return {
      background:
        'radial-gradient(circle at center, var(--panel-bg) 0 53%, transparent 54%), conic-gradient(var(--dash-teal) 0 100%)'
    }
  }
  const publicPercent = percent(publicCount, total)
  return {
    background:
      `radial-gradient(circle at center, var(--panel-bg) 0 53%, transparent 54%), conic-gradient(var(--dash-teal) 0 ${publicPercent}%, var(--dash-plum) ${publicPercent}% 100%)`
  }
})

const favoriteRingStyle = computed(() => {
  const total = stats.value.favoriteSummary.total
  const recent = stats.value.favoriteSummary.recent7Days
  const recentPercent = percent(recent, total)
  return {
    background:
      `radial-gradient(circle at center, var(--panel-bg) 0 54%, transparent 55%), conic-gradient(var(--dash-plum) 0 ${recentPercent}%, var(--dash-teal) ${recentPercent}% 100%)`
  }
})

const sampleRows = computed(() => {
  const rows = stats.value.readingSummary.dailyReads.slice(-14)
  const max = Math.max(1, ...rows.map(item => item.count))
  return rows.map(item => ({
    date: item.date,
    count: item.count,
    height: item.count ? Math.max(6, Math.round((item.count / max) * 100)) : 0
  }))
})

const dailyAxis = computed(() => {
  const rows = sampleRows.value
  if (!rows.length) return ['01', '04', '07', '10', '14']
  return [0, 3, 6, 10, 13]
    .map(index => rows[index]?.date?.slice(5) || '')
    .filter(Boolean)
})

const favoriteSpark = computed(() => {
  const rows = stats.value.favoriteSummary.dailyFavorites
  const max = Math.max(1, ...rows.map(item => item.count))
  return rows.map(item => ({
    date: item.date,
    count: item.count,
    height: item.count ? Math.max(6, Math.round((item.count / max) * 100)) : 0
  }))
})

const hasFavoriteTrend = computed(() => favoriteSpark.value.some(item => item.count > 0))

function percent(count, total) {
  if (!total) return 0
  return Math.round((count / total) * 100)
}

function statusColor(key) {
  const colors = {
    draft: 'var(--dash-leaf)',
    pending: 'var(--dash-amber)',
    published: 'var(--dash-steel)',
    rejected: 'var(--dash-rose)'
  }
  return colors[key] || 'var(--dash-steel)'
}

function formatNumber(value) {
  const parsed = Number(value)
  return Number.isFinite(parsed) ? parsed.toLocaleString('zh-CN') : '0'
}

function formatCompact(value) {
  const parsed = Number(value)
  if (!Number.isFinite(parsed) || parsed <= 0) return '0'
  if (parsed >= 10000) return `${(parsed / 10000).toFixed(1)}w`
  if (parsed >= 1000) return `${(parsed / 1000).toFixed(1)}k`
  return String(parsed)
}

function formatBytes(value) {
  const parsed = Number(value)
  if (!Number.isFinite(parsed) || parsed <= 0) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  let size = parsed
  let unit = 0
  while (size >= 1024 && unit < units.length - 1) {
    size /= 1024
    unit += 1
  }
  const digits = size >= 100 ? 0 : size >= 10 ? 1 : 2
  return `${size.toFixed(digits)} ${units[unit]}`
}

function pad(value) {
  return String(value).padStart(2, '0')
}

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
  const overview = await safeRequest(() => getAdminDashboardOverview())
  stats.value = buildDashboardStats(overview?.data || {})
  loading.value = false
}

function goArticle(article) {
  router.push(`/admin/articles/edit/${article.id}`)
}

function goUsers() {
  router.push('/admin/users')
}

onMounted(() => {
  void pageCopyStore.loadAdminCopies()
  loadDashboard()
})
</script>

<style scoped>
.dashboard-page {
  --dash-steel: var(--accent-color);
  --dash-teal: color-mix(in srgb, var(--primary-color) 58%, var(--accent-color));
  --dash-leaf: var(--primary-color);
  --dash-amber: var(--warning-color);
  --dash-rose: var(--danger-color);
  --dash-plum: color-mix(in srgb, var(--accent-color) 62%, var(--primary-color));
  --dash-border: color-mix(in srgb, var(--text-color) 10%, transparent);
  --dash-border-soft: color-mix(in srgb, var(--text-color) 8%, transparent);
  --dash-fill: color-mix(in srgb, var(--panel-bg) 64%, transparent);
  --dash-fill-soft: color-mix(in srgb, var(--panel-bg) 86%, transparent);
  display: grid;
  gap: 16px;
}

.dashboard-alert {
  border-radius: var(--radius-md);
}

.tech-board {
  display: grid;
  gap: 14px;
  min-width: 0;
  padding: 18px;
  container-type: inline-size;
  border: 1px solid var(--border-color);
  border-radius: 14px;
  background:
    linear-gradient(135deg, color-mix(in srgb, var(--dash-teal) 8%, transparent), transparent 28%),
    linear-gradient(180deg, color-mix(in srgb, var(--panel-bg) 96%, var(--bg-color)), color-mix(in srgb, var(--panel-bg) 80%, var(--bg-color)));
}

.signal-row {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 10px;
}

.signal-card,
.dash-panel {
  position: relative;
  overflow: hidden;
  border: 1px solid var(--border-color);
  border-radius: 10px;
  background:
    linear-gradient(180deg, color-mix(in srgb, var(--panel-bg) 94%, transparent), color-mix(in srgb, var(--panel-bg) 86%, transparent)),
    var(--panel-bg);
  box-shadow: var(--shadow-md);
}

.signal-card::before,
.signal-card::after,
.dash-panel::before,
.dash-panel::after {
  content: "";
  position: absolute;
  width: 18px;
  height: 18px;
  pointer-events: none;
}

.signal-card::before,
.dash-panel::before {
  left: -1px;
  top: -1px;
  border-left: 2px solid color-mix(in srgb, var(--dash-teal) 62%, transparent);
  border-top: 2px solid color-mix(in srgb, var(--dash-teal) 62%, transparent);
  border-radius: 10px 0 0 0;
}

.signal-card::after,
.dash-panel::after {
  right: -1px;
  bottom: -1px;
  border-right: 2px solid color-mix(in srgb, var(--dash-steel) 50%, transparent);
  border-bottom: 2px solid color-mix(in srgb, var(--dash-steel) 50%, transparent);
  border-radius: 0 0 10px 0;
}

.signal-card {
  min-height: 112px;
  padding: 14px;
}

.signal-topline {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.signal-icon {
  display: grid;
  place-items: center;
  width: 38px;
  aspect-ratio: 1;
  border-radius: 999px;
  color: var(--panel-bg);
  background:
    radial-gradient(circle at 34% 28%, color-mix(in srgb, var(--panel-bg) 42%, transparent), transparent 32%),
    linear-gradient(135deg, var(--dash-steel), var(--dash-teal));
  font-size: 13px;
  font-weight: 900;
}

.signal-card:nth-child(2) .signal-icon {
  background:
    radial-gradient(circle at 34% 28%, color-mix(in srgb, var(--panel-bg) 42%, transparent), transparent 32%),
    linear-gradient(135deg, var(--dash-amber), color-mix(in srgb, var(--dash-amber) 76%, var(--panel-bg)));
}

.signal-card:nth-child(3) .signal-icon {
  background:
    radial-gradient(circle at 34% 28%, color-mix(in srgb, var(--panel-bg) 42%, transparent), transparent 32%),
    linear-gradient(135deg, var(--dash-teal), color-mix(in srgb, var(--dash-teal) 76%, var(--panel-bg)));
}

.signal-card:nth-child(4) .signal-icon {
  background:
    radial-gradient(circle at 34% 28%, color-mix(in srgb, var(--panel-bg) 42%, transparent), transparent 32%),
    linear-gradient(135deg, var(--dash-plum), color-mix(in srgb, var(--dash-plum) 76%, var(--panel-bg)));
}

.signal-card:nth-child(5) .signal-icon {
  background:
    radial-gradient(circle at 34% 28%, color-mix(in srgb, var(--panel-bg) 42%, transparent), transparent 32%),
    linear-gradient(135deg, var(--dash-leaf), color-mix(in srgb, var(--dash-leaf) 76%, var(--panel-bg)));
}

.signal-code {
  color: color-mix(in srgb, var(--text-color) 22%, transparent);
  font-family: Consolas, "SFMono-Regular", monospace;
  font-size: 12px;
  font-weight: 800;
}

.signal-card small,
.panel-kicker,
.mini-label {
  display: block;
  color: var(--muted-text-color);
  font-size: 12px;
  font-weight: 760;
}

.signal-card strong {
  display: block;
  margin-top: 12px;
  color: var(--text-color);
  font-family: Georgia, "Times New Roman", serif;
  font-size: 28px;
  font-weight: 520;
  line-height: 1;
}

.signal-card span:last-child {
  display: block;
  margin-top: 8px;
  color: var(--muted-text-color);
  font-size: 12px;
}

.dashboard-core {
  display: grid;
  grid-template-columns: minmax(0, 1.02fr) minmax(0, 1.5fr) minmax(0, 0.98fr);
  gap: 12px;
}

.dash-panel {
  padding: 15px;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 13px;
}

.panel-head h3 {
  margin: 3px 0 0;
  color: var(--text-color);
  font-size: 16px;
  line-height: 1.25;
}

.micro-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 56px;
  height: 28px;
  padding: 0 10px;
  border: 1px solid color-mix(in srgb, var(--accent-color) 20%, transparent);
  border-radius: 999px;
  color: var(--dash-steel);
  background: color-mix(in srgb, var(--panel-bg) 70%, transparent);
  font-size: 12px;
  font-weight: 760;
  white-space: nowrap;
}

.ring-stack {
  display: grid;
  gap: 10px;
}

.ring-block {
  display: grid;
  grid-template-columns: 106px minmax(0, 1fr);
  align-items: center;
  gap: 13px;
  min-height: 134px;
  padding: 12px;
  border: 1px solid var(--dash-border);
  border-radius: 9px;
  background: color-mix(in srgb, var(--panel-bg) 62%, transparent);
}

.ring {
  position: relative;
  width: 102px;
  aspect-ratio: 1;
  border-radius: 50%;
  box-shadow:
    inset 0 0 0 1px var(--dash-border),
    0 12px 24px color-mix(in srgb, var(--text-color) 10%, transparent);
}

.ring::after {
  content: attr(data-center);
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
  color: var(--text-color);
  font-family: Consolas, "SFMono-Regular", monospace;
  font-size: 13px;
  font-weight: 850;
}

.legend-list {
  display: grid;
  gap: 7px;
}

.legend-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  color: var(--muted-text-color);
  font-size: 12px;
}

.legend-row span:first-child {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.legend-dot {
  width: 8px;
  height: 8px;
  border-radius: 999px;
}

.maintenance-summary {
  display: grid;
  gap: 9px;
  margin-top: 10px;
  padding: 11px;
  border: 1px solid var(--dash-border);
  border-radius: 8px;
  background:
    linear-gradient(135deg, color-mix(in srgb, var(--dash-amber) 9%, transparent), transparent 72%),
    color-mix(in srgb, var(--panel-bg) 62%, transparent);
}

.maintenance-summary p {
  margin: 0;
  color: var(--text-color);
  font-size: 13px;
  font-weight: 760;
  line-height: 1.45;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 7px;
}

.summary-cell {
  min-height: 58px;
  padding: 8px;
  border: 1px solid var(--dash-border-soft);
  border-radius: 7px;
  background: color-mix(in srgb, var(--panel-bg) 68%, transparent);
}

.summary-cell strong {
  display: block;
  margin-top: 5px;
  color: var(--text-color);
  font-family: Consolas, "SFMono-Regular", monospace;
  font-size: 16px;
  line-height: 1;
}

.reading-activity-panel {
  min-height: 382px;
}

.activity-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 132px;
  gap: 12px;
}

.chart-box {
  position: relative;
  min-height: 258px;
  overflow: visible;
  border: 1px solid var(--dash-border);
  border-radius: 10px;
  background:
    linear-gradient(180deg, transparent 24%, color-mix(in srgb, var(--text-color) 6%, transparent) 24% 25%, transparent 25% 49%, color-mix(in srgb, var(--text-color) 6%, transparent) 49% 50%, transparent 50% 74%, color-mix(in srgb, var(--text-color) 6%, transparent) 74% 75%, transparent 75%),
    linear-gradient(90deg, color-mix(in srgb, var(--panel-bg) 52%, transparent), color-mix(in srgb, var(--dash-teal) 7%, transparent)),
    color-mix(in srgb, var(--panel-bg) 62%, transparent);
}

.chart-bars {
  position: absolute;
  inset: 42px 18px 34px;
  display: grid;
  grid-template-columns: repeat(14, minmax(0, 1fr));
  align-items: end;
  gap: 8px;
}

.chart-bars > .bar-cell {
  position: relative;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  min-width: 0;
  height: 100%;
}

.chart-bars .bar {
  position: relative;
  width: 100%;
  min-height: 16px;
  border-radius: 999px 999px 4px 4px;
  background: linear-gradient(180deg, var(--dash-teal), var(--dash-steel));
  box-shadow: 0 8px 18px color-mix(in srgb, var(--dash-teal) 18%, transparent);
}

.chart-bars > .bar-cell:nth-child(4n) .bar {
  background: linear-gradient(180deg, var(--dash-amber), var(--dash-steel));
}

.chart-bars > .bar-cell:nth-child(5n) .bar {
  background: linear-gradient(180deg, var(--dash-leaf), var(--dash-teal));
}

.bar-tip {
  position: absolute;
  bottom: calc(100% + 8px);
  left: 50%;
  z-index: 5;
  padding: 5px 8px;
  border: 1px solid color-mix(in srgb, var(--dash-teal) 24%, transparent);
  border-radius: 7px;
  color: color-mix(in srgb, var(--dash-teal) 86%, var(--text-color));
  background: color-mix(in srgb, var(--panel-bg) 96%, transparent);
  box-shadow: 0 8px 18px color-mix(in srgb, var(--text-color) 14%, transparent);
  font-size: 12px;
  font-weight: 780;
  line-height: 1;
  white-space: nowrap;
  pointer-events: none;
  opacity: 0;
  visibility: hidden;
  transform: translateX(-50%);
  transition: opacity 0.15s ease, visibility 0.15s ease, transform 0.15s ease;
}

.bar:hover .bar-tip,
.bar:focus-visible .bar-tip {
  opacity: 1;
  visibility: visible;
  transform: translate(-50%, -2px);
}

.chart-empty {
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
  color: var(--muted-text-color);
  font-size: 13px;
}

.axis-labels {
  position: absolute;
  left: 18px;
  right: 18px;
  bottom: 12px;
  display: flex;
  justify-content: space-between;
  color: color-mix(in srgb, var(--muted-text-color) 80%, transparent);
  font-family: Consolas, "SFMono-Regular", monospace;
  font-size: 11px;
}

.activity-side {
  display: grid;
  gap: 9px;
}

.small-stat {
  min-height: 78px;
  padding: 11px;
  border: 1px solid var(--dash-border);
  border-radius: 9px;
  background: color-mix(in srgb, var(--panel-bg) 66%, transparent);
}

.small-stat strong {
  display: block;
  margin-top: 6px;
  color: var(--text-color);
  font-size: 21px;
  line-height: 1;
}

.small-stat small {
  color: var(--muted-text-color);
  font-size: 12px;
}

.progress-lanes {
  display: grid;
  gap: 8px;
  margin-top: 12px;
}

.lane {
  display: grid;
  grid-template-columns: 72px minmax(0, 1fr) 36px;
  align-items: center;
  gap: 9px;
  color: var(--muted-text-color);
  font-size: 12px;
}

.lane-track {
  height: 8px;
  overflow: hidden;
  border-radius: 999px;
  background: color-mix(in srgb, var(--text-color) 9%, transparent);
}

.lane-track span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, var(--dash-steel), var(--dash-teal));
}

.rank-list {
  display: grid;
  gap: 8px;
}

.rank-item {
  display: grid;
  grid-template-columns: 26px minmax(0, 1fr) auto;
  align-items: center;
  gap: 9px;
  padding: 9px;
  border: 1px solid var(--dash-border);
  border-radius: 8px;
  background: color-mix(in srgb, var(--panel-bg) 64%, transparent);
}

.rank-no {
  display: grid;
  place-items: center;
  width: 24px;
  aspect-ratio: 1;
  border-radius: 7px;
  color: var(--panel-bg);
  background: var(--dash-steel);
  font-family: Consolas, "SFMono-Regular", monospace;
  font-size: 12px;
  font-weight: 900;
}

.rank-item:nth-child(1) .rank-no {
  background: var(--dash-amber);
}

.rank-item:nth-child(2) .rank-no {
  background: var(--dash-teal);
}

.rank-item strong {
  display: block;
  overflow: hidden;
  color: var(--text-color);
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.rank-item small {
  display: block;
  margin-top: 3px;
  color: var(--muted-text-color);
  font-size: 11px;
}

.rank-value {
  color: var(--dash-steel);
  font-family: Consolas, "SFMono-Regular", monospace;
  font-size: 12px;
  font-weight: 850;
}

.heat-summary {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 7px;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid var(--dash-border-soft);
}

.heat-cell {
  min-height: 64px;
  padding: 9px 8px;
  border: 1px solid var(--dash-border);
  border-radius: 8px;
  background:
    linear-gradient(135deg, color-mix(in srgb, var(--dash-teal) 8%, transparent), transparent 72%),
    color-mix(in srgb, var(--panel-bg) 66%, transparent);
}

.heat-cell strong {
  display: block;
  margin-top: 6px;
  color: var(--text-color);
  font-family: Consolas, "SFMono-Regular", monospace;
  font-size: 15px;
  line-height: 1;
}

.lower-grid {
  display: grid;
  grid-template-columns: minmax(0, 0.98fr) minmax(0, 1fr) minmax(0, 0.9fr);
  gap: 12px;
}

.feedback-chart {
  display: grid;
  grid-template-columns: 116px minmax(0, 1fr);
  align-items: center;
  gap: 12px;
}

.feedback-ring {
  position: relative;
  width: 112px;
  aspect-ratio: 1;
  border-radius: 50%;
  box-shadow:
    inset 0 0 0 1px var(--dash-border),
    0 12px 24px color-mix(in srgb, var(--text-color) 10%, transparent);
}

.feedback-ring::after {
  content: "收藏";
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
  color: var(--text-color);
  font-weight: 850;
}

.sparkline {
  display: flex;
  align-items: end;
  gap: 6px;
  height: 48px;
  margin-top: 10px;
  padding: 0 2px;
}

.sparkline span {
  flex: 1;
  min-width: 5px;
  border-radius: 999px 999px 3px 3px;
  background: linear-gradient(180deg, var(--dash-plum), var(--dash-teal));
}

.resource-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.resource-cell {
  min-height: 82px;
  padding: 10px;
  border: 1px solid var(--dash-border);
  border-radius: 8px;
  background:
    linear-gradient(135deg, color-mix(in srgb, var(--dash-leaf) 10%, transparent), transparent 70%),
    color-mix(in srgb, var(--panel-bg) 64%, transparent);
}

.resource-cell strong {
  display: block;
  margin-top: 8px;
  color: var(--text-color);
  font-size: 21px;
}

.tag-strip {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
  margin-top: 12px;
}

.tag-strip span {
  padding: 6px 9px;
  border: 1px solid color-mix(in srgb, var(--accent-color) 14%, transparent);
  border-radius: 999px;
  color: var(--text-color);
  background: color-mix(in srgb, var(--panel-bg) 68%, transparent);
  font-size: 12px;
  font-weight: 700;
}

.queue-list {
  display: grid;
  gap: 8px;
}

.queue-item {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 9px 10px;
  border: 1px solid var(--dash-border);
  border-radius: 8px;
  color: var(--text-color);
  background: color-mix(in srgb, var(--panel-bg) 66%, transparent);
  text-align: left;
}

.queue-item:hover {
  border-color: color-mix(in srgb, var(--dash-steel) 30%, transparent);
}

.queue-item strong {
  overflow: hidden;
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.queue-item span {
  color: var(--muted-text-color);
  font-size: 12px;
  white-space: nowrap;
}

.empty-state {
  padding: 16px 0;
  color: var(--muted-text-color);
  font-size: 13px;
  text-align: center;
}

@container (max-width: 840px) {
  .signal-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .dashboard-core,
  .lower-grid {
    grid-template-columns: 1fr;
  }
}

@container (max-width: 720px) {
  .tech-board {
    padding: 12px;
  }

  .signal-row,
  .activity-grid,
  .feedback-chart,
  .ring-block,
  .resource-grid {
    grid-template-columns: 1fr;
    align-items: start;
  }

  .chart-bars {
    gap: 2px;
  }
}
</style>
