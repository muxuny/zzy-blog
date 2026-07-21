<template>
  <article
    class="reading-history-item"
    :class="{ 'is-unavailable': !item.available }"
  >
    <RouterLink
      v-if="item.available"
      class="item-open-link"
      :to="`/article/${item.articleId}`"
      :aria-label="`打开文章：${item.title}`"
    />

    <el-button
      class="remove-button"
      circle
      text
      type="danger"
      :icon="Delete"
      :loading="removing"
      :disabled="disabled"
      title="删除历史"
      aria-label="删除历史"
      @click.stop="$emit('remove', item)"
    />

    <template v-if="item.available">
      <div class="available-content" :class="{ 'has-cover': item.coverImage }">
        <img
          v-if="item.coverImage"
          class="item-cover"
          :src="item.coverImage"
          :alt="item.title"
        />
        <div class="item-copy">
          <h3 class="item-title">
            <span class="title-text">{{ item.title }}</span>
          </h3>
          <p v-if="item.summary" class="item-summary">{{ item.summary }}</p>
          <div class="item-meta">
            <span v-if="item.authorName">作者 {{ item.authorName }}</span>
            <span v-if="item.viewCount !== undefined">浏览 {{ item.viewCount || 0 }}</span>
          </div>
        </div>
      </div>
    </template>
    <template v-else>
      <div class="unavailable-content">
        <h3 class="item-title">
          <el-tooltip
            content="该文章暂未公开"
            placement="top"
            :trigger="['hover', 'focus']"
          >
            <span class="title-snapshot" tabindex="0">{{ item.title }}</span>
          </el-tooltip>
        </h3>
        <p class="unavailable-note">该文章暂未公开</p>
      </div>
    </template>

    <div class="reading-meta" aria-label="阅读时间信息">
      <span>最近阅读 {{ formatReadingTime(item.lastReadAt) }}</span>
      <span>首次阅读 {{ formatReadingTime(item.firstReadAt) }}</span>
      <span>阅读 {{ item.readCount || 0 }} 次</span>
    </div>
  </article>
</template>

<script setup>
import { Delete } from '@element-plus/icons-vue'
import { formatReadingTime } from '../utils/readingHistory'

defineProps({
  item: { type: Object, required: true },
  removing: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false }
})

defineEmits(['remove'])
</script>

<style scoped>
.reading-history-item {
  position: relative;
  min-width: 0;
  padding: 18px 64px 16px 18px;
  overflow-wrap: anywhere;
  border: 1px solid var(--soft-border-color);
  border-radius: var(--radius-md);
  background: var(--panel-bg);
  transition: border-color 0.18s ease, box-shadow 0.18s ease;
}

.reading-history-item:not(.is-unavailable):hover {
  border-color: color-mix(in srgb, var(--primary-color) 36%, var(--soft-border-color));
  box-shadow: var(--shadow-sm);
}

.item-open-link {
  position: absolute;
  inset: 0;
  z-index: 1;
  border-radius: inherit;
}

.item-open-link:focus-visible {
  outline: 2px solid var(--primary-color);
  outline-offset: 3px;
}

.remove-button {
  position: absolute;
  top: 14px;
  right: 16px;
  z-index: 3;
  width: 36px;
  height: 36px;
  min-width: 36px;
  min-height: 36px;
  margin: 0;
}

.available-content {
  display: grid;
  min-width: 0;
}

.available-content.has-cover {
  grid-template-columns: 144px minmax(0, 1fr);
  gap: 18px;
}

.item-cover {
  display: block;
  width: 144px;
  height: 96px;
  border: 1px solid var(--soft-border-color);
  border-radius: var(--radius-sm);
  object-fit: cover;
}

.item-copy,
.unavailable-content {
  min-width: 0;
}

.item-title {
  margin: 1px 0 0;
  color: var(--text-color);
  font-size: 19px;
  line-height: 1.45;
  overflow-wrap: anywhere;
}

.title-text {
  font-weight: 800;
}

.title-snapshot {
  color: color-mix(in srgb, var(--muted-text-color) 86%, var(--text-color));
  font-weight: 760;
}

.title-snapshot:focus-visible {
  border-radius: 2px;
  outline: 2px solid var(--muted-text-color);
  outline-offset: 3px;
}

.item-summary {
  display: -webkit-box;
  margin: 8px 0 0;
  overflow: hidden;
  color: var(--muted-text-color);
  font-size: 14px;
  line-height: 1.65;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.item-meta,
.reading-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 5px 16px;
  color: var(--muted-text-color);
  font-size: 12px;
}

.item-meta {
  margin-top: 10px;
}

.reading-meta {
  margin-top: 14px;
  padding-top: 11px;
  border-top: 1px solid var(--soft-border-color);
}

.reading-history-item.is-unavailable {
  border-color: color-mix(in srgb, var(--muted-text-color) 30%, var(--soft-border-color));
  background: color-mix(in srgb, var(--muted-text-color) 7%, var(--panel-bg));
}

.unavailable-note {
  margin: 7px 0 0;
  color: var(--muted-text-color);
  font-size: 13px;
  font-weight: 700;
}

@media (max-width: 640px) {
  .reading-history-item {
    padding: 14px 54px 14px 14px;
  }

  .remove-button {
    top: 10px;
    right: 10px;
  }

  .available-content.has-cover {
    grid-template-columns: minmax(0, 1fr);
    gap: 13px;
  }

  .item-cover {
    width: 100%;
    height: auto;
    aspect-ratio: 16 / 9;
  }

  .item-title {
    font-size: 17px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .reading-history-item {
    transition: none;
  }
}
</style>
