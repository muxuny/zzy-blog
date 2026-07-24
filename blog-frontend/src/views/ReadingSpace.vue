<template>
  <div class="layout">
    <AppHeader />
    <main class="reading-main" :aria-busy="loading">
      <header class="page-head">
        <div class="head-copy">
          <span class="eyebrow">Reading desk</span>
          <h1>我的阅读更像一个安静的续接台。</h1>
          <p>
            这里不负责发现热门内容，只负责把读者和自己的阅读轨迹接起来。轻微动态集中在继续阅读和历史焦点上。
          </p>
        </div>
        <aside class="head-meta" aria-label="阅读摘要">
          <div class="meta-line">
            <span class="meta-label">最近阅读</span>
            <span class="meta-value">
              {{ overview.lastRead ? formatReadingOverviewTime(overview.lastRead.lastReadAt) : '暂无记录' }}
            </span>
          </div>
          <div class="meta-line">
            <span class="meta-label">历史文章</span>
            <span class="meta-value">{{ overview.historyTotal }} 篇</span>
          </div>
          <div class="meta-line">
            <span class="meta-label">收藏</span>
            <span class="meta-value">{{ overview.favoriteTotal }} 篇可继续</span>
          </div>
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
        <section class="reading-layout">
          <article
            v-if="overview.lastRead"
            class="continue-panel"
            :class="[
              { 'is-unavailable': !overview.lastRead.available },
              { 'is-wave-pulsing': wavePulsing }
            ]"
          >
            <RouterLink
              v-if="overview.lastRead.available"
              class="card-open-link"
              :to="`/article/${overview.lastRead.articleId}`"
              :aria-label="`继续阅读：${overview.lastRead.title}`"
            />

            <template v-if="overview.lastRead.available">
              <div class="continue-copy">
                <span class="status-pill">{{ continueStatusText(overview.lastRead) }}</span>
                <h2 class="continue-title">{{ overview.lastRead.title }}</h2>
                <p class="continue-summary">
                  {{ continueSummary(overview.lastRead) }}
                </p>
                <div class="progress-block">
                  <div class="progress-label">
                    <span>当前进度</span>
                    <strong>{{ safeProgressPercent(overview.lastRead.progressPercent) }}%</strong>
                  </div>
                  <div
                    class="progress-track"
                    aria-hidden="true"
                    :style="{ '--progress': `${safeProgressPercent(overview.lastRead.progressPercent)}%` }"
                  >
                    <span />
                  </div>
                </div>
                <div class="continue-actions">
                  <RouterLink class="primary-button" :to="`/article/${overview.lastRead.articleId}`">
                    继续阅读
                  </RouterLink>
                  <RouterLink class="ghost-button" to="/reading/history">
                    查看完整历史
                  </RouterLink>
                </div>
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
                  <h2 class="title-snapshot" tabindex="0">{{ overview.lastRead.title }}</h2>
                </el-tooltip>
                <p class="continue-summary">
                  这篇文章暂时无法继续打开，但阅读轨迹仍会保留。上次阅读 {{ formatReadingTime(overview.lastRead.lastReadAt) }}。
                </p>
                <div class="progress-block">
                  <div class="progress-label">
                    <span>保留进度</span>
                    <strong>{{ safeProgressPercent(overview.lastRead.progressPercent) }}%</strong>
                  </div>
                  <div
                    class="progress-track"
                    aria-hidden="true"
                    :style="{ '--progress': `${safeProgressPercent(overview.lastRead.progressPercent)}%` }"
                  >
                    <span />
                  </div>
                </div>
                <span class="unavailable-note">该文章暂未公开</span>
                <div class="continue-actions">
                  <RouterLink class="ghost-button" to="/reading/history">
                    查看完整历史
                  </RouterLink>
                  <RouterLink class="ghost-button" to="/">
                    发现更多文章
                  </RouterLink>
                </div>
              </div>
            </template>

            <div
              class="signal-wave"
              aria-hidden="true"
              @pointermove.stop="moveSignalWave"
              @pointerleave.stop="resetSignalWave"
              @pointerdown.stop.prevent="pulseSignalWave"
            >
              <svg class="wave-svg" viewBox="0 0 640 120" preserveAspectRatio="none" focusable="false">
                <path
                  class="wave-path is-glow"
                  pathLength="100"
                  d="M4 70 C 58 46, 84 45, 124 66 S 202 89, 250 56 322 28, 374 58 456 96, 520 58 590 30, 636 58"
                />
                <path
                  class="wave-path is-core"
                  pathLength="100"
                  d="M4 70 C 58 46, 84 45, 124 66 S 202 89, 250 56 322 28, 374 58 456 96, 520 58 590 30, 636 58"
                />
                <path
                  class="wave-path is-echo"
                  pathLength="100"
                  d="M4 82 C 66 68, 100 82, 148 92 S 236 80, 282 72 356 80, 410 92 488 84, 540 70 604 68, 636 76"
                />
              </svg>
              <span class="wave-spark" />
            </div>
          </article>

          <div
            v-else
            class="continue-panel empty-status"
            :class="{ 'is-wave-pulsing': wavePulsing }"
            role="status"
            aria-live="polite"
          >
            <div class="continue-copy">
              <span class="status-pill muted">暂无轨迹</span>
              <h2 class="continue-title">还没有可继续阅读的文章</h2>
              <p class="continue-summary">先从公开首页打开一篇文章，下一次这里会直接接上你的阅读现场。</p>
              <div class="continue-actions">
                <RouterLink class="primary-button" to="/">
                  发现更多文章
                </RouterLink>
                <RouterLink class="ghost-button" to="/reading/history">
                  查看完整历史
                </RouterLink>
              </div>
            </div>
            <div
              class="signal-wave"
              aria-hidden="true"
              @pointermove.stop="moveSignalWave"
              @pointerleave.stop="resetSignalWave"
              @pointerdown.stop.prevent="pulseSignalWave"
            >
              <svg class="wave-svg" viewBox="0 0 640 120" preserveAspectRatio="none" focusable="false">
                <path
                  class="wave-path is-glow"
                  pathLength="100"
                  d="M4 70 C 58 46, 84 45, 124 66 S 202 89, 250 56 322 28, 374 58 456 96, 520 58 590 30, 636 58"
                />
                <path
                  class="wave-path is-core"
                  pathLength="100"
                  d="M4 70 C 58 46, 84 45, 124 66 S 202 89, 250 56 322 28, 374 58 456 96, 520 58 590 30, 636 58"
                />
                <path
                  class="wave-path is-echo"
                  pathLength="100"
                  d="M4 82 C 66 68, 100 82, 148 92 S 236 80, 282 72 356 80, 410 92 488 84, 540 70 604 68, 636 76"
                />
              </svg>
              <span class="wave-spark" />
            </div>
          </div>

          <aside class="reading-side">
            <section class="section-title side-title">
              <div>
                <span class="eyebrow">最近历史</span>
                <h3>时间线</h3>
              </div>
              <RouterLink class="section-link" to="/reading/history">
                全部 {{ overview.historyTotal }}
              </RouterLink>
            </section>

            <div
              v-if="overview.recentHistory.length"
              class="timeline"
              aria-label="最近阅读记录"
            >
              <article
                v-for="(item, index) in overview.recentHistory"
                :key="item.articleId"
                class="timeline-item"
                :class="{ 'is-active': index === 0, 'is-unavailable': !item.available }"
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
                    <p class="preview-meta">
                      <span>{{ formatReadingTime(item.lastReadAt) }}</span>
                      <span v-if="formatReadingProgress(item.progressPercent)">
                        {{ formatReadingProgress(item.progressPercent) }}
                      </span>
                      <span v-if="item.authorName">{{ item.authorName }}</span>
                    </p>
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
                    <p class="preview-meta">
                      <span>{{ formatReadingTime(item.lastReadAt) }}</span>
                      <span v-if="formatReadingProgress(item.progressPercent)">
                        {{ formatReadingProgress(item.progressPercent) }}
                      </span>
                      <span class="unavailable-note">该文章暂未公开</span>
                    </p>
                  </div>
                </template>
              </article>
            </div>

            <div v-else class="timeline empty-timeline" role="status">
              <article class="timeline-item is-empty">
                <h3 class="preview-title">暂无阅读历史</h3>
                <p class="preview-meta">阅读公开文章后，这里会出现最近轨迹。</p>
              </article>
            </div>
          </aside>
        </section>

        <section class="section-title favorite-title">
          <div>
            <span class="eyebrow">最近收藏</span>
            <h3>最近收藏索引</h3>
          </div>
          <RouterLink class="section-link" to="/favorites">
            全部 {{ overview.favoriteTotal }}
          </RouterLink>
        </section>

        <section
          v-if="overview.recentFavorites.length"
          class="favorite-index"
          aria-label="最近收藏文章"
        >
          <article
            v-for="item in overview.recentFavorites"
            :key="item.articleId"
            class="favorite-line"
            :class="{ 'is-unavailable': !item.available }"
          >
            <RouterLink
              v-if="item.available"
              class="card-open-link"
              :to="`/article/${item.articleId}`"
              :aria-label="`打开收藏文章：${item.title}`"
            />

            <template v-if="item.available">
              <div class="preview-copy">
                <strong class="favorite-title-text">{{ item.title }}</strong>
                <p class="preview-meta">
                  <span v-if="item.authorName">{{ item.authorName }}</span>
                  <span>收藏于 {{ formatReadingTime(item.favoritedAt) }}</span>
                </p>
              </div>
              <span class="status-pill">公开</span>
            </template>

            <template v-else>
              <div class="preview-copy">
                <el-tooltip
                  content="该文章暂未公开"
                  placement="top"
                  :trigger="['hover', 'focus']"
                >
                  <strong class="favorite-title-text title-snapshot" tabindex="0">{{ item.title }}</strong>
                </el-tooltip>
                <p class="preview-meta">
                  <span>收藏于 {{ formatReadingTime(item.favoritedAt) }}</span>
                  <span class="unavailable-note">该文章暂未公开</span>
                </p>
              </div>
              <span class="status-pill muted">不可读</span>
            </template>
          </article>
        </section>

        <section v-else class="favorite-index empty-favorites" aria-label="最近收藏">
          <article class="favorite-line is-empty">
            <div class="preview-copy">
              <strong class="favorite-title-text">暂无收藏文章</strong>
              <p class="preview-meta">收藏文章后，会在这里形成轻量索引。</p>
            </div>
            <RouterLink class="ghost-button" to="/">
              去发现文章
            </RouterLink>
          </article>
        </section>
      </template>
    </main>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import AppHeader from '../components/AppHeader.vue'
