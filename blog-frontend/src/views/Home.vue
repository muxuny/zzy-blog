<template>
  <div class="layout">
    <AppHeader />
    <main class="home-shell">
      <section class="hero-board">
        <div class="hero-panel">
          <span class="eyebrow">个人写作库</span>
          <h1>把项目经验写成可以回看的路标。</h1>
          <p class="hero-description">
            这里记录开发实践、阅读笔记和阶段性思考。文章不追求热闹，更关心一个问题从出现到解决的过程。
          </p>

          <form class="hero-search" @submit.prevent="submitSearch">
            <el-input
              v-model="keywordInput"
              clearable
              size="large"
              placeholder="搜索问题、方案或文章标题"
              @clear="clearSearch"
            />
            <el-button class="search-button" type="primary" size="large" native-type="submit">搜索</el-button>
          </form>

          <div class="hero-topics" aria-label="写作方向">
            <span v-for="topic in writingTopics" :key="topic">{{ topic }}</span>
          </div>
        </div>

        <button
          v-if="featuredArticle"
          type="button"
          class="spotlight-card"
          @click="goArticle(featuredArticle)"
        >
          <span class="spotlight-label">最近更新</span>
          <div class="spotlight-visual" :class="{ 'has-image': featuredArticle.coverImage }">
            <img v-if="featuredArticle.coverImage" :src="featuredArticle.coverImage" :alt="featuredArticle.title" />
            <span v-else>{{ featuredInitial }}</span>
          </div>
          <div class="spotlight-meta">
            <span>{{ formatDate(featuredArticle.createdAt) }}</span>
            <span v-if="featuredArticle.authorName">{{ featuredArticle.authorName }}</span>
            <span>阅读 {{ featuredArticle.viewCount || 0 }}</span>
          </div>
          <h2>{{ featuredArticle.title }}</h2>
          <p>{{ featuredArticle.summary || truncate(featuredArticle.content, 110) }}</p>
          <div v-if="featuredTags.length" class="spotlight-tags">
            <span v-for="tag in featuredTags" :key="tag.id">{{ tag.name }}</span>
          </div>
        </button>

        <section v-else class="spotlight-card spotlight-empty">
          <span class="spotlight-label">最近更新</span>
          <div class="spotlight-visual"><span>B</span></div>
          <h2>还没有公开文章</h2>
          <p>发布第一篇文章后，这里会展示最新内容。</p>
        </section>
      </section>

      <section class="signal-strip">
        <div class="signal-card">
          <strong>{{ total }}</strong>
          <span>公开文章</span>
        </div>
        <div class="signal-card">
          <strong>{{ tags.length }}</strong>
          <span>话题标签</span>
        </div>
        <div class="signal-card wide">
          <strong>{{ filterSummary }}</strong>
          <span>当前视图</span>
        </div>
      </section>

      <div class="content-layout">
        <aside class="home-sidebar" aria-label="首页侧边信息">
          <section class="side-panel">
            <div class="panel-head">
              <span>筛选</span>
              <h2>话题导航</h2>
            </div>
            <div class="topic-list" aria-label="话题导航">
              <button
                class="topic-button"
                :class="{ active: !activeTag }"
                :aria-pressed="!activeTag"
                type="button"
                @click="clearTag"
              >
                全部文章
              </button>
              <button
                v-for="tag in topTags"
                :key="tag.id"
                class="topic-button"
                :class="{ active: activeTag === tag.id }"
                :aria-pressed="activeTag === tag.id"
                type="button"
                @click="filterByTag(tag)"
              >
                {{ tag.name }}
              </button>
            </div>
          </section>

          <section class="side-panel direction-panel">
            <div class="panel-head">
              <span>方向</span>
              <h2>写作地图</h2>
            </div>
            <ul>
              <li v-for="topic in writingTopics" :key="topic">
                <span>{{ topic }}</span>
              </li>
            </ul>
          </section>

          <section v-if="featuredArticle" class="side-panel note-panel">
            <span>最新记录</span>
            <p>{{ formatDate(featuredArticle.createdAt) }} 更新，适合从这里开始阅读。</p>
          </section>
        </aside>

        <section class="article-section">
          <div class="section-head">
            <span class="eyebrow">文章流</span>
            <div>
              <h2>{{ articleSectionTitle }}</h2>
              <p>{{ articleSectionSubtitle }}</p>
            </div>
          </div>

          <el-skeleton v-if="loading" :rows="8" animated />

          <template v-else>
            <div v-if="streamArticles.length" class="article-stream">
              <ArticleCard v-for="article in streamArticles" :key="article.id" :article="article" />
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
      </div>

      <footer class="site-footer">
        <span>My Blog</span>
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
import AppHeader from '../components/AppHeader.vue'
import ArticleCard from '../components/ArticleCard.vue'

const router = useRouter()
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
const writingTopics = ['前端工程', '后端实践', '数据库', '项目复盘']

