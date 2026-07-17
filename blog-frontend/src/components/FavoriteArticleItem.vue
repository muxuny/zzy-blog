<template>
  <article class="favorite-item" :class="{ 'is-unavailable': !item.available }">
    <RouterLink
      v-if="item.available"
      class="card-open-link"
      :to="`/article/${item.articleId}`"
      :aria-label="`打开文章：${item.title}`"
    />

    <div class="item-heading">
      <h2 class="item-title">
        <span v-if="item.available" class="title-text">{{ item.title }}</span>
        <el-tooltip
          v-else
          content="该文章暂未公开"
          placement="top"
          :trigger="['hover', 'focus']"
        >
          <span class="title-snapshot" tabindex="0">{{ item.title }}</span>
        </el-tooltip>
      </h2>
    </div>

    <el-button
      class="remove-button"
      circle
      text
      type="danger"
      :loading="removing"
      title="取消收藏"
      aria-label="取消收藏"
      @click.stop="$emit('remove', item)"
    >
      <el-icon><Delete /></el-icon>
    </el-button>

    <template v-if="item.available">
      <div class="available-details">
        <p v-if="item.summary" class="item-summary">{{ item.summary }}</p>
        <img
          v-if="item.coverImage"
          class="item-cover"
          :src="item.coverImage"
          :alt="item.title"
        />
        <div v-if="item.authorName || item.viewCount !== undefined" class="item-meta">
          <span v-if="item.authorName">{{ item.authorName }}</span>
          <span>阅读 {{ item.viewCount || 0 }}</span>
        </div>
        <div v-if="item.tags?.length" class="item-tags" aria-label="文章标签">
          <el-tag v-for="tag in item.tags" :key="tag.id" size="small" effect="plain">
            {{ tag.name }}
          </el-tag>
        </div>
      </div>
    </template>

    <div class="item-foot">
      <span>收藏于 {{ formatDate(item.favoritedAt) }}</span>
      <span v-if="!item.available" class="unavailable-note">该文章暂未公开</span>
    </div>
  </article>
</template>

<script setup>
import { Delete } from '@element-plus/icons-vue'
import { formatDate } from '../utils'

defineProps({
  item: { type: Object, required: true },
  removing: { type: Boolean, default: false }
})

defineEmits(['remove'])
</script>

<style scoped>
.favorite-item {
  position: relative;
  min-width: 0;
  padding: 18px 20px;
  border: 1px solid var(--soft-border-color);
  border-radius: var(--radius-md);
  background: var(--panel-bg);
  transition: border-color 0.18s ease, box-shadow 0.18s ease;
}

.favorite-item:not(.is-unavailable):hover {
  border-color: color-mix(in srgb, var(--primary-color) 36%, var(--soft-border-color));
  box-shadow: var(--shadow-sm);
}

.card-open-link {
  position: absolute;
  inset: 0;
  z-index: 1;
  border-radius: inherit;
}

.card-open-link:focus-visible {
  outline: 2px solid var(--primary-color);
  outline-offset: 3px;
}

.favorite-item.is-unavailable {
  border-color: color-mix(in srgb, var(--muted-text-color) 30%, var(--soft-border-color));
  background: color-mix(in srgb, var(--muted-text-color) 7%, var(--panel-bg));
}

.item-heading {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 36px;
  min-height: 36px;
  align-items: start;
  gap: 14px;
}

.item-title {
  min-width: 0;
  margin: 2px 0 0;
  font-size: 20px;
  line-height: 1.45;
  overflow-wrap: anywhere;
}

.title-text {
  color: var(--text-color);
  font-weight: 800;
}

.title-snapshot {
  display: inline;
  color: color-mix(in srgb, var(--muted-text-color) 86%, var(--text-color));
  font-weight: 760;
}

.title-snapshot:focus-visible {
  border-radius: 2px;
  outline: 2px solid var(--muted-text-color);
  outline-offset: 3px;
}

.remove-button {
  position: absolute;
  top: 18px;
  right: 20px;
  z-index: 2;
  width: 36px;
  height: 36px;
  min-width: 36px;
  min-height: 36px;
  margin: 0;
}

.available-details {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 144px;
  gap: 10px 18px;
  margin-top: 10px;
}

.item-summary {
  grid-column: 1;
  margin: 0;
  display: -webkit-box;
  overflow: hidden;
  color: var(--muted-text-color);
  font-size: 14px;
  line-height: 1.7;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.item-cover {
  grid-column: 2;
  grid-row: 1 / span 3;
  width: 144px;
  height: 96px;
  border: 1px solid var(--soft-border-color);
  border-radius: var(--radius-sm);
  object-fit: cover;
}

.item-meta,
.item-tags,
.item-foot {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.item-meta {
  grid-column: 1;
  gap: 6px 14px;
  color: var(--muted-text-color);
  font-size: 13px;
}

.item-tags {
  grid-column: 1;
  gap: 7px;
}

.item-foot {
  justify-content: space-between;
  gap: 8px 16px;
  margin-top: 13px;
  padding-top: 11px;
  border-top: 1px solid var(--soft-border-color);
  color: var(--muted-text-color);
  font-size: 12px;
}

.unavailable-note {
  color: var(--text-color);
  font-weight: 700;
}

@media (max-width: 640px) {
  .favorite-item {
    padding: 14px;
  }

  .item-heading {
    gap: 10px;
  }

  .remove-button {
    top: 14px;
    right: 14px;
  }

  .item-title {
    font-size: 18px;
  }

  .available-details {
    grid-template-columns: minmax(0, 1fr);
  }

  .item-cover {
    grid-column: 1;
    grid-row: auto;
    width: 100%;
    height: auto;
    aspect-ratio: 16 / 9;
  }
}
</style>
