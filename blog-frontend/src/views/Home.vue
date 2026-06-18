<template>
  <div class="layout"><AppHeader />
    <el-main class="main">
      <div class="container">
        <aside class="sidebar">
          <section class="tag-panel">
            <div class="panel-label">筛选</div>
            <h2>标签</h2>
            <div class="tag-list">
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
          <ArticleCard v-for="a in articles" :key="a.id" :article="a" />
          <div class="pagination" v-if="total>size"><el-pagination v-model:current-page="page" :total="total" :page-size="size" layout="prev,pager,next" @current-change="load" /></div>
          <el-empty v-if="!articles.length && !loading" description="暂无文章" />
        </div>
      </div>
    </el-main>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { getArticles } from '../api/article'
import { getTags } from '../api/tag'
import AppHeader from '../components/AppHeader.vue'
import ArticleCard from '../components/ArticleCard.vue'

const articles = ref([]), tags = ref([]), page = ref(1), size = ref(10), total = ref(0), activeTag = ref(null), loading = ref(false)
onMounted(async () => { const r = await getTags(); tags.value = r.data || []; await load() })
async function load() {
  loading.value = true
  const p = { page: page.value, size: size.value }
  if (activeTag.value) p.tagId = activeTag.value
  const r = await getArticles(p)
  articles.value = r.data || []; total.value = r.total || 0; loading.value = false
}
function filterByTag(t) { activeTag.value = activeTag.value === t.id ? null : t.id; page.value = 1; load() }
</script>
<style scoped>
.main {
  width: min(100%, var(--content-width));
  margin: 0 auto;
  padding: 28px 24px 56px;
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

.panel-label {
  margin-bottom: 4px;
  color: var(--accent-color);
  font-size: 12px;
  font-weight: 800;
}

.tag-panel h2 {
  margin-bottom: 14px;
  font-size: 20px;
  color: var(--text-color);
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

.content {
  min-width: 0;
}

.pagination {
  display: flex;
  justify-content: center;
  margin: 28px 0 0;
}

@media (max-width: 860px) {
  .container {
    grid-template-columns: 1fr;
  }

  .sidebar {
    position: static;
  }
}
</style>
