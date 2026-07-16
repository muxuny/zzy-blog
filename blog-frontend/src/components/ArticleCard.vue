<template>
  <el-card
    class="article-card"
    shadow="hover"
    role="link"
    tabindex="0"
    @click="goArticle"
    @keydown.enter.prevent="goArticle"
    @keydown.space.prevent="goArticle"
  >
    <div class="card-body">
      <div class="card-marker" aria-hidden="true">
        <span>{{ articleInitial }}</span>
      </div>
      <div class="card-content">
        <h3 class="card-title">{{ article.title }}</h3>
        <p class="card-summary">{{ article.summary || truncate(article.content, 120) }}</p>
        <div class="card-meta">
          <span>{{ formatDate(article.createdAt) }}</span>
          <span v-if="article.authorName">{{ article.authorName }}</span>
          <span>阅读 {{ article.viewCount || 0 }}</span>
        </div>
        <div class="card-tags" v-if="article.tags?.length">
          <router-link
            v-for="tag in article.tags"
            :key="tag.id"
            :to="`/tag/${tag.name}`"
            class="tag-link"
            @click.stop
            @keydown.enter.stop
            @keydown.space.stop
          >
            <el-tag size="small">{{ tag.name }}</el-tag>
          </router-link>
        </div>
      </div>
      <div class="card-image" v-if="article.coverImage">
        <img :src="article.coverImage" :alt="article.title" />
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { computed } from 'vue'
import { formatDate, truncate } from '../utils'

const props = defineProps({ article: { type: Object, required: true } })
const router = useRouter()
const articleInitial = computed(() => props.article.title?.trim()?.charAt(0) || 'B')

function goArticle() {
  router.push(`/article/${props.article.id}`)
}

</script>

<style scoped>
.article-card {
  margin-bottom: 0;
  cursor: pointer;
  background: var(--card-bg);
  border: 1px solid var(--soft-border-color);
  border-radius: var(--radius-lg);
  box-shadow: none;
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
}

.article-card :deep(.el-card__body) {
  padding: 20px;
}

.article-card:hover,
.article-card:focus-visible {
  transform: translateY(-2px);
  border-color: color-mix(in srgb, var(--primary-color) 38%, var(--soft-border-color));
  box-shadow: var(--shadow-md);
}

.article-card:focus-visible {
  outline: 2px solid var(--primary-color);
  outline-offset: 2px;
}

.card-body {
  display: flex;
  gap: 18px;
  align-items: stretch;
}

.card-marker {
  display: grid;
  place-items: center;
  width: 48px;
  height: 48px;
  flex: 0 0 48px;
  border-radius: var(--radius-md);
  background:
    linear-gradient(135deg, color-mix(in srgb, var(--primary-color) 18%, transparent), transparent),
    var(--bg-color);
  color: var(--primary-color);
  font-weight: 900;
}

.card-content {
  flex: 1;
  min-width: 0;
}

.card-title {
  margin: 0 0 10px;
  color: var(--text-color);
  font-size: 22px;
  line-height: 1.35;
  font-weight: 800;
}

.card-summary {
  color: var(--muted-text-color);
  font-size: 14px;
  line-height: 1.75;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

.card-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 14px;
  margin: 12px 0;
  color: var(--muted-text-color);
  font-size: 13px;
}

.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
}

.tag-link {
  display: inline-flex;
  border-radius: var(--radius-sm);
  color: inherit;
  text-decoration: none;
  cursor: pointer;
  transition: transform 0.16s ease;
}

.tag-link:hover {
  transform: translateY(-1px);
}

.tag-link :deep(.el-tag) {
  cursor: pointer;
  transition: border-color 0.16s ease, background-color 0.16s ease, color 0.16s ease;
}

.tag-link:hover :deep(.el-tag),
.tag-link:focus-visible :deep(.el-tag) {
  border-color: var(--primary-color);
  background: color-mix(in srgb, var(--primary-color) 14%, transparent);
  color: var(--primary-color);
}

.tag-link:focus-visible {
  outline: 2px solid var(--primary-color);
  outline-offset: 2px;
}

.card-image {
  width: 190px;
  flex: 0 0 190px;
}

.card-image img {
  width: 100%;
  height: 128px;
  object-fit: cover;
  border-radius: var(--radius-md);
  border: 1px solid var(--soft-border-color);
}

@media (max-width: 720px) {
  .card-body {
    flex-direction: column;
  }

  .card-marker {
    display: none;
  }

  .card-image {
    width: 100%;
    flex-basis: auto;
  }

  .card-image img {
    height: auto;
    aspect-ratio: 16 / 9;
  }
}
</style>
