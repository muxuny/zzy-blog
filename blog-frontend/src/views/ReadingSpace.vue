<template>
  <div class="layout">
    <AppHeader />
    <main class="reading-main" :aria-busy="loading">
      <header class="page-heading">
        <div class="heading-copy">
          <span class="page-kicker">个人阅读</span>
          <h1>我的阅读</h1>
        </div>
        <RouterLink class="discover-link" to="/">
          <span>发现更多文章</span>
          <el-icon><ArrowRight /></el-icon>
        </RouterLink>
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
        <section class="reading-section last-read-section">
          <div class="section-heading">
            <div>
              <span class="section-label">继续阅读</span>
              <h2>上次阅读</h2>
            </div>
          </div>

          <article
            v-if="overview.lastRead"
            class="last-read"
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
                class="last-read-cover"
                :src="overview.lastRead.coverImage"
                alt=""
              />
              <div class="last-read-copy">
                <h3 class="last-read-title">{{ overview.lastRead.title }}</h3>
                <p v-if="overview.lastRead.summary" class="last-read-summary">
                  {{ overview.lastRead.summary }}
                </p>
                <span class="last-read-time">
                  上次阅读 {{ formatReadingTime(overview.lastRead.lastReadAt) }}
                </span>
              </div>
            </template>

            <template v-else>
              <div class="last-read-copy unavailable-copy">
                <el-tooltip
                  content="该文章暂未公开"
                  placement="top"
                  :trigger="['hover', 'focus']"
                >
                  <h3 class="title-snapshot" tabindex="0">{{ overview.lastRead.title }}</h3>
                </el-tooltip>
                <span class="last-read-time">
                  上次阅读 {{ formatReadingTime(overview.lastRead.lastReadAt) }}
                </span>
                <span class="unavailable-note">该文章暂未公开</span>
              </div>
            </template>
          </article>

          <div v-else class="empty-status" role="status" aria-live="polite">
            <el-empty description="还没有可继续阅读的文章">
              <el-button type="primary" :icon="ArrowRight" @click="goDiscover">
                去发现文章
              </el-button>
            </el-empty>
          </div>
        </section>

        <section class="reading-section recent-section">
          <div class="section-heading">
            <div>
              <span class="section-label">回看轨迹</span>
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
              class="history-preview"
              :class="{ 'is-unavailable': !item.available }"
            >
              <span class="timeline-dot" aria-hidden="true" />
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
                    <span class="unavailable-note">该文章暂未公开</span>
                  </div>
                </div>
              </template>
            </article>
          </div>

          <el-empty v-else description="暂无阅读历史" />
        </section>

        <section class="reading-section favorite-section">
          <div class="section-heading">
            <div>
              <span class="section-label">稍后再看</span>
              <h2>最近收藏</h2>
            </div>
            <RouterLink class="section-link" to="/favorites">
              查看全部 {{ overview.favoriteTotal }}
            </RouterLink>
          </div>

          <div
            v-if="overview.recentFavorites.length"
            class="favorite-grid"
            aria-label="最近收藏文章"
          >
            <article
              v-for="item in overview.recentFavorites"
              :key="item.articleId"
              class="favorite-preview"
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
import { formatReadingTime } from '../utils/readingHistory'

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
  width: min(100%, var(--content-width));
  margin: 0 auto;
  padding: 30px 24px 64px;
}

.page-heading,
.section-heading {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 18px;
}

.page-heading {
  padding-bottom: 22px;
  border-bottom: 1px solid var(--soft-border-color);
}

.heading-copy,
.section-heading > div,
.preview-copy,
.last-read-copy {
  min-width: 0;
}

.page-kicker,
.section-label {
  color: var(--accent-color);
  font-size: 12px;
  font-weight: 800;
}

.page-heading h1 {
  margin: 3px 0 0;
  color: var(--text-color);
  font-size: 30px;
  line-height: 1.25;
}

.discover-link,
.section-link {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  min-height: 36px;
  font-size: 13px;
  font-weight: 750;
}

