<template>
  <div class="layout">
    <AppHeader />
    <el-main class="main">
      <div class="page-actions">
        <button type="button" class="back-button" @click="router.push('/creator/articles')">
          <el-icon><ArrowLeft /></el-icon>
          <span>返回我的文章</span>
        </button>
        <button v-if="article" type="button" class="edit-button" @click="goEdit">
          <el-icon><Edit /></el-icon>
          <span>编辑文章</span>
        </button>
      </div>

      <el-skeleton v-if="loading" :rows="10" animated />

      <section v-else-if="errorMessage" class="empty-state">
        <h1>{{ errorMessage }}</h1>
        <p>请返回我的文章列表后重试。</p>
      </section>

      <article v-else-if="article" class="preview-page">
        <header class="preview-head">
          <span class="page-kicker">文章预览</span>
          <h1>{{ article.title }}</h1>
          <div class="preview-meta">
            <el-tag :type="statusType(article.status)">{{ statusText(article.status) }}</el-tag>
            <el-tag :type="articleVisibilityType(article.visibility)" effect="plain">
              {{ articleVisibilityText(article.visibility) }}
            </el-tag>
            <span>{{ formatDate(article.updatedAt || article.createdAt) }}</span>
            <span v-if="article.createdBy">{{ article.createdBy }}</span>
          </div>
          <p v-if="article.summary" class="preview-summary">{{ article.summary }}</p>
          <div v-if="article.tags?.length" class="preview-tags">
            <el-tag v-for="tag in article.tags" :key="tag.id" size="small">{{ tag.name }}</el-tag>
          </div>
        </header>

        <el-alert
          v-if="article.reviewReason"
          class="review-alert"
          type="warning"
          :title="`审核反馈：${article.reviewReason}`"
          show-icon
          :closable="false"
        />

        <figure v-if="article.coverImage" class="preview-cover">
          <img :src="article.coverImage" :alt="article.title" />
        </figure>

        <div class="preview-reading-layout" :class="{ 'has-toc': toc.length }">
          <section class="preview-body">
            <nav v-if="toc.length" class="mobile-toc" aria-label="文章目录">
              <span class="page-kicker">目录</span>
              <button
                v-for="item in toc"
                :key="item.id"
                type="button"
                :class="['toc-link', `level-${item.level}`, { active: activeHeadingId === item.id }]"
                @click="scrollToHeading(item.id)"
              >
                {{ item.text }}
              </button>
            </nav>

            <MarkdownRenderer :content="article.content" />
          </section>

          <aside v-if="toc.length" class="preview-sidebar">
            <nav ref="tocPanelRef" class="toc-panel" aria-label="文章目录">
              <span class="page-kicker">目录</span>
              <button
                v-for="item in toc"
                :key="item.id"
                type="button"
                :data-toc-id="item.id"
                :class="['toc-link', `level-${item.level}`, { active: activeHeadingId === item.id }]"
                @click="scrollToHeading(item.id)"
              >
                {{ item.text }}
              </button>
            </nav>
          </aside>
        </div>
      </article>
    </el-main>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Edit } from '@element-plus/icons-vue'
import AppHeader from '../../components/AppHeader.vue'
import MarkdownRenderer from '../../components/MarkdownRenderer.vue'
import { getMyArticle } from '../../api/myArticle'
import { articleVisibilityText, articleVisibilityType } from '../../utils/articleVisibility'
import { getCreatorEditRoute } from '../../utils/creatorPreview'
import { formatDate } from '../../utils'
import { extractMarkdownToc } from '../../utils/reading'
import { getActiveHeadingId, getScrollTopForVisibleItem } from '../../utils/scrollSpy'

const route = useRoute()
const router = useRouter()
const article = ref(null)
const loading = ref(true)
const errorMessage = ref('')
const activeHeadingId = ref('')
const tocPanelRef = ref(null)

const toc = computed(() => extractMarkdownToc(article.value?.content || ''))

