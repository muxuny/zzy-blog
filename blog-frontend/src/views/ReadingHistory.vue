<template>
  <div class="layout">
    <AppHeader />
    <main class="history-main" :aria-busy="loading">
      <header class="page-head">
        <div class="head-copy">
          <RouterLink class="back-link" to="/reading">
            <el-icon><ArrowLeft /></el-icon>
            <span>返回阅读空间</span>
          </RouterLink>
          <span class="eyebrow">{{ pageCopy.eyebrow }}</span>
          <h1>{{ pageCopy.title }}</h1>
          <p v-if="pageCopy.description">{{ pageCopy.description }}</p>
        </div>
        <aside class="head-meta" aria-label="阅读历史摘要">
          <div class="meta-line">
            <span class="meta-label">总数</span>
            <span class="meta-value">{{ total }} 条记录</span>
          </div>
          <div class="meta-line">
            <span class="meta-label">分页</span>
            <span class="meta-value">每页 {{ size }} 条</span>
          </div>
          <el-button
            class="clear-button"
            type="danger"
            plain
            :icon="Delete"
            :loading="clearing"
            :disabled="total === 0 || removingIds.size > 0"
            @click="clearAllHistory"
          >
            清空历史
          </el-button>
        </aside>
      </header>

      <div v-if="error" class="error-row">
        <el-alert type="error" :title="error" :closable="false" show-icon />
        <el-button :icon="Refresh" @click="retryLoad">重试</el-button>
      </div>

      <div v-if="loading" class="history-skeleton" role="status" aria-live="polite">
        <span class="sr-only">正在加载阅读历史</span>
        <el-skeleton :rows="8" animated />
      </div>

      <template v-else-if="!error">
        <div v-if="groups.length" class="history-timeline" aria-label="按时间分组的阅读历史">
          <section v-for="group in groups" :key="group.key" class="timeline-group">
            <h2 class="group-label">{{ group.label }}</h2>
            <div class="timeline-items">
              <div
                v-for="item in group.items"
                :key="item.articleId"
                class="timeline-entry"
              >
                <ReadingHistoryItem
                  :item="item"
                  :removing="isRemoving(item.articleId)"
                  :disabled="clearing"
                  @remove="removeHistory"
                />
              </div>
            </div>
          </section>
        </div>

        <div v-else class="empty-status" role="status" aria-live="polite">
          <el-empty description="暂无阅读历史">
            <el-button type="primary" @click="goDiscover">去发现文章</el-button>
          </el-empty>
        </div>

        <div v-if="total > size" class="pagination">
          <el-pagination
            v-model:current-page="page"
            :total="total"
            :page-size="size"
            layout="prev,pager,next"
            @current-change="handlePageChange"
          />
        </div>
      </template>
    </main>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Delete, Refresh } from '@element-plus/icons-vue'
import AppHeader from '../components/AppHeader.vue'
import ReadingHistoryItem from '../components/ReadingHistoryItem.vue'
import {
  clearReadingHistory,
  deleteReadingHistory,
  getReadingHistory
} from '../api/reading'
import {
  buildReadingHistoryParams,
  getPageAfterHistoryDeletion,
  groupReadingHistory
} from '../utils/readingHistory'
import { usePageCopyStore } from '../stores/pageCopy'

const pageCopyStore = usePageCopyStore()
const page = ref(1)
const size = ref(10)
const total = ref(0)
const items = ref([])
const groupingNow = ref(new Date())
const loading = ref(false)
const error = ref('')
const clearing = ref(false)
const removingIds = ref(new Set())
const router = useRouter()
let componentActive = true
let requestVersion = 0
let dayRefreshTimer = null

const groups = computed(() => groupReadingHistory(items.value, groupingNow.value))
const pageCopy = computed(() => pageCopyStore.resolveCopy('reading.history'))

onMounted(() => {
  void pageCopyStore.loadPublicCopies()
  void load()
  scheduleNextDayRefresh()
})

onBeforeUnmount(() => {
  componentActive = false
  requestVersion += 1
  if (dayRefreshTimer !== null) {
    clearTimeout(dayRefreshTimer)
    dayRefreshTimer = null
  }
})

