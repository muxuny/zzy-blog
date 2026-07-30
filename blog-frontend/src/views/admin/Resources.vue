<template>
  <div class="resources-page">
    <div class="page-head">
      <div>
        <span class="page-eyebrow">内容资源</span>
        <h2>资源管理</h2>
      </div>
    </div>

    <div class="resource-summary">
      <section class="resource-tile">
        <span class="tiny-label">标签数量</span>
        <strong>{{ tags.length }}</strong>
        <div class="row-meta">用于首页筛选和文章归类</div>
      </section>
      <section class="resource-tile">
        <span class="tiny-label">图片素材</span>
        <strong>{{ images.length }}</strong>
        <div class="row-meta">封面和正文图片</div>
      </section>
      <section class="resource-tile">
        <span class="tiny-label">最近上传</span>
        <strong>{{ recentUploadCount }}</strong>
        <div class="row-meta">近 7 天变化</div>
      </section>
      <section class="resource-tile">
        <span class="tiny-label">资源操作</span>
        <strong>2</strong>
        <div class="row-meta">新增标签 / 上传图片</div>
      </section>
    </div>

    <div class="resource-board">
      <section class="surface">
        <div class="panel-head">
          <div>
            <span class="section-eyebrow">标签库</span>
            <h3>新增和维护标签</h3>
          </div>
        </div>

        <form class="inline-create" @submit.prevent="handleCreate">
          <input v-model="newName" aria-label="标签名" placeholder="标签名">
          <button class="tool-button is-primary" type="submit" :disabled="creating">
            {{ creating ? '新增中' : '新增' }}
          </button>
        </form>

        <div v-if="tags.length" class="tag-list">
          <div v-for="tag in tags" :key="tag.id || tag.name" class="tag-item">
            <span class="chip">{{ tag.name }}</span>
            <span class="row-meta">{{ formatDate(tag.createdAt) || '暂无时间' }}</span>
            <button class="row-action-button is-danger" type="button" @click="handleDeleteTag(tag.id)">删除</button>
          </div>
        </div>
        <div v-else class="empty-state">暂无标签</div>
      </section>

      <section class="surface">
        <div class="panel-head">
          <div>
            <span class="section-eyebrow">图片素材</span>
            <h3>上传和清理图片</h3>
          </div>
          <button class="tool-button is-primary" type="button" :disabled="uploading" @click="openUpload">
            {{ uploading ? '上传中' : '上传图片' }}
          </button>
          <input
            ref="fileInput"
            class="visually-hidden-file"
            type="file"
            accept="image/*"
            @change="handleUpload"
          >
        </div>

        <div v-if="images.length" class="mini-image-grid" aria-label="图片缩略预览">
          <div v-for="image in images" :key="image.id || image.url" class="image-card">
            <span class="mini-image" :style="imageBackground(image)" aria-hidden="true" />
            <div>
              <div class="image-name">{{ image.originalName || image.name || '未命名图片' }}</div>
              <div class="row-meta">{{ imageSize(image.size) }} / {{ image.createdBy || '未知' }}</div>
            </div>
            <button class="row-action-button is-danger" type="button" @click="handleDeleteImage(image.id)">删除</button>
          </div>
        </div>
        <div v-else class="empty-state">暂无图片素材</div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createTag, deleteTag, getTags } from '../../api/tag'
import { deleteImage, getImages, uploadImage } from '../../api/image'
import { formatDate } from '../../utils'

const tags = ref([])
const images = ref([])
const newName = ref('')
const creating = ref(false)
const uploading = ref(false)
const fileInput = ref(null)

const recentUploadCount = computed(() => {
  const sevenDaysAgo = Date.now() - 7 * 24 * 60 * 60 * 1000
  return images.value.filter(image => {
    const time = new Date(image.createdAt || 0).getTime()
    return Number.isFinite(time) && time >= sevenDaysAgo
  }).length
})

