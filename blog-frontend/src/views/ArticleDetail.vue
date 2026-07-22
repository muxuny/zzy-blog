<template>
  <div class="layout">
    <AppHeader />
    <div
      v-if="article"
      class="article-progress-bar"
      aria-hidden="true"
      :style="{ '--article-read-progress': `${currentReadingProgress}%` }"
    >
      <span />
    </div>
    <el-main class="main">
      <button type="button" class="back-button" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回</span>
      </button>

      <el-skeleton v-if="loading" :rows="10" animated />

      <section v-else-if="errorMessage" class="empty-state">
        <h1>{{ errorMessage }}</h1>
        <p>请返回首页浏览其他内容。</p>
        <button type="button" class="home-button" @click="router.push('/')">返回首页</button>
      </section>

      <article v-else-if="article" class="article-page">
        <header class="detail-head" :class="{ 'has-cover': article.coverImage }">
          <div class="detail-copy">
            <span class="eyebrow">文章</span>
            <h1>{{ article.title }}</h1>
            <div class="hero-meta">
              <span>{{ formatDate(article.createdAt) }}</span>
              <span>{{ authorName }}</span>
              <span>阅读 {{ article.viewCount || 0 }}</span>
              <span>{{ readingStats.readingTimeText }}</span>
            </div>
            <div v-if="articleTags.length" class="article-tags">
              <el-tag v-for="tag in articleTags" :key="tag.id" size="small">{{ tag.name }}</el-tag>
            </div>
            <div class="favorite-action">
              <el-button
                class="favorite-button"
                :loading="favoriteLoading"
                :aria-pressed="favorited"
                @click="handleFavorite"
              >
                <el-icon>
                  <StarFilled v-if="favorited" />
                  <Star v-else />
                </el-icon>
                <span>{{ favorited ? '已收藏' : '收藏' }}</span>
              </el-button>
            </div>
          </div>

          <aside class="detail-side">
            <figure v-if="article.coverImage" class="article-cover">
              <img :src="article.coverImage" :alt="article.title" />
            </figure>
            <section class="detail-meta" aria-label="文章阅读摘要">
              <div class="meta-line">
                <span class="meta-label">字数</span>
                <strong class="meta-value">{{ readingStats.wordCount }}</strong>
              </div>
              <div class="meta-line">
                <span class="meta-label">预计阅读</span>
                <strong class="meta-value">{{ readingStats.readingTimeText }}</strong>
              </div>
              <div class="meta-line">
                <span class="meta-label">作者</span>
                <strong class="meta-value">{{ authorName }}</strong>
              </div>
            </section>
          </aside>
        </header>

        <div class="reading-canvas">
          <main class="article-body">
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

          </main>

          <aside class="reading-sidebar">
            <nav v-if="toc.length" ref="tocPanelRef" class="toc-panel" aria-label="文章目录">
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
            <section v-if="toc.length" class="section-meter" aria-label="章节位置">
              <button
                v-for="item in toc"
                :key="`meter-${item.id}`"
                type="button"
                class="meter-dot"
                :class="{ active: activeHeadingId === item.id }"
                :title="item.text"
                :aria-label="`跳转到${item.text}`"
                @click="scrollToHeading(item.id)"
              />
            </section>
            <section class="note-panel">
              <span class="eyebrow">阅读提示</span>
              <p>目录会跟随章节、小节和细分标题生成，适合快速回到关键段落。</p>
            </section>
          </aside>
        </div>

        <section v-if="hasContinuation" class="continuation-section">
          <div v-if="neighbors.previous || neighbors.next" class="neighbor-grid">
            <button
              v-if="neighbors.previous"
              type="button"
              class="neighbor-card previous"
              @click="openArticle(neighbors.previous.id)"
            >
              <span>上一篇</span>
              <strong>{{ neighbors.previous.title }}</strong>
              <small>{{ formatDate(neighbors.previous.createdAt) }} · 阅读 {{ neighbors.previous.viewCount || 0 }}</small>
            </button>
            <button
              v-if="neighbors.next"
              type="button"
              class="neighbor-card next"
              @click="openArticle(neighbors.next.id)"
            >
              <span>下一篇</span>
              <strong>{{ neighbors.next.title }}</strong>
              <small>{{ formatDate(neighbors.next.createdAt) }} · 阅读 {{ neighbors.next.viewCount || 0 }}</small>
            </button>
          </div>

          <div v-if="relatedArticles.length" class="related-panel">
            <div class="section-heading">
              <span class="eyebrow">继续阅读</span>
              <h2>相关文章</h2>
            </div>
            <div class="related-grid">
              <button
                v-for="item in relatedArticles"
                :key="item.id"
                type="button"
                class="related-card"
                @click="openArticle(item.id)"
              >
                <strong>{{ item.title }}</strong>
                <small>{{ formatDate(item.createdAt) }} · 阅读 {{ item.viewCount || 0 }}</small>
                <span v-if="item.tags?.length" class="related-tags">
                  <el-tag v-for="tag in item.tags.slice(0, 2)" :key="tag.id" size="small">{{ tag.name }}</el-tag>
                </span>
              </button>
            </div>
          </div>
        </section>
      </article>
    </el-main>

    <el-dialog
      v-model="showResumeDialog"
      class="resume-reading-dialog"
      width="min(420px, calc(100vw - 32px))"
      align-center
      append-to-body
    >
      <template #header>
        <span class="resume-dialog-title">继续上次阅读</span>
      </template>
      <div class="resume-dialog-copy">
        <strong>上次读到 {{ readingPosition?.progressPercent ?? 0 }}%</strong>
        <p>可以从保存的位置继续，也可以先留在文章开头。</p>
      </div>
      <template #footer>
        <div class="resume-dialog-actions">
          <el-button @click="dismissResumePrompt">稍后再说</el-button>
          <el-button type="primary" @click="continueReadingFromSavedPosition">
            继续阅读
          </el-button>
        </div>
      </template>
    </el-dialog>

    <div v-show="showFloatingBackButton" class="floating-reading-tools" aria-label="阅读操作">
      <el-tooltip content="返回" placement="left">
        <button
          type="button"
          class="floating-tool-button floating-back-button"
          aria-label="返回"
          @click="handleFloatingBack"
        >
          <el-icon><ArrowLeft /></el-icon>
        </button>
      </el-tooltip>
      <el-tooltip content="返回顶部" placement="left">
        <button
          v-show="showBackToTop"
          type="button"
          class="floating-tool-button floating-top-button"
          aria-label="返回顶部"
          @click="scrollToTop()"
        >
          <el-icon><Top /></el-icon>
        </button>
      </el-tooltip>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { onBeforeRouteLeave, useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Star, StarFilled, Top } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getArticle, getArticleNeighbors, getRelatedArticles } from '../api/article'
