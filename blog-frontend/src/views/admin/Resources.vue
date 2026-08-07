<template>
  <div class="resources-page">
    <div class="page-head">
      <div>
        <span class="page-eyebrow">{{ pageCopy.eyebrow }}</span>
        <h2>{{ pageCopy.title }}</h2>
        <p v-if="pageCopy.description" class="page-description">{{ pageCopy.description }}</p>
      </div>
    </div>

    <div class="resource-summary">
      <section class="resource-tile">
        <span class="tiny-label">标签数量</span>
        <strong>{{ tagTotal }}</strong>
        <div class="row-meta">用于首页筛选和文章归类</div>
      </section>
      <section class="resource-tile">
        <span class="tiny-label">图片素材</span>
        <strong>{{ imageTotal }}</strong>
        <div class="row-meta">封面和正文图片</div>
      </section>
      <section class="resource-tile">
        <span class="tiny-label">最近上传</span>
        <strong>{{ recentUploadCount }}</strong>
        <div class="row-meta">本页近 7 天</div>
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

        <el-pagination
          v-if="showTagPagination"
          v-model:current-page="tagPage"
          :total="tagTotal"
          :page-size="tagSize"
          layout="prev,pager,next"
          class="resource-pagination"
          @current-change="handleTagPageChange"
        />
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
            <button
              class="mini-image preview-thumb-button"
              type="button"
              :style="imageBackground(image)"
              :aria-label="`预览图片：${getImageName(image)}`"
              @click="openImagePreview(image)"
            >
              <span class="visually-hidden-text">预览图片</span>
            </button>
            <div>
              <div class="image-name">{{ getImageName(image) }}</div>
              <div class="row-meta">{{ formatImageSize(image.size) }} / {{ image.createdBy || '未知' }}</div>
            </div>
            <div class="image-card-actions">
              <button class="row-action-button" type="button" @click="openImagePreview(image)">预览</button>
              <button class="row-action-button is-danger" type="button" @click="handleDeleteImage(image.id)">删除</button>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">暂无图片素材</div>

        <el-pagination
          v-if="showImagePagination"
          v-model:current-page="imagePage"
          :total="imageTotal"
          :page-size="imageSize"
          layout="prev,pager,next"
          class="resource-pagination"
          @current-change="handleImagePageChange"
        />
      </section>
    </div>

    <el-dialog
      v-model="previewDialog.visible"
      class="image-preview-dialog"
      :title="previewImageName"
      width="min(920px, 92vw)"
      align-center
      @closed="closeImagePreview"
    >
      <div v-if="previewImage" class="image-preview-content">
        <div class="image-preview-stage" :class="{ 'is-error': previewDialog.loadError }">
          <img
            v-if="previewImage.url && !previewDialog.loadError"
            :src="previewImage.url"
            :alt="previewImageName"
            @error="previewDialog.loadError = true"
          >
          <div v-else class="image-preview-error">图片无法预览</div>
        </div>

        <dl class="image-preview-meta">
          <div>
            <dt>文件名</dt>
            <dd>{{ previewImageName }}</dd>
          </div>
          <div>
            <dt>文件大小</dt>
            <dd>{{ formatImageSize(previewImage.size) }}</dd>
          </div>
          <div>
            <dt>上传人</dt>
            <dd>{{ previewImage.createdBy || '未知' }}</dd>
          </div>
          <div>
            <dt>上传时间</dt>
            <dd>{{ formatDate(previewImage.createdAt) || '暂无时间' }}</dd>
          </div>
          <div class="image-url-row">
            <dt>图片地址</dt>
            <dd>{{ previewImage.url || '暂无地址' }}</dd>
          </div>
        </dl>
      </div>

      <template #footer>
        <div class="image-preview-footer">
          <button class="tool-button" type="button" :disabled="!previewImage?.url" @click="copyImageUrl">复制地址</button>
          <button class="tool-button is-danger" type="button" :disabled="!previewImage?.id" @click="handleDeletePreviewImage">删除图片</button>
          <button class="tool-button is-primary" type="button" @click="previewDialog.visible = false">关闭</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createTag, deleteTag, getAdminTags } from '../../api/tag'
