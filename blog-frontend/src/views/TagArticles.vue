<template>
  <div class="layout">
    <AppHeader />
    <main class="tag-shell">
      <header class="page-head">
        <div class="head-copy">
          <span class="eyebrow">{{ pageCopy.eyebrow }}</span>
          <h1>{{ pageCopy.title }}</h1>
          <p v-if="pageCopy.description">{{ pageCopy.description }}</p>
        </div>
        <aside class="head-meta" aria-label="标签文章摘要">
          <div class="meta-line">
            <span class="meta-label">文章</span>
            <span class="meta-value">{{ articleCountText }}</span>
          </div>
          <div class="meta-line">
            <span class="meta-label">来源</span>
            <span class="meta-value">公开文章</span>
          </div>
          <div class="meta-line">
            <span class="meta-label">排序</span>
            <span class="meta-value">最近发布</span>
          </div>
        </aside>
      </header>

      <el-skeleton v-if="loading" :rows="8" animated />
      <template v-else>
        <section v-if="articles.length" class="article-stream" aria-label="标签文章列表">
          <ArticleCard v-for="article in articles" :key="article.id" :article="article" />
        </section>
        <el-empty v-else description="该标签下暂无文章" />

        <div v-if="showPagination" class="pagination">
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
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getArticles } from '../api/article'
import { getTags } from '../api/tag'
import AppHeader from '../components/AppHeader.vue'
import ArticleCard from '../components/ArticleCard.vue'
import { normalizePageResult, shouldShowPagination } from '../utils/pagination'
import { usePageCopyStore } from '../stores/pageCopy'

const route = useRoute()
const pageCopyStore = usePageCopyStore()
const articles = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const loading = ref(false)
const resolvedTagId = ref(null)
let requestVersion = 0

const tagName = computed(() => String(route.params.name || ''))
const pageCopy = computed(() => pageCopyStore.resolveCopy('tag.index', { tagName: tagName.value }))
const articleCountText = computed(() => total.value ? `${total.value} 篇` : '暂无')
const showPagination = computed(() => shouldShowPagination(total.value, size.value))

void pageCopyStore.loadPublicCopies()

watch(
  () => tagName.value,
  () => {
    page.value = 1
    void resolveTagAndLoad()
  },
  { immediate: true }
)

async function resolveTagAndLoad() {
  const requestId = ++requestVersion
  loading.value = true
  try {
    const tagResult = await getTags()
    if (requestId !== requestVersion) return
    const tag = (tagResult.data || []).find(item => item.name === tagName.value)
    resolvedTagId.value = tag?.id || null
    if (!resolvedTagId.value) {
      articles.value = []
      total.value = 0
      return
    }
    await loadArticles(requestId)
  } finally {
    if (requestId === requestVersion) loading.value = false
  }
}

async function loadArticles(activeRequestId = ++requestVersion) {
  if (!resolvedTagId.value) {
    articles.value = []
    total.value = 0
    return
  }

  loading.value = true
  try {
    const result = await getArticles({
      tagId: resolvedTagId.value,
      page: page.value,
      size: size.value
    })
    if (activeRequestId !== requestVersion) return
    const pageResult = normalizePageResult(result, size.value)
    const maxPage = Math.max(1, Math.ceil(pageResult.total / size.value))
    if (page.value > maxPage) {
      page.value = maxPage
      await loadArticles(activeRequestId)
      return
    }
    articles.value = pageResult.records
    total.value = pageResult.total
  } finally {
    if (activeRequestId === requestVersion) loading.value = false
  }
}

function handlePageChange(nextPage) {
  page.value = nextPage
  void loadArticles()
}
</script>
<style scoped>
.tag-shell {
  position: relative;
  isolation: isolate;
  width: min(1180px, calc(100% - 36px));
  margin: 0 auto;
  padding: 22px 0 64px;
}

.tag-shell::before {
  position: fixed;
  inset: 0;
  z-index: -1;
  background:
    linear-gradient(90deg, var(--theme-grid-x) 1px, transparent 1px),
    linear-gradient(180deg, var(--theme-grid-y) 1px, transparent 1px);
  background-size: 44px 44px;
  content: '';
  opacity: 0.54;
  pointer-events: none;
}

.page-head {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(260px, 320px);
  gap: 30px;
  align-items: end;
  margin-bottom: 26px;
  padding-top: 22px;
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
  overflow-wrap: anywhere;
}

.head-copy p {
  max-width: 620px;
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
  min-width: 0;
  overflow: hidden;
  color: var(--text-color);
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.article-stream {
  display: grid;
  gap: 12px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 26px;
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
  .tag-shell {
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
}

@media (max-width: 440px) {
  .meta-line {
    grid-template-columns: 1fr;
  }
}
</style>
