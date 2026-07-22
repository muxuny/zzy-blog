<template>
  <div class="layout">
    <AppHeader />
    <main class="reading-main" :aria-busy="loading">
      <header class="page-head">
        <div class="head-copy">
          <span class="eyebrow">Reading console</span>
          <h1>我的阅读</h1>
          <p>
            回到自己正在看的内容，也保留最近轨迹和收藏入口。这里负责阅读延续，首页继续负责内容发现。
          </p>
        </div>
        <aside class="head-meta" aria-label="阅读摘要">
          <div class="meta-line">
            <span class="meta-label">继续</span>
            <span class="meta-value">
              {{ overview.lastRead ? formatReadingTime(overview.lastRead.lastReadAt) : '暂无记录' }}
            </span>
          </div>
          <div class="meta-line">
            <span class="meta-label">历史</span>
            <span class="meta-value">{{ overview.historyTotal }} 条阅读轨迹</span>
          </div>
          <div class="meta-line">
            <span class="meta-label">收藏</span>
            <span class="meta-value">{{ overview.favoriteTotal }} 篇文章</span>
          </div>
          <RouterLink class="discover-link" to="/">
            <span>发现更多文章</span>
            <el-icon><ArrowRight /></el-icon>
          </RouterLink>
        </aside>
      </header>

      <div v-if="loadError" class="error-row">
        <el-alert type="error" :title="loadError" :closable="false" show-icon />
        <el-button :icon="Refresh" @click="retryLoad">重试</el-button>
      </div>

      <div v-else-if="loading" class="overview-skeleton" role="status" aria-live="polite">
        <span class="sr-only">正在加载我的阅读</span>
        <el-skeleton :rows="8" animated />
      </div>

      <template v-else>
        <section class="section-title">
          <div>
            <span class="eyebrow">Resume</span>
            <h2>继续阅读</h2>
          </div>
          <p>主任务在左侧，最近轨迹在右侧，减少重新寻找的成本。</p>
        </section>

        <section class="reading-layout">
          <article
            v-if="overview.lastRead"
            class="continue-panel"
            :class="{
              'has-cover': overview.lastRead.available && overview.lastRead.coverImage,
              'is-unavailable': !overview.lastRead.available
            }"
          >
            <RouterLink
              v-if="overview.lastRead.available"
              class="card-open-link"
              :to="`/article/${overview.lastRead.articleId}`"
              :aria-label="`继续阅读：${overview.lastRead.title}`"
            />

            <template v-if="overview.lastRead.available">
              <img
                v-if="overview.lastRead.coverImage"
                class="continue-cover"
                :src="overview.lastRead.coverImage"
                alt=""
              />
              <div class="continue-copy">
                <span class="status-pill">上次阅读</span>
                <h3 class="continue-title">{{ overview.lastRead.title }}</h3>
                <p v-if="overview.lastRead.summary" class="continue-summary">
                  {{ overview.lastRead.summary }}
                </p>
                <span class="continue-time">
                  上次阅读 {{ formatReadingTime(overview.lastRead.lastReadAt) }}
                </span>
                <span
                  v-if="formatReadingProgress(overview.lastRead.progressPercent)"
                  class="reading-progress"
                >
                  {{ formatReadingProgress(overview.lastRead.progressPercent) }}
                </span>
                <span
                  v-if="formatReadingProgress(overview.lastRead.progressPercent)"
                  class="reading-progress-track"
                  aria-hidden="true"
                  :style="{ '--reading-progress': `${overview.lastRead.progressPercent || 0}%` }"
                >
                  <i />
                </span>
              </div>
            </template>

            <template v-else>
              <div class="continue-copy unavailable-copy">
                <span class="status-pill muted">暂不可读</span>
                <el-tooltip
                  content="该文章暂未公开"
                  placement="top"
                  :trigger="['hover', 'focus']"
                >
                  <h3 class="title-snapshot" tabindex="0">{{ overview.lastRead.title }}</h3>
                </el-tooltip>
                <span class="continue-time">
                  上次阅读 {{ formatReadingTime(overview.lastRead.lastReadAt) }}
                </span>
                <span
                  v-if="formatReadingProgress(overview.lastRead.progressPercent)"
                  class="reading-progress"
                >
                  {{ formatReadingProgress(overview.lastRead.progressPercent) }}
                </span>
                <span class="unavailable-note">该文章暂未公开</span>
              </div>
            </template>
          </article>

          <div v-else class="continue-panel empty-status" role="status" aria-live="polite">
            <el-empty description="还没有可继续阅读的文章">
              <el-button type="primary" :icon="ArrowRight" @click="goDiscover">
                去发现文章
              </el-button>
            </el-empty>
          </div>

          <aside class="reading-side">
            <div class="section-heading">
              <div>
                <span class="eyebrow">Recent trail</span>
                <h2>最近阅读</h2>
              </div>
              <RouterLink class="section-link" to="/reading/history">
                查看全部 {{ overview.historyTotal }}
              </RouterLink>
            </div>

            <div
              v-if="overview.recentHistory.length"
              class="history-timeline"
              aria-label="最近阅读记录"
            >
              <article
                v-for="item in overview.recentHistory"
                :key="item.articleId"
                class="timeline-item"
                :class="{ 'is-unavailable': !item.available }"
              >
                <RouterLink
                  v-if="item.available"
                  class="card-open-link"
                  :to="`/article/${item.articleId}`"
                  :aria-label="`打开文章：${item.title}`"
                />

                <template v-if="item.available">
                  <div class="preview-copy">
                    <h3 class="preview-title">{{ item.title }}</h3>
                    <div class="preview-meta">
                      <span>{{ formatReadingTime(item.lastReadAt) }}</span>
                      <span v-if="formatReadingProgress(item.progressPercent)">
                        {{ formatReadingProgress(item.progressPercent) }}
                      </span>
                      <span v-if="item.authorName">{{ item.authorName }}</span>
                    </div>
                  </div>
                </template>

                <template v-else>
                  <div class="preview-copy">
                    <el-tooltip
                      content="该文章暂未公开"
                      placement="top"
                      :trigger="['hover', 'focus']"
                    >
                      <h3 class="preview-title title-snapshot" tabindex="0">{{ item.title }}</h3>
                    </el-tooltip>
                    <div class="preview-meta">
                      <span>{{ formatReadingTime(item.lastReadAt) }}</span>
                      <span v-if="formatReadingProgress(item.progressPercent)">
                        {{ formatReadingProgress(item.progressPercent) }}
                      </span>
                      <span class="unavailable-note">该文章暂未公开</span>
                    </div>
                  </div>
                </template>
              </article>
            </div>

            <el-empty v-else description="暂无阅读历史" />
          </aside>
        </section>

        <section class="section-title favorite-title">
          <div>
            <span class="eyebrow">Pinned</span>
            <h2>最近收藏</h2>
          </div>
          <RouterLink class="section-link" to="/favorites">
            查看全部 {{ overview.favoriteTotal }}
          </RouterLink>
        </section>

        <section class="reading-section favorite-section">
          <div class="section-heading sr-only-heading">
            <div>
              <span class="section-label">稍后再看</span>
              <h2>最近收藏</h2>
            </div>
          </div>

          <div
            v-if="overview.recentFavorites.length"
            class="favorite-index"
            aria-label="最近收藏文章"
          >
            <article
              v-for="item in overview.recentFavorites"
              :key="item.articleId"
              class="favorite-line"
              :class="{
                'has-cover': item.available && item.coverImage,
                'is-unavailable': !item.available
              }"
            >
              <RouterLink
                v-if="item.available"
                class="card-open-link"
                :to="`/article/${item.articleId}`"
                :aria-label="`打开收藏文章：${item.title}`"
              />

              <template v-if="item.available">
                <img
                  v-if="item.coverImage"
                  class="favorite-cover"
                  :src="item.coverImage"
                  alt=""
                />
                <div class="preview-copy">
                  <h3 class="preview-title">{{ item.title }}</h3>
                  <div class="preview-meta">
                    <span v-if="item.authorName">{{ item.authorName }}</span>
                    <span>收藏于 {{ formatReadingTime(item.favoritedAt) }}</span>
                  </div>
                </div>
              </template>

              <template v-else>
                <div class="preview-copy">
                  <el-tooltip
                    content="该文章暂未公开"
                    placement="top"
                    :trigger="['hover', 'focus']"
                  >
                    <h3 class="preview-title title-snapshot" tabindex="0">{{ item.title }}</h3>
                  </el-tooltip>
                  <div class="preview-meta">
                    <span>收藏于 {{ formatReadingTime(item.favoritedAt) }}</span>
                    <span class="unavailable-note">该文章暂未公开</span>
                  </div>
                </div>
              </template>
            </article>
          </div>

          <el-empty v-else description="暂无收藏文章" />
        </section>
      </template>
    </main>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight, Refresh } from '@element-plus/icons-vue'
