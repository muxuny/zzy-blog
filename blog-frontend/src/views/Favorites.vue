<template>
  <div class="layout">
    <AppHeader />
    <main class="favorites-main" :aria-busy="loading">
      <RouterLink class="back-to-reading" to="/reading" aria-label="返回我的阅读">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回我的阅读</span>
      </RouterLink>

      <header class="page-head">
        <div class="head-copy">
          <span class="eyebrow">{{ pageCopy.eyebrow }}</span>
          <h1>{{ pageCopy.title }}</h1>
          <p v-if="pageCopy.description">{{ pageCopy.description }}</p>
        </div>
        <aside class="head-meta" aria-label="收藏摘要">
          <div class="meta-line">
            <span class="meta-label">收藏</span>
            <span class="meta-value">{{ total }} 篇文章</span>
          </div>
          <div class="meta-line">
            <span class="meta-label">筛选</span>
            <span class="meta-value">{{ hasFilters ? '已启用条件' : '全部收藏' }}</span>
          </div>
        </aside>
      </header>

      <form class="creator-toolbar" @submit.prevent="submitSearch">
        <el-input
          v-model="keywordInput"
          class="keyword-field"
          clearable
          aria-label="关键词"
          placeholder="关键词"
          @clear="submitSearch"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>

        <el-select
          v-model="tagId"
          class="tag-field"
          clearable
          aria-label="标签"
          placeholder="标签"
          @change="handleTagChange"
        >
          <el-option v-for="tag in tags" :key="tag.id" :label="tag.name" :value="tag.id" />
        </el-select>

        <el-button type="primary" native-type="submit" :icon="Search">搜索</el-button>
        <el-button native-type="button" :icon="Refresh" @click="resetFilters">重置</el-button>
      </form>

      <div v-if="loadError" class="error-row">
        <el-alert
          type="error"
          :title="loadError"
          :closable="false"
          show-icon
        />
        <el-button :icon="Refresh" @click="retryLoad">重试</el-button>
      </div>

      <div v-if="loading" class="favorite-skeleton" role="status" aria-live="polite">
        <span class="sr-only">正在加载收藏列表</span>
        <el-skeleton :rows="6" animated />
      </div>

      <template v-else-if="!loadError">
        <div v-if="items.length" class="favorite-index">
          <FavoriteArticleItem
            v-for="item in items"
            :key="item.articleId"
            :item="item"
            :removing="isRemoving(item.articleId)"
            @remove="removeFavorite"
          />
        </div>

        <div v-else class="empty-status" role="status" aria-live="polite">
          <el-empty :description="emptyDescription" />
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
import { ElMessage } from 'element-plus'
import { ArrowLeft, Refresh, Search } from '@element-plus/icons-vue'
import AppHeader from '../components/AppHeader.vue'
import FavoriteArticleItem from '../components/FavoriteArticleItem.vue'
import { getFavorites, unfavoriteArticle } from '../api/favorite'
import { getTags } from '../api/tag'
import { buildFavoriteListParams } from '../utils/favorite'
import { usePageCopyStore } from '../stores/pageCopy'

const pageCopyStore = usePageCopyStore()
const items = ref([])
const tags = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const keywordInput = ref('')
const keyword = ref('')
const tagId = ref('')
const loading = ref(false)
const loadError = ref('')
const removingIds = ref(new Set())
let componentActive = true
let loadRequestVersion = 0
let tagRequestVersion = 0
let removeRequestSequence = 0
const removeRequestVersions = new Map()

const hasFilters = computed(() => {
  const params = buildFavoriteListParams({
    keyword: keyword.value,
    tagId: tagId.value
  })
  return 'keyword' in params || 'tagId' in params
})
const emptyDescription = computed(() => hasFilters.value ? '当前筛选无结果' : '尚未收藏文章')
const pageCopy = computed(() => pageCopyStore.resolveCopy('favorites.index'))

onMounted(() => {
  void pageCopyStore.loadPublicCopies()
  void loadTags()
  void load()
})

onBeforeUnmount(() => {
  componentActive = false
  loadRequestVersion += 1
  tagRequestVersion += 1
  removeRequestVersions.clear()
})

async function load() {
  if (!componentActive) return null
  const requestId = ++loadRequestVersion
  const requestPage = page.value
  loading.value = true
  loadError.value = ''
  const params = buildFavoriteListParams({
    page: requestPage,
    size: size.value,
    keyword: keyword.value,
    tagId: tagId.value
  })

  try {
    const result = await getFavorites(params)
    if (!componentActive || requestId !== loadRequestVersion) return null
    const records = result.data || []
    const nextTotal = Number(result.total) || 0
    const maxPage = Math.max(1, Math.ceil(nextTotal / size.value))
    if (page.value > maxPage) {
      page.value = maxPage
      return await load()
    }
    items.value = records
    total.value = nextTotal
    return { applied: true, page: requestPage, recordCount: records.length }
  } catch (error) {
    if (!componentActive || requestId !== loadRequestVersion) return null
    items.value = []
    total.value = 0
    loadError.value = error.response?.data?.message || error.message || '收藏列表加载失败，请重试'
    return null
  } finally {
    if (componentActive && requestId === loadRequestVersion) loading.value = false
  }
}

