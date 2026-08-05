<template>
  <div class="layout">
    <AppHeader />
    <main class="home-shell">
      <section class="index-hero">
        <div class="hero-copy-block">
          <span class="eyebrow">{{ pageCopy.eyebrow }}</span>
          <h1>{{ pageCopy.title }}</h1>
          <p v-if="pageCopy.description" class="hero-description">{{ pageCopy.description }}</p>
        </div>

        <aside class="signal-panel" aria-label="近 7 天更新">
          <span class="eyebrow">近 7 天更新</span>
          <h2>{{ contentSignal.activeTopic }}</h2>
          <p>{{ contentSignal.summary }}</p>
          <ol class="signal-bars signal-grid" :aria-label="contentSignal.chartLabel">
            <li
              v-for="(day, index) in contentSignal.bars"
              :key="day.key"
              class="signal-day"
              :class="{ 'is-today': day.isToday, 'is-peak': day.isPeak }"
              :title="day.tooltip"
              :aria-label="day.accessibleLabel"
              tabindex="0"
            >
              <span
                class="signal-bar"
                :style="{ '--signal-height': day.height, '--signal-delay': `${index * -260}ms` }"
              />
              <span class="signal-day-label" aria-hidden="true">{{ day.weekdayLabel }}</span>
            </li>
          </ol>
          <div class="signal-chart-note" aria-hidden="true">
            <span>近 7 天</span>
            <span>柱高 = 更新篇数</span>
          </div>
          <button
            v-if="featuredArticle"
            type="button"
            class="signal-caption"
            @click="goArticle(featuredArticle)"
          >
            <span><i aria-hidden="true" />最近更新</span>
            <strong>{{ contentSignal.latestTitle }}</strong>
          </button>
          <div v-else class="signal-caption signal-caption-static">
            <span><i aria-hidden="true" />等待发布</span>
            <strong>暂无公开文章</strong>
          </div>
        </aside>
      </section>

      <section class="toolbar" aria-label="文章筛选">
        <form class="searchbox" @submit.prevent="submitSearch">
          <el-input
            v-model="keywordInput"
            clearable
            size="large"
            placeholder="搜索问题、方案或文章标题"
            @clear="clearSearch"
          />
          <el-button class="search-button" type="primary" size="large" native-type="submit">搜索</el-button>
        </form>

        <div class="filters" aria-label="话题导航">
          <button
            class="filter"
            :class="{ 'is-active': !activeTag }"
            :aria-pressed="!activeTag"
            type="button"
            @click="clearTag"
          >
            全部文章
          </button>
          <button
            v-for="tag in topTags"
            :key="tag.id"
            class="filter"
            :class="{ 'is-active': activeTag === tag.id }"
            :aria-pressed="activeTag === tag.id"
            type="button"
            @click="filterByTag(tag)"
          >
            {{ tag.name }}
          </button>
        </div>
      </section>

      <section class="content-grid">
        <section class="article-section" aria-label="文章流">
          <div class="section-title">
            <div>
              <span class="eyebrow">文章流</span>
              <h2>{{ articleSectionTitle }}</h2>
            </div>
            <p>{{ articleSectionSubtitle }}</p>
          </div>

          <el-skeleton v-if="loading" :rows="8" animated />

          <template v-else>
            <div v-if="streamArticles.length" class="article-stream">
              <article
                v-for="article in streamArticles"
                :key="article.id"
                class="article-entry"
                role="button"
                tabindex="0"
                @click="goArticle(article)"
                @keydown.enter.prevent="goArticle(article)"
                @keydown.space.prevent="goArticle(article)"
              >
                <div class="entry-copy">
                  <div class="article-meta">
                    <span>{{ formatDate(article.updatedAt || article.createdAt) }}</span>
                    <span v-if="article.authorName">{{ article.authorName }}</span>
                    <span v-if="article.tags?.length" class="topic">{{ article.tags[0].name }}</span>
                  </div>
                  <h3>{{ article.title }}</h3>
                  <p>{{ article.summary || truncate(article.content, 140) }}</p>
                  <div class="entry-tags" v-if="article.tags?.length">
                    <button
                      v-for="tag in article.tags"
                      :key="tag.id"
                      class="tag-chip"
                      type="button"
                      @click.stop="filterByTag(tag)"
                      @keydown.enter.stop.prevent="filterByTag(tag)"
                      @keydown.space.stop.prevent="filterByTag(tag)"
                    >
                      {{ tag.name }}
                    </button>
                  </div>
                </div>
                <div class="article-visual" :class="{ 'has-cover': article.coverImage }" aria-hidden="true">
                  <img v-if="article.coverImage" :src="article.coverImage" :alt="article.title" />
                  <span v-else>{{ articleInitial(article) }}</span>
                </div>
              </article>
            </div>

            <el-empty v-if="!articles.length" :description="emptyDescription" />

            <div class="pagination" v-if="total > size">
              <el-pagination
                v-model:current-page="page"
                :total="total"
                :page-size="size"
                layout="prev,pager,next"
                @current-change="load"
              />
            </div>
          </template>
        </section>

        <aside class="side-index" aria-label="首页侧边信息">
          <section v-if="featuredArticle" class="side-section featured-brief">
            <span class="eyebrow">最近更新</span>
            <h2>{{ featuredArticle.title }}</h2>
            <p>{{ featuredArticle.summary || truncate(featuredArticle.content, 96) }}</p>
            <button type="button" class="brief-link" @click="goArticle(featuredArticle)">打开文章</button>
          </section>

          <section v-if="recentArticles.length" class="side-section latest-panel">
            <div class="panel-head">
              <span>入口</span>
              <h2>最近更新</h2>
            </div>
            <ul class="latest-list">
              <li v-for="article in recentArticles" :key="article.id">
                <button type="button" @click="goArticle(article)">
                  <span>{{ formatDate(article.updatedAt || article.createdAt) }}</span>
                  <strong>{{ article.title }}</strong>
                </button>
              </li>
            </ul>
          </section>
        </aside>
      </section>

      <footer class="site-footer">
        <span>ZZY Blog</span>
        <span>© {{ currentYear }}</span>
        <span>把经验写下来，让下一次开始更轻一点。</span>
      </footer>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getArticles } from '../api/article'
