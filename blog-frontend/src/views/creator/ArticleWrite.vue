<template>
  <div class="layout">
    <AppHeader />
    <el-main class="compose-shell">
      <header class="page-head">
        <div class="head-copy">
          <span class="eyebrow">创作中心</span>
          <h1>{{ isEdit ? '编辑文章' : '写文章' }}</h1>
          <p>把标题、摘要、分组和正文放在同一个工作流里，状态动作保持在明确的底部区域。</p>
        </div>
        <aside class="head-meta preflight-panel" aria-label="发布前检查">
          <div class="preflight-head">
            <span class="rail-kicker">发布前检查</span>
            <strong>{{ preflightReadyCount }}/{{ preflightChecks.length }}</strong>
          </div>
          <ul class="preflight-list">
            <li
              v-for="item in preflightChecks"
              :key="item.key"
              class="preflight-item"
              :class="{ 'is-done': item.done }"
            >
              <span class="preflight-dot" aria-hidden="true" />
              <span>{{ item.label }}</span>
              <strong>{{ item.text }}</strong>
            </li>
          </ul>
          <div class="draft-stats" aria-label="文章统计">
            <span>{{ writingStats.wordCount }} 字</span>
            <span>{{ writingStats.readingTimeText }}</span>
          </div>
        </aside>
      </header>

      <div class="compose-grid">
        <section class="compose-panel">
          <el-alert
            v-if="detailError"
            :title="detailError"
            type="error"
            show-icon
            class="form-alert"
          />
          <el-alert
            v-if="tagError"
            :title="tagError"
            type="warning"
            show-icon
            :closable="false"
            class="form-alert"
          />
          <el-alert
            v-if="groupError"
            :title="groupError"
            type="warning"
            show-icon
            :closable="false"
            class="form-alert"
          />

          <el-form
            v-loading="pageLoading"
            :model="form"
            label-position="top"
            class="article-form"
          >
            <div class="field-grid">
              <el-form-item label="标题" class="wide-field">
                <el-input v-model="form.title" placeholder="文章标题" :disabled="formDisabled" />
              </el-form-item>
              <el-form-item label="摘要" class="wide-field">
                <el-input
                  v-model="form.summary"
                  type="textarea"
                  :rows="2"
                  placeholder="一句话概括文章内容"
                  :disabled="formDisabled"
                />
              </el-form-item>
              <el-form-item label="标签">
                <el-select
                  v-model="form.tagIds"
                  multiple
                  placeholder="选择标签"
                  class="tag-select"
                  :disabled="formDisabled || tagsLoading"
                >
                  <el-option v-for="tag in tags" :key="tag.id" :label="tag.name" :value="tag.id" />
                </el-select>
              </el-form-item>
              <el-form-item label="文章分组">
                <div class="group-control">
                  <el-select
                    v-model="form.groupId"
                    clearable
                    placeholder="未分组"
                    class="group-select"
                    :disabled="formDisabled || groupsLoading"
                  >
                    <el-option
                      v-for="group in articleGroups"
                      :key="group.id"
                      :label="group.name"
                      :value="group.id"
                    />
                  </el-select>
                  <el-button :disabled="formDisabled || groupsLoading" @click="createGroupInline">
                    新建分组
                  </el-button>
                </div>
              </el-form-item>
              <el-form-item label="可见性" class="wide-field">
                <el-radio-group v-model="form.visibility" :disabled="formDisabled" class="visibility-control">
                  <el-radio-button
                    v-for="option in visibilityOptions"
                    :key="option.value"
                    :label="option.value"
                  >
                    {{ option.label }}
                  </el-radio-button>
                </el-radio-group>
                <p class="visibility-help">
                  公开文章审核通过后会出现在首页；仅自己可见的文章只会保留在你的文章列表里。
                </p>
              </el-form-item>
              <el-form-item label="封面图" class="wide-field">
                <div :class="{ 'disabled-control': formDisabled }">
                  <ImageUploader v-model="form.coverImage" />
                </div>
              </el-form-item>
              <el-form-item label="内容" class="wide-field editor-field">
                <div :class="{ 'disabled-control': formDisabled }" class="editor-control">
                  <ArticleEditor v-model="form.content" />
                </div>
              </el-form-item>
            </div>
            <div class="compose-actions">
              <el-button
                type="primary"
                :loading="savingStatus === 'draft'"
                :disabled="formDisabled || saving"
                @click="save('draft')"
              >
                保存草稿
              </el-button>
              <el-button
                type="success"
                :loading="savingStatus === 'pending'"
                :disabled="formDisabled || saving"
                @click="save('pending')"
              >
                提交审核
              </el-button>
              <el-button :disabled="saving" @click="$router.push('/creator/articles')">取消</el-button>
            </div>
          </el-form>
        </section>

        <aside class="compose-rail" aria-label="创作提示">
          <section class="rail-panel">
            <span class="rail-kicker">状态边界</span>
            <p>草稿可以继续保存或提交审核；待审核文章需要撤回后再编辑。</p>
          </section>
          <section class="rail-panel">
            <span class="rail-kicker">可见性</span>
            <p>仅自己可见的文章不会进入公开首页，但仍会保留在创作中心。</p>
          </section>
          <section class="rail-panel">
            <span class="rail-kicker">分组</span>
            <p>分组只服务创作管理，不影响公开阅读页的标签筛选。</p>
          </section>
        </aside>
      </div>
    </el-main>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import AppHeader from '../../components/AppHeader.vue'