import { getReadingOverview } from '../api/reading'
import { formatReadingOverviewTime, formatReadingProgress, formatReadingTime } from '../utils/readingHistory'

const overview = ref({
  lastRead: null,
  recentHistory: [],
  historyTotal: 0,
  recentFavorites: [],
  favoriteTotal: 0
})
const loading = ref(false)
const loadError = ref('')
const wavePulsing = ref(false)
let componentActive = true
let requestVersion = 0
let wavePulseTimer = 0

onMounted(() => {
  void load()
})

onBeforeUnmount(() => {
  componentActive = false
  requestVersion += 1
  if (wavePulseTimer) window.clearTimeout(wavePulseTimer)
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

function safeProgressPercent(value) {
  const number = Number(value)
  if (!Number.isFinite(number)) return 0
  return Math.min(100, Math.max(0, Math.round(number)))
}

function moveSignalWave(event) {
  if (!(event.currentTarget instanceof HTMLElement)) return

  const wave = event.currentTarget
  const rect = wave.getBoundingClientRect()
  const xRatio = rect.width ? clampRatio((event.clientX - rect.left) / rect.width) : 0.5
  const yRatio = rect.height ? clampRatio((event.clientY - rect.top) / rect.height) : 0.5
  const lift = (0.5 - yRatio) * 14
  const bend = (xRatio - 0.5) * 16

  wave.style.setProperty('--wave-cursor-x', `${Math.round(xRatio * 100)}%`)
  wave.style.setProperty('--wave-lift', `${lift.toFixed(1)}px`)
  wave.style.setProperty('--wave-bend', `${bend.toFixed(1)}px`)
}

function resetSignalWave(event) {
  if (!(event.currentTarget instanceof HTMLElement)) return
  setSignalWaveDefaults(event.currentTarget)
}

function pulseSignalWave(event) {
  moveSignalWave(event)
  wavePulsing.value = false

  window.requestAnimationFrame(() => {
    wavePulsing.value = true
    if (wavePulseTimer) window.clearTimeout(wavePulseTimer)
    wavePulseTimer = window.setTimeout(() => {
      wavePulsing.value = false
      wavePulseTimer = 0
    }, 680)
  })
}

function setSignalWaveDefaults(wave) {
  wave.style.setProperty('--wave-cursor-x', '50%')
  wave.style.setProperty('--wave-lift', '0px')
  wave.style.setProperty('--wave-bend', '0px')
}

function clampRatio(value) {
  if (!Number.isFinite(value)) return 0.5
  return Math.min(1, Math.max(0, value))
}

function continueStatusText(item) {
  const progressText = formatReadingProgress(item?.progressPercent)
  return progressText || '上次阅读'
}

function continueSummary(item) {
  if (item?.summary) return item.summary
  const timeText = formatReadingTime(item?.lastReadAt)
  return timeText ? `上次阅读停在 ${timeText}，可以从这里直接接上。` : '进入文章后，会提示是否继续上次阅读位置。'
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

.section-link {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  min-height: 36px;
  color: var(--primary-color);
  font-size: 13px;
  font-weight: 750;
}

.section-link:hover {
  color: var(--primary-hover-color);
}

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

.section-title {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 18px;
}

.section-title {
  margin: 26px 0 16px;
}

.section-title h2 {
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

.timeline-item::before {
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

.continue-panel:not(.is-unavailable):hover,
.timeline-item:not(.is-unavailable):hover,
.favorite-line:not(.is-unavailable):hover {
  transform: translateY(-2px);
  border-color: color-mix(in srgb, var(--primary-color) 72%, var(--border-color));
  background: var(--panel-bg);
  box-shadow: var(--shadow-sm), 0 18px 42px var(--theme-glow-color);
}

.timeline-item:not(.is-unavailable):hover::before {
  opacity: 1;
  transform: scaleY(1);
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

.favorite-line {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 14px;
  align-items: center;
  min-height: 104px;
  padding: 18px 20px 18px 24px;
}

@media (max-width: 980px) {
  .page-head,
  .reading-layout {
    grid-template-columns: 1fr;
  }

  .head-meta {
    padding: 16px;
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
  .favorite-line {
    grid-template-columns: minmax(0, 1fr);
  }

  .error-row .el-button {
    justify-self: end;
  }

  .continue-copy,
  .favorite-line {
    padding: 18px;
  }

  .continue-title {
    font-size: 30px;
  }

}

@media (max-width: 480px) {
  .meta-line {
    grid-template-columns: 1fr;
  }

  .section-title {
    align-items: flex-start;
    flex-direction: column;
  }
}

@media (prefers-reduced-motion: reduce) {
  :deep(.el-skeleton.is-animated .el-skeleton__item) {
    animation: none;
  }

  .continue-panel,
  .timeline-item,
  .timeline-item::before,
  .favorite-line {
    transition: none;
  }

  .continue-panel:not(.is-unavailable):hover,
  .timeline-item:not(.is-unavailable):hover,
  .favorite-line:not(.is-unavailable):hover {
    transform: none;
  }
}

/* Prototype-aligned reading desk */
.page-head {
  gap: 42px;
  align-items: end;
  margin-bottom: 34px;
  padding-top: 30px;
}

.page-head h1 {
  max-width: 760px;
  font-size: clamp(54px, 6.2vw, 88px);
  line-height: 0.94;
}

.head-copy p {
  max-width: 760px;
}

.reading-layout {
  grid-template-columns: minmax(0, 1.1fr) minmax(280px, 0.9fr);
  gap: 22px;
  align-items: stretch;
  margin-bottom: 34px;
}

.continue-panel {
  --wave-cursor-x: 50%;
  --wave-lift: 0px;
  --wave-bend: 0px;
  isolation: isolate;
  display: block;
  min-height: 340px;
  padding: 26px 26px 150px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background:
    linear-gradient(135deg, color-mix(in srgb, var(--primary-color) 10%, transparent), transparent 58%),
    color-mix(in srgb, var(--panel-bg) 96%, transparent);
  box-shadow: var(--shadow-md);
}

.continue-panel::after {
  right: auto;
  bottom: 0;
  left: -20%;
  width: 140%;
  height: 1px;
  border: 0;
  border-radius: 0;
  background: linear-gradient(90deg, transparent, var(--primary-color), var(--accent-color), transparent);
  opacity: 0.86;
  transform: translateX(-36%);
  transition: transform 0.6s ease;
}

.continue-panel:not(.is-unavailable):hover {
  transform: none;
  box-shadow: var(--shadow-md);
}

.continue-panel:not(.is-unavailable):hover::after {
  transform: translateX(36%);
}

.continue-copy {
  position: relative;
  z-index: 2;
  display: block;
  padding: 0;
}

.continue-title,
.title-snapshot {
  max-width: 600px;
  margin: 18px 0 14px;
  color: var(--text-color);
  font-family: Georgia, "Times New Roman", serif;
  font-size: clamp(30px, 4vw, 48px);
  font-weight: 500;
  line-height: 1.08;
}

.continue-summary {
  max-width: 540px;
  margin: 0;
  color: var(--muted-text-color);
  font-size: 15px;
  line-height: 1.75;
}

.progress-block {
  position: relative;
  z-index: 2;
  display: grid;
  gap: 8px;
  max-width: 520px;
  margin-top: 22px;
}

.progress-label {
  display: flex;
  justify-content: space-between;
  color: var(--muted-text-color);
  font-size: 13px;
}

.progress-label strong {
  color: var(--text-color);
}

.progress-track {
  height: 6px;
  overflow: hidden;
  border-radius: 999px;
  background: color-mix(in srgb, var(--border-color) 70%, transparent);
}

.progress-track span {
  display: block;
  width: var(--progress);
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, var(--primary-color), var(--accent-color));
  transition: width 0.24s ease;
}

.continue-actions {
  position: relative;
  z-index: 2;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 24px;
}

.signal-wave {
  position: absolute;
  right: 26px;
  bottom: 18px;
  left: 26px;
  z-index: 3;
  height: 112px;
  background: transparent;
  color: var(--primary-color);
  cursor: pointer;
  opacity: 0.88;
  pointer-events: auto;
  touch-action: manipulation;
  user-select: none;
  transition: opacity 0.2s ease;
}

.signal-wave::before,
.signal-wave::after {
  position: absolute;
  right: 0;
  left: 0;
  pointer-events: none;
  content: '';
}

.signal-wave::before {
  bottom: 28px;
  height: 1px;
  border-radius: 999px;
  background: linear-gradient(
    90deg,
    transparent,
    color-mix(in srgb, var(--primary-color) 24%, transparent),
    color-mix(in srgb, var(--accent-color) 22%, transparent),
    transparent
  );
  opacity: 0.48;
}

.signal-wave::after {
  top: 18px;
  bottom: 8px;
  background: radial-gradient(
    ellipse at var(--wave-cursor-x) 56%,
    color-mix(in srgb, var(--accent-color) 18%, transparent),
    transparent 34%
  );
  opacity: 0;
  transition: opacity 0.2s ease;
}

.wave-svg {
  display: block;
  width: 100%;
  height: 100%;
  overflow: visible;
  pointer-events: none;
  transform: translateY(var(--wave-lift));
  transform-origin: 50% 58%;
  transition: transform 0.18s ease;
}

.wave-path {
  fill: none;
  pointer-events: none;
  stroke-linecap: round;
  stroke-linejoin: round;
  transform-box: fill-box;
  transform-origin: center;
  vector-effect: non-scaling-stroke;
  transition:
    opacity 0.18s ease,
    stroke 0.18s ease,
    transform 0.18s ease;
}

.wave-path.is-glow {
  stroke: color-mix(in srgb, var(--primary-color) 70%, var(--accent-color));
  stroke-width: 8px;
  filter: drop-shadow(0 0 10px color-mix(in srgb, var(--primary-color) 44%, transparent))
    drop-shadow(0 0 22px color-mix(in srgb, var(--accent-color) 34%, transparent));
  opacity: 0.26;
  transform: translateY(var(--wave-bend));
}

.wave-path.is-core {
  stroke: color-mix(in srgb, var(--primary-color) 82%, var(--accent-color));
  stroke-width: 2.8px;
  stroke-dasharray: 14 10 1 14;
  stroke-dashoffset: 0;
  filter: drop-shadow(0 0 8px color-mix(in srgb, var(--primary-color) 34%, transparent));
  opacity: 0.9;
  transform: translateY(calc(var(--wave-bend) * -0.28));
}

.wave-path.is-echo {
  stroke: color-mix(in srgb, var(--accent-color) 58%, transparent);
  stroke-width: 1.4px;
  stroke-dasharray: 1 13;
  opacity: 0.46;
  transform: translateY(calc(var(--wave-bend) * 0.34));
}

.wave-spark {
  position: absolute;
  top: 48%;
  left: var(--wave-cursor-x);
  width: 42px;
  height: 42px;
  border-radius: 999px;
  background: radial-gradient(
    circle,
    color-mix(in srgb, #fff 72%, var(--accent-color)) 0 6%,
    color-mix(in srgb, var(--accent-color) 36%, transparent) 7% 32%,
    transparent 68%
  );
  opacity: 0;
  pointer-events: none;
  translate: -50% -50%;
  transform: scale(0.74);
  transition:
    opacity 0.18s ease,
    transform 0.18s ease;
}

.continue-panel:not(.is-unavailable):hover .signal-wave,
.empty-status:hover .signal-wave {
  opacity: 1;
}

.continue-panel:not(.is-unavailable):hover .signal-wave::after,
.empty-status:hover .signal-wave::after {
  opacity: 1;
}

.continue-panel:not(.is-unavailable):hover .wave-path.is-core {
  animation: waveScan 1.45s linear infinite;
}

.continue-panel:not(.is-unavailable):hover .wave-spark,
.empty-status:hover .wave-spark {
  opacity: 0.72;
  transform: scale(1);
}

.continue-panel.is-wave-pulsing .wave-svg {
  animation: wavePulse 0.68s cubic-bezier(0.2, 0.9, 0.22, 1);
}

.continue-panel.is-wave-pulsing .wave-spark {
  animation: waveSpark 0.68s ease-out;
}

.empty-status .signal-wave {
  opacity: 0.78;
}

@keyframes waveScan {
  to {
    stroke-dashoffset: -120;
  }
}

@keyframes wavePulse {
  0%,
  100% {
    transform: translateY(var(--wave-lift)) scaleY(1);
  }

  38% {
    transform: translateY(calc(var(--wave-lift) - 4px)) scaleY(1.28);
  }
}

@keyframes waveSpark {
  0% {
    opacity: 0.78;
    transform: scale(0.5);
  }

  100% {
    opacity: 0;
    transform: scale(2.6);
  }
}

.primary-button,
.ghost-button {
  display: inline-flex;
  min-height: 36px;
  align-items: center;
  justify-content: center;
  padding: 0 16px;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 760;
  text-decoration: none;
}

.primary-button {
  border: 1px solid var(--primary-color);
  background: var(--primary-color);
  color: var(--button-text-color);
}

.ghost-button {
  border: 1px solid var(--border-color);
  background: color-mix(in srgb, var(--panel-bg) 90%, transparent);
  color: var(--muted-text-color);
}

.primary-button:hover,
.primary-button:focus-visible,
.ghost-button:hover,
.ghost-button:focus-visible {
  border-color: color-mix(in srgb, var(--primary-color) 60%, var(--border-color));
  color: var(--primary-color);
  outline: none;
}

.primary-button:hover,
.primary-button:focus-visible {
  color: var(--button-text-color);
  box-shadow: 0 14px 28px -22px var(--theme-glow-color);
}

.reading-side {
  display: grid;
  gap: 16px;
  min-width: 0;
  padding: 20px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background: color-mix(in srgb, var(--panel-bg) 94%, transparent);
  box-shadow: var(--shadow-soft);
}

.side-title {
  margin: 0;
}

.section-title {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 14px;
  margin: 28px 0 14px;
}

.section-title h3 {
  margin: 0;
  color: var(--text-color);
  font-family: Georgia, "Times New Roman", serif;
  font-size: 24px;
  font-weight: 500;
  line-height: 1.18;
}

.section-title p,
.section-title .section-link {
  margin-bottom: 0;
  color: var(--muted-text-color);
  font-size: 14px;
}

.timeline {
  position: relative;
  display: grid;
  gap: 0;
  padding-left: 18px;
  border-left: 1px solid var(--border-color);
}

.timeline-item {
  position: relative;
  min-height: 0;
  padding: 14px 0;
  border: 0;
  border-bottom: 1px solid var(--soft-border-color);
  border-radius: 0;
  background: transparent;
  transition:
    transform 0.18s ease,
    border-color 0.18s ease;
}

.timeline-item:last-child {
  border-bottom: 0;
}

.timeline-item::before {
  top: 21px;
  bottom: auto;
  left: -23px;
  width: 9px;
  height: 9px;
  border: 2px solid var(--panel-bg);
  border-radius: 999px;
  background: var(--primary-color);
  box-shadow: 0 0 0 1px color-mix(in srgb, var(--primary-color) 36%, transparent);
  opacity: 1;
  transform: none;
}

.timeline-item:not(.is-unavailable):hover,
.timeline-item.is-active {
  transform: translateX(4px);
  border-bottom-color: color-mix(in srgb, var(--primary-color) 46%, var(--border-color));
  background: transparent;
  box-shadow: none;
}

.timeline-item:not(.is-unavailable):hover::before,
.timeline-item.is-active::before {
  opacity: 1;
  transform: none;
}

.preview-title,
.favorite-title-text {
  margin: 0;
  color: var(--text-color);
  font-size: 17px;
  line-height: 1.4;
}

.favorite-title-text {
  display: block;
  font-weight: 780;
}

.preview-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 5px 12px;
  margin: 6px 0 0;
  color: var(--muted-text-color);
  font-size: 13px;
  line-height: 1.65;
}

.favorite-title {
  margin-top: 0;
}

.favorite-index {
  display: grid;
  gap: 10px;
  padding: 16px 0;
  border-top: 1px solid var(--border-color);
}

.favorite-line {
  position: relative;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 14px;
  align-items: center;
  min-height: 0;
  padding: 14px 0;
  border: 0;
  border-bottom: 1px solid var(--soft-border-color);
  border-radius: 0;
  background: transparent;
}

.favorite-line:last-child {
  border-bottom: 0;
}

.favorite-line:not(.is-unavailable):hover {
  transform: none;
  border-bottom-color: color-mix(in srgb, var(--primary-color) 46%, var(--border-color));
  background: transparent;
  box-shadow: none;
}

.favorite-line .status-pill {
  margin-bottom: 0;
}

.empty-timeline .timeline-item,
.empty-favorites .favorite-line {
  color: var(--muted-text-color);
}

@media (max-width: 980px) {
  .page-head,
  .reading-layout {
    grid-template-columns: minmax(0, 1fr);
  }

  .head-meta {
    padding: 16px;
  }

  .section-title {
    display: grid;
    align-items: start;
  }

  .section-title p {
    max-width: none;
    text-align: left;
  }

  .reading-side {
    padding: 18px;
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

  .error-row {
    grid-template-columns: minmax(0, 1fr);
  }

  .error-row .el-button {
    justify-self: end;
  }

  .continue-panel {
    min-height: 0;
    padding: 20px 20px 132px;
  }

  .signal-wave {
    right: 18px;
    bottom: 14px;
    left: 18px;
    height: 92px;
  }

  .section-title,
  .favorite-line {
    grid-template-columns: minmax(0, 1fr);
  }
}

@media (max-width: 480px) {
  .meta-line {
    grid-template-columns: 1fr;
  }

  .section-title {
    align-items: flex-start;
    flex-direction: column;
  }
}

@media (prefers-reduced-motion: reduce) {
  .continue-panel::after,
  .wave-path,
  .wave-spark,
  .wave-svg,
  .progress-track span,
  .timeline-item,
  .favorite-line,
  .primary-button,
  .ghost-button {
    animation: none;
    transition: none;
  }

  .wave-path,
  .wave-spark,
  .wave-svg {
    translate: none;
    transform: none;
  }

  .timeline-item:not(.is-unavailable):hover,
  .timeline-item.is-active {
    transform: none;
  }
}
</style>