const featuredArticle = computed(() => articles.value[0] || null)
const featuredInitial = computed(() => featuredArticle.value?.title?.trim()?.charAt(0) || 'B')
const featuredTags = computed(() => featuredArticle.value?.tags?.slice(0, 3) || [])
const streamArticles = computed(() => articles.value)
const topTags = computed(() => tags.value.slice(0, 12))
const filterSummary = computed(() => {
  const parts = []
  if (activeTagName.value) parts.push(`话题：${activeTagName.value}`)
  if (keyword.value) parts.push(`搜索：${keyword.value}`)
  return parts.length ? parts.join(' / ') : '正在浏览全部公开文章'
})
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
</script>

<style scoped>
.home-shell {
  width: min(100%, var(--content-width));
  margin: 0 auto;
  padding: 34px 24px 44px;
}

.hero-board {
  display: grid;
  grid-template-columns: minmax(0, 1.28fr) minmax(340px, 0.72fr);
  gap: 22px;
  align-items: stretch;
  margin-bottom: 18px;
}

.hero-panel {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-height: 430px;
  overflow: hidden;
  padding: clamp(32px, 5vw, 54px);
  border: 1px solid color-mix(in srgb, var(--primary-color) 30%, #142033);
  border-radius: var(--radius-lg);
  background:
    linear-gradient(90deg, rgba(255, 255, 255, 0.07) 1px, transparent 1px) 0 0 / 46px 46px,
    linear-gradient(0deg, rgba(255, 255, 255, 0.06) 1px, transparent 1px) 0 0 / 46px 46px,
    linear-gradient(135deg, #112033 0%, #172a42 52%, #12383c 100%);
  box-shadow: 0 24px 70px rgba(17, 32, 51, 0.2);
  color: #f7fbff;
}

.hero-panel::after {
  position: absolute;
  right: clamp(20px, 5vw, 58px);
  bottom: clamp(16px, 3vw, 32px);
  color: rgba(255, 255, 255, 0.08);
  content: "</>";
  font-family: Consolas, "Liberation Mono", monospace;
  font-size: clamp(86px, 12vw, 150px);
  font-weight: 800;
  line-height: 1;
  pointer-events: none;
}

.eyebrow,
.spotlight-label {
  color: color-mix(in srgb, var(--accent-color) 86%, white);
  font-size: 12px;
  font-weight: 850;
  letter-spacing: 0;
}

.hero-panel .eyebrow {
  position: relative;
  z-index: 1;
}

.hero-panel h1 {
  position: relative;
  z-index: 1;
  max-width: 780px;
  margin: 14px 0 18px;
  color: #ffffff;
  font-size: clamp(42px, 5.5vw, 72px);
  line-height: 1.04;
  font-weight: 900;
}

.hero-description {
  position: relative;
  z-index: 1;
  max-width: 680px;
  margin: 0;
  color: rgba(241, 247, 255, 0.78);
  font-size: 17px;
  line-height: 1.85;
}

.hero-search {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 10px;
  max-width: 680px;
  margin-top: 30px;
  padding: 8px;
  border: 1px solid rgba(255, 255, 255, 0.14);
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
}

.hero-search :deep(.el-input__wrapper) {
  min-height: 42px;
  border-radius: var(--radius-sm);
  background: rgba(255, 255, 255, 0.96);
  color: #182235;
  box-shadow: none;
}

.hero-search :deep(.el-input__inner) {
  color: #182235;
  caret-color: var(--primary-color);
}

.hero-search :deep(.el-input__inner::placeholder) {
  color: #667085;
}

.hero-search :deep(.el-input__clear) {
  color: #667085;
}

.search-button {
  min-width: 86px;
  min-height: 42px;
}

.hero-topics {
  position: relative;
  z-index: 1;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 20px;
}

.hero-topics span {
  padding: 7px 12px;
  border: 1px solid rgba(255, 255, 255, 0.16);
  border-radius: 999px;
  color: rgba(244, 248, 255, 0.78);
  background: rgba(255, 255, 255, 0.08);
  font-size: 13px;
}

.spotlight-card {
  display: grid;
  align-content: start;
  gap: 13px;
  min-width: 0;
  padding: 20px;
  border: 1px solid var(--soft-border-color);
  border-radius: var(--radius-lg);
  background:
    linear-gradient(180deg, color-mix(in srgb, var(--panel-bg) 96%, white), var(--panel-bg)),
    var(--panel-bg);
  box-shadow: var(--shadow-md);
  color: inherit;
  text-align: left;
  cursor: pointer;
  transition: transform 0.18s ease, border-color 0.18s ease, box-shadow 0.18s ease;
}

.spotlight-card:hover {
  transform: translateY(-3px);
  border-color: color-mix(in srgb, var(--primary-color) 40%, var(--soft-border-color));
  box-shadow: 0 28px 64px rgba(23, 35, 52, 0.16);
}

.spotlight-card:focus-visible,
.topic-button:focus-visible {
  outline: 2px solid var(--primary-color);
  outline-offset: 2px;
}

.spotlight-visual {
  position: relative;
  display: grid;
  place-items: center;
  aspect-ratio: 4 / 3;
  min-height: 0;
  overflow: hidden;
  border-radius: var(--radius-md);
  background:
    linear-gradient(135deg, color-mix(in srgb, var(--primary-color) 24%, transparent), transparent 52%),
    repeating-linear-gradient(135deg, color-mix(in srgb, var(--accent-color) 18%, transparent) 0 1px, transparent 1px 12px),
    color-mix(in srgb, var(--accent-color) 10%, var(--bg-color));
}

.spotlight-visual span {
  color: var(--primary-color);
  font-size: 88px;
  font-weight: 900;
  line-height: 1;
}

.spotlight-visual.has-image img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.spotlight-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 12px;
  color: var(--muted-text-color);
  font-size: 13px;
}

.spotlight-card h2 {
  margin: 0;
  color: var(--text-color);
  font-size: 25px;
  line-height: 1.2;
  font-weight: 880;
}

.spotlight-card p {
  margin: 0;
  color: var(--muted-text-color);
  line-height: 1.75;
}

.spotlight-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.spotlight-tags span {
  padding: 4px 9px;
  border-radius: 999px;
  background: color-mix(in srgb, var(--primary-color) 10%, transparent);
  color: var(--primary-color);
  font-size: 12px;
  font-weight: 700;
}

.signal-strip {
  display: grid;
  grid-template-columns: minmax(130px, auto) minmax(130px, auto) minmax(0, 1fr);
  gap: 12px;
  margin-bottom: 22px;
}

.signal-card {
  display: grid;
  gap: 5px;
  min-height: 84px;
  padding: 16px 18px;
  border: 1px solid var(--soft-border-color);
  border-radius: var(--radius-md);
  background: var(--panel-bg);
  box-shadow: var(--shadow-sm);
}

.signal-card.wide {
  background:
    linear-gradient(90deg, color-mix(in srgb, var(--primary-color) 10%, transparent), transparent),
    var(--panel-bg);
}

.signal-card strong {
  color: var(--text-color);
  font-size: 25px;
  line-height: 1.1;
}

.signal-card span {
  color: var(--muted-text-color);
  font-size: 14px;
}

.content-layout {
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  gap: 22px;
  align-items: start;
}

.home-sidebar {
  position: sticky;
  top: calc(var(--app-header-height) + 22px);
  display: grid;
  gap: 14px;
}

.side-panel,
.article-section {
  border: 1px solid var(--soft-border-color);
  border-radius: var(--radius-lg);
  background: var(--panel-bg);
  box-shadow: var(--shadow-sm);
}

.side-panel {
  padding: 18px;
}

.panel-head {
  margin-bottom: 14px;
}

.panel-head span,
.note-panel > span {
  color: var(--accent-color);
  font-size: 12px;
  font-weight: 850;
}

.panel-head h2 {
  margin: 4px 0 0;
  color: var(--text-color);
  font-size: 19px;
  line-height: 1.25;
}

.topic-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.topic-button {
  min-height: 34px;
  padding: 0 13px;
  border: 1px solid var(--border-color);
  border-radius: 999px;
  background: var(--panel-bg);
  color: var(--muted-text-color);
  font: inherit;
  cursor: pointer;
}

.topic-button.active,
.topic-button:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
  background: color-mix(in srgb, var(--primary-color) 8%, transparent);
}