onMounted(loadResources)

async function loadResources() {
  const [tagResult, imageResult] = await Promise.all([
    getTags(),
    getImages({ page: 1, size: 100 })
  ])
  tags.value = tagResult.data || []
  images.value = imageResult.data || []
}

async function handleCreate() {
  const name = newName.value.trim()
  if (!name || creating.value) return
  creating.value = true
  try {
    await createTag({ name })
    ElMessage.success('创建成功')
    newName.value = ''
    const result = await getTags()
    tags.value = result.data || []
  } finally {
    creating.value = false
  }
}

async function handleDeleteTag(id) {
  if (!id) return
  await deleteTag(id)
  ElMessage.success('删除成功')
  tags.value = tags.value.filter(tag => tag.id !== id)
}

function openUpload() {
  fileInput.value?.click()
}

async function handleUpload(event) {
  const file = event.target.files?.[0]
  if (!file || uploading.value) return
  uploading.value = true
  try {
    await uploadImage(file)
    ElMessage.success('上传成功')
    const result = await getImages({ page: 1, size: 100 })
    images.value = result.data || []
  } finally {
    uploading.value = false
    event.target.value = ''
  }
}

async function handleDeleteImage(id) {
  if (!id) return
  await deleteImage(id)
  ElMessage.success('删除成功')
  const result = await getImages({ page: 1, size: 100 })
  images.value = result.data || []
}

function imageBackground(image) {
  if (!image?.url) return {}
  return { backgroundImage: `url("${image.url}")` }
}

function imageSize(size) {
  if (!Number.isFinite(Number(size))) return '未知大小'
  return `${(Number(size) / 1024).toFixed(1)} KB`
}
</script>

<style scoped>
.resources-page {
  display: grid;
  gap: 16px;
}

.resource-summary {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.resource-tile {
  min-height: 112px;
  padding: 16px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background:
    linear-gradient(135deg, color-mix(in srgb, var(--primary-color) 9%, transparent), transparent 62%),
    color-mix(in srgb, var(--panel-bg) 88%, transparent);
  box-shadow: var(--shadow-sm);
}

.resource-tile strong {
  display: block;
  margin: 8px 0 4px;
  color: var(--text-color);
  font-family: Georgia, "Times New Roman", serif;
  font-size: 34px;
  font-weight: 500;
  line-height: 1;
}

.resource-board {
  display: grid;
  grid-template-columns: minmax(280px, 0.72fr) minmax(0, 1fr);
  gap: 16px;
}

.inline-create {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 8px;
  margin-bottom: 14px;
}

.tag-list {
  display: grid;
  gap: 8px;
}

.tag-item {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto auto;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid var(--soft-border-color);
}

.tag-item:last-child {
  border-bottom: 0;
}

.mini-image-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.image-card {
  display: grid;
  grid-template-columns: 64px minmax(0, 1fr) auto;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border: 1px solid var(--soft-border-color);
  border-radius: var(--radius-sm);
  background: color-mix(in srgb, var(--panel-bg) 72%, transparent);
}

.mini-image {
  width: 64px;
  height: 48px;
  border-radius: var(--radius-sm);
  background:
    linear-gradient(135deg, color-mix(in srgb, var(--primary-color) 24%, transparent), transparent 66%),
    color-mix(in srgb, var(--code-bg) 80%, var(--panel-bg));
  background-position: center;
  background-size: cover;
}

.image-name {
  overflow: hidden;
  color: var(--text-color);
  font-weight: 760;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.visually-hidden-file {
  position: absolute;
  width: 1px;
  height: 1px;
  overflow: hidden;
  opacity: 0;
  pointer-events: none;
}

@media (max-width: 1080px) {
  .resource-summary {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .resource-board {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .resource-summary,
  .mini-image-grid {
    grid-template-columns: 1fr;
  }

  .tag-item,
  .image-card {
    grid-template-columns: 1fr;
  }
}
</style>