import AppHeader from '../components/AppHeader.vue'
import { getReadingOverview } from '../api/reading'
import { formatReadingProgress, formatReadingTime } from '../utils/readingHistory'

const overview = ref({
  lastRead: null,
  recentHistory: [],
  historyTotal: 0,
  recentFavorites: [],
  favoriteTotal: 0
})
const loading = ref(false)
const loadError = ref('')
const router = useRouter()
let componentActive = true
let requestVersion = 0

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
  loading.value = true
  loadError.value = ''

  try {
    const result = await getReadingOverview()
    if (!componentActive || requestId !== requestVersion) return null

    const data = result.data && typeof result.data === 'object' && !Array.isArray(result.data)
      ? result.data
      : {}
    const historyTotal = Number(data.historyTotal)
    const favoriteTotal = Number(data.favoriteTotal)
    overview.value = {
      lastRead: data.lastRead && typeof data.lastRead === 'object' && !Array.isArray(data.lastRead)
        ? data.lastRead
        : null,
      recentHistory: Array.isArray(data.recentHistory) ? data.recentHistory : [],
      historyTotal: Number.isFinite(historyTotal) && historyTotal > 0 ? Math.floor(historyTotal) : 0,
      recentFavorites: Array.isArray(data.recentFavorites) ? data.recentFavorites : [],
      favoriteTotal: Number.isFinite(favoriteTotal) && favoriteTotal > 0 ? Math.floor(favoriteTotal) : 0
    }
    return { applied: true }
  } catch (error) {
    if (!componentActive || requestId !== requestVersion) return null
    loadError.value = error.response?.data?.message || error.message || '我的阅读加载失败，请重试'
    return null
  } finally {
    if (componentActive && requestId === requestVersion) loading.value = false
  }
}

