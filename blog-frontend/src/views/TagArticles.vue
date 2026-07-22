<template>
  <div class="layout">
    <AppHeader />
    <main class="tag-shell">
      <header class="page-head">
        <div class="head-copy">
          <span class="eyebrow">话题索引</span>
          <h1>{{ tagName }}</h1>
          <p>按标签收束后的文章流，只保留同一话题下的公开记录。</p>
        </div>
        <aside class="head-meta" aria-label="标签文章摘要">
          <div class="meta-line">
            <span class="meta-label">文章</span>
            <span class="meta-value">{{ articleCountText }}</span>
          </div>
          <div class="meta-line">
            <span class="meta-label">来源</span>
            <span class="meta-value">公开文章</span>
          </div>
          <div class="meta-line">
            <span class="meta-label">排序</span>
            <span class="meta-value">最近发布</span>
          </div>
        </aside>
      </header>

      <section v-if="articles.length" class="article-stream" aria-label="标签文章列表">
        <ArticleCard v-for="article in articles" :key="article.id" :article="article" />
      </section>
      <el-empty v-else description="该标签下暂无文章" />
    </main>
  </div>
</template>
<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getArticles } from '../api/article'
import { getTags } from '../api/tag'
import AppHeader from '../components/AppHeader.vue'
import ArticleCard from '../components/ArticleCard.vue'
const route = useRoute()
const articles = ref([])
const tagName = computed(() => String(route.params.name || ''))
const articleCountText = computed(() => articles.value.length ? `${articles.value.length} 篇` : '暂无')

onMounted(async () => {
  const tr = await getTags()
  const tag = (tr.data || []).find(t => t.name === tagName.value)
  if (tag) {
    const r = await getArticles({ tagId: tag.id, size: 100 })
    articles.value = r.data || []
  }
})
</script>
<style scoped>
.tag-shell {
  position: relative;
  isolation: isolate;
  width: min(1180px, calc(100% - 36px));
  margin: 0 auto;
  padding: 22px 0 64px;
}

.tag-shell::before {
  position: fixed;
  inset: 0;
  z-index: -1;
  background:
    linear-gradient(90deg, var(--theme-grid-x) 1px, transparent 1px),
    linear-gradient(180deg, var(--theme-grid-y) 1px, transparent 1px);
  background-size: 44px 44px;
  content: '';
  opacity: 0.54;
  pointer-events: none;
}

.page-head {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(260px, 320px);
  gap: 30px;
  align-items: end;
  margin-bottom: 26px;
  padding-top: 22px;
}

.head-copy {
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
  overflow-wrap: anywhere;
}

.head-copy p {
  max-width: 620px;
  margin: 0;
  color: var(--muted-text-color);
  font-size: 17px;
  line-height: 1.85;
}

.head-meta {
  border-left: 1px solid var(--border-color);
  padding-left: 18px;
}

.meta-line {
  display: grid;
  grid-template-columns: 72px minmax(0, 1fr);
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid var(--soft-border-color);
}

.meta-line:last-child {
  border-bottom: 0;
}

.meta-label {
  color: var(--accent-color);
  font-size: 12px;
  font-weight: 760;
}

.meta-value {
  min-width: 0;
  overflow: hidden;
  color: var(--text-color);
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.article-stream {
  display: grid;
  gap: 12px;
}

@media (max-width: 900px) {
  .page-head {
    grid-template-columns: 1fr;
  }

  .head-meta {
    border-left: 0;
    border-top: 1px solid var(--border-color);
    padding-top: 12px;
    padding-left: 0;
  }
}

@media (max-width: 720px) {
  .tag-shell {
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
}

@media (max-width: 440px) {
  .meta-line {
    grid-template-columns: 1fr;
  }
}
</style>
