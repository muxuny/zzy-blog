<template>
  <div class="layout">
    <AppHeader />
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
        <header class="article-hero" :class="{ 'has-cover': article.coverImage }">
          <div class="article-hero-copy">
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
          </div>
          <figure v-if="article.coverImage" class="article-cover">
            <img :src="article.coverImage" :alt="article.title" />
          </figure>
        </header>

        <section class="reading-summary">
          <div>
            <span>字数</span>
            <strong>{{ readingStats.wordCount }}</strong>
          </div>
          <div>
            <span>预计阅读</span>
            <strong>{{ readingStats.readingTimeText }}</strong>
          </div>
          <div>
            <span>作者</span>
            <strong>{{ authorName }}</strong>
          </div>
        </section>

        <div class="reading-layout">
          <main class="article-body">
            <nav v-if="toc.length" class="mobile-toc" aria-label="文章目录">
              <span class="eyebrow">目录</span>
              <button
                v-for="item in toc"
                :key="item.id"
                type="button"
                :class="`toc-link level-${item.level}`"
                @click="scrollToHeading(item.id)"
              >
                {{ item.text }}
              </button>
            </nav>

            <MarkdownRenderer :content="article.content" />

            <button type="button" class="top-button" @click="scrollToTop">回到顶部</button>
          </main>

          <aside class="reading-sidebar">
            <nav v-if="toc.length" class="toc-panel" aria-label="文章目录">
              <span class="eyebrow">目录</span>
              <button
                v-for="item in toc"
                :key="item.id"
                type="button"
                :class="`toc-link level-${item.level}`"
                @click="scrollToHeading(item.id)"
              >
                {{ item.text }}
              </button>
            </nav>
            <section class="note-panel">
              <span class="eyebrow">阅读提示</span>
              <p>目录会跟随 Markdown 二级和三级标题生成，适合快速回到关键段落。</p>
            </section>
          </aside>
        </div>
      </article>
    </el-main>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getArticle } from '../api/article'
import { formatDate } from '../utils'
import { shouldUseHistoryBack } from '../utils/navigation'
import { extractMarkdownToc, getReadingStats } from '../utils/reading'
import AppHeader from '../components/AppHeader.vue'
import MarkdownRenderer from '../components/MarkdownRenderer.vue'

const route = useRoute()
const router = useRouter()
const article = ref(null)
const loading = ref(true)
const errorMessage = ref('')

const articleTags = computed(() => article.value?.tags || [])
const authorName = computed(() => article.value?.authorName || article.value?.createdBy || '匿名作者')
const toc = computed(() => extractMarkdownToc(article.value?.content || ''))
const readingStats = computed(() => getReadingStats(article.value?.content || ''))

onMounted(async () => {
  try {
    const r = await getArticle(route.params.id)
    article.value = r.data
  } catch {
    errorMessage.value = '文章不存在或暂不可见'
  } finally {
    loading.value = false
  }
})

function goBack() {
  if (shouldUseHistoryBack(history.state)) router.back()
  else router.push('/')
}

function scrollToHeading(id) {
  document.getElementById(id)?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

function scrollToTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}
</script>

<style scoped>
.main {
  width: min(100%, var(--content-width));
  margin: 0 auto;
  padding: 34px 24px 72px;
}

.back-button {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: 36px;
  margin-bottom: 16px;
  padding: 0 12px;
  border: 1px solid var(--soft-border-color);
  border-radius: var(--radius-sm);
  background: var(--panel-bg);
  color: var(--muted-text-color);
  font: inherit;
  font-weight: 700;
  cursor: pointer;
  box-shadow: var(--shadow-sm);
}

.back-button:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
  background: color-mix(in srgb, var(--primary-color) 8%, var(--panel-bg));
}

.back-button:focus-visible,
.home-button:focus-visible,
.toc-link:focus-visible,
.top-button:focus-visible {
  outline: 2px solid var(--primary-color);
  outline-offset: 2px;
}

.empty-state {
  padding: 44px 28px;
  border: 1px solid var(--soft-border-color);
  border-radius: var(--radius-lg);
  background: var(--panel-bg);
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

.home-button,
.top-button {
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
  gap: 20px;
}

.article-hero {
  display: grid;
  grid-template-columns: 1fr;
  gap: 26px;
  align-items: stretch;
  padding: clamp(24px, 4vw, 44px);
  border: 1px solid var(--soft-border-color);
  border-radius: var(--radius-lg);
  background: var(--panel-bg);
  box-shadow: var(--shadow-sm);
}

.article-hero.has-cover {
  grid-template-columns: minmax(0, 1fr) minmax(280px, 420px);
}

.article-hero-copy {
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-width: 0;
}

.eyebrow {
  color: var(--accent-color);
  font-size: 12px;
  font-weight: 850;
}

.article-hero h1 {
  max-width: 900px;
  margin: 12px 0 18px;
  color: var(--text-color);
  font-size: clamp(36px, 6vw, 68px);
  line-height: 1;
  font-weight: 900;
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

.article-cover {
  margin: 0;
  min-width: 0;
}

.article-cover img {
  width: 100%;
  height: 100%;
  min-height: 280px;
  object-fit: cover;
  border: 1px solid var(--soft-border-color);
  border-radius: var(--radius-md);
}

.reading-summary {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.reading-summary div {
  padding: 14px 16px;
  border: 1px solid var(--soft-border-color);
  border-radius: var(--radius-md);
  background: var(--panel-bg);
}

.reading-summary span,
.reading-summary strong {
  display: block;
}

.reading-summary span {
  color: var(--muted-text-color);
  font-size: 12px;
}

.reading-summary strong {
  margin-top: 6px;
  color: var(--text-color);
  font-size: 16px;
}

.reading-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 280px;
  gap: 30px;
  align-items: start;
}

.article-body {
  min-width: 0;
  padding: clamp(22px, 4vw, 38px);
  border: 1px solid var(--soft-border-color);
  border-radius: var(--radius-lg);
  background: var(--panel-bg);
  box-shadow: var(--shadow-sm);
}

.reading-sidebar {
  position: sticky;
  top: calc(var(--app-header-height) + 22px);
  display: grid;
  gap: 14px;
}

.toc-panel,
.note-panel,
.mobile-toc {
  padding: 16px;
  border: 1px solid var(--soft-border-color);
  border-radius: var(--radius-md);
  background: var(--panel-bg);
}

.toc-panel,
.mobile-toc {
  display: grid;
  gap: 6px;
}

.note-panel p {
  margin: 8px 0 0;
  color: var(--muted-text-color);
  font-size: 13px;
  line-height: 1.7;
}

.toc-link {
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

.toc-link:hover {
  background: color-mix(in srgb, var(--primary-color) 8%, transparent);
  color: var(--primary-color);
}

.toc-link.level-3 {
  padding-left: 20px;
}

.mobile-toc {
  display: none;
  margin-bottom: 22px;
}

.top-button {
  display: block;
  margin: 30px 0 0 auto;
}

@media (max-width: 980px) {
  .article-hero,
  .reading-layout {
    grid-template-columns: 1fr;
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
    padding: 22px 14px 48px;
  }

  .article-hero,
  .article-body {
    padding: 18px;
  }

  .reading-summary {
    grid-template-columns: 1fr;
  }

  .article-cover img {
    min-height: 210px;
  }
}
</style>
