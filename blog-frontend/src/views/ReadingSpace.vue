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
            :class="[{ 'is-unavailable': !overview.lastRead.available }, { 'is-field-rippling': fieldRippling }]"
            @pointermove="updateReadingField"
            @pointerleave="resetReadingField"
            @pointerdown="triggerReadingRipple"
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

            <div class="magnetic-field" aria-hidden="true">
              <span class="magnetic-ripple" />
              <span class="magnetic-particle is-soft" style="--particle-x: 10%; --particle-y: 62%; --particle-size: 4px; --particle-delay: -0.4s" />
              <span class="magnetic-particle is-mid" style="--particle-x: 22%; --particle-y: 38%; --particle-size: 7px; --particle-delay: -1.8s" />
              <span class="magnetic-particle is-strong" style="--particle-x: 36%; --particle-y: 70%; --particle-size: 5px; --particle-delay: -2.5s" />
              <span class="magnetic-particle is-soft" style="--particle-x: 52%; --particle-y: 46%; --particle-size: 6px; --particle-delay: -1.1s" />
              <span class="magnetic-particle is-mid" style="--particle-x: 68%; --particle-y: 64%; --particle-size: 4px; --particle-delay: -3s" />
              <span class="magnetic-particle is-strong" style="--particle-x: 84%; --particle-y: 34%; --particle-size: 8px; --particle-delay: -2s" />
              <span class="magnetic-streak" style="--streak-x: 16%; --streak-y: 28%; --streak-delay: -0.6s" />
              <span class="magnetic-streak" style="--streak-x: 58%; --streak-y: 76%; --streak-delay: -1.7s" />
              <span class="magnetic-streak" style="--streak-x: 78%; --streak-y: 54%; --streak-delay: -2.4s" />
            </div>
          </article>

          <div
            v-else
            class="continue-panel empty-status"
            :class="{ 'is-field-rippling': fieldRippling }"
            role="status"
            aria-live="polite"
            @pointermove="updateReadingField"
            @pointerleave="resetReadingField"
            @pointerdown="triggerReadingRipple"
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
            <div class="magnetic-field" aria-hidden="true">
              <span class="magnetic-ripple" />
              <span class="magnetic-particle is-soft" style="--particle-x: 12%; --particle-y: 66%; --particle-size: 4px; --particle-delay: -0.4s" />
              <span class="magnetic-particle is-mid" style="--particle-x: 28%; --particle-y: 40%; --particle-size: 6px; --particle-delay: -1.8s" />
              <span class="magnetic-particle is-strong" style="--particle-x: 46%; --particle-y: 74%; --particle-size: 5px; --particle-delay: -2.5s" />
              <span class="magnetic-particle is-soft" style="--particle-x: 62%; --particle-y: 48%; --particle-size: 6px; --particle-delay: -1.1s" />
              <span class="magnetic-particle is-mid" style="--particle-x: 82%; --particle-y: 62%; --particle-size: 5px; --particle-delay: -3s" />
              <span class="magnetic-streak" style="--streak-x: 18%; --streak-y: 30%; --streak-delay: -0.6s" />
              <span class="magnetic-streak" style="--streak-x: 56%; --streak-y: 76%; --streak-delay: -1.7s" />
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
const fieldRippling = ref(false)
let componentActive = true
let requestVersion = 0
let fieldRippleTimer = 0

onMounted(() => {
  void load()
})