.direction-panel ul {
  display: grid;
  gap: 10px;
  list-style: none;
}

.direction-panel li {
  position: relative;
  min-height: 36px;
  padding: 8px 10px 8px 28px;
  border-radius: var(--radius-sm);
  background: var(--bg-color);
  color: var(--text-color);
  font-weight: 700;
}

.direction-panel li::before {
  position: absolute;
  top: 14px;
  left: 12px;
  width: 7px;
  height: 7px;
  border-radius: 999px;
  background: var(--accent-color);
  content: "";
}

.note-panel {
  background:
    linear-gradient(135deg, color-mix(in srgb, var(--accent-color) 12%, transparent), transparent),
    var(--panel-bg);
}

.note-panel p {
  margin: 8px 0 0;
  color: var(--muted-text-color);
  line-height: 1.75;
}

.article-section {
  min-width: 0;
  padding: 24px;
}

.section-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 20px;
}

.section-head h2 {
  margin: 4px 0 0;
  color: var(--text-color);
  font-size: 28px;
}

.section-head p {
  margin: 4px 0 0;
  color: var(--muted-text-color);
  font-size: 14px;
}

.article-stream {
  display: grid;
  gap: 16px;
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

@media (max-width: 980px) {
  .hero-board,
  .content-layout {
    grid-template-columns: 1fr;
  }

  .home-sidebar {
    position: static;
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .note-panel {
    grid-column: 1 / -1;
  }

  .signal-strip {
    grid-template-columns: 1fr 1fr;
  }

  .signal-card.wide {
    grid-column: 1 / -1;
  }
}

@media (max-width: 640px) {
  .home-shell {
    padding: 22px 14px 36px;
  }

  .hero-panel,
  .spotlight-card,
  .article-section {
    padding: 18px;
  }

  .hero-panel {
    min-height: 0;
  }

  .hero-panel h1 {
    font-size: 38px;
  }

  .hero-search,
  .signal-strip,
  .home-sidebar {
    grid-template-columns: 1fr;
  }
}
</style>