function scheduleNextDayRefresh() {
  if (!componentActive) return
  if (dayRefreshTimer !== null) clearTimeout(dayRefreshTimer)

  const now = new Date()
  const nextDay = new Date(now)
  nextDay.setHours(24, 0, 0, 0)
  const delay = Math.max(1, nextDay.getTime() - now.getTime())

  dayRefreshTimer = setTimeout(() => {
    dayRefreshTimer = null
    if (!componentActive) return
    groupingNow.value = new Date()
    scheduleNextDayRefresh()
  }, delay)
}

async function load() {
  if (!componentActive) return null
  const requestId = ++requestVersion
  const requestPage = page.value
  loading.value = true
  error.value = ''
  const params = buildReadingHistoryParams({
    page: requestPage,
    size: size.value
  })

  try {
    const result = await getReadingHistory(params)
    if (!componentActive || requestId !== requestVersion) return null
    const records = Array.isArray(result.data) ? result.data : []
    const nextTotal = Number(result.total) || 0
    const maxPage = Math.max(1, Math.ceil(nextTotal / size.value))
    if (page.value > maxPage) {
      page.value = maxPage
      return await load()
    }
    items.value = records
    total.value = nextTotal
    return { applied: true, page: requestPage }
  } catch {
    if (!componentActive || requestId !== requestVersion) return null
    error.value = '阅读历史加载失败，请重试'
    return null
  } finally {
    if (componentActive && requestId === requestVersion) loading.value = false
  }
}

function retryLoad() {
  void load()
}

function handlePageChange(nextPage) {
  page.value = nextPage
  void load()
}

function goDiscover() {
  router.push('/')
}

function isRemoving(articleId) {
  return removingIds.value.has(articleId)
}

async function removeHistory(item) {
  if (clearing.value || isRemoving(item.articleId) || !componentActive) return
  removingIds.value = new Set(removingIds.value).add(item.articleId)

  try {
    await deleteReadingHistory(item.articleId)
    if (!componentActive) return
    page.value = getPageAfterHistoryDeletion({
      page: page.value,
      size: size.value,
      total: total.value
    })
    ElMessage.success('已删除阅读历史')
    await load()
  } catch {
    // The shared request interceptor presents the request failure.
  } finally {
    if (componentActive) {
      const nextRemovingIds = new Set(removingIds.value)
      nextRemovingIds.delete(item.articleId)
      removingIds.value = nextRemovingIds
    }
  }
}

