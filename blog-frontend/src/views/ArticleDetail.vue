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

      <article v-else-if="article" class="article-shell">
        <header class="article-head">
          <span class="article-kicker">文章</span>
          <h1 class="article-title">{{ article.title }}</h1>
          <div class="article-meta">
            <span>{{ formatDate(article.createdAt) }}</span>
            <span>{{ authorName }}</span>
            <span>阅读 {{ article.viewCount || 0 }}</span>
            <span>{{ readingStats.readingTimeText }}</span>
          </div>
          <div v-if="articleTags.length" class="article-tags">
            <el-tag v-for="tag in articleTags" :key="tag.id" size="small">{{ tag.name }}</el-tag>
          </div>
        </header>

        <figure v-if="article.coverImage" class="article-cover">
          <img :src="article.coverImage" :alt="article.title" />
        </figure>

        <div class="reading-layout">
          <div class="article-body">
            <section class="mobile-info">
              <div class="info-grid">
                <div>
                  <span>字数</span>
                  <strong>{{ readingStats.wordCount }}</strong>
                </div>
                <div>
                  <span>阅读</span>
                  <strong>{{ readingStats.readingTimeText }}</strong>
                </div>
              </div>
            </section>

            <nav v-if="toc.length" class="mobile-toc" aria-label="文章目录">
              <h2>目录</h2>
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
          </div>

          <aside class="reading-sidebar">
            <section class="side-panel">
              <div class="side-label">阅读信息</div>
              <dl class="article-info">
                <div>
                  <dt>作者</dt>
                  <dd>{{ authorName }}</dd>
                </div>
                <div>
                  <dt>发布</dt>
                  <dd>{{ formatDate(article.createdAt) }}</dd>
                </div>
                <div>
                  <dt>阅读</dt>
                  <dd>{{ article.viewCount || 0 }}</dd>
                </div>
                <div>
                  <dt>字数</dt>
                  <dd>{{ readingStats.wordCount }}</dd>
                </div>
                <div>
                  <dt>预计</dt>
                  <dd>{{ readingStats.readingTimeText }}</dd>
                </div>
              </dl>
            </section>

            <section v-if="toc.length" class="side-panel toc-panel">
              <div class="side-label">目录</div>
              <nav class="toc-list" aria-label="文章目录">
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
            </section>
          </aside>
        </div>

        <button type="button" class="top-button" @click="scrollToTop">回到顶部</button>
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
  padding: 38px 24px 72px;
}

.back-button {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: 36px;
  margin-bottom: 14px;
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

.article-shell {
  padding: 34px;
  border: 1px solid var(--soft-border-color);
  border-radius: var(--radius-lg);
  background: var(--panel-bg);
  box-shadow: var(--shadow-sm);
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
  line-height: 1.3;
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

.home-button:hover,
.top-button:hover {
  background: color-mix(in srgb, var(--primary-color) 88%, #000);
}

.article-head {
  margin-bottom: 24px;
  padding-bottom: 22px;
  border-bottom: 1px solid var(--soft-border-color);
}

.article-kicker,
.side-label {
  display: inline-block;
  margin-bottom: 8px;
  color: var(--accent-color);
  font-size: 12px;
  font-weight: 800;
}

.article-title {
  max-width: 880px;
  margin-bottom: 14px;
  color: var(--text-color);
  font-size: clamp(32px, 5vw, 50px);
  line-height: 1.12;
  font-weight: 850;
}

.article-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 16px;
  margin-bottom: 16px;
  color: var(--muted-text-color);
  font-size: 14px;
}

.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.article-cover {
  margin: 0 0 28px;
}

.article-cover img {
  width: 100%;
  max-height: 420px;
  object-fit: cover;
  border: 1px solid var(--soft-border-color);
  border-radius: var(--radius-lg);
}

.reading-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 260px;
  gap: 28px;
  align-items: start;
}

.article-body {
  min-width: 0;
}

.reading-sidebar {
  position: sticky;
  top: calc(var(--app-header-height) + 22px);
  display: grid;
  gap: 14px;
}

.side-panel,
.mobile-info,
.mobile-toc {
  padding: 16px;
  border: 1px solid var(--soft-border-color);
  border-radius: var(--radius-md);
  background: var(--bg-color);
}

.article-info {
  display: grid;
  gap: 12px;
  margin: 0;
}

.article-info div,
.info-grid {
  display: grid;
  grid-template-columns: 64px minmax(0, 1fr);
  gap: 10px;
}

.article-info dt,
.info-grid span {
  color: var(--muted-text-color);
  font-size: 13px;
}

.article-info dd,
.info-grid strong {
  margin: 0;
  color: var(--text-color);
  font-size: 13px;
  font-weight: 750;
}

.toc-list,
.mobile-toc {
  display: grid;
  gap: 6px;
}

.mobile-toc h2 {
  margin: 0 0 8px;
  color: var(--text-color);
  font-size: 16px;
}

.toc-link {
  width: 100%;
  padding: 6px 8px;
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

.mobile-info,
.mobile-toc {
  display: none;
  margin-bottom: 18px;
}

.top-button {
  display: block;
  margin: 28px 0 0 auto;
}

@media (max-width: 900px) {
  .reading-layout {
    grid-template-columns: 1fr;
  }

  .reading-sidebar {
    display: none;
  }

  .mobile-info,
  .mobile-toc {
    display: grid;
  }
}

@media (max-width: 640px) {
  .main {
    padding: 22px 14px 48px;
  }

  .article-shell {
    padding: 22px 18px;
  }

  .article-info div,
  .info-grid {
    grid-template-columns: 1fr;
    gap: 4px;
  }
}
</style>
