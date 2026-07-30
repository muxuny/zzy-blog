<template>
  <div class="article-editor-page">
    <div class="page-head">
      <div>
        <span class="page-eyebrow">{{ pageEyebrow }}</span>
        <h2>{{ pageTitle }}</h2>
      </div>
      <button class="tool-button" type="button" :disabled="!!savingStatus" @click="$router.back()">返回</button>
    </div>

    <div class="editor-layout">
      <section class="editor-panel">
        <div class="panel-head">
          <div>
            <span class="section-eyebrow">文章内容</span>
            <h3>正文与基础信息</h3>
          </div>
        </div>

        <el-form :model="form" label-position="top" class="article-form">
          <el-form-item label="标题">
            <el-input v-model="form.title" placeholder="文章标题" />
          </el-form-item>
          <el-form-item label="摘要">
            <el-input v-model="form.summary" type="textarea" :rows="2" placeholder="文章摘要" />
          </el-form-item>
          <el-form-item label="封面图">
            <ImageUploader v-model="form.coverImage" />
          </el-form-item>
          <el-form-item label="标签">
            <el-select v-model="form.tagIds" multiple placeholder="选择标签" class="tag-select">
              <el-option v-for="tag in tags" :key="tag.id" :label="tag.name" :value="tag.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="可见性">
            <el-radio-group v-model="form.visibility">
              <el-radio-button label="public">公开</el-radio-button>
              <el-radio-button label="private">仅自己可见</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="内容">
            <ArticleEditor v-model="form.content" />
          </el-form-item>
        </el-form>
      </section>

      <aside class="editor-panel editor-side">
        <div>
          <span class="section-eyebrow">发布状态</span>
          <h3>{{ statusText(form.status) }}</h3>
          <p class="panel-caption">保存、提交审核或直接发布都在这里完成。</p>
        </div>

        <div class="info-grid compact-info">
          <div class="info-item">
            <span class="tiny-label">已选标签</span>
            <strong>{{ form.tagIds.length }}</strong>
          </div>
          <div class="info-item">
            <span class="tiny-label">可见性</span>
            <strong>{{ visibilityLabel }}</strong>
          </div>
        </div>

        <div class="editor-actions">
          <button
            class="tool-button"
            type="button"
            :disabled="!!savingStatus"
            @click="save('draft')"
          >
            {{ savingStatus === 'draft' ? '保存中' : '保存草稿' }}
          </button>
          <button
            class="tool-button is-primary"
            type="button"
            :disabled="!!savingStatus"
            @click="save('pending')"
          >
            {{ savingStatus === 'pending' ? '提交中' : '提交审核' }}
          </button>
          <button
            class="tool-button is-success"
            type="button"
            :disabled="!!savingStatus"
            @click="save('published')"
          >
            {{ savingStatus === 'published' ? '发布中' : '发布' }}
          </button>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createAdminArticle, getAdminArticle, updateAdminArticle } from '../../api/article'
import { getTags } from '../../api/tag'
import ArticleEditor from '../../components/admin/ArticleEditor.vue'
import ImageUploader from '../../components/admin/ImageUploader.vue'
import { articleVisibilityText, normalizeArticleVisibility } from '../../utils/articleVisibility'
import { normalizeArticleMarkdown } from '../../utils/reading'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.params.id)
const tags = ref([])
const savingStatus = ref('')
const form = reactive({
  title: '',
  content: '',
  summary: '',
  coverImage: '',
  status: 'draft',
  visibility: 'public',
  tagIds: []
})

const pageEyebrow = computed(() => (isEdit.value ? '内容编辑' : '内容创建'))
const pageTitle = computed(() => (isEdit.value ? '编辑文章' : '写文章'))
const visibilityLabel = computed(() => articleVisibilityText(form.visibility))

onMounted(async () => {
  const tagResult = await getTags()
  tags.value = tagResult.data || []
  if (isEdit.value) {
    const result = await getAdminArticle(route.params.id)
    const article = result.data
    form.title = article.title
    form.content = article.content
    form.summary = article.summary || ''
    form.coverImage = article.coverImage || ''
    form.status = article.status || 'draft'
    form.visibility = normalizeArticleVisibility(article.visibility)
    form.tagIds = (article.tags || []).map(tag => tag.id)
  }
})

function statusText(status) {
  if (status === 'pending') return '待审核'
  if (status === 'published') return '已发布'
  if (status === 'rejected') return '已驳回'
  return '草稿'
}

async function save(status) {
  if (savingStatus.value) return
  savingStatus.value = status
  form.status = status
  const payload = {
    ...form,
    content: normalizeArticleMarkdown(form.content),
    tagIds: [...form.tagIds]
  }
  try {
    if (isEdit.value) {
      await updateAdminArticle(route.params.id, payload)
      ElMessage.success('更新成功')
    } else {
      await createAdminArticle(payload)
      ElMessage.success('创建成功')
    }
    router.push('/admin/articles')
  } finally {
    savingStatus.value = ''
  }
}
</script>

<style scoped>
.article-editor-page {
  display: grid;
  gap: 16px;
}

.editor-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(240px, 280px);
  align-items: start;
  gap: 16px;
}

.article-form {
  display: grid;
  gap: 2px;
}

.tag-select {
  width: 100%;
}

.editor-side {
  position: sticky;
  top: 18px;
  display: grid;
  gap: 16px;
}

.compact-info {
  display: grid;
  gap: 8px;
}

.info-item {
  display: grid;
  gap: 6px;
  min-height: 66px;
  padding: 12px;
  border: 1px solid var(--soft-border-color);
  border-radius: var(--radius-sm);
  background: color-mix(in srgb, var(--panel-bg) 72%, transparent);
}

.info-item strong {
  color: var(--text-color);
}

.editor-actions {
  display: grid;
  gap: 8px;
}

.tool-button.is-success {
  border-color: color-mix(in srgb, var(--accent-color) 42%, transparent);
  color: color-mix(in srgb, var(--accent-color) 72%, var(--text-color));
  background: color-mix(in srgb, var(--accent-color) 10%, transparent);
}

:deep(.el-form-item__label) {
  color: var(--muted-text-color);
  font-size: 12px;
  font-weight: 760;
}

@media (max-width: 980px) {
  .editor-layout {
    grid-template-columns: 1fr;
  }

  .editor-side {
    position: static;
  }
}
</style>
