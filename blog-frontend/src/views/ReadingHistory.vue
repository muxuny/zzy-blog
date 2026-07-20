<template>
  <div class="layout">
    <AppHeader />
    <main class="history-main" :aria-busy="loading">
      <RouterLink class="back-link" to="/reading">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回阅读空间</span>
      </RouterLink>

      <header class="page-heading">
        <div class="heading-copy">
          <h1>阅读历史</h1>
          <span class="history-count">共 {{ total }} 条</span>
        </div>
        <el-button
          type="danger"
          plain
          :icon="Delete"
          :loading="clearing"
          :disabled="total === 0 || removingIds.size > 0"
          @click="clearAllHistory"
        >
          清空历史
        </el-button>
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

const page = ref(1)
const size = ref(10)
const total = ref(0)
const items = ref([])
const loading = ref(false)
const error = ref('')
const clearing = ref(false)
const removingIds = ref(new Set())
const router = useRouter()
let componentActive = true
let requestVersion = 0

const groups = computed(() => groupReadingHistory(items.value))

onMounted(() => {
  void load()
})

onBeforeUnmount(() => {
  componentActive = false
  requestVersion += 1
})

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
  width: min(100%, var(--content-width));
  margin: 0 auto;
  padding: 24px 24px 56px;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  min-height: 36px;
  margin-bottom: 12px;
  color: var(--muted-text-color);
  font-size: 13px;
  font-weight: 700;
}

.page-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 22px;
  padding-bottom: 17px;
  border-bottom: 1px solid var(--soft-border-color);
}

.heading-copy {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: 8px 14px;
  min-width: 0;
}

.page-heading h1 {
  margin: 0;
  color: var(--text-color);
  font-size: 30px;
  line-height: 1.25;
}

.history-count {
  color: var(--muted-text-color);
  font-size: 13px;
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
  width: min(100%, 980px);
}

.timeline-group {
  display: grid;
  grid-template-columns: 88px minmax(0, 1fr);
  gap: 0 12px;
}

.timeline-group + .timeline-group {
  margin-top: 30px;
}

.group-label {
  margin: 3px 0 0;
  color: var(--muted-text-color);
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
  background: var(--border-color);
  content: '';
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
  box-shadow: 0 0 0 1px var(--border-color);
  content: '';
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 28px;
}

@media (max-width: 720px) {
  .history-main {
    padding: 18px 14px 40px;
  }

  .page-heading {
    align-items: flex-start;
  }

  .page-heading h1 {
    font-size: 26px;
  }

  .error-row {
    grid-template-columns: minmax(0, 1fr);
  }

  .error-row .el-button {
    justify-self: end;
  }

  .timeline-group {
    grid-template-columns: minmax(0, 1fr);
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
  .page-heading {
    align-items: stretch;
    flex-direction: column;
  }

  .page-heading .el-button {
    align-self: flex-start;
  }
}
</style>