.discover-link:focus-visible,
.section-link:focus-visible {
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

.reading-section {
  padding: 34px 0 38px;
  border-bottom: 1px solid var(--soft-border-color);
}

.reading-section:last-child {
  border-bottom: 0;
}

.section-heading {
  margin-bottom: 18px;
}

.section-heading h2 {
  margin: 2px 0 0;
  color: var(--text-color);
  font-size: 22px;
  line-height: 1.3;
}

.last-read,
.history-preview,
.favorite-preview {
  position: relative;
  min-width: 0;
}

.last-read {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  min-height: 196px;
  overflow: hidden;
  border: 1px solid var(--soft-border-color);
  border-radius: var(--radius-md);
  background: var(--panel-bg);
  transition: border-color 0.18s ease, box-shadow 0.18s ease, transform 0.18s ease;
}

.last-read.has-cover {
  grid-template-columns: minmax(220px, 36%) minmax(0, 1fr);
}

.last-read:not(.is-unavailable):hover {
  border-color: color-mix(in srgb, var(--primary-color) 36%, var(--soft-border-color));
  box-shadow: var(--shadow-sm);
  transform: translateY(-2px);
}

.last-read-cover {
  width: 100%;
  height: 100%;
  min-height: 196px;
  object-fit: cover;
}

.last-read-copy {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 28px 30px;
}

.last-read-title,
.preview-title,
.title-snapshot {
  overflow-wrap: anywhere;
}

.last-read-title {
  margin: 0;
  color: var(--text-color);
  font-size: 25px;
  line-height: 1.35;
}

.last-read-summary {
  display: -webkit-box;
  margin: 12px 0 18px;
  overflow: hidden;
  color: var(--muted-text-color);
  font-size: 14px;
  line-height: 1.75;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
}

.last-read-time {
  margin-top: auto;
  color: var(--muted-text-color);
  font-size: 12px;
}

.last-read.is-unavailable,
.history-preview.is-unavailable,
.favorite-preview.is-unavailable {
  background: color-mix(in srgb, var(--muted-text-color) 7%, var(--panel-bg));
}

.last-read.is-unavailable {
  min-height: 150px;
  border-color: color-mix(in srgb, var(--muted-text-color) 30%, var(--soft-border-color));
}

.unavailable-copy {
  align-items: flex-start;
  gap: 10px;
}

.unavailable-copy .last-read-time {
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

.last-read .card-open-link:focus-visible {
  outline-offset: -3px;
}

.history-timeline {
  position: relative;
  display: grid;
  gap: 0;
  width: min(100%, 900px);
}

.history-timeline::before {
  position: absolute;
  top: 21px;
  bottom: 21px;
  left: 6px;
  width: 1px;
  background: var(--border-color);
  content: '';
}

.history-preview {
  padding: 14px 16px 15px 32px;
  border-bottom: 1px solid var(--soft-border-color);
  transition: background-color 0.18s ease;
}

.history-preview:last-child {
  border-bottom: 0;
}

.history-preview:not(.is-unavailable):hover {
  background: color-mix(in srgb, var(--primary-color) 6%, transparent);
}

.timeline-dot {
  position: absolute;
  top: 23px;
  left: 0;
  z-index: 2;
  pointer-events: none;
  width: 13px;
  height: 13px;
  border: 3px solid var(--panel-bg);
  border-radius: 50%;
  background: var(--accent-color);
  box-shadow: 0 0 0 1px var(--border-color);
}

.history-preview.is-unavailable .timeline-dot {
  background: var(--muted-text-color);
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

.favorite-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.favorite-preview {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  align-items: center;
  min-height: 116px;
  padding: 18px 20px;
  border: 1px solid var(--soft-border-color);
  border-radius: var(--radius-md);
  background: var(--panel-bg);
  transition: border-color 0.18s ease, box-shadow 0.18s ease;
}

.favorite-preview.has-cover {
  grid-template-columns: 112px minmax(0, 1fr);
  gap: 18px;
}

.favorite-preview:not(.is-unavailable):hover {
  border-color: color-mix(in srgb, var(--primary-color) 36%, var(--soft-border-color));
  box-shadow: var(--shadow-sm);
}

.favorite-preview.is-unavailable {
  border-color: color-mix(in srgb, var(--muted-text-color) 30%, var(--soft-border-color));
}

.favorite-cover {
  width: 112px;
  height: 80px;
  border-radius: var(--radius-sm);
  object-fit: cover;
}

@media (max-width: 720px) {
  .reading-main {
    padding: 22px 14px 44px;
  }

  .page-heading {
    align-items: flex-start;
  }

  .page-heading h1 {
    font-size: 26px;
  }

  .error-row,
  .favorite-grid,
  .last-read.has-cover {
    grid-template-columns: minmax(0, 1fr);
  }

  .error-row .el-button {
    justify-self: end;
  }

  .last-read-cover {
    height: auto;
    min-height: 0;
    aspect-ratio: 16 / 9;
  }

  .last-read-copy {
    padding: 22px;
  }

  .last-read-title {
    font-size: 22px;
  }
}

@media (max-width: 480px) {
  .page-heading,
  .section-heading {
    align-items: flex-start;
    flex-direction: column;
  }

  .reading-section {
    padding: 28px 0 32px;
  }

  .last-read-copy,
  .favorite-preview {
    padding: 16px;
  }

  .favorite-preview.has-cover {
    grid-template-columns: minmax(0, 1fr);
  }

  .favorite-cover {
    width: 100%;
    height: auto;
    aspect-ratio: 16 / 9;
  }
}

@media (prefers-reduced-motion: reduce) {
  :deep(.el-skeleton.is-animated .el-skeleton__item) {
    animation: none;
  }

  .last-read,
  .history-preview,
  .favorite-preview {
    transition: none;
  }

  .last-read:not(.is-unavailable):hover {
    transform: none;
  }
}
</style>