import ArticleEditor from '../../components/admin/ArticleEditor.vue'
import ImageUploader from '../../components/admin/ImageUploader.vue'
import { createArticleGroup, getArticleGroups } from '../../api/articleGroup'
import { getTags } from '../../api/tag'
import { createMyArticle, getMyArticle, updateMyArticle } from '../../api/myArticle'
import { ARTICLE_VISIBILITY_PRIVATE, ARTICLE_VISIBILITY_PUBLIC, normalizeArticleVisibility } from '../../utils/articleVisibility'
import { buildArticleGroupIdsForSave, getFirstArticleGroupId } from '../../utils/articleGroups'
import { getReadingStats, normalizeArticleMarkdown } from '../../utils/reading'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.params.id)
const tags = ref([])
const articleGroups = ref([])
const tagsLoading = ref(false)
const groupsLoading = ref(false)
const detailLoading = ref(false)
const detailError = ref('')
const tagError = ref('')
const groupError = ref('')
const existingGroups = ref([])
const savingStatus = ref('')
const saving = computed(() => !!savingStatus.value)
const pageLoading = computed(() => tagsLoading.value || groupsLoading.value || detailLoading.value)
const formDisabled = computed(() => pageLoading.value || saving.value || !!detailError.value)
const form = reactive({
  title: '',
  content: '',
  summary: '',
  coverImage: '',
  status: 'draft',
  visibility: ARTICLE_VISIBILITY_PUBLIC,
  tagIds: [],
  groupId: ''
})
const visibilityOptions = [
  { label: '公开', value: ARTICLE_VISIBILITY_PUBLIC },
  { label: '仅自己可见', value: ARTICLE_VISIBILITY_PRIVATE }
]
const writingStats = computed(() => getReadingStats(form.content || ''))
const preflightChecks = computed(() => [
  {
    key: 'title',
    label: '标题',
    text: form.title.trim() ? '已填写' : '未填写',
    done: !!form.title.trim()
  },
  {
    key: 'content',
    label: '正文',
    text: writingStats.value.wordCount ? `${writingStats.value.wordCount} 字` : '未填写',
    done: writingStats.value.wordCount > 0
  },
  {
    key: 'summary',
    label: '摘要',
    text: form.summary.trim() ? '已填写' : '可补充',
    done: !!form.summary.trim()
  },
  {
    key: 'tags',
    label: '标签',
    text: form.tagIds.length ? `${form.tagIds.length} 个` : '建议选择',
    done: form.tagIds.length > 0
  }
])
const preflightReadyCount = computed(() => preflightChecks.value.filter(item => item.done).length)

onMounted(() => {
  loadTags()
  loadArticleGroups()
  if (isEdit.value) loadArticle()
})

async function loadTags() {
  tagsLoading.value = true
  tagError.value = ''
  try {
    const tagResult = await getTags()
    tags.value = tagResult.data || []
  } catch {
    tagError.value = '标签加载失败，你仍可以先编辑文章内容。'
  } finally {
    tagsLoading.value = false
  }
}

