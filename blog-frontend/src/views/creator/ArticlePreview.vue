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
        <el-alert
          v-if="article.reviewReason"
          class="review-alert"
          type="warning"
          :title="`审核反馈：${article.reviewReason}`"
          show-icon
          :closable="false"
        />

        <div class="reading-canvas preview-reading-layout" :class="{ 'has-toc': toc.length }">
          <div class="reading-primary">
            <header class="detail-head preview-head">
              <div class="detail-copy">
                <span class="eyebrow">文章预览</span>
                <h1>{{ article.title }}</h1>
                <p v-if="article.summary" class="detail-summary">{{ article.summary }}</p>
                <div class="detail-meta">
                  <span>{{ statusText(article.status) }}</span>
                  <span>{{ articleVisibilityText(article.visibility) }}</span>
                  <span>{{ formatDate(article.updatedAt || article.createdAt) }}</span>
                  <span v-if="article.createdBy">{{ article.createdBy }}</span>
                </div>
                <div v-if="article.tags?.length" class="detail-actions">
                  <div class="article-tags">
                    <span v-for="tag in article.tags" :key="tag.id" class="tag-chip">{{ tag.name }}</span>
                  </div>
                </div>
              </div>
            </header>

            <figure v-if="article.coverImage" class="preview-cover">
              <img :src="article.coverImage" :alt="article.title" />
            </figure>

            <section class="article-body preview-body">
              <nav v-if="toc.length" class="mobile-toc" aria-label="文章目录">
                <span class="eyebrow">目录</span>
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
          </div>

          <aside v-if="toc.length" class="reading-sidebar preview-sidebar">
            <nav ref="tocPanelRef" class="toc-panel" aria-label="文章目录">
              <span class="eyebrow">目录</span>
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
            <section class="note-panel">
              <span class="eyebrow">阅读提示</span>
              <p>目录会跟随章节、小节和细分标题生成，适合快速回到关键段落。</p>
            </section>
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
import { articleVisibilityText } from '../../utils/articleVisibility'
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

</script>

<style scoped>
.main {
  position: relative;
  isolation: isolate;
  width: min(1180px, calc(100% - 36px));
  margin: 0 auto;
  padding: 22px 0 72px;
  overflow: visible;
}

.main::before {
  position: fixed;
  inset: 0;
  z-index: -1;
  background:
    linear-gradient(90deg, var(--theme-grid-x) 1px, transparent 1px),
    linear-gradient(180deg, var(--theme-grid-y) 1px, transparent 1px);
  background-size: 44px 44px;
  content: '';
  opacity: 0.52;
  pointer-events: none;
}

.page-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 22px;
  padding: 12px 0;
  border-bottom: 1px solid var(--soft-border-color);
}

.back-button,
.edit-button {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: 36px;
  padding: 0 12px;
  border: 1px solid var(--border-color);
  border-radius: 999px;
  background: color-mix(in srgb, var(--panel-bg) 92%, transparent);
  color: var(--muted-text-color);
  font: inherit;
  font-weight: 700;
  cursor: pointer;
  transition:
    transform 0.2s ease,
    border-color 0.2s ease,
    background-color 0.2s ease,
    color 0.2s ease,
    box-shadow 0.2s ease;
}

.edit-button {
  border-color: color-mix(in srgb, var(--primary-color) 42%, var(--soft-border-color));
  background: var(--surface-wash-color);
  color: var(--primary-color);
}

.back-button:hover,
.back-button:focus-visible {
  border-color: var(--primary-color);
  color: var(--primary-color);
  background: var(--surface-wash-color);
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.edit-button:hover,
.edit-button:focus-visible {
  border-color: var(--primary-hover-color);
  background: color-mix(in srgb, var(--primary-color) 14%, transparent);
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.back-button:focus-visible,
.edit-button:focus-visible,
.toc-link:focus-visible {
  outline: 2px solid var(--primary-color);
  outline-offset: 2px;
}

.empty-state {
  max-width: var(--reading-width);
  margin: 0 auto;
  padding: 40px 24px;
  border-top: 1px solid var(--border-color);
  border-bottom: 1px solid var(--soft-border-color);
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
  gap: 28px;
}

.detail-head {
  position: relative;
  margin-bottom: 30px;
  padding: 6px 0 24px;
  border-bottom: 1px solid color-mix(in srgb, var(--border-color) 72%, transparent);
  background: transparent;
  box-shadow: none;
}

.reading-primary,
.detail-copy {
  min-width: 0;
}

.detail-copy {
  display: grid;
  align-content: start;
}

.eyebrow {
  display: inline-flex;
  margin: 0 0 12px;
  color: var(--primary-color);
  font-size: 12px;
  font-weight: 760;
  letter-spacing: 0;
}

.detail-head h1 {
  max-width: 820px;
  margin: 0 0 22px;
  color: var(--text-color);
  font-family: Georgia, "Times New Roman", serif;
  font-size: clamp(52px, 7.4vw, 92px);
  font-weight: 500;
  line-height: 0.98;
  overflow-wrap: anywhere;
}

.detail-summary {
  max-width: 680px;
  margin: 0 0 20px;
  color: color-mix(in srgb, var(--text-color) 62%, var(--muted-text-color));
  font-size: clamp(16px, 1.7vw, 18px);
  line-height: 1.75;
  overflow-wrap: anywhere;
}

.detail-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 14px;
  color: var(--muted-text-color);
  font-size: 13px;
}

.detail-meta span + span {
  position: relative;
  padding-left: 14px;
}

.detail-meta span + span::before {
  position: absolute;
  top: 50%;
  left: 0;
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: color-mix(in srgb, var(--primary-color) 36%, transparent);
  content: '';
  transform: translateY(-50%);
}

.detail-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 12px;
  align-items: center;
  margin-top: 20px;
}