const statusMap = {
  draft: { text: '草稿', type: 'info' },
  pending: { text: '待审核', type: 'warning' },
  published: { text: '已发布', type: 'success' },
  rejected: { text: '已驳回', type: 'danger' }
}

onMounted(() => {
  loadArticle()
  window.addEventListener('scroll', updateScrollState, { passive: true })
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', updateScrollState)
})

watch(() => route.params.id, () => {
  window.scrollTo({ top: 0, behavior: 'auto' })
  loadArticle()
})

async function loadArticle() {
  loading.value = true
  errorMessage.value = ''
  article.value = null
  activeHeadingId.value = ''
  try {
    const result = await getMyArticle(route.params.id)
    article.value = result.data
    await nextTick()
    updateScrollState()
  } catch {
    article.value = null
    errorMessage.value = '文章预览加载失败'
  } finally {
    loading.value = false
  }
}

function goEdit() {
  const editRoute = getCreatorEditRoute(article.value)
  if (editRoute) router.push(editRoute)
}

function scrollToHeading(id) {
  document.getElementById(id)?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

function updateScrollState() {
  if (!toc.value.length) {
    activeHeadingId.value = ''
    return
  }

  const headings = toc.value
    .map(item => {
      const el = document.getElementById(item.id)
      return el ? { id: item.id, top: el.getBoundingClientRect().top + window.scrollY } : null
    })
    .filter(Boolean)
  const nextActiveHeadingId = getActiveHeadingId(headings, window.scrollY, 140)
  if (activeHeadingId.value !== nextActiveHeadingId) {
    activeHeadingId.value = nextActiveHeadingId
  }
  if (nextActiveHeadingId) nextTick(scrollActiveTocItemIntoView)
}

function scrollActiveTocItemIntoView() {
  if (!activeHeadingId.value || !tocPanelRef.value) return

  const activeItem = Array.from(tocPanelRef.value.querySelectorAll('[data-toc-id]'))
    .find(item => item.dataset.tocId === activeHeadingId.value)
  if (!activeItem) return

  const panelRect = tocPanelRef.value.getBoundingClientRect()
  const activeRect = activeItem.getBoundingClientRect()
  const nextScrollTop = getScrollTopForVisibleItem({
    containerScrollTop: tocPanelRef.value.scrollTop,
    containerHeight: tocPanelRef.value.clientHeight,
    itemTop: activeRect.top - panelRect.top + tocPanelRef.value.scrollTop,
    itemHeight: activeRect.height,
    padding: 12
  })

  if (Math.abs(nextScrollTop - tocPanelRef.value.scrollTop) < 1) return

  tocPanelRef.value.scrollTo({
    top: nextScrollTop,
    behavior: 'auto'
  })
}

function statusText(status) {
  return statusMap[status]?.text || status || '-'
}

function statusType(status) {
  return statusMap[status]?.type || 'info'
}
</script>

<style scoped>
.main {
  width: min(100%, var(--content-width));
  margin: 0 auto;
  padding: 32px 24px 72px;
  overflow: visible;
}

.page-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.back-button,
.edit-button {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: 36px;
  padding: 0 12px;
  border: 1px solid var(--soft-border-color);
  border-radius: var(--radius-sm);
  background: var(--panel-bg);
  color: var(--muted-text-color);
  font: inherit;
  font-weight: 700;
  cursor: pointer;
  box-shadow: var(--shadow-sm);
}

.edit-button {
  border-color: color-mix(in srgb, var(--primary-color) 42%, var(--soft-border-color));
  background: var(--primary-color);
  color: #fff;
}

.back-button:hover,
.back-button:focus-visible {
  border-color: var(--primary-color);
  color: var(--primary-color);
  background: color-mix(in srgb, var(--primary-color) 8%, var(--panel-bg));
}

.edit-button:hover,
.edit-button:focus-visible {
  border-color: var(--primary-hover-color);
  background: var(--primary-hover-color);
}

.back-button:focus-visible,
.edit-button:focus-visible,
.toc-link:focus-visible {
  outline: 2px solid var(--primary-color);
  outline-offset: 2px;
}

.empty-state,
.preview-head,
.preview-body,
.toc-panel,
.mobile-toc {
  border: 1px solid var(--soft-border-color);
  border-radius: var(--radius-lg);
  background: var(--panel-bg);
  box-shadow: var(--shadow-sm);
}

.empty-state {
  max-width: var(--reading-width);
  margin: 0 auto;
  padding: 40px 24px;
  text-align: center;
}

.empty-state h1 {
  margin: 0 0 10px;
  color: var(--text-color);
  font-size: 24px;
}

.empty-state p {
  margin: 0;
  color: var(--muted-text-color);
}

.preview-page {
  display: grid;
  gap: 16px;
}

.preview-head {
  padding: clamp(24px, 4vw, 36px);
}

.page-kicker {
  color: var(--accent-color);
  font-size: 12px;
  font-weight: 800;
}

.preview-head h1 {
  margin: 10px 0 14px;
  color: var(--text-color);
  font-size: clamp(32px, 5vw, 54px);
  line-height: 1.08;
  font-weight: 900;
}

.preview-meta,
.preview-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 12px;
  align-items: center;
}