async function loadArticleGroups() {
  groupsLoading.value = true
  groupError.value = ''
  try {
    const result = await getArticleGroups()
    articleGroups.value = result.data || []
  } catch {
    groupError.value = '分组加载失败，你仍然可以先编辑文章内容。'
  } finally {
    groupsLoading.value = false
  }
}

async function loadArticle() {
  detailLoading.value = true
  detailError.value = ''
  try {
    const result = await getMyArticle(route.params.id)
    const article = result.data
    if (article.status === 'pending') {
      ElMessage.warning('待审核文章请先撤回再编辑')
      router.push('/creator/articles')
      return
    }
    form.title = article.title || ''
    form.content = article.content || ''
    form.summary = article.summary || ''
    form.coverImage = article.coverImage || ''
    form.status = article.status || 'draft'
    form.visibility = normalizeArticleVisibility(article.visibility)
    form.tagIds = (article.tags || []).map(tag => tag.id)
    existingGroups.value = article.groups || []
    form.groupId = getFirstArticleGroupId(existingGroups.value)
  } catch {
    detailError.value = '文章详情加载失败，请返回我的文章后重试。'
    ElMessage.error(detailError.value)
    router.push('/creator/articles')
  } finally {
    detailLoading.value = false
  }
}

async function createGroupInline() {
  try {
    const prompt = await ElMessageBox.prompt('请输入分组名称', '新建分组', {
      confirmButtonText: '创建',
      cancelButtonText: '取消',
      inputPlaceholder: '例如：技术笔记',
      inputValidator: value => {
        if (!value || !value.trim()) return false
        return true
      },
      inputErrorMessage: '分组名称不能为空'
    })
    const result = await createArticleGroup({ name: prompt.value.trim() })
    const group = result.data
    articleGroups.value = [...articleGroups.value, group]
    form.groupId = group.id
    ElMessage.success('分组已创建')
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    throw error
  }
}

async function save(status) {
  if (formDisabled.value || saving.value) return
  savingStatus.value = status
  try {
    form.status = status
    const payload = {
      title: form.title,
      summary: form.summary,
      coverImage: form.coverImage,
      status: form.status,
      visibility: form.visibility,
      content: normalizeArticleMarkdown(form.content),
      tagIds: [...form.tagIds],
      groupIds: buildArticleGroupIdsForSave(form.groupId, existingGroups.value)
    }
    if (isEdit.value) {
      await updateMyArticle(route.params.id, payload)
    } else {
      await createMyArticle(payload)
    }
    ElMessage.success(status === 'pending' ? '已提交审核' : '草稿已保存')
    router.push('/creator/articles')
  } finally {
    savingStatus.value = ''
  }
}
</script>

<style scoped>
.compose-shell {
  position: relative;
  isolation: isolate;
  width: min(1180px, calc(100% - 36px));
  margin: 0 auto;
  padding: 22px 0 72px;
}

.compose-shell::before {
  position: fixed;
  inset: 0;
  z-index: -1;
  background:
    linear-gradient(90deg, var(--theme-grid-x) 1px, transparent 1px),
    linear-gradient(180deg, var(--theme-grid-y) 1px, transparent 1px);
  background-size: 44px 44px;
  content: '';
  opacity: 0.52;
  pointer-events: none;
}

.page-head {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(260px, 340px);
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
}

.head-copy p {
  max-width: 660px;
  margin: 0;
  color: var(--muted-text-color);
  font-size: 17px;
  line-height: 1.85;
}

.head-meta {
  align-self: end;
  border-left: 1px solid var(--border-color);
  padding-left: 18px;
}

.preflight-panel {
  display: grid;
  gap: 12px;
}

.preflight-head {
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--soft-border-color);
}

.preflight-head strong {
  color: var(--text-color);
  font-size: 13px;
  font-weight: 800;
}