import { getTags } from '../api/tag'
import { formatDate, truncate } from '../utils'
import { buildContentSignal } from '../utils/contentSignal'
import { usePageCopyStore } from '../stores/pageCopy'
import AppHeader from '../components/AppHeader.vue'

const router = useRouter()
const pageCopyStore = usePageCopyStore()
const articles = ref([])
const tags = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const activeTag = ref(null)
const activeTagName = ref('')
const keywordInput = ref('')
const keyword = ref('')
const loading = ref(false)
const currentYear = new Date().getFullYear()

const featuredArticle = computed(() => articles.value[0] || null)
const streamArticles = computed(() => articles.value)
const recentArticles = computed(() => articles.value.slice(0, 3))
const topTags = computed(() => tags.value.slice(0, 12))
const contentSignal = computed(() => buildContentSignal({
  articles: articles.value,
  topTags: topTags.value
}))
const pageCopy = computed(() => pageCopyStore.resolveCopy('home.hero'))
const articleSectionTitle = computed(() => (activeTagName.value || keyword.value ? '筛选结果' : '最近记录'))
const articleSectionSubtitle = computed(() => {
  if (keyword.value) return `正在查找与“${keyword.value}”有关的文章`
  if (activeTagName.value) return `正在查看“${activeTagName.value}”话题下的公开文章`
  return '按发布时间整理的公开内容'
})
const emptyDescription = computed(() => {
  if (keyword.value) return '没有找到匹配文章'
  if (activeTagName.value) return '这个话题下还没有公开文章'
  return '还没有公开文章'
})

void pageCopyStore.loadPublicCopies()

onMounted(async () => {
  const r = await getTags()
  tags.value = r.data || []
  await load()
})

async function load() {
  loading.value = true
  const params = { page: page.value, size: size.value }
  if (activeTag.value) params.tagId = activeTag.value
  if (keyword.value) params.keyword = keyword.value
  const r = await getArticles(params)
  articles.value = r.data || []
  total.value = r.total || 0
  loading.value = false
}

function filterByTag(tag) {
  if (activeTag.value === tag.id) {
    clearTag()
    return
  }
  activeTag.value = tag.id
  activeTagName.value = tag.name
  page.value = 1
  load()
}

function clearTag() {
  activeTag.value = null
  activeTagName.value = ''
  page.value = 1
  load()
}

function submitSearch() {
  keyword.value = keywordInput.value.trim()
  page.value = 1
  load()
}

function clearSearch() {
  keywordInput.value = ''
  keyword.value = ''
  page.value = 1
  load()
}

function goArticle(article) {
  router.push(`/article/${article.id}`)
}

function articleInitial(article) {
  return article?.title?.trim()?.charAt(0) || 'B'
}
</script>

<style scoped>
.home-shell {
  width: min(1180px, calc(100% - 36px));
  margin: 0 auto;
  padding: 22px 0 72px;
}