import { favoriteArticle, unfavoriteArticle, getFavoriteStatus } from '../api/favorite'
import { saveReadingPosition } from '../api/reading'
import { useAuthStore } from '../stores/auth'
import { formatDate } from '../utils'
import {
  buildFavoriteLoginRedirect,
  canConsumeFavoriteIntent,
  clearFavoriteIntentQuery,
  hasFavoriteIntent,
  isFavoriteRequestContextCurrent
} from '../utils/favorite'
import { shouldUseHistoryBack } from '../utils/navigation'
import { extractMarkdownToc, getReadingStats } from '../utils/reading'
import {
  buildReadingPositionPayload,
  calculateReadingProgress,
  shouldSaveReadingPosition,
  shouldShowResumePrompt
} from '../utils/readingPosition'
import { getActiveHeadingId, getScrollTopForVisibleItem } from '../utils/scrollSpy'
import AppHeader from '../components/AppHeader.vue'
import MarkdownRenderer from '../components/MarkdownRenderer.vue'

const favoriteRequestConfig = { skipAuthRedirect: true }
const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const article = ref(null)
const loading = ref(true)
const errorMessage = ref('')
const favorited = ref(false)
const favoriteLoading = ref(false)
const neighbors = ref({ previous: null, next: null })
const relatedArticles = ref([])
const activeHeadingId = ref('')
const currentReadingProgress = ref(0)
const showBackToTop = ref(false)
const readingPosition = ref(null)
const resumePromptDismissed = ref(false)
const loadToken = ref(0)
const tocPanelRef = ref(null)
let componentActive = true
let favoriteStateVersion = 0
let favoriteOperationId = 0
let lastPositionSavedAt = 0

