<template>
  <div class="layout">
    <AppHeader />
    <el-main class="main">
      <section class="intro-section">
        <div class="intro-copy">
          <span class="section-kicker">个人博客</span>
          <h1>My Blog</h1>
          <p>记录开发实践、阅读笔记和阶段性思考，把零散经验整理成可以反复回看的文字。</p>
          <div class="intro-topics">
            <span>前端</span>
            <span>后端</span>
            <span>项目复盘</span>
          </div>
        </div>
        <div class="intro-stats">
          <div>
            <strong>{{ total }}</strong>
            <span>公开文章</span>
          </div>
          <div>
            <strong>{{ tags.length }}</strong>
            <span>标签</span>
          </div>
        </div>
      </section>

      <section class="search-section">
        <form class="search-box" @submit.prevent="submitSearch">
          <el-input
            v-model="keywordInput"
            clearable
            size="large"
            placeholder="搜索文章标题"
            @clear="clearSearch"
          />
          <el-button type="primary" size="large" native-type="submit">搜索</el-button>
        </form>
        <p class="filter-summary">{{ filterSummary }}</p>
      </section>

      <div class="container">
        <aside class="sidebar">
          <section class="tag-panel">
            <div class="panel-label">筛选</div>
            <h2>标签</h2>
            <p class="tag-count">共 {{ tags.length }} 个标签</p>
            <div class="tag-list">
              <button
                class="tag-filter"
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
                class="tag-filter"
                :class="{ active: activeTag === tag.id }"
                :aria-pressed="activeTag === tag.id"
                type="button"
                @click="filterByTag(tag)"
              >
                {{ tag.name }}
              </button>
            </div>
          </section>
        </aside>

        <div class="content">
          <el-skeleton v-if="loading" :rows="8" animated />

          <template v-else>
            <section v-if="featuredArticle" class="featured-section" @click="goArticle(featuredArticle)">
              <div class="featured-copy">
                <span class="section-kicker">最近更新</span>
                <h2>{{ featuredArticle.title }}</h2>
                <p>{{ featuredArticle.summary || truncate(featuredArticle.content, 150) }}</p>
                <div class="featured-meta">
                  <span>{{ formatDate(featuredArticle.createdAt) }}</span>
                  <span v-if="featuredArticle.authorName">{{ featuredArticle.authorName }}</span>
                  <span>阅读 {{ featuredArticle.viewCount || 0 }}</span>
                </div>
              </div>
              <div v-if="featuredArticle.coverImage" class="featured-image">
                <img :src="featuredArticle.coverImage" :alt="featuredArticle.title" />
              </div>
            </section>

            <ArticleCard v-for="a in listArticles" :key="a.id" :article="a" />

            <div class="pagination" v-if="total > size">
              <el-pagination
                v-model:current-page="page"
                :total="total"
                :page-size="size"
                layout="prev,pager,next"
                @current-change="load"
              />
            </div>

            <el-empty v-if="!articles.length" :description="emptyDescription" />
          </template>
        </div>
      </div>

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
const listArticles = computed(() => articles.value.slice(1))
const filterSummary = computed(() => {
  const parts = []
  if (activeTagName.value) parts.push(`标签：${activeTagName.value}`)
  if (keyword.value) parts.push(`搜索：${keyword.value}`)
  return parts.length ? parts.join(' / ') : '正在浏览全部公开文章'
})
const emptyDescription = computed(() => {
  if (keyword.value) return '没有找到匹配文章'
  if (activeTagName.value) return '这个标签下还没有公开文章'
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
  padding: 28px 24px 42px;
}

.intro-section {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 28px;
  align-items: end;
  padding: 28px;
  margin-bottom: 18px;
  border: 1px solid var(--soft-border-color);
  border-radius: var(--radius-lg);
  background: var(--panel-bg);
  box-shadow: var(--shadow-sm);
}

.section-kicker,
.panel-label {
  display: inline-block;
  margin-bottom: 6px;
  color: var(--accent-color);
  font-size: 12px;
  font-weight: 800;
}

.intro-copy h1 {
  margin: 0 0 12px;
  color: var(--text-color);
  font-size: clamp(34px, 5vw, 56px);
  line-height: 1;
}

.intro-copy p {
  max-width: 680px;
  margin: 0;
  color: var(--muted-text-color);
  font-size: 16px;
  line-height: 1.8;
}

.intro-topics {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 18px;
}

.intro-topics span {
  padding: 5px 10px;
  border: 1px solid var(--soft-border-color);
  border-radius: 999px;
  color: var(--muted-text-color);
  font-size: 13px;
}

.intro-stats {
  display: flex;
  gap: 12px;
}

.intro-stats div {
  min-width: 112px;
  padding: 14px;
  border: 1px solid var(--soft-border-color);
  border-radius: var(--radius-md);
  background: var(--bg-color);
}

.intro-stats strong,
.intro-stats span {
  display: block;
}

.intro-stats strong {
  color: var(--text-color);
  font-size: 28px;
  line-height: 1;
}

.intro-stats span {
  margin-top: 8px;
  color: var(--muted-text-color);
  font-size: 13px;
}

.search-section {
  display: grid;
  gap: 10px;
  margin-bottom: 22px;
}

.search-box {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 10px;
}

.filter-summary {
  margin: 0;
  color: var(--muted-text-color);
  font-size: 14px;
}

.container {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  gap: 26px;
  align-items: start;
}

.sidebar {
  position: sticky;
  top: calc(var(--app-header-height) + 24px);
}

.tag-panel {
  padding: 18px;
  border: 1px solid var(--soft-border-color);
  border-radius: var(--radius-md);
  background: var(--panel-bg);
  box-shadow: var(--shadow-sm);
}

.tag-panel h2 {
  margin: 0;
  font-size: 20px;
  color: var(--text-color);
}

.tag-count {
  margin: 6px 0 14px;
  color: var(--muted-text-color);
  font-size: 13px;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-filter {
  min-height: 32px;
  padding: 0 12px;
  border: 1px solid var(--border-color);
  border-radius: 999px;
  background: var(--panel-bg);
  color: var(--muted-text-color);
  cursor: pointer;
}

.tag-filter.active,
.tag-filter:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
  background: color-mix(in srgb, var(--primary-color) 8%, transparent);
}

.tag-filter:focus-visible,
.featured-section:focus-visible {
  outline: 2px solid var(--primary-color);
  outline-offset: 2px;
}

.content {
  min-width: 0;
}

.featured-section {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 220px;
  gap: 18px;
  margin-bottom: 18px;
  padding: 22px;
  border: 1px solid color-mix(in srgb, var(--primary-color) 24%, var(--soft-border-color));
  border-radius: var(--radius-lg);
  background: var(--panel-bg);
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition: transform 0.18s ease, border-color 0.18s ease, box-shadow 0.18s ease;
}

.featured-section:hover {
  transform: translateY(-2px);
  border-color: var(--primary-color);
  box-shadow: var(--shadow-md);
}

.featured-copy h2 {
  margin: 0 0 10px;
  color: var(--text-color);
  font-size: 26px;
  line-height: 1.25;
}

.featured-copy p {
  margin: 0;
  color: var(--muted-text-color);
  line-height: 1.8;
}

.featured-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 14px;
  margin-top: 14px;
  color: var(--muted-text-color);
  font-size: 13px;
}

.featured-image img {
  width: 100%;
  height: 148px;
  object-fit: cover;
  border: 1px solid var(--soft-border-color);
  border-radius: var(--radius-md);
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

@media (max-width: 940px) {
  .intro-section,
  .container,
  .featured-section {
    grid-template-columns: 1fr;
  }

  .sidebar {
    position: static;
  }
}

@media (max-width: 640px) {
  .main {
    padding: 22px 14px 36px;
  }

  .intro-section {
    padding: 22px 18px;
  }

  .intro-stats,
  .search-box {
    grid-template-columns: 1fr;
    display: grid;
  }
}
</style>