.index-hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(280px, 360px);
  gap: 30px;
  align-items: end;
  margin-bottom: 28px;
  padding-top: 22px;
}

.hero-copy-block {
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

.index-hero h1 {
  max-width: 820px;
  margin: 0 0 18px;
  color: var(--text-color);
  font-family: Georgia, "Times New Roman", serif;
  font-size: clamp(42px, 7vw, 82px);
  font-weight: 500;
  line-height: 0.98;
}

.hero-description {
  max-width: 680px;
  margin: 0;
  color: var(--muted-text-color);
  font-size: 17px;
  line-height: 1.85;
}

.signal-panel,
.side-section {
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background: color-mix(in srgb, var(--panel-bg) 94%, transparent);
  box-shadow: var(--shadow-sm);
}

.signal-panel {
  position: relative;
  min-height: 240px;
  overflow: hidden;
  padding: 22px;
}

.signal-panel::before {
  position: absolute;
  inset: 0;
  background:
    repeating-linear-gradient(90deg, transparent 0 22px, var(--theme-grid-x) 22px 23px),
    linear-gradient(135deg, color-mix(in srgb, var(--accent-color) 12%, transparent), transparent 46%);
  content: '';
  opacity: 0.86;
  pointer-events: none;
}

.signal-panel > * {
  position: relative;
}

.signal-panel h2 {
  margin: 0 0 10px;
  color: var(--text-color);
  font-family: Georgia, "Times New Roman", serif;
  font-size: clamp(28px, 4vw, 44px);
  font-weight: 500;
  line-height: 1.08;
}

.signal-panel p {
  margin: 0;
  color: var(--muted-text-color);
  line-height: 1.7;
}

.signal-bars {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 10px;
  height: 150px;
  margin: 20px 0 0;
  padding: 0;
  list-style: none;
}

.signal-day {
  display: grid;
  grid-template-rows: minmax(0, 1fr) auto;
  gap: 8px;
  align-items: end;
  min-width: 0;
  height: 100%;
  cursor: help;
}

.signal-bar {
  display: block;
  width: 100%;
  height: var(--signal-height);
  min-height: 26px;
  border-radius: 999px 999px 4px 4px;
  background: linear-gradient(
    180deg,
    color-mix(in srgb, var(--accent-color) 90%, transparent),
    color-mix(in srgb, var(--primary-color) 92%, transparent)
  );
  transform-origin: bottom;
  animation: signal-breathe 3.6s ease-in-out infinite;
  animation-delay: var(--signal-delay);
}

.signal-day.is-peak .signal-bar {
  background: linear-gradient(
    180deg,
    color-mix(in srgb, var(--warning-color) 86%, var(--accent-color)),
    color-mix(in srgb, var(--primary-color) 82%, var(--warning-color))
  );
  box-shadow: 0 12px 28px color-mix(in srgb, var(--warning-color) 18%, transparent);
}

.signal-day-label {
  display: block;
  color: color-mix(in srgb, var(--muted-text-color) 84%, transparent);
  font-size: 11px;
  font-weight: 700;
  line-height: 1;
  text-align: center;
}

.signal-day.is-today .signal-day-label {
  color: var(--primary-color);
}

.signal-day:hover .signal-bar,
.signal-day:focus-visible .signal-bar {
  filter: saturate(1.16);
}

.signal-day:focus-visible {
  border-radius: 999px 999px var(--radius-sm) var(--radius-sm);
  outline: 2px solid var(--primary-color);
  outline-offset: 3px;
}

.signal-chart-note {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-top: 8px;
  color: color-mix(in srgb, var(--muted-text-color) 74%, transparent);
  font-size: 11px;
  line-height: 1.2;
}

.signal-caption {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  width: 100%;
  margin-top: 18px;
  padding: 0;
  border: 0;
  background: transparent;
  color: var(--muted-text-color);
  font: inherit;
  font-size: 13px;
  text-align: left;
  cursor: pointer;
}

.signal-caption span {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-width: max-content;
}

.signal-caption i {
  display: inline-flex;
  width: 8px;
  height: 8px;
  border-radius: 999px;
  background: var(--danger-color);
  box-shadow: 0 0 0 6px color-mix(in srgb, var(--danger-color) 18%, transparent);
}

.signal-caption strong {
  min-width: 0;
  overflow: hidden;
  color: var(--text-color);
  font-size: 13px;
  font-weight: 760;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.signal-caption:hover strong,
.signal-caption:focus-visible strong {
  color: var(--primary-color);
}

.signal-caption-static {
  cursor: default;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 22px;
}

.searchbox {
  display: grid;
  grid-template-columns: minmax(240px, 1fr) auto;
  flex: 1;
  gap: 10px;
  min-width: 280px;
}

.searchbox :deep(.el-input__wrapper) {
  min-height: 42px;
  border-radius: 999px;
  background: color-mix(in srgb, var(--panel-bg) 94%, transparent);
  box-shadow: 0 0 0 1px var(--border-color) inset;
  transition: background-color 0.22s ease, box-shadow 0.22s ease;
}

.searchbox :deep(.el-input__wrapper.is-focus) {
  background: var(--panel-bg);
  box-shadow: 0 0 0 1px color-mix(in srgb, var(--accent-color) 80%, var(--border-color)) inset,
    0 0 0 4px color-mix(in srgb, var(--accent-color) 14%, transparent);
}

.search-button {
  min-width: 86px;
  min-height: 42px;
  border-radius: 999px;
}

.filters {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}

.filter,
.tag-chip,
.brief-link {
  min-height: 34px;
  border: 1px solid var(--border-color);
  border-radius: 999px;
  background: color-mix(in srgb, var(--panel-bg) 92%, transparent);
  color: var(--muted-text-color);
  font: inherit;
  cursor: pointer;
  transition:
    transform 0.22s ease,
    border-color 0.22s ease,
    background-color 0.22s ease,
    color 0.22s ease,
    box-shadow 0.22s ease;
}

.filter {
  padding: 0 14px;
}

.filter:hover,
.filter.is-active,
.tag-chip:hover,
.tag-chip:focus-visible,
.brief-link:hover,
.brief-link:focus-visible {
  transform: translateY(-1px);
  border-color: color-mix(in srgb, var(--primary-color) 72%, var(--border-color));
  background: var(--surface-wash-color);
  color: var(--primary-color);
  box-shadow: var(--shadow-sm);
}

.content-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 22px;
  align-items: start;
}

.article-section {
  min-width: 0;
}

.section-title {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 14px;
}

.section-title h2 {
  margin: 0;
  color: var(--text-color);
  font-family: Georgia, "Times New Roman", serif;
  font-size: clamp(28px, 4vw, 44px);
  font-weight: 500;
  line-height: 1.08;
}

.section-title p {
  max-width: 360px;
  margin: 0 0 4px;
  color: var(--muted-text-color);
  font-size: 14px;
  line-height: 1.7;
  text-align: right;
}

.article-stream {
  display: grid;
  gap: 12px;
}

.article-entry {
  position: relative;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 110px;
  gap: 12px;
  min-height: 150px;
  padding: 22px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background: color-mix(in srgb, var(--panel-bg) 96%, transparent);
  cursor: pointer;
  transition:
    transform 0.22s ease,
    border-color 0.22s ease,
    background-color 0.22s ease,
    box-shadow 0.22s ease;
}

.article-entry::before {
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
  transform-origin: center;
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.article-entry:hover,
.article-entry:focus-visible {
  transform: translateY(-2px);
  border-color: color-mix(in srgb, var(--primary-color) 72%, var(--border-color));
  background: var(--panel-bg);
  box-shadow: var(--shadow-sm);
}

.article-entry:hover::before,
.article-entry:focus-visible::before {
  opacity: 1;
  transform: scaleY(1);
}

.article-entry:focus-visible,
.filter:focus-visible,
.tag-chip:focus-visible,
.brief-link:focus-visible,
.signal-caption:focus-visible,
.latest-list button:focus-visible {
  outline: 2px solid var(--primary-color);
  outline-offset: 2px;
}

.entry-copy {
  min-width: 0;
}

.article-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 14px;
  margin-bottom: 8px;
  color: var(--muted-text-color);
  font-size: 13px;
}

.topic {
  color: var(--primary-color);
  font-weight: 760;
}

.article-entry h3 {
  margin: 0 0 8px;
  color: var(--text-color);
  font-size: 21px;
  line-height: 1.35;
  font-weight: 780;
}

.article-entry p {
  display: -webkit-box;
  margin: 0;
  overflow: hidden;
  color: var(--muted-text-color);
  font-size: 14px;
  line-height: 1.7;
  text-overflow: ellipsis;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.entry-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
  margin-top: 14px;
}

.tag-chip {
  min-height: 28px;
  padding: 0 11px;
  font-size: 12px;
  font-weight: 650;
}

.article-visual {
  position: relative;
  min-height: 106px;
  overflow: hidden;
  border-radius: var(--radius-md);
  background:
    linear-gradient(135deg, color-mix(in srgb, var(--primary-color) 26%, transparent), transparent),
    repeating-linear-gradient(180deg, color-mix(in srgb, var(--accent-color) 20%, transparent) 0 4px, transparent 4px 12px),
    var(--code-bg);
}

.article-visual::after {
  position: absolute;
  top: 0;
  left: -30%;
  width: 34%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.36), transparent);
  content: '';
  transform: skewX(-18deg);
  transition: left 0.52s ease;
}

