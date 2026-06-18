<template>
  <div class="layout"><AppHeader />
    <el-main class="main">
      <header class="tag-page-head">
        <span>标签</span>
        <h1>{{ route.params.name }}</h1>
      </header>
      <ArticleCard v-for="a in articles" :key="a.id" :article="a" />
      <el-empty v-if="!articles.length" description="该标签下暂无文章" />
    </el-main>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getArticles } from '../api/article'
import { getTags } from '../api/tag'
import AppHeader from '../components/AppHeader.vue'
import ArticleCard from '../components/ArticleCard.vue'
const route = useRoute()
const articles = ref([])
onMounted(async () => {
  const tr = await getTags()
  const tag = (tr.data||[]).find(t => t.name === route.params.name)
  if (tag) { const r = await getArticles({ tagId: tag.id, size: 100 }); articles.value = r.data||[] }
})
</script>
<style scoped>
.main {
  width: min(100%, var(--reading-width));
  margin: 0 auto;
  padding: 34px 24px 64px;
}

.tag-page-head {
  margin-bottom: 22px;
  padding-left: 16px;
  border-left: 4px solid var(--accent-color);
}

.tag-page-head span {
  color: var(--muted-text-color);
  font-size: 13px;
  font-weight: 750;
}

.tag-page-head h1 {
  color: var(--text-color);
  font-size: 32px;
  line-height: 1.2;
}
</style>