onBeforeUnmount(() => {
  componentActive = false
  requestVersion += 1
  if (fieldRippleTimer) window.clearTimeout(fieldRippleTimer)
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

function updateReadingField(event) {
  if (!(event.currentTarget instanceof HTMLElement)) return

  const panel = event.currentTarget
  const rect = panel.getBoundingClientRect()
  const xRatio = rect.width ? (event.clientX - rect.left) / rect.width : 0.5
  const yRatio = rect.height ? (event.clientY - rect.top) / rect.height : 0.68
  const shiftX = Math.max(-1, Math.min(1, (xRatio - 0.5) * 2))
  const shiftY = Math.max(-1, Math.min(1, (yRatio - 0.68) * 2))

  panel.style.setProperty('--field-cursor-x', `${Math.round(xRatio * 100)}%`)
  panel.style.setProperty('--field-cursor-y', `${Math.round(yRatio * 100)}%`)
  panel.style.setProperty('--field-shift-x', `${(shiftX * 16).toFixed(1)}px`)
  panel.style.setProperty('--field-shift-y', `${(shiftY * 12).toFixed(1)}px`)
  panel.style.setProperty('--field-shift-x-soft', `${(shiftX * 8).toFixed(1)}px`)
  panel.style.setProperty('--field-shift-y-soft', `${(shiftY * 6).toFixed(1)}px`)
  panel.style.setProperty('--field-shift-x-strong', `${(shiftX * 24).toFixed(1)}px`)
  panel.style.setProperty('--field-shift-y-strong', `${(shiftY * 18).toFixed(1)}px`)
}

function resetReadingField(event) {
  if (!(event.currentTarget instanceof HTMLElement)) return
  setReadingFieldDefaults(event.currentTarget)
}

function triggerReadingRipple(event) {
  updateReadingField(event)
  fieldRippling.value = false

  window.requestAnimationFrame(() => {
    fieldRippling.value = true
    if (fieldRippleTimer) window.clearTimeout(fieldRippleTimer)
    fieldRippleTimer = window.setTimeout(() => {
      fieldRippling.value = false
      fieldRippleTimer = 0
    }, 520)
  })
}

function setReadingFieldDefaults(panel) {
  panel.style.setProperty('--field-cursor-x', '50%')
  panel.style.setProperty('--field-cursor-y', '68%')
  panel.style.setProperty('--field-shift-x', '0px')
  panel.style.setProperty('--field-shift-y', '0px')
  panel.style.setProperty('--field-shift-x-soft', '0px')
  panel.style.setProperty('--field-shift-y-soft', '0px')
  panel.style.setProperty('--field-shift-x-strong', '0px')
  panel.style.setProperty('--field-shift-y-strong', '0px')
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
  --field-cursor-x: 50%;
  --field-cursor-y: 68%;
  --field-shift-x: 0px;
  --field-shift-y: 0px;
  --field-shift-x-soft: 0px;
  --field-shift-y-soft: 0px;
  --field-shift-x-strong: 0px;
  --field-shift-y-strong: 0px;
  isolation: isolate;
  display: block;
  min-height: 322px;
  padding: 26px 26px 124px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background:
    radial-gradient(
      circle at var(--field-cursor-x) var(--field-cursor-y),
      color-mix(in srgb, var(--primary-color) 9%, transparent),
      transparent 34%
    ),
    linear-gradient(135deg, color-mix(in srgb, var(--primary-color) 12%, transparent), transparent 58%),
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

.magnetic-field {
  position: absolute;
  inset: 0;
  z-index: 1;
  overflow: hidden;
  border-radius: inherit;
  pointer-events: none;
}

.magnetic-field::before,
.magnetic-field::after {
  position: absolute;
  content: '';
}

.magnetic-field::before {
  inset: 0;
  background:
    radial-gradient(
      circle at var(--field-cursor-x) var(--field-cursor-y),
      color-mix(in srgb, var(--primary-color) 20%, transparent),
      transparent 31%
    ),
    radial-gradient(
      ellipse at 68% 100%,
      color-mix(in srgb, var(--accent-color) 20%, transparent),
      transparent 54%
    ),
    repeating-linear-gradient(
      90deg,
      color-mix(in srgb, var(--border-color) 34%, transparent) 0 1px,
      transparent 1px 38px
    ),
    repeating-linear-gradient(
      0deg,
      color-mix(in srgb, var(--border-color) 20%, transparent) 0 1px,
      transparent 1px 32px
    ),
    linear-gradient(
      180deg,
      transparent,
      color-mix(in srgb, var(--surface-wash-color) 42%, transparent) 58%,
      color-mix(in srgb, var(--panel-bg) 24%, transparent)
    );
  background-position: 0 0, 0 0, 0 0, 0 0, 0 0;
  opacity: 0.78;
  animation: fieldBreath 8.5s ease-in-out infinite;
  transition: background-position 0.2s ease, opacity 0.2s ease;
  -webkit-mask-image: linear-gradient(180deg, rgb(0 0 0 / 10%), rgb(0 0 0 / 38%) 34%, #000 62%);
  mask-image: linear-gradient(180deg, rgb(0 0 0 / 10%), rgb(0 0 0 / 38%) 34%, #000 62%);
}

.magnetic-field::after {
  right: -20%;
  bottom: -32%;
  left: -20%;
  height: 72%;
  background:
    linear-gradient(
      104deg,
      transparent 20%,
      color-mix(in srgb, var(--primary-color) 18%, transparent) 46%,
      color-mix(in srgb, var(--accent-color) 14%, transparent) 54%,
      transparent 76%
    ),
    radial-gradient(
      ellipse at 50% 100%,
      color-mix(in srgb, var(--primary-color) 16%, transparent),
      transparent 62%
    );
  opacity: 0.74;
  transform: translateX(-12%);
  animation: fieldSweep 7.6s ease-in-out infinite;
  -webkit-mask-image: linear-gradient(90deg, transparent, #000 16%, #000 84%, transparent);
  mask-image: linear-gradient(90deg, transparent, #000 16%, #000 84%, transparent);
}

.magnetic-particle {
  --float-x: 10px;
  --float-y: -8px;
  --float-scale: 1;
  position: absolute;
  top: var(--particle-y);
  left: var(--particle-x);
  width: var(--particle-size);
  height: var(--particle-size);
  border: 1px solid color-mix(in srgb, var(--primary-color) 48%, transparent);
  border-radius: 999px;
  background: color-mix(in srgb, var(--primary-color) 42%, var(--panel-bg));
  box-shadow: 0 0 14px color-mix(in srgb, var(--primary-color) 34%, transparent);
  opacity: 0.68;
  translate: var(--field-shift-x) var(--field-shift-y);
  transform: translate3d(0, 0, 0) scale(var(--float-scale));
  animation: magneticFloat 6.2s ease-in-out infinite;
  animation-delay: var(--particle-delay);
  transition:
    opacity 0.22s ease,
    translate 0.22s ease,
    background-color 0.22s ease,
    box-shadow 0.22s ease;
}

.magnetic-particle.is-soft {
  --float-x: 6px;
  --float-y: -5px;
  --float-scale: 0.9;
  opacity: 0.44;
  translate: var(--field-shift-x-soft) var(--field-shift-y-soft);
}

.magnetic-particle.is-strong {
  --float-x: 14px;
  --float-y: -10px;
  --float-scale: 1.08;
  opacity: 0.76;
  translate: var(--field-shift-x-strong) var(--field-shift-y-strong);
}

.magnetic-streak {
  --float-x: 18px;
  --float-y: -4px;
  position: absolute;
  top: var(--streak-y);
  left: var(--streak-x);
  width: 54px;
  height: 1px;
  border-radius: 999px;
  background: linear-gradient(90deg, transparent, var(--accent-color), transparent);
  opacity: 0.34;
  translate: var(--field-shift-x-soft) var(--field-shift-y-soft);
  transform: translate3d(0, 0, 0) rotate(-8deg);
  animation: streakGlide 7.2s ease-in-out infinite;
  animation-delay: var(--streak-delay);
}

.magnetic-ripple {
  position: absolute;
  top: var(--field-cursor-y);
  left: var(--field-cursor-x);
  width: 18px;
  height: 18px;
  border: 1px solid color-mix(in srgb, var(--primary-color) 38%, transparent);
  border-radius: 999px;
  opacity: 0;
  translate: -50% -50%;
  transform: scale(0.2);
}

.continue-panel:not(.is-unavailable):hover .magnetic-field::before,
.empty-status:hover .magnetic-field::before {
  opacity: 1;
}

.continue-panel:not(.is-unavailable):hover .magnetic-particle,
.empty-status:hover .magnetic-particle {
  opacity: 0.9;
  background: color-mix(in srgb, var(--primary-color) 56%, var(--panel-bg));
  box-shadow: 0 0 18px color-mix(in srgb, var(--primary-color) 46%, transparent);
}

.continue-panel.is-field-rippling .magnetic-ripple {
  animation: rippleBurst 0.52s ease-out;
}

.empty-status .magnetic-field {
  opacity: 0.68;
}

@keyframes fieldBreath {
  0%,
  100% {
    background-position: 0 0, 0 0, 0 0, 0 0, 0 0;
    filter: saturate(0.96);
  }

  50% {
    background-position: 0 0, 0 0, 14px 0, 0 10px, 0 0;
    filter: saturate(1.16) brightness(1.03);
  }
}

@keyframes fieldSweep {
  0%,
  100% {
    opacity: 0.52;
    transform: translateX(-14%);
  }

  50% {
    opacity: 0.86;
    transform: translateX(10%);
  }
}

@keyframes magneticFloat {
  0%,
  100% {
    filter: saturate(0.96);
    transform: translate3d(0, 0, 0) scale(var(--float-scale));
  }

  50% {
    filter: saturate(1.22) brightness(1.05);
    transform: translate3d(var(--float-x), var(--float-y), 0) scale(var(--float-scale));
  }
}

@keyframes streakGlide {
  0%,
  100% {
    opacity: 0.24;
    transform: translate3d(-12px, 0, 0) rotate(-8deg);
  }

  50% {
    opacity: 0.5;
    transform: translate3d(14px, -2px, 0) rotate(-8deg);
  }
}

@keyframes rippleBurst {
  0% {
    opacity: 0.45;
    transform: scale(0.2);
  }

  100% {
    opacity: 0;
    transform: scale(8.4);
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
    padding: 20px 20px 106px;
  }

  .magnetic-field {
    position: absolute;
    inset: 0;
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
  .magnetic-field::before,
  .magnetic-field::after,
  .magnetic-particle,
  .magnetic-streak,
  .magnetic-ripple,
  .progress-track span,
  .timeline-item,
  .favorite-line,
  .primary-button,
  .ghost-button {
    animation: none;
    transition: none;
  }

  .magnetic-particle,
  .magnetic-particle.is-soft,
  .magnetic-particle.is-strong,
  .magnetic-streak,
  .magnetic-ripple {
    translate: none;
    transform: none;
  }

  .timeline-item:not(.is-unavailable):hover,
  .timeline-item.is-active {
    transform: none;
  }
}
</style>