.article-entry:hover .article-visual::after,
.article-entry:focus-visible .article-visual::after {
  left: 105%;
}

.article-visual span {
  position: absolute;
  right: 12px;
  bottom: 4px;
  color: color-mix(in srgb, var(--primary-color) 66%, transparent);
  font-family: Georgia, "Times New Roman", serif;
  font-size: 64px;
  font-weight: 500;
  line-height: 1;
}

.article-visual.has-cover img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.side-index {
  position: sticky;
  top: calc(var(--app-header-height) + 20px);
  display: grid;
  gap: 12px;
}

.side-section {
  padding: 18px;
}

.featured-brief {
  display: grid;
  gap: 10px;
}

.featured-brief h2,
.panel-head h2 {
  margin: 0;
  color: var(--text-color);
  font-size: 19px;
  line-height: 1.35;
  font-weight: 760;
}

.featured-brief p {
  margin: 0;
  color: var(--muted-text-color);
  line-height: 1.7;
}

.brief-link {
  justify-self: start;
  padding: 0 14px;
  color: var(--primary-color);
  font-weight: 700;
}

.panel-head {
  margin-bottom: 14px;
}

.panel-head span {
  color: var(--accent-color);
  font-size: 12px;
  font-weight: 760;
}

.latest-list {
  display: grid;
  gap: 8px;
  list-style: none;
}