.preview-meta {
  color: var(--muted-text-color);
  font-size: 13px;
}

.preview-summary {
  margin: 18px 0 0;
  color: var(--muted-text-color);
  font-size: 15px;
  line-height: 1.8;
}

.preview-tags {
  margin-top: 18px;
}

.review-alert {
  border-radius: var(--radius-md);
}

.preview-cover {
  margin: 0;
}

.preview-cover img {
  display: block;
  width: 100%;
  max-height: 380px;
  object-fit: cover;
  border: 1px solid var(--soft-border-color);
  border-radius: var(--radius-lg);
}

.preview-reading-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  gap: 26px;
  align-items: start;
  max-width: var(--reading-width);
  margin: 0 auto;
  width: 100%;
}

.preview-reading-layout.has-toc {
  grid-template-columns: minmax(0, 1fr) 260px;
  max-width: none;
  margin: 0;
}

.preview-body {
  min-width: 0;
  padding: clamp(22px, 4vw, 34px);
}

.preview-sidebar {
  position: sticky;
  top: calc(var(--app-header-height) + 22px);
}

.toc-panel,
.mobile-toc {
  display: grid;
  gap: 6px;
  padding: 16px;
}

.toc-panel {
  max-height: calc(100vh - var(--app-header-height) - 150px);
  overflow-y: auto;
  overscroll-behavior: contain;
  scrollbar-gutter: stable;
}

.mobile-toc {
  display: none;
  margin-bottom: 22px;
  box-shadow: none;
}

.toc-link {
  width: 100%;
  padding: 7px 8px;
  border: 0;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--muted-text-color);
  font: inherit;
  font-size: 13px;
  line-height: 1.45;
  text-align: left;
  cursor: pointer;
}

.toc-link:hover {
  background: color-mix(in srgb, var(--primary-color) 8%, transparent);
  color: var(--primary-color);
}

.toc-link.active {
  background: color-mix(in srgb, var(--primary-color) 12%, transparent);
  color: var(--primary-color);
  font-weight: 800;
}

.toc-link.level-3 {
  padding-left: 20px;
}

.toc-link.level-4 {
  padding-left: 32px;
  font-size: 12px;
}

@media (max-width: 980px) {
  .preview-reading-layout.has-toc {
    grid-template-columns: 1fr;
    max-width: var(--reading-width);
    margin: 0 auto;
  }

  .preview-sidebar {
    display: none;
  }

  .mobile-toc {
    display: grid;
  }
}

@media (max-width: 640px) {
  .main {
    padding: 24px 14px 56px;
  }

  .page-actions {
    align-items: stretch;
  }

  .back-button,
  .edit-button {
    flex: 1 1 150px;
    justify-content: center;
  }

  .preview-head,
  .preview-body {
    padding: 18px;
  }
}
</style>
