<template>
  <div class="layout">
    <AppHeader />
    <el-main class="main">
      <section class="home-hero">
        <div class="hero-copy">
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
              placeholder="搜索文章标题"
              @clear="clearSearch"
            />
            <el-button type="primary" size="large" native-type="submit">搜索</el-button>
          </form>

          <div class="hero-topics" aria-label="写作方向">
            <span>前端工程</span>
            <span>后端实践</span>
            <span>数据库</span>
            <span>项目复盘</span>
          </div>
        </div>

        <button
          v-if="featuredArticle"
          type="button"
          class="featured-card"
          @click="goArticle(featuredArticle)"
        >
          <span class="featured-label">最近更新</span>
          <div class="feature-art" :class="{ 'has-image': featuredArticle.coverImage }">
            <img v-if="featuredArticle.coverImage" :src="featuredArticle.coverImage" :alt="featuredArticle.title" />
            <span v-else>{{ featuredInitial }}</span>
          </div>
          <h2>{{ featuredArticle.title }}</h2>
          <p>{{ featuredArticle.summary || truncate(featuredArticle.content, 110) }}</p>
          <div class="featured-meta">
            <span>{{ formatDate(featuredArticle.createdAt) }}</span>
            <span v-if="featuredArticle.authorName">{{ featuredArticle.authorName }}</span>
            <span>阅读 {{ featuredArticle.viewCount || 0 }}</span>
          </div>
        </button>

        <section v-else class="featured-card featured-empty">
          <span class="featured-label">最近更新</span>
          <div class="feature-art"><span>B</span></div>
          <h2>还没有公开文章</h2>
          <p>发布第一篇文章后，这里会展示最新内容。</p>
        </section>
      </section>

      <section class="summary-strip">
        <div>
          <strong>{{ total }}</strong>
          <span>公开文章</span>
        </div>
        <div>
          <strong>{{ tags.length }}</strong>
          <span>话题标签</span>
        </div>
        <p>{{ filterSummary }}</p>
      </section>

      <section class="topic-nav" aria-label="话题导航">
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
          v-for="tag in tags"
          :key="tag.id"
          class="topic-button"
          :class="{ active: activeTag === tag.id }"
          :aria-pressed="activeTag === tag.id"
          type="button"
          @click="filterByTag(tag)"
        >
          {{ tag.name }}
        </button>
      </section>

      <section class="article-section">
        <div class="section-head">
          <span class="eyebrow">文章</span>
          <h2>最近记录</h2>
        </div>

        <el-skeleton v-if="loading" :rows="8" animated />

        <template v-else>
          <div v-if="listArticles.length" class="article-stream">
            <ArticleCard v-for="article in listArticles" :key="article.id" :article="article" />
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

      <footer class="site-footer">
        <span>My Blog</span>
        <span>© {{ currentYear }}</span>
        <span>把经验写下来，让下一次开始更轻一点。</span>
      </footer>
    </el-main>
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