.latest-list button {
  display: grid;
  width: 100%;
  gap: 4px;
  padding: 11px 12px;
  border: 1px solid transparent;
  border-radius: var(--radius-sm);
  background: color-mix(in srgb, var(--bg-color) 82%, var(--panel-bg));
  color: inherit;
  text-align: left;
  cursor: pointer;
  transition: border-color 0.18s ease, background-color 0.18s ease, transform 0.18s ease;
}

.latest-list button:hover {
  transform: translateX(2px);
  border-color: color-mix(in srgb, var(--primary-color) 30%, transparent);
  background: color-mix(in srgb, var(--primary-color) 7%, var(--panel-bg));
}

.latest-list span {
  color: var(--muted-text-color);
  font-size: 12px;
}

.latest-list strong {
  color: var(--text-color);
  font-size: 14px;
  line-height: 1.45;
  font-weight: 760;
}

.pagination {
  display: flex;
  justify-content: center;
  margin: 28px 0 0;
}

.site-footer {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px 16px;
  margin-top: 38px;
  padding-top: 22px;
  border-top: 1px solid var(--soft-border-color);
  color: var(--muted-text-color);
  font-size: 13px;
}

@keyframes signal-breathe {
  0%,
  100% {
    filter: saturate(0.92);
    transform: scaleY(0.92);
  }

  45% {
    filter: saturate(1.12);
    transform: scaleY(1.06);
  }
}

@media (max-width: 980px) {
  .index-hero,
  .content-grid {
    grid-template-columns: 1fr;
  }

  .toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .filters {
    justify-content: flex-start;
  }

  .side-index {
    position: static;
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .home-shell {
    width: min(100% - 28px, var(--content-width));
    padding: 16px 0 44px;
  }

  .index-hero {
    gap: 22px;
    padding-top: 14px;
  }

  .index-hero h1 {
    font-size: 42px;
  }

  .toolbar,
  .searchbox,
  .section-title,
  .article-entry,
  .side-index {
    grid-template-columns: 1fr;
  }

  .searchbox {
    min-width: 0;
  }

  .section-title {
    display: grid;
    align-items: start;
  }

  .section-title p {
    max-width: none;
    text-align: left;
  }

  .article-entry {
    padding: 18px;
  }

  .article-visual {
    min-height: 118px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .signal-bar {
    animation: none;
  }

  .article-entry,
  .article-entry::before,
  .article-visual::after,
  .filter,
  .tag-chip,
  .brief-link,
  .latest-list button {
    transition: none;
  }

  .article-entry:hover,
  .article-entry:focus-visible,
  .filter:hover,
  .filter.is-active,
  .tag-chip:hover,
  .tag-chip:focus-visible,
  .brief-link:hover,
  .brief-link:focus-visible,
  .latest-list button:hover {
    transform: none;
  }
}
</style>