const articleTags = computed(() => article.value?.tags || [])
const authorName = computed(() => article.value?.authorName || article.value?.createdBy || '匿名作者')
const toc = computed(() => extractMarkdownToc(article.value?.content || ''))
const readingStats = computed(() => getReadingStats(article.value?.content || ''))
const hasContinuation = computed(() =>
  neighbors.value.previous || neighbors.value.next || relatedArticles.value.length
)
const showResumePrompt = computed(() =>
  authStore.isLoggedIn &&
  !resumePromptDismissed.value &&
  shouldShowResumePrompt(readingPosition.value)
)
const showResumeDialog = computed({
  get: () => showResumePrompt.value,
  set: value => {
    if (!value) dismissResumePrompt()
  }
})
const showFloatingBackButton = computed(() => !!article.value && showBackToTop.value)

onMounted(() => {
  loadArticle()
  window.addEventListener('scroll', updateScrollState, { passive: true })
  document.addEventListener('visibilitychange', handleVisibilityChange)
})

onBeforeUnmount(() => {
  flushReadingPosition()
  componentActive = false
  loadToken.value += 1
  favoriteStateVersion += 1
  favoriteOperationId += 1
  window.removeEventListener('scroll', updateScrollState)
  document.removeEventListener('visibilitychange', handleVisibilityChange)
})

onBeforeRouteLeave(() => {
  flushReadingPosition()
})

watch(() => route.params.id, () => {
  flushReadingPosition()
  scrollToTop(false)
  loadArticle()
})

watch(() => authStore.token, authToken => {
  favoriteStateVersion += 1
  favoriteOperationId += 1
  favoriteLoading.value = false
  favorited.value = false
  if (!componentActive || !article.value || !authToken) return
  void initializeFavorite(article.value.id, loadToken.value).catch(() => {})
})

async function loadArticle() {
  const token = ++loadToken.value
  favoriteStateVersion += 1
  favoriteOperationId += 1
  favoriteLoading.value = false
  loading.value = true
  errorMessage.value = ''
  article.value = null
  favorited.value = false
  neighbors.value = { previous: null, next: null }
  relatedArticles.value = []
  activeHeadingId.value = ''
  currentReadingProgress.value = 0
  readingPosition.value = null
  resumePromptDismissed.value = false
  lastPositionSavedAt = 0
  try {
    const r = await getArticle(route.params.id)
    if (token !== loadToken.value) return
    article.value = r.data
    readingPosition.value = r.data?.readingPosition || null
    resumePromptDismissed.value = false
    lastPositionSavedAt = 0
    void initializeFavorite(r.data.id, token).catch(() => {})
    await nextTick()
    updateScrollState()
    loadContinuationData(r.data.id, token)
  } catch {
    if (token !== loadToken.value) return
    errorMessage.value = '文章不存在或暂不可见'
  } finally {
    if (token === loadToken.value) loading.value = false
  }
}

function isCurrentFavoriteRequest(articleId, token, stateVersion, requestAuthToken) {
  return isFavoriteRequestContextCurrent({
    componentActive,
    requestArticleId: articleId,
    currentArticleId: article.value?.id,
    requestLoadToken: token,
    currentLoadToken: loadToken.value,
    requestStateVersion: stateVersion,
    currentStateVersion: favoriteStateVersion,
    requestAuthToken,
    currentAuthToken: localStorage.getItem('token')
  })
}

async function loadFavoriteState(articleId, token) {
  const stateVersion = favoriteStateVersion
  const requestAuthToken = localStorage.getItem('token')
  if (!authStore.isLoggedIn || !requestAuthToken) {
    if (componentActive && token === loadToken.value && article.value?.id === articleId) {
      favorited.value = false
    }
    return
  }
  try {
    const result = await getFavoriteStatus(articleId, favoriteRequestConfig)
    if (!isCurrentFavoriteRequest(articleId, token, stateVersion, requestAuthToken)) return
    favorited.value = !!result.data?.favorited
  } catch (error) {
    if (!isCurrentFavoriteRequest(articleId, token, stateVersion, requestAuthToken)) return
    favorited.value = false
    if (error.response?.status === 401) authStore.logout()
  }
}

