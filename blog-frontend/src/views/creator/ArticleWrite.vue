<template>
  <div class="layout">
    <AppHeader />
    <el-main class="main">
      <div class="page-header">
        <div>
          <span class="page-kicker">创作中心</span>
          <h1>{{ isEdit ? '编辑文章' : '写文章' }}</h1>
        </div>
      </div>

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
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="文章标题" :disabled="formDisabled" />
        </el-form-item>
        <el-form-item label="摘要">
          <el-input
            v-model="form.summary"
            type="textarea"
            :rows="2"
            placeholder="一句话概括文章内容"
            :disabled="formDisabled"
          />
        </el-form-item>
        <el-form-item label="封面图">
          <div :class="{ 'disabled-control': formDisabled }">
            <ImageUploader v-model="form.coverImage" />
          </div>
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
        <el-form-item label="内容">
          <div :class="{ 'disabled-control': formDisabled }" class="editor-control">
            <ArticleEditor v-model="form.content" />
          </div>
        </el-form-item>
        <div class="actions">
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
import { buildArticleGroupIdsForSave, getFirstArticleGroupId } from '../../utils/articleGroups'
import { normalizeArticleMarkdown } from '../../utils/reading'

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
  tagIds: [],
  groupId: ''
})

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
.main {
  width: min(100%, var(--content-width));
  margin: 0 auto;
  padding: 32px 24px 64px;
}

.page-header {
  margin-bottom: 18px;
}

.page-kicker {
  color: var(--accent-color);
  font-size: 12px;
  font-weight: 800;
}

.page-header h1 {
  margin: 4px 0 0;
  color: var(--text-color);
  font-size: 30px;
}

.article-form {
  padding: 22px;
  border: 1px solid var(--soft-border-color);
  border-radius: var(--radius-md);
  background: var(--panel-bg);
}

.form-alert {
  margin-bottom: 12px;
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

.editor-control {
  width: 100%;
}

.disabled-control {
  pointer-events: none;
  opacity: 0.62;
}

.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

@media (max-width: 760px) {
  .main {
    padding: 24px 14px 48px;
  }

  .group-control {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