.preflight-list {
  display: grid;
  gap: 8px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.preflight-item {
  display: grid;
  grid-template-columns: 10px minmax(0, 1fr) auto;
  gap: 9px;
  align-items: center;
  min-height: 28px;
  min-width: 0;
  color: var(--muted-text-color);
  font-size: 13px;
}

.preflight-item strong {
  color: var(--muted-text-color);
  font-size: 12px;
  font-weight: 760;
}

.preflight-item.is-done,
.preflight-item.is-done strong {
  color: var(--text-color);
}

.preflight-dot {
  width: 7px;
  height: 7px;
  border: 1px solid color-mix(in srgb, var(--muted-text-color) 44%, transparent);
  border-radius: 999px;
  background: transparent;
}

.preflight-item.is-done .preflight-dot {
  border-color: color-mix(in srgb, var(--primary-color) 76%, transparent);
  background: var(--primary-color);
  box-shadow: 0 0 0 4px color-mix(in srgb, var(--primary-color) 10%, transparent);
}

.draft-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding-top: 10px;
  border-top: 1px solid var(--soft-border-color);
}

.draft-stats span {
  display: inline-flex;
  min-height: 26px;
  align-items: center;
  padding: 0 9px;
  border: 1px solid color-mix(in srgb, var(--border-color) 80%, transparent);
  border-radius: 999px;
  color: var(--muted-text-color);
  font-size: 12px;
}

.compose-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 280px;
  gap: 28px;
  align-items: start;
}

.compose-panel {
  min-width: 0;
  padding-top: 18px;
  border-top: 1px solid var(--border-color);
}

.article-form {
  display: grid;
  gap: 18px;
}

.field-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px 18px;
}

.wide-field {
  grid-column: 1 / -1;
}

.article-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.article-form :deep(.el-form-item__label) {
  color: var(--accent-color);
  font-size: 12px;
  font-weight: 760;
}

.article-form :deep(.el-input__wrapper),
.article-form :deep(.el-select__wrapper),
.article-form :deep(.el-textarea__inner) {
  background: color-mix(in srgb, var(--panel-bg) 94%, transparent);
}

.article-form :deep(.el-input__wrapper),
.article-form :deep(.el-select__wrapper) {
  min-height: 42px;
}

.form-alert {
  margin-bottom: 12px;
  border-radius: var(--radius-md);
}

.tag-select {
  width: 100%;
}

.group-control {
  display: flex;
  width: 100%;
  gap: 10px;
  align-items: center;
}

.group-select {
  flex: 1;
}

.visibility-control {
  width: min(100%, 360px);
}

.visibility-control :deep(.el-radio-button__inner) {
  border-color: var(--border-color);
  background: color-mix(in srgb, var(--panel-bg) 94%, transparent);
}

.visibility-help {
  margin: 8px 0 0;
  color: var(--muted-text-color);
  font-size: 13px;
  line-height: 1.6;
}

.editor-control {
  width: 100%;
  overflow: hidden;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background: color-mix(in srgb, var(--panel-bg) 94%, transparent);
}

.disabled-control {
  pointer-events: none;
  opacity: 0.62;
}

.compose-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: flex-end;
  padding-top: 18px;
  border-top: 1px solid var(--soft-border-color);
}

.compose-actions .el-button {
  min-height: 40px;
  border-radius: 999px;
}

.compose-rail {
  position: sticky;
  top: calc(var(--app-header-height) + 20px);
  display: grid;
  gap: 16px;
  padding-left: 18px;
  border-left: 1px solid var(--border-color);
}

.rail-panel {
  padding-bottom: 16px;
  border-bottom: 1px solid var(--soft-border-color);
}

.rail-panel:last-child {
  border-bottom: 0;
}

.rail-kicker {
  color: var(--primary-color);
  font-size: 12px;
  font-weight: 800;
}

.rail-panel p {
  margin: 8px 0 0;
  color: var(--muted-text-color);
  font-size: 13px;
  line-height: 1.7;
}

@media (max-width: 980px) {
  .page-head,
  .compose-grid {
    grid-template-columns: 1fr;
  }

  .head-meta,
  .compose-rail {
    border-left: 0;
    padding-left: 0;
  }

  .head-meta {
    border-top: 1px solid var(--border-color);
    padding-top: 12px;
  }

  .compose-rail {
    position: static;
    border-top: 1px solid var(--border-color);
    padding-top: 16px;
  }
}

@media (max-width: 760px) {
  .compose-shell {
    width: min(100% - 28px, var(--content-width));
    padding: 16px 0 48px;
  }

  .page-head h1 {
    font-size: 42px;
  }

  .field-grid {
    grid-template-columns: 1fr;
  }

  .group-control,
  .compose-actions {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