async function setFavorite(articleId, nextValue, token = loadToken.value) {
  if (favoriteLoading.value || !componentActive) return false
  const requestAuthToken = localStorage.getItem('token')
  if (!requestAuthToken) return false
  const stateVersion = ++favoriteStateVersion
  const operationId = ++favoriteOperationId
  favoriteLoading.value = true
  try {
    if (nextValue) await favoriteArticle(articleId, favoriteRequestConfig)
    else await unfavoriteArticle(articleId, favoriteRequestConfig)
    if (!isCurrentFavoriteRequest(articleId, token, stateVersion, requestAuthToken)) return false
    favorited.value = nextValue
    ElMessage.success(nextValue ? '已收藏' : '已取消收藏')
    return true
  } catch (error) {
    if (
      error.response?.status === 401 &&
      isCurrentFavoriteRequest(articleId, token, stateVersion, requestAuthToken)
    ) {
      authStore.logout()
      await navigateToFavoriteLogin(articleId, nextValue)
    }
    return false
  } finally {
    if (operationId === favoriteOperationId) favoriteLoading.value = false
  }
}

async function navigateToFavoriteLogin(articleId, nextValue = true) {
  try {
    await router.replace({
      path: '/login',
      query: { redirect: buildFavoriteLoginRedirect(articleId, nextValue) }
    })
  } catch {
    // The current page remains usable when a navigation guard rejects the redirect.
  }
}

async function handleFavorite() {
  if (!article.value) return
  if (!authStore.isLoggedIn) {
    await navigateToFavoriteLogin(article.value.id)
    return
  }
  await setFavorite(article.value.id, !favorited.value)
}

async function initializeFavorite(articleId, token) {
  if (!authStore.isLoggedIn || token !== loadToken.value) return
  if (hasFavoriteIntent(route.query)) {
    const intentAuthToken = localStorage.getItem('token')
    if (!intentAuthToken) return
    const intentFullPath = route.fullPath
    const intentPath = route.path
    const intentArticleId = articleId
    try {
      await setFavorite(articleId, true, token)
    } finally {
      if (canConsumeFavoriteIntent({
        componentActive,
        intentArticleId,
        currentArticleId: article.value?.id,
        intentLoadToken: token,
        currentLoadToken: loadToken.value,
        intentFullPath,
        currentFullPath: route.fullPath,
        intentPath,
        currentPath: route.path,
        browserPathname: typeof window === 'undefined' ? '' : window.location.pathname,
        intentAuthToken,
        currentAuthToken: localStorage.getItem('token'),
        hasIntent: hasFavoriteIntent(route.query)
      })) {
        await replaceFavoriteIntentQuery(clearFavoriteIntentQuery(route.query))
      }
    }
    return
  }
  await loadFavoriteState(articleId, token)
}

async function replaceFavoriteIntentQuery(query) {
  try {
    await router.replace({ query })
  } catch {
    // Keep the current intent so a later initialization can retry it.
  }
}

async function loadContinuationData(articleId, token) {
  try {
    const [neighborsResult, relatedResult] = await Promise.all([
      getArticleNeighbors(articleId),
      getRelatedArticles(articleId, 4)
    ])
    if (token !== loadToken.value) return
    neighbors.value = neighborsResult.data || { previous: null, next: null }
    relatedArticles.value = relatedResult.data || []
  } catch {
    if (token !== loadToken.value) return
    neighbors.value = { previous: null, next: null }
    relatedArticles.value = []
  }
}

function goBack() {
  if (shouldUseHistoryBack(history.state)) router.back()
  else router.push('/')
}

function handleFloatingBack() {
  flushReadingPosition()
  goBack()
}

function prefersReducedMotion() {
  return typeof window !== 'undefined' &&
    typeof window.matchMedia === 'function' &&
    window.matchMedia('(prefers-reduced-motion: reduce)').matches
}

function scrollToHeading(id) {
  document.getElementById(id)?.scrollIntoView({
    behavior: prefersReducedMotion() ? 'auto' : 'smooth',
    block: 'start'
  })
}

function getArticleBodyMetrics() {
  const body = document.querySelector('.article-body')
  if (!body) return null
  const rect = body.getBoundingClientRect()
  return {
    articleTop: rect.top + window.scrollY,
    articleHeight: body.scrollHeight,
    viewportHeight: window.innerHeight
  }
}