function retryLoad() {
  void load()
}

function goDiscover() {
  router.push('/')
}
</script>

<style scoped>
.reading-main {
  position: relative;
  isolation: isolate;
  width: min(1180px, calc(100% - 36px));
  margin: 0 auto;
  padding: 22px 0 72px;
}

.reading-main::before {
  position: fixed;
  inset: 0;
  z-index: -1;
  background:
    linear-gradient(90deg, var(--theme-grid-x) 1px, transparent 1px),
    linear-gradient(180deg, var(--theme-grid-y) 1px, transparent 1px);
  background-size: 44px 44px;
  content: '';
  opacity: 0.62;
  pointer-events: none;
}

.page-head {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(280px, 360px);
  gap: 30px;
  align-items: end;
  margin-bottom: 28px;
  padding-top: 22px;
}

.head-copy,
.preview-copy,
.continue-copy {
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
  max-width: 680px;
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

.discover-link,
.section-link {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  min-height: 36px;
  color: var(--primary-color);
  font-size: 13px;
  font-weight: 750;
}

.head-meta .discover-link {
  width: 100%;
  justify-content: space-between;
  padding: 12px 0;
  color: var(--muted-text-color);
}

.discover-link:hover,
.section-link:hover {
  color: var(--primary-hover-color);
}

.discover-link:focus-visible,
.section-link:focus-visible,
.timeline-item:focus-within,
.favorite-line:focus-within {
  border-radius: var(--radius-sm);
  outline: 2px solid var(--primary-color);
  outline-offset: 3px;
}

.error-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 10px;
  align-items: center;
  margin-top: 22px;
}

.overview-skeleton {
  padding: 28px 2px;
}

