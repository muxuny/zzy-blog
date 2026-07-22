<template>
  <article
    class="article-card article-entry"
    role="link"
    tabindex="0"
    @click="goArticle"
    @keydown.enter.prevent="goArticle"
    @keydown.space.prevent="goArticle"
  >
    <div class="entry-copy">
      <div class="article-meta">
        <span>{{ formatDate(article.updatedAt || article.createdAt) }}</span>
        <span v-if="article.authorName">{{ article.authorName }}</span>
        <span v-if="article.tags?.length" class="topic">{{ article.tags[0].name }}</span>
      </div>
      <h3 class="card-title">{{ article.title }}</h3>
      <p class="card-summary">{{ article.summary || truncate(article.content, 140) }}</p>
      <div class="entry-tags" v-if="article.tags?.length">
        <router-link
          v-for="tag in article.tags"
          :key="tag.id"
          :to="`/tag/${tag.name}`"
          class="tag-chip tag-link"
          @click.stop
          @keydown.enter.stop
          @keydown.space.stop
        >
          {{ tag.name }}
        </router-link>
      </div>
    </div>
    <div class="article-visual" :class="{ 'has-cover': article.coverImage }" aria-hidden="true">
      <img v-if="article.coverImage" :src="article.coverImage" :alt="article.title" />
      <span v-else>{{ articleInitial }}</span>
    </div>
  </article>
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
  position: relative;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 110px;
  gap: 12px;
  overflow: hidden;
  min-height: 150px;
  margin-bottom: 0;
  padding: 22px;
  cursor: pointer;
  background: color-mix(in srgb, var(--panel-bg) 96%, transparent);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  transition:
    transform 0.22s ease,
    box-shadow 0.22s ease,
    border-color 0.22s ease,
    background-color 0.22s ease;
}

.article-card::before {
  position: absolute;
  top: 14px;
  bottom: 14px;
  left: -1px;
  width: 3px;
  border-radius: 999px;
  background: linear-gradient(180deg, var(--primary-color), var(--accent-color));
  content: '';
  opacity: 0;
  transform: scaleY(0.55);
  transform-origin: center;
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.article-card:hover::before,
.article-card:focus-visible::before {
  opacity: 1;
  transform: scaleY(1);
}

.article-card:hover,
.article-card:focus-visible {
  transform: translateY(-2px);
  border-color: color-mix(in srgb, var(--primary-color) 38%, var(--soft-border-color));
  box-shadow: 0 18px 42px var(--theme-glow-color);
}

.article-card:focus-visible {
  outline: 2px solid var(--primary-color);
  outline-offset: 2px;
}

.entry-copy {
  min-width: 0;
}

.article-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 14px;
  margin-bottom: 8px;
  color: var(--muted-text-color);
  font-size: 13px;
}

.topic {
  color: var(--primary-color);
  font-weight: 760;
}

.card-title {
  margin: 0 0 8px;
  color: var(--text-color);
  font-size: 21px;
  line-height: 1.35;
  font-weight: 780;
}

.card-summary {
  display: -webkit-box;
  margin: 0;
  overflow: hidden;
  color: var(--muted-text-color);
  font-size: 14px;
  line-height: 1.7;
  text-overflow: ellipsis;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.entry-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
  margin-top: 14px;
}

.tag-chip {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 11px;
  border: 1px solid var(--border-color);
  border-radius: 999px;
  background: color-mix(in srgb, var(--panel-bg) 92%, transparent);
  color: var(--muted-text-color);
  font-size: 12px;
  font-weight: 650;
  text-decoration: none;
  cursor: pointer;
  transition:
    transform 0.22s ease,
    border-color 0.22s ease,
    background-color 0.22s ease,
    color 0.22s ease,
    box-shadow 0.22s ease;
}

.tag-link:hover,
.tag-link:focus-visible {
  transform: translateY(-1px);
  border-color: color-mix(in srgb, var(--primary-color) 72%, var(--border-color));
  background: var(--surface-wash-color);
  color: var(--primary-color);
  box-shadow: var(--shadow-sm);
}

.tag-link:focus-visible {
  outline: 2px solid var(--primary-color);
  outline-offset: 2px;
}

.article-visual {
  position: relative;
  min-height: 106px;
  overflow: hidden;
  border-radius: var(--radius-md);
  background:
    linear-gradient(135deg, color-mix(in srgb, var(--primary-color) 26%, transparent), transparent),
    repeating-linear-gradient(180deg, color-mix(in srgb, var(--accent-color) 20%, transparent) 0 4px, transparent 4px 12px),
    var(--code-bg);
}

.article-visual::after {
  position: absolute;
  top: 0;
  left: -30%;
  width: 34%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.36), transparent);
  content: '';
  transform: skewX(-18deg);
  transition: left 0.52s ease;
}

.article-card:hover .article-visual::after,
.article-card:focus-visible .article-visual::after {
  left: 105%;
}

.article-visual span {
  position: absolute;
  right: 12px;
  bottom: 4px;
  color: color-mix(in srgb, var(--primary-color) 66%, transparent);
  font-family: Georgia, "Times New Roman", serif;
  font-size: 64px;
  font-weight: 500;
  line-height: 1;
}

.article-visual.has-cover img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

@media (max-width: 720px) {
  .article-card {
    grid-template-columns: 1fr;
    padding: 18px;
  }

  .article-visual {
    min-height: 118px;
    aspect-ratio: 16 / 9;
  }
}

@media (prefers-reduced-motion: reduce) {
  .article-card,
  .article-card::before,
  .article-visual::after,
  .tag-link {
    transition: none;
  }

  .article-card:hover,
  .article-card:focus-visible,
  .tag-link:hover {
    transform: none;
  }
}
</style>