function getAnchorOffset(anchorId) {
  if (!anchorId) return null
  const element = document.getElementById(anchorId)
  if (!element) return null
  return Math.max(0, Math.round(window.scrollY - (element.getBoundingClientRect().top + window.scrollY)))
}

function buildCurrentReadingPositionPayload() {
  if (!article.value?.id || !article.value?.updatedAt) return null
  const metrics = getArticleBodyMetrics()
  if (!metrics) return null
  const progressPercent = calculateReadingProgress({
    scrollY: window.scrollY,
    articleTop: metrics.articleTop,
    articleHeight: metrics.articleHeight,
    viewportHeight: metrics.viewportHeight
  })
  return buildReadingPositionPayload({
    progressPercent,
    scrollY: window.scrollY,
    anchorId: activeHeadingId.value,
    anchorOffset: getAnchorOffset(activeHeadingId.value),
    articleUpdatedAt: article.value.updatedAt
  })
}

function scheduleReadingPositionSave(force = false) {
  if (!authStore.isLoggedIn || !componentActive || !article.value?.id) return
  const now = Date.now()
  if (!shouldSaveReadingPosition({ now, lastSavedAt: lastPositionSavedAt, intervalMs: 9000, force })) {
    return
  }
  const payload = buildCurrentReadingPositionPayload()
  if (!payload) return
  lastPositionSavedAt = now
  void saveReadingPosition(article.value.id, payload, {
    skipErrorMessage: true,
    skipAuthRedirect: true
  }).catch(() => {})
}

function flushReadingPosition() {
  scheduleReadingPositionSave(true)
}

function handleVisibilityChange() {
  if (document.visibilityState === 'hidden') {
    flushReadingPosition()
  }
}

function continueReadingFromSavedPosition() {
  const scrollY = Number(readingPosition.value?.resumeScrollY)
  if (!Number.isFinite(scrollY)) return
  resumePromptDismissed.value = true
  window.scrollTo({
    top: Math.max(0, scrollY),
    behavior: prefersReducedMotion() ? 'auto' : 'smooth'
  })
}

function dismissResumePrompt() {
  resumePromptDismissed.value = true
}