.sr-only,
.sr-only-heading {
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

.section-title,
.section-heading {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 18px;
}

.section-title {
  margin: 26px 0 16px;
}

.section-title h2,
.section-heading h2 {
  margin: 0;
  color: var(--text-color);
  font-family: Georgia, "Times New Roman", serif;
  font-size: clamp(26px, 3.4vw, 42px);
  font-weight: 500;
  line-height: 1.08;
}

.section-title p {
  max-width: 420px;
  margin: 0 0 4px;
  color: var(--muted-text-color);
  font-size: 14px;
  line-height: 1.7;
  text-align: right;
}

.section-heading {
  margin-bottom: 14px;
}

.section-heading h2 {
  font-size: 22px;
}

.reading-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.06fr) minmax(300px, 0.94fr);
  gap: 22px;
  align-items: start;
}

.continue-panel,
.timeline-item,
.favorite-line {
  position: relative;
  min-width: 0;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background: color-mix(in srgb, var(--panel-bg) 96%, transparent);
  transition:
    transform 0.22s ease,
    border-color 0.22s ease,
    background-color 0.22s ease,
    box-shadow 0.22s ease;
}

.continue-panel {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  min-height: 286px;
  overflow: hidden;
  box-shadow: var(--shadow-md);
}

.continue-panel::before,
.timeline-item::before,
.favorite-line::before {
  position: absolute;
  top: 14px;
  bottom: 14px;
  left: -1px;
  width: 3px;
  border-radius: 999px;
  background: linear-gradient(180deg, var(--primary-color), var(--accent-color));
  content: '';
  opacity: 0;
  pointer-events: none;
  transform: scaleY(0.55);
  transform-origin: center;
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.continue-panel::after {
  position: absolute;
  right: 28px;
  bottom: 28px;
  width: 154px;
  height: 154px;
  border: 1px solid color-mix(in srgb, var(--primary-color) 24%, transparent);
  border-radius: 999px;
  content: '';
  opacity: 0.72;
  pointer-events: none;
}

.continue-panel.has-cover {
  grid-template-columns: minmax(220px, 36%) minmax(0, 1fr);
}

.continue-panel:not(.is-unavailable):hover,
.timeline-item:not(.is-unavailable):hover,
.favorite-line:not(.is-unavailable):hover {
  transform: translateY(-2px);
  border-color: color-mix(in srgb, var(--primary-color) 72%, var(--border-color));
  background: var(--panel-bg);
  box-shadow: var(--shadow-sm), 0 18px 42px var(--theme-glow-color);
}

.continue-panel:not(.is-unavailable):hover::before,
.timeline-item:not(.is-unavailable):hover::before,
.favorite-line:not(.is-unavailable):hover::before {
  opacity: 1;
  transform: scaleY(1);
}

.continue-cover {
  width: 100%;
  height: 100%;
  min-height: 286px;
  object-fit: cover;
}

.continue-copy {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 30px;
}

.status-pill {
  display: inline-flex;
  width: max-content;
  min-height: 28px;
  align-items: center;
  margin-bottom: 14px;
  padding: 4px 10px;
  border: 1px solid color-mix(in srgb, var(--primary-color) 34%, var(--border-color));
  border-radius: 999px;
  color: var(--primary-color);
  font-size: 12px;
  font-weight: 800;
}

.status-pill.muted {
  border-color: color-mix(in srgb, var(--muted-text-color) 30%, var(--border-color));
  color: var(--muted-text-color);
}

.continue-title,
.preview-title,
.title-snapshot {
  overflow-wrap: anywhere;
}

.continue-title {
  max-width: 620px;
  margin: 0;
  color: var(--text-color);
  font-family: Georgia, "Times New Roman", serif;
  font-size: clamp(30px, 4vw, 48px);
  font-weight: 500;
  line-height: 1.08;
}

.continue-summary {
  display: -webkit-box;
  max-width: 560px;
  margin: 14px 0 18px;
  overflow: hidden;
  color: var(--muted-text-color);
  font-size: 14px;
  line-height: 1.75;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
}

.continue-time {
  margin-top: auto;
  color: var(--muted-text-color);
  font-size: 12px;
}

.continue-panel.is-unavailable,
.timeline-item.is-unavailable,
.favorite-line.is-unavailable {
  border-color: color-mix(in srgb, var(--muted-text-color) 30%, var(--soft-border-color));
  background: color-mix(in srgb, var(--muted-text-color) 7%, var(--panel-bg));
}

.continue-panel.is-unavailable {
  min-height: 170px;
}

.unavailable-copy {
  align-items: flex-start;
  gap: 10px;
}

.unavailable-copy .continue-time {
  margin-top: 0;
}

.title-snapshot {
  color: color-mix(in srgb, var(--muted-text-color) 84%, var(--text-color));
}

.unavailable-copy .title-snapshot {
  margin: 0;
  font-size: 23px;
  line-height: 1.4;
}

.title-snapshot:focus-visible {
  border-radius: 2px;
  outline: 2px solid var(--muted-text-color);
  outline-offset: 3px;
}

.unavailable-note {
  color: var(--text-color);
  font-size: 12px;
  font-weight: 700;
}

.card-open-link {
  position: absolute;
  inset: 0;
  z-index: 1;
  border-radius: inherit;
}

.card-open-link:focus-visible {
  outline: 2px solid var(--primary-color);
  outline-offset: 3px;
}

.continue-panel .card-open-link:focus-visible {
  outline-offset: -3px;
}

.reading-side {
  min-width: 0;
}

.history-timeline {
  position: relative;
  display: grid;
  gap: 12px;
}

.timeline-item {
  padding: 16px 18px 16px 24px;
}

.preview-title {
  margin: 0;
  color: var(--text-color);
  font-size: 17px;
  line-height: 1.45;
}

.preview-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 5px 14px;
  margin-top: 7px;
  color: var(--muted-text-color);
  font-size: 12px;
}