async function loadTags() {
  if (!componentActive) return
  const requestId = ++tagRequestVersion
  try {
    const result = await getTags()
    if (!componentActive || requestId !== tagRequestVersion) return
    tags.value = result.data || []
  } catch {
    if (!componentActive || requestId !== tagRequestVersion) return
    tags.value = []
  }
}

function submitSearch() {
  keyword.value = buildFavoriteListParams({ keyword: keywordInput.value }).keyword || ''
  keywordInput.value = keyword.value
  page.value = 1
  void load()
}

function handleTagChange() {
  page.value = 1
  void load()
}

function resetFilters() {
  keywordInput.value = ''
  keyword.value = ''
  tagId.value = ''
  page.value = 1
  void load()
}

function retryLoad() {
  void load()
}

function handlePageChange(nextPage) {
  page.value = nextPage
  void load()
}

function isRemoving(articleId) {
  return removingIds.value.has(articleId)
}

function isRemoveRequestCurrent(articleId, requestId) {
  return componentActive && removeRequestVersions.get(articleId) === requestId
}

async function removeFavorite(item) {
  if (isRemoving(item.articleId) || !componentActive) return
  const requestId = ++removeRequestSequence
  removeRequestVersions.set(item.articleId, requestId)
  removingIds.value = new Set(removingIds.value).add(item.articleId)

  try {
    await unfavoriteArticle(item.articleId)
    if (!isRemoveRequestCurrent(item.articleId, requestId)) return
    ElMessage.success('已取消收藏')
    await load()
    if (!isRemoveRequestCurrent(item.articleId, requestId)) return
  } catch {
    // The shared request interceptor presents the request failure.
  } finally {
    if (isRemoveRequestCurrent(item.articleId, requestId)) {
      removeRequestVersions.delete(item.articleId)
      const nextRemovingIds = new Set(removingIds.value)
      nextRemovingIds.delete(item.articleId)
      removingIds.value = nextRemovingIds
    }
  }
}
</script>

<style scoped>
.favorites-main {
  position: relative;
  isolation: isolate;
  width: min(1180px, calc(100% - 36px));
  margin: 0 auto;
  padding: 22px 0 64px;
}

.favorites-main::before {
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

.back-to-reading {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  min-height: 36px;
  margin-bottom: 14px;
  color: var(--muted-text-color);
  font-size: 13px;
  font-weight: 700;
}

.back-to-reading:hover {
  color: var(--primary-color);
}

.back-to-reading:focus-visible {
  outline: 2px solid var(--primary-color);
  outline-offset: 2px;
}

.page-head {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(260px, 320px);
  gap: 30px;
  align-items: end;
  margin-bottom: 24px;
}

.head-copy {
  min-width: 0;
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
  border-left: 1px solid var(--border-color);
  padding-left: 18px;
}

.meta-line {
  display: grid;
  grid-template-columns: 72px minmax(0, 1fr);
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid var(--soft-border-color);
}

.meta-line:last-child {
  border-bottom: 0;
}

.meta-label {
  color: var(--accent-color);
  font-size: 12px;
  font-weight: 760;
}

.meta-value {
  color: var(--text-color);
  font-size: 13px;
}

.creator-toolbar {
  display: grid;
  grid-template-columns: minmax(240px, 1fr) 210px auto auto;
  gap: 10px;
  align-items: center;
  margin-bottom: 22px;
  padding: 14px 0;
  border-top: 1px solid var(--soft-border-color);
  border-bottom: 1px solid var(--soft-border-color);
}

.creator-toolbar .el-button {
  min-height: 40px;
  margin-left: 0;
}

.creator-toolbar :deep(.el-input__wrapper),
.creator-toolbar :deep(.el-select__wrapper) {
  min-height: 40px;
  border-radius: 999px;
  background: color-mix(in srgb, var(--panel-bg) 94%, transparent);
}

.keyword-field,
.tag-field {
  min-width: 0;
}

.error-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 10px;
  align-items: center;
  margin-bottom: 16px;
}

.error-row .el-button {
  min-height: 40px;
}

.favorite-skeleton {
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

.favorite-index {
  display: grid;
  gap: 12px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 28px;
}

@media (max-width: 900px) {
  .page-head {
    grid-template-columns: 1fr;
  }

  .head-meta {
    border-left: 0;
    border-top: 1px solid var(--border-color);
    padding-top: 12px;
    padding-left: 0;
  }
}

@media (max-width: 720px) {
  .favorites-main {
    width: min(100% - 28px, var(--content-width));
    padding: 16px 0 44px;
  }

  .page-head h1 {
    font-size: 42px;
  }

  .creator-toolbar {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .keyword-field,
  .tag-field {
    grid-column: 1 / -1;
  }

  .error-row {
    grid-template-columns: minmax(0, 1fr);
  }

  .error-row .el-button {
    justify-self: end;
  }
}

@media (max-width: 440px) {
  .meta-line,
  .creator-toolbar {
    grid-template-columns: 1fr;
  }
}
</style>