function updateScrollState() {
  showBackToTop.value = window.scrollY > 360
  const metrics = getArticleBodyMetrics()
  if (metrics) {
    currentReadingProgress.value = calculateReadingProgress({
      scrollY: window.scrollY,
      articleTop: metrics.articleTop,
      articleHeight: metrics.articleHeight,
      viewportHeight: metrics.viewportHeight
    })
  }
  if (!toc.value.length) {
    activeHeadingId.value = ''
    scheduleReadingPositionSave(false)
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
  scheduleReadingPositionSave(false)
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

function openArticle(id) {
  router.push(`/article/${id}`)
}

function scrollToTop(smooth = true) {
  window.scrollTo({ top: 0, behavior: smooth && !prefersReducedMotion() ? 'smooth' : 'auto' })
}
</script>

<style scoped>
.article-progress-bar {
  position: fixed;
  top: 0;
  left: 0;
  z-index: 120;
  width: 100%;
  height: 3px;
  pointer-events: none;
}

.article-progress-bar span {
  display: block;
  width: var(--article-read-progress);
  height: 100%;
  background: linear-gradient(90deg, var(--primary-color), var(--accent-color), var(--danger-color));
  box-shadow: 0 0 16px var(--theme-glow-color);
  transition: width 0.12s linear;
}

.main {
  width: min(1180px, calc(100% - 36px));
  margin: 0 auto;
  padding: 22px 0 72px;
  overflow: visible;
}

.back-button {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: 36px;
  margin-bottom: 16px;
  padding: 0 12px;
  border: 1px solid var(--border-color);
  border-radius: 999px;
  background: color-mix(in srgb, var(--panel-bg) 94%, transparent);
  color: var(--muted-text-color);
  font: inherit;
  font-weight: 700;
  cursor: pointer;
  box-shadow: var(--shadow-sm);
  transition:
    transform 0.22s ease,
    border-color 0.22s ease,
    background-color 0.22s ease,
    color 0.22s ease;
}

.back-button:hover {
  transform: translateY(-1px);
  border-color: color-mix(in srgb, var(--primary-color) 72%, var(--border-color));
  color: var(--primary-color);
  background: var(--surface-wash-color);
}

.back-button:focus-visible,
.home-button:focus-visible,
.toc-link:focus-visible,
.meter-dot:focus-visible,
.neighbor-card:focus-visible,
.related-card:focus-visible,
.floating-tool-button:focus-visible {
  outline: 2px solid var(--primary-color);
  outline-offset: 2px;
}

.empty-state {
  padding: 44px 28px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background: color-mix(in srgb, var(--panel-bg) 94%, transparent);
  text-align: center;
  box-shadow: var(--shadow-sm);
}

.empty-state h1 {
  margin-bottom: 10px;
  color: var(--text-color);
  font-size: 24px;
}

.empty-state p {
  margin-bottom: 20px;
  color: var(--muted-text-color);
}

.home-button {
  min-height: 38px;
  padding: 0 16px;
  border: 1px solid var(--primary-color);
  border-radius: var(--radius-sm);
  background: var(--primary-color);
  color: #fff;
  font: inherit;
  font-weight: 700;
  cursor: pointer;
}

.article-page {
  display: grid;
  gap: 28px;
}

.detail-head {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(280px, 360px);
  gap: 30px;
  align-items: end;
  padding-top: 12px;
}

.detail-copy,
.detail-side {
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

.hero-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 16px;
  color: var(--muted-text-color);
  font-size: 14px;
}

.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 18px;
}

.favorite-action {
  display: flex;
  align-items: center;
  min-height: 40px;
  margin-top: 18px;
}

.favorite-button {
  width: 112px;
  height: 40px;
  flex: 0 0 auto;
}

.detail-side {
  display: grid;
  gap: 12px;
}

.article-cover {
  position: relative;
  min-width: 0;
  min-height: 210px;
  margin: 0;
  overflow: hidden;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background:
    linear-gradient(135deg, color-mix(in srgb, var(--primary-color) 20%, transparent), transparent),
    var(--panel-bg);
  box-shadow: var(--shadow-sm);
}

.article-cover img {
  display: block;
  width: 100%;
  height: 100%;
  min-height: 210px;
  object-fit: cover;
}

.detail-meta {
  border-left: 1px solid var(--border-color);
  padding-left: 18px;
}

.meta-line {
  display: grid;
  grid-template-columns: 78px minmax(0, 1fr);
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

.reading-canvas {
  display: grid;
  grid-template-columns: minmax(0, var(--reading-width)) 280px;
  gap: 34px;
  align-items: start;
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
.note-panel,
.mobile-toc,
.section-meter {
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background: color-mix(in srgb, var(--panel-bg) 94%, transparent);
  box-shadow: var(--shadow-sm);
}

.toc-panel,
.note-panel,
.mobile-toc {
  padding: 16px;
}

.toc-panel,
.mobile-toc {
  display: grid;
  gap: 6px;
}

.toc-panel {
  max-height: calc(100vh - var(--app-header-height) - 218px);
  overflow-y: auto;
  overscroll-behavior: contain;
  scrollbar-gutter: stable;
}

.note-panel p {
  margin: 8px 0 0;
  color: var(--muted-text-color);
  font-size: 13px;
  line-height: 1.7;
}

.section-meter {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 14px 16px;
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
  transform: translateY(-50%) scale(0.45);
  transition: opacity 0.18s ease, transform 0.18s ease;
}

.toc-link.active::before {
  opacity: 1;
  transform: translateY(-50%) scale(1);
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

.mobile-toc {
  display: none;
  margin-bottom: 22px;
}

.continuation-section {
  display: grid;
  gap: 18px;
}

.neighbor-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.neighbor-card,
.related-card {
  position: relative;
  display: grid;
  gap: 8px;
  width: 100%;
  min-width: 0;
  padding: 18px 20px;
  overflow: hidden;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background: color-mix(in srgb, var(--panel-bg) 96%, transparent);
  color: var(--text-color);
  font: inherit;
  text-align: left;
  cursor: pointer;
  transition:
    transform 0.22s ease,
    border-color 0.22s ease,
    background-color 0.22s ease,
    box-shadow 0.22s ease;
}

.neighbor-card::before,
.related-card::before {
  position: absolute;
  top: 14px;
  bottom: 14px;
  left: -1px;
  width: 3px;
  border-radius: 999px;
  background: linear-gradient(180deg, var(--primary-color), var(--accent-color));
  content: '';
  opacity: 0;
  transform: scaleY(0.55);
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.neighbor-card:hover,
.related-card:hover {
  transform: translateY(-2px);
  border-color: color-mix(in srgb, var(--primary-color) 72%, var(--border-color));
  background: var(--panel-bg);
  box-shadow: var(--shadow-sm);
}

.neighbor-card:hover::before,
.related-card:hover::before {
  opacity: 1;
  transform: scaleY(1);
}

.neighbor-card span,
.neighbor-card small,
.related-card small {
  color: var(--muted-text-color);
  font-size: 13px;
}

.neighbor-card strong,
.related-card strong {
  min-width: 0;
  color: var(--text-color);
  line-height: 1.5;
  overflow-wrap: anywhere;
}

.related-panel {
  display: grid;
  gap: 14px;
}

.section-heading h2 {
  margin: 6px 0 0;
  color: var(--text-color);
  font-family: Georgia, "Times New Roman", serif;
  font-size: 30px;
  font-weight: 500;
  line-height: 1.08;
}

.related-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.related-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.resume-reading-dialog :deep(.el-dialog__header) {
  padding-bottom: 0;
}

.resume-dialog-title {
  color: var(--text-color);
  font-size: 19px;
  font-weight: 850;
}

.resume-dialog-copy {
  display: grid;
  gap: 8px;
  color: var(--muted-text-color);
  line-height: 1.7;
}

.resume-dialog-copy strong {
  color: var(--text-color);
  font-size: 26px;
  line-height: 1.2;
}

.resume-dialog-copy p {
  margin: 0;
}

.resume-dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.floating-reading-tools {
  position: fixed;
  right: calc(24px + env(safe-area-inset-right));
  bottom: calc(28px + env(safe-area-inset-bottom));
  z-index: 20;
  display: inline-flex;
  gap: 4px;
  align-items: center;
  padding: 6px;
  border: 1px solid color-mix(in srgb, var(--soft-border-color) 86%, transparent);
  border-radius: 999px;
  background: var(--panel-bg);
  box-shadow:
    0 16px 36px var(--theme-glow-color),
    0 4px 12px rgba(15, 23, 42, 0.1);
}

.floating-tool-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  padding: 0;
  border: 0;
  border-radius: 999px;
  background: transparent;
  color: var(--text-color);
  font: inherit;
  font-weight: 800;
  cursor: pointer;
  transition: background-color 0.18s ease, color 0.18s ease, transform 0.18s ease;
}

.floating-tool-button:hover {
  background: color-mix(in srgb, var(--primary-color) 12%, var(--panel-bg));
  color: var(--primary-color);
  transform: translateY(-1px);
}

.floating-tool-button:focus-visible {
  outline: 2px solid color-mix(in srgb, var(--primary-color) 38%, transparent);
  outline-offset: 2px;
}

.floating-tool-button:active {
  transform: translateY(0) scale(0.96);
}

.floating-tool-button .el-icon {
  font-size: 18px;
}

@media (max-width: 980px) {
  .detail-head,
  .reading-canvas {
    grid-template-columns: 1fr;
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

  .mobile-toc {
    display: grid;
  }
}

@media (max-width: 640px) {
  .main {
    width: min(100% - 28px, var(--content-width));
    padding: 16px 0 48px;
  }

  .detail-head h1 {
    font-size: 42px;
  }

  .article-body {
    padding: 18px 0;
  }

  .neighbor-grid,
  .related-grid {
    grid-template-columns: 1fr;
  }

  .floating-reading-tools {
    right: calc(14px + env(safe-area-inset-right));
    bottom: calc(18px + env(safe-area-inset-bottom));
    padding: 4px;
  }

  .floating-tool-button {
    width: 36px;
    height: 36px;
  }

  .article-cover,
  .article-cover img {
    min-height: 210px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .article-progress-bar span,
  .back-button,
  .toc-link,
  .toc-link::before,
  .meter-dot,
  .neighbor-card,
  .neighbor-card::before,
  .related-card,
  .related-card::before,
  .floating-tool-button {
    transition: none;
  }

  .back-button:hover,
  .meter-dot:hover,
  .meter-dot.active,
  .neighbor-card:hover,
  .related-card:hover,
  .floating-tool-button:hover {
    transform: none;
  }
}
</style>
