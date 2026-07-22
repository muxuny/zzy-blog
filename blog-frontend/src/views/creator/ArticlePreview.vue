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
        <header class="detail-head preview-head">
          <div class="detail-copy">
            <span class="eyebrow">文章预览</span>
            <h1>{{ article.title }}</h1>
            <div class="preview-meta">
              <span class="status-pill" :data-status="article.status">{{ statusText(article.status) }}</span>
              <span class="status-pill visibility-pill">{{ articleVisibilityText(article.visibility) }}</span>
              <span>{{ formatDate(article.updatedAt || article.createdAt) }}</span>
              <span v-if="article.createdBy">{{ article.createdBy }}</span>
            </div>
            <p v-if="article.summary" class="preview-summary">{{ article.summary }}</p>
            <div v-if="article.tags?.length" class="preview-tags">
              <span v-for="tag in article.tags" :key="tag.id" class="tag-chip">{{ tag.name }}</span>
            </div>
          </div>
          <aside class="detail-meta" aria-label="预览摘要">
            <div class="meta-line">
              <span class="meta-label">状态</span>
              <span class="meta-value">{{ statusText(article.status) }}</span>
            </div>
            <div class="meta-line">
              <span class="meta-label">可见性</span>
              <span class="meta-value">{{ articleVisibilityText(article.visibility) }}</span>
            </div>
            <div class="meta-line">
              <span class="meta-label">更新</span>
              <span class="meta-value">{{ formatDate(article.updatedAt || article.createdAt) }}</span>
            </div>
          </aside>
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

        <div class="reading-canvas preview-reading-layout" :class="{ 'has-toc': toc.length }">
          <section class="article-body preview-body">
            <section v-if="toc.length" class="section-meter mobile-section-meter" aria-label="章节位置">
              <button
                v-for="item in toc"
                :key="item.id"
                type="button"
                class="meter-dot"
                :class="{ active: activeHeadingId === item.id }"
                :title="item.text"
                @click="scrollToHeading(item.id)"
              />
            </section>

            <MarkdownRenderer :content="article.content" />
          </section>

          <aside v-if="toc.length" class="reading-sidebar preview-sidebar">
            <section class="section-meter" aria-label="章节位置">
              <button
                v-for="item in toc"
                :key="item.id"
                type="button"
                class="meter-dot"
                :class="{ active: activeHeadingId === item.id }"
                :title="item.text"
                @click="scrollToHeading(item.id)"
              />
            </section>
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
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(260px, 340px);
  gap: 30px;
  align-items: end;
  padding-top: 6px;
}

.detail-copy {
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

.detail-head h1 {
  max-width: 900px;
  margin: 0 0 18px;
  color: var(--text-color);
  font-family: Georgia, "Times New Roman", serif;
  font-size: clamp(42px, 7vw, 82px);
  font-weight: 500;
  line-height: 0.98;
  overflow-wrap: anywhere;
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
  font-size: 17px;
  line-height: 1.85;
}

.preview-tags {
  margin-top: 18px;
}

.tag-chip,
.status-pill {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 800;
  white-space: nowrap;
}

.tag-chip {
  padding: 4px 10px;
  border: 1px solid var(--border-color);
  background: color-mix(in srgb, var(--panel-bg) 92%, transparent);
  color: var(--muted-text-color);
}

.status-pill {
  padding: 4px 10px;
  background: color-mix(in srgb, var(--muted-text-color) 12%, transparent);
  color: var(--muted-text-color);
}

.status-pill[data-status="pending"] {
  background: color-mix(in srgb, var(--warning-color) 18%, transparent);
  color: var(--warning-color);
}

.status-pill[data-status="published"],
.visibility-pill {
  background: color-mix(in srgb, var(--primary-color) 14%, transparent);
  color: var(--primary-color);
}

.status-pill[data-status="rejected"] {
  background: color-mix(in srgb, var(--danger-color) 14%, transparent);
  color: var(--danger-color);
}

.detail-meta {
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
  grid-template-columns: minmax(0, var(--reading-width));
  gap: 34px;
  align-items: start;
}

.reading-canvas.has-toc {
  grid-template-columns: minmax(0, 1fr) 260px;
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
  gap: 14px;
}

.toc-panel,
.section-meter {
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background: color-mix(in srgb, var(--panel-bg) 94%, transparent);
  box-shadow: var(--shadow-sm);
}

.toc-panel {
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

.section-meter {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 14px 16px;
}

.mobile-section-meter {
  display: none;
  margin-bottom: 22px;
}

.meter-dot {
  width: 10px;
  height: 10px;
  padding: 0;
  border: 1px solid color-mix(in srgb, var(--primary-color) 38%, var(--border-color));
  border-radius: 999px;
  background: color-mix(in srgb, var(--panel-bg) 92%, transparent);
  cursor: pointer;
  transition:
    transform 0.18s ease,
    border-color 0.18s ease,
    background-color 0.18s ease,
    box-shadow 0.18s ease;
}

.meter-dot:hover,
.meter-dot.active {
  transform: scale(1.18);
  border-color: var(--primary-color);
  background: var(--primary-color);
  box-shadow: 0 0 0 6px var(--surface-wash-color);
}

.toc-link {
  position: relative;
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

.toc-link::before {
  position: absolute;
  top: 50%;
  left: -7px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--primary-color);
  content: '';
  opacity: 0;
  transform: translateY(-50%) scale(0.72);
  transition: opacity 0.18s ease, transform 0.18s ease;
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

.toc-link.active::before {
  opacity: 1;
  transform: translateY(-50%) scale(1);
}

.toc-link.level-3 {
  padding-left: 20px;
}

.toc-link.level-4 {
  padding-left: 32px;
  font-size: 12px;
}

@media (max-width: 980px) {
  .detail-head,
  .reading-canvas.has-toc {
    grid-template-columns: 1fr;
    max-width: var(--reading-width);
    margin: 0 auto;
  }

  .detail-meta {
    border-left: 0;
    border-top: 1px solid var(--border-color);
    padding-top: 12px;
    padding-left: 0;
  }

  .reading-sidebar {
    display: none;
  }

  .mobile-section-meter {
    display: flex;
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

  .meta-line {
    grid-template-columns: 1fr;
  }
}

@media (prefers-reduced-motion: reduce) {
  .back-button,
  .edit-button,
  .toc-link::before,
  .meter-dot {
    transition: none;
  }

  .back-button:hover,
  .back-button:focus-visible,
  .edit-button:hover,
  .edit-button:focus-visible,
  .meter-dot:hover,
  .meter-dot.active {
    transform: none;
  }
}
</style>
