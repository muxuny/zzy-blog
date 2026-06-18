<template>
  <div class="layout"><AppHeader />
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
          <h1 class="article-title">{{ article.title }}</h1>
          <div class="article-meta">
            <span>{{ formatDate(article.createdAt) }}</span>
            <span v-if="article.authorName">{{ article.authorName }}</span>
            <span>阅读 {{ article.viewCount || 0 }}</span>
          </div>
          <div class="article-tags" v-if="article.tags?.length">
            <el-tag v-for="t in article.tags" :key="t.id" size="small">{{ t.name }}</el-tag>
          </div>
        </header>
        <MarkdownRenderer :content="article.content" />
      </article>
    </el-main>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getArticle } from '../api/article'
import { formatDate } from '../utils'
import { shouldUseHistoryBack } from '../utils/navigation'
import AppHeader from '../components/AppHeader.vue'
import MarkdownRenderer from '../components/MarkdownRenderer.vue'
const route = useRoute()
const router = useRouter()
const article = ref(null)
const loading = ref(true)
const errorMessage = ref('')
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
</script>
<style scoped>
.main {
  width: min(100%, var(--reading-width));
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

.back-button:focus-visible {
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

.home-button {
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

.home-button:hover {
  background: color-mix(in srgb, var(--primary-color) 88%, #000);
}

.home-button:focus-visible {
  outline: 2px solid var(--primary-color);
  outline-offset: 2px;
}

.article-head {
  margin-bottom: 28px;
  padding-bottom: 22px;
  border-bottom: 1px solid var(--soft-border-color);
}

.article-title {
  margin-bottom: 14px;
  color: var(--text-color);
  font-size: clamp(30px, 5vw, 44px);
  line-height: 1.14;
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

@media (max-width: 640px) {
  .main {
    padding: 22px 14px 48px;
  }

  .article-shell {
    padding: 22px 18px;
  }
}
</style>