async function clearAllHistory() {
  if (clearing.value || removingIds.value.size > 0 || !componentActive) return
  clearing.value = true

  try {
    await ElMessageBox.confirm(
      '清空后无法恢复，确定清空全部阅读历史吗？',
      '清空阅读历史',
      {
        confirmButtonText: '清空',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
  } catch {
    if (componentActive) clearing.value = false
    return
  }

  if (!componentActive) return
  try {
    await clearReadingHistory()
    if (!componentActive) return
    page.value = 1
    ElMessage.success('阅读历史已清空')
    await load()
  } catch {
    // The shared request interceptor presents the request failure.
  } finally {
    if (componentActive) clearing.value = false
  }
}
</script>

<style scoped>
.history-main {
  position: relative;
  isolation: isolate;
  width: min(1180px, calc(100% - 36px));
  margin: 0 auto;
  padding: 22px 0 64px;
}

.history-main::before {
  position: fixed;
  inset: 0;
  z-index: -1;
  background:
    linear-gradient(90deg, var(--theme-grid-x) 1px, transparent 1px),
    linear-gradient(180deg, var(--theme-grid-y) 1px, transparent 1px);
  background-size: 44px 44px;
  content: '';
  opacity: 0.56;
  pointer-events: none;
}

.page-head {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(280px, 340px);
  gap: 30px;
  align-items: end;
  margin-bottom: 28px;
  padding-top: 22px;
}

.head-copy {
  min-width: 0;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  min-height: 36px;
  margin-bottom: 14px;
  color: var(--muted-text-color);
  font-size: 13px;
  font-weight: 700;
}

.back-link:hover {
  color: var(--primary-color);
}

.eyebrow {
  display: inline-flex;
  margin: 0 0 12px;
  color: var(--primary-color);
  font-size: 12px;
  font-weight: 760;
  letter-spacing: 0;
}

.page-head h1 {
  max-width: 820px;
  margin: 0 0 18px;
  color: var(--text-color);
  font-family: Georgia, "Times New Roman", serif;
  font-size: clamp(42px, 7vw, 76px);
  font-weight: 500;
  line-height: 0.98;
}

.head-copy p {
  max-width: 640px;
  margin: 0;
  color: var(--muted-text-color);
  font-size: 17px;
  line-height: 1.85;
}

.head-meta {
  display: grid;
  gap: 13px;
  padding: 18px 20px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background: var(--panel-bg);
  box-shadow: var(--shadow-soft);
}

.meta-line {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 24px;
  align-items: center;
  padding: 0;
}

.meta-label {
  color: var(--muted-text-color);
  font-size: 14px;
  font-weight: 500;
}

.meta-value {
  min-width: 0;
  overflow: hidden;
  color: var(--text-color);
  font-size: 18px;
  font-weight: 800;
  line-height: 1.25;
  text-align: right;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.clear-button {
  width: 100%;
  min-height: 38px;
  margin-top: 6px;
  border-radius: 999px;
}

.back-link:focus-visible,
.clear-button:focus-visible {
  outline: 2px solid var(--primary-color);
  outline-offset: 2px;
}

.error-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 10px;
  align-items: center;
  margin-bottom: 18px;
}

.history-skeleton {
  padding: 18px 2px;
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

.history-timeline {
  display: grid;
  width: min(100%, 980px);
  gap: 30px;
}

.timeline-group {
  display: grid;
  grid-template-columns: 96px minmax(0, 1fr);
  gap: 0 14px;
}

.group-label {
  margin: 3px 0 0;
  color: var(--primary-color);
  font-size: 13px;
  font-weight: 800;
  line-height: 1.4;
  text-align: right;
}

.timeline-items {
  position: relative;
  display: grid;
  gap: 12px;
  min-width: 0;
  padding-left: 28px;
}

.timeline-items::before {
  position: absolute;
  top: 7px;
  bottom: 7px;
  left: 8px;
  width: 1px;
  background: linear-gradient(180deg, var(--primary-color), var(--accent-color));
  content: '';
  opacity: 0.45;
}

.timeline-group:not(:last-child) .timeline-items::before {
  bottom: -37px;
}

.timeline-entry {
  position: relative;
  min-width: 0;
}

.timeline-entry::before {
  position: absolute;
  top: 25px;
  left: -26px;
  z-index: 1;
  width: 13px;
  height: 13px;
  border: 3px solid var(--panel-bg);
  border-radius: 50%;
  background: var(--accent-color);
  box-shadow: 0 0 0 1px var(--border-color), 0 0 0 7px var(--surface-wash-color);
  content: '';
  pointer-events: none;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

@media (max-width: 900px) {
  .page-head {
    grid-template-columns: minmax(0, 1fr);
  }

  .head-meta {
    padding: 16px;
  }
}

@media (max-width: 720px) {
  .history-main {
    width: min(100% - 28px, var(--content-width));
    padding: 16px 0 44px;
  }

  .page-head {
    gap: 22px;
    padding-top: 14px;
  }

  .page-head h1 {
    font-size: 42px;
  }

  .error-row,
  .timeline-group {
    grid-template-columns: minmax(0, 1fr);
  }

  .error-row .el-button {
    justify-self: end;
  }

  .group-label {
    margin: 0 0 9px 28px;
    text-align: left;
  }

  .timeline-items {
    padding-left: 24px;
  }

  .timeline-items::before {
    left: 6px;
  }

  .timeline-entry::before {
    left: -24px;
  }
}

@media (max-width: 440px) {
  .meta-line {
    grid-template-columns: 1fr;
    gap: 4px;
  }

  .meta-value {
    text-align: left;
  }
}

@media (prefers-reduced-motion: reduce) {
  :deep(.el-skeleton.is-animated .el-skeleton__item) {
    animation: none;
  }
}
</style>