.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 0;
}

.tag-chip {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 800;
  white-space: nowrap;
  padding: 4px 10px;
  border: 1px solid var(--border-color);
  background: color-mix(in srgb, var(--panel-bg) 92%, transparent);
  color: var(--muted-text-color);
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
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
}

.reading-canvas {
  display: grid;
  grid-template-columns: minmax(0, 760px);
  gap: 36px;
  align-items: start;
  justify-content: center;
}

.reading-canvas.has-toc {
  grid-template-columns: minmax(0, 760px) 280px;
  max-width: none;
  margin: 0;
}

.article-body {
  min-width: 0;
  padding: clamp(20px, 3vw, 34px) 0;
  border-top: 1px solid var(--border-color);
  border-bottom: 1px solid var(--soft-border-color);
}

.reading-sidebar {
  position: sticky;
  top: calc(var(--app-header-height) + 22px);
  display: grid;
  gap: 16px;
  padding-left: 18px;
  border-left: 1px solid color-mix(in srgb, var(--border-color) 72%, transparent);
}

.toc-panel {
  display: grid;
  gap: 6px;
  max-height: calc(100vh - var(--app-header-height) - 150px);
  padding: 0;
  border: 0;
  border-radius: 0;
  background: transparent;
  box-shadow: none;
  overflow-y: auto;
  overscroll-behavior: contain;
  scrollbar-gutter: stable;
}

.mobile-toc {
  display: none;
  gap: 6px;
  margin-bottom: 22px;
  padding: 16px;
  border: 1px solid color-mix(in srgb, var(--border-color) 78%, transparent);
  border-radius: var(--radius-md);
  background: color-mix(in srgb, var(--panel-bg) 84%, transparent);
  box-shadow: none;
}

.note-panel {
  padding: 0;
  border: 0;
  background: transparent;
  box-shadow: none;
}

.note-panel p {
  margin: 8px 0 0;
  color: var(--muted-text-color);
  font-size: 13px;
  line-height: 1.7;
}

.toc-link {
  position: relative;
  width: 100%;
  padding: 7px 0;
  border: 0;
  border-radius: 0;
  background: transparent;
  color: var(--muted-text-color);
  font: inherit;
  font-size: 13px;
  line-height: 1.45;
  text-align: left;
  cursor: pointer;
}

.toc-link::before {
  position: absolute;
  top: 50%;
  left: -22px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--primary-color);
  content: '';
  opacity: 0;
  transform: translateY(-50%) scale(0.45);
  transition: opacity 0.18s ease, transform 0.18s ease;
}

.toc-link.active::before {
  opacity: 1;
  transform: translateY(-50%) scale(1);
}

.toc-link:hover {
  background: transparent;
  color: var(--primary-color);
}

.toc-link.active {
  background: transparent;
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
  .reading-canvas.has-toc {
    grid-template-columns: 1fr;
    max-width: var(--reading-width);
    margin: 0 auto;
  }

  .reading-sidebar {
    display: none;
  }

  .mobile-toc {
    display: grid;
  }
}

@media (max-width: 640px) {
  .main {
    width: min(100% - 28px, var(--content-width));
    padding: 16px 0 56px;
  }

  .page-actions {
    align-items: stretch;
  }

  .back-button,
  .edit-button {
    flex: 1 1 150px;
    justify-content: center;
  }

  .detail-head h1 {
    font-size: 42px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .back-button,
  .edit-button,
  .toc-link::before {
    transition: none;
  }

  .back-button:hover,
  .back-button:focus-visible,
  .edit-button:hover,
  .edit-button:focus-visible {
    transform: none;
  }
}
</style>