import { deleteImage, getImages, uploadImage } from '../../api/image'
import { formatDate } from '../../utils'
import {
  getPageAfterSingleDeletion,
  normalizePageResult,
  shouldShowPagination
} from '../../utils/pagination'
import { usePageCopyStore } from '../../stores/pageCopy'

const pageCopyStore = usePageCopyStore()
const tags = ref([])
const images = ref([])
const tagPage = ref(1)
const tagSize = ref(10)
const tagTotal = ref(0)
const imagePage = ref(1)
const imageSize = ref(12)
const imageTotal = ref(0)
const newName = ref('')
const creating = ref(false)
const uploading = ref(false)
const fileInput = ref(null)
const previewDialog = ref({
  visible: false,
  image: null,
  loadError: false
})

const showTagPagination = computed(() => shouldShowPagination(tagTotal.value, tagSize.value))
const showImagePagination = computed(() => shouldShowPagination(imageTotal.value, imageSize.value))
const recentUploadCount = computed(() => {
  const sevenDaysAgo = Date.now() - 7 * 24 * 60 * 60 * 1000
  return images.value.filter(image => {
    const time = new Date(image.createdAt || 0).getTime()
    return Number.isFinite(time) && time >= sevenDaysAgo
  }).length
})
const pageCopy = computed(() => pageCopyStore.resolveCopy('admin.resources'))
const previewImage = computed(() => previewDialog.value.image)
const previewImageName = computed(() => getImageName(previewImage.value))

onMounted(() => {
  void pageCopyStore.loadAdminCopies()
  loadResources()
})

async function loadResources() {
  await Promise.all([loadTags(), loadImages()])
}

async function loadTags() {
  const result = await getAdminTags({ page: tagPage.value, size: tagSize.value })
  const pageResult = normalizePageResult(result, tagSize.value)
  const maxPage = Math.max(1, Math.ceil(pageResult.total / tagSize.value))
  if (tagPage.value > maxPage) {
    tagPage.value = maxPage
    await loadTags()
    return
  }
  tags.value = pageResult.records
  tagTotal.value = pageResult.total
}

async function loadImages() {
  const result = await getImages({ page: imagePage.value, size: imageSize.value })
  const pageResult = normalizePageResult(result, imageSize.value)
  const maxPage = Math.max(1, Math.ceil(pageResult.total / imageSize.value))
  if (imagePage.value > maxPage) {
    imagePage.value = maxPage
    await loadImages()
    return
  }
  images.value = pageResult.records
  imageTotal.value = pageResult.total
}

function handleTagPageChange(nextPage) {
  tagPage.value = nextPage
  void loadTags()
}

function handleImagePageChange(nextPage) {
  imagePage.value = nextPage
  void loadImages()
}

async function handleCreate() {
  const name = newName.value.trim()
  if (!name || creating.value) return
  creating.value = true
  try {
    await createTag({ name })
    ElMessage.success('创建成功')
    newName.value = ''
    tagPage.value = 1
    await loadTags()
  } finally {
    creating.value = false
  }
}

async function handleDeleteTag(id) {
  if (!id) return
  await deleteTag(id)
  ElMessage.success('删除成功')
  tagPage.value = getPageAfterSingleDeletion({
    page: tagPage.value,
    size: tagSize.value,
    total: tagTotal.value
  })
  await loadTags()
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
    imagePage.value = 1
    await loadImages()
  } finally {
    uploading.value = false
    event.target.value = ''
  }
}