.reading-progress {
  color: var(--accent-color);
  font-weight: 750;
}

.reading-progress-track {
  display: block;
  width: min(100%, 320px);
  height: 7px;
  margin-top: 10px;
  overflow: hidden;
  border-radius: 999px;
  background: color-mix(in srgb, var(--border-color) 70%, transparent);
}

.reading-progress-track i {
  display: block;
  width: var(--reading-progress);
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, var(--primary-color), var(--accent-color), var(--danger-color));
}

.favorite-title {
  margin-top: 34px;
}

.reading-section {
  padding-bottom: 8px;
}

.favorite-index {
  display: grid;
  gap: 12px;
}

.favorite-line {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 14px;
  align-items: center;
  min-height: 104px;
  padding: 18px 20px 18px 24px;
}

.favorite-line.has-cover {
  grid-template-columns: 112px minmax(0, 1fr);
}

.favorite-cover {
  width: 112px;
  height: 80px;
  border-radius: var(--radius-sm);
  object-fit: cover;
}

@media (max-width: 980px) {
  .page-head,
  .reading-layout {
    grid-template-columns: 1fr;
  }

  .head-meta {
    border-left: 0;
    border-top: 1px solid var(--border-color);
    padding-top: 12px;
    padding-left: 0;
  }

  .section-title {
    display: grid;
    align-items: start;
  }

  .section-title p {
    max-width: none;
    text-align: left;
  }
}

@media (max-width: 720px) {
  .reading-main {
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
  .continue-panel.has-cover,
  .favorite-line,
  .favorite-line.has-cover {
    grid-template-columns: minmax(0, 1fr);
  }

  .error-row .el-button {
    justify-self: end;
  }

  .continue-cover {
    height: auto;
    min-height: 0;
    aspect-ratio: 16 / 9;
  }

  .continue-copy,
  .favorite-line {
    padding: 18px;
  }

  .continue-title {
    font-size: 30px;
  }

  .favorite-cover {
    width: 100%;
    height: auto;
    aspect-ratio: 16 / 9;
  }
}

@media (max-width: 480px) {
  .head-meta .discover-link,
  .meta-line {
    grid-template-columns: 1fr;
  }

  .section-title,
  .section-heading {
    align-items: flex-start;
    flex-direction: column;
  }
}

@media (prefers-reduced-motion: reduce) {
  :deep(.el-skeleton.is-animated .el-skeleton__item) {
    animation: none;
  }

  .continue-panel,
  .continue-panel::before,
  .timeline-item,
  .timeline-item::before,
  .favorite-line,
  .favorite-line::before,
  .reading-progress-track i {
    transition: none;
  }

  .continue-panel:not(.is-unavailable):hover,
  .timeline-item:not(.is-unavailable):hover,
  .favorite-line:not(.is-unavailable):hover {
    transform: none;
  }
}
</style>