const featuredArticle = computed(() => articles.value[0] || null)
const featuredInitial = computed(() => featuredArticle.value?.title?.trim()?.charAt(0) || 'B')
const listArticles = computed(() => articles.value.slice(1))
const filterSummary = computed(() => {
  const parts = []
  if (activeTagName.value) parts.push(`话题：${activeTagName.value}`)
  if (keyword.value) parts.push(`搜索：${keyword.value}`)
  return parts.length ? parts.join(' / ') : '正在浏览全部公开文章'
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
.main {
  width: min(100%, var(--content-width));
  margin: 0 auto;
  padding: 34px 24px 42px;
}

.home-hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(340px, 430px);
  gap: 26px;
  align-items: stretch;
  min-height: 390px;
}

.hero-copy,
.featured-card,
.summary-strip,
.topic-nav,
.article-section {
  border: 1px solid var(--soft-border-color);
  background: var(--panel-bg);
  box-shadow: var(--shadow-sm);
}

.hero-copy {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: clamp(28px, 5vw, 52px);
  border-radius: var(--radius-lg);
}

.eyebrow,
.featured-label {
  color: var(--accent-color);
  font-size: 12px;
  font-weight: 850;
}

.hero-copy h1 {
  max-width: 780px;
  margin: 12px 0 18px;
  color: var(--text-color);
  font-size: clamp(38px, 6vw, 72px);
  line-height: 0.98;
  font-weight: 900;
}

.hero-description {
  max-width: 680px;
  margin: 0;
  color: var(--muted-text-color);
  font-size: 17px;
  line-height: 1.85;
}

.hero-search {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 10px;
  max-width: 680px;
  margin-top: 30px;
}

.hero-topics {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 20px;
}

.hero-topics span {
  padding: 6px 11px;
  border: 1px solid var(--soft-border-color);
  border-radius: 999px;
  color: var(--muted-text-color);
  background: var(--bg-color);
  font-size: 13px;
}

.featured-card {
  display: grid;
  align-content: start;
  gap: 14px;
  min-width: 0;
  padding: 22px;
  border-radius: var(--radius-lg);
  color: inherit;
  text-align: left;
  cursor: pointer;
}

.featured-card:hover {
  border-color: color-mix(in srgb, var(--primary-color) 36%, var(--soft-border-color));
  box-shadow: var(--shadow-md);
}

.featured-card:focus-visible,
.topic-button:focus-visible {
  outline: 2px solid var(--primary-color);
  outline-offset: 2px;
}

.feature-art {
  display: grid;
  place-items: center;
  min-height: 168px;
  overflow: hidden;
  border-radius: var(--radius-md);
  background:
    linear-gradient(135deg, color-mix(in srgb, var(--primary-color) 24%, transparent), transparent),
    color-mix(in srgb, var(--accent-color) 12%, var(--bg-color));
}

.feature-art span {
  color: var(--primary-color);
  font-size: 82px;
  font-weight: 900;
  line-height: 1;
}

.feature-art.has-image img {
  width: 100%;
  height: 100%;
  min-height: 168px;
  object-fit: cover;
}

.featured-card h2 {
  margin: 0;
  color: var(--text-color);
  font-size: 27px;
  line-height: 1.2;
  font-weight: 880;
}

.featured-card p {
  margin: 0;
  color: var(--muted-text-color);
  line-height: 1.75;
}

.featured-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 14px;
  color: var(--muted-text-color);
  font-size: 13px;
}

.summary-strip {
  display: grid;
  grid-template-columns: auto auto minmax(0, 1fr);
  gap: 18px;
  align-items: center;
  margin: 18px 0;
  padding: 14px 18px;
  border-radius: var(--radius-md);
}

.summary-strip div {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.summary-strip strong {
  color: var(--text-color);
  font-size: 24px;
}

.summary-strip span,
.summary-strip p {
  color: var(--muted-text-color);
  font-size: 14px;
}

.summary-strip p {
  justify-self: end;
  margin: 0;
}

.topic-nav {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 22px;
  padding: 14px;
  border-radius: var(--radius-md);
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

.article-section {
  padding: 22px;
  border-radius: var(--radius-lg);
}

.section-head {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 16px;
}

.section-head h2 {
  margin: 4px 0 0;
  color: var(--text-color);
  font-size: 28px;
}

.article-stream {
  display: grid;
  gap: 14px;
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
  .home-hero {
    grid-template-columns: 1fr;
  }

  .summary-strip {
    grid-template-columns: 1fr 1fr;
  }

  .summary-strip p {
    grid-column: 1 / -1;
    justify-self: start;
  }
}

@media (max-width: 640px) {
  .main {
    padding: 22px 14px 36px;
  }

  .hero-copy,
  .featured-card,
  .article-section {
    padding: 18px;
  }

  .hero-search,
  .summary-strip {
    grid-template-columns: 1fr;
  }
}
</style>