async function handleDeleteImage(id, options = {}) {
  if (!id) return
  await deleteImage(id)
  ElMessage.success('删除成功')
  if (options.closePreview) {
    closeImagePreview()
  }
  imagePage.value = getPageAfterSingleDeletion({
    page: imagePage.value,
    size: imageSize.value,
    total: imageTotal.value
  })
  await loadImages()
}

function getImageName(image) {
  return image?.originalName || image?.filename || image?.name || '未命名图片'
}

function openImagePreview(image) {
  if (!image?.url) {
    ElMessage.warning('图片地址不可用')
    return
  }
  previewDialog.value = {
    visible: true,
    image,
    loadError: false
  }
}

function closeImagePreview() {
  previewDialog.value = {
    visible: false,
    image: null,
    loadError: false
  }
}

async function copyImageUrl() {
  const url = previewImage.value?.url
  if (!url) {
    ElMessage.warning('图片地址不可用')
    return
  }
  try {
    if (!navigator.clipboard?.writeText) {
      throw new Error('Clipboard API unavailable')
    }
    await navigator.clipboard.writeText(url)
    ElMessage.success('图片地址已复制')
  } catch (error) {
    ElMessage.error('复制失败')
  }
}

async function handleDeletePreviewImage() {
  await handleDeleteImage(previewImage.value?.id, { closePreview: true })
}

function imageBackground(image) {
  if (!image?.url) return {}
  return { backgroundImage: `url("${image.url}")` }
}

function formatImageSize(size) {
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

.preview-thumb-button {
  position: relative;
  border: 0;
  padding: 0;
  cursor: pointer;
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
}

.preview-thumb-button:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.preview-thumb-button:focus-visible {
  outline: 2px solid color-mix(in srgb, var(--primary-color) 72%, white);
  outline-offset: 2px;
}

.visually-hidden-text {
  position: absolute;
  width: 1px;
  height: 1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
}

.image-card-actions {
  display: flex;
  justify-content: flex-end;
  gap: 6px;
  flex-wrap: wrap;
}

:global(.image-preview-dialog .el-dialog__body) {
  padding-top: 10px;
}

.image-preview-content {
  display: grid;
  gap: 16px;
}

.image-preview-stage {
  display: grid;
  place-items: center;
  min-height: 280px;
  max-height: min(62vh, 620px);
  overflow: hidden;
  border: 1px solid var(--soft-border-color);
  border-radius: var(--radius-md);
  background:
    linear-gradient(135deg, color-mix(in srgb, var(--primary-color) 8%, transparent), transparent 64%),
    color-mix(in srgb, var(--code-bg) 74%, var(--panel-bg));
}

.image-preview-stage img {
  display: block;
  max-width: 100%;
  max-height: min(62vh, 620px);
  object-fit: contain;
}

.image-preview-error {
  color: var(--muted-color);
}

.image-preview-meta {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin: 0;
}

.image-preview-meta div {
  min-width: 0;
  padding: 10px 12px;
  border: 1px solid var(--soft-border-color);
  border-radius: var(--radius-sm);
  background: color-mix(in srgb, var(--panel-bg) 78%, transparent);
}

.image-preview-meta dt {
  margin-bottom: 4px;
  color: var(--muted-color);
  font-size: 12px;
}

.image-preview-meta dd {
  margin: 0;
  overflow-wrap: anywhere;
  color: var(--text-color);
  font-weight: 680;
}

.image-url-row {
  grid-column: 1 / -1;
}

.image-preview-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  flex-wrap: wrap;
}

.image-preview-footer .tool-button.is-danger {
  border-color: color-mix(in srgb, var(--danger-color) 52%, transparent);
  color: var(--danger-color);
}

.resource-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
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

  .image-card-actions,
  .image-preview-footer {
    justify-content: flex-start;
  }

  .image-preview-meta {
    grid-template-columns: 1fr;
  }

  .image-preview-stage {
    min-height: 220px;
  }

  .resource-pagination {
    justify-content: center;
  }
}
</style>
