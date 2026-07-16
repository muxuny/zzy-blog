<template>
  <div>
    <h2>{{ isEdit ? '编辑文章' : '写文章' }}</h2>

    <el-form :model="form" label-position="top" class="article-form">
      <el-form-item label="标题">
        <el-input v-model="form.title" placeholder="文章标题" />
      </el-form-item>
      <el-form-item label="摘要">
        <el-input v-model="form.summary" type="textarea" :rows="2" />
      </el-form-item>
      <el-form-item label="封面图">
        <ImageUploader v-model="form.coverImage" />
      </el-form-item>
      <el-form-item label="标签">
        <el-select v-model="form.tagIds" multiple placeholder="选择标签" class="tag-select">
          <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.id" />
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
      <el-form-item>
        <el-button :loading="savingStatus === 'draft'" :disabled="!!savingStatus" @click="save('draft')">
          保存草稿
        </el-button>
        <el-button
          type="primary"
          :loading="savingStatus === 'pending'"
          :disabled="!!savingStatus"
          @click="save('pending')"
        >
          提交审核
        </el-button>
        <el-button
          type="success"
          :loading="savingStatus === 'published'"
          :disabled="!!savingStatus"
          @click="save('published')"
        >
          发布
        </el-button>
        <el-button :disabled="!!savingStatus" @click="$router.back()">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { createAdminArticle, getAdminArticle, updateAdminArticle } from '../../api/article'
import { getTags } from '../../api/tag'
import { ElMessage } from 'element-plus'
import ArticleEditor from '../../components/admin/ArticleEditor.vue'
import ImageUploader from '../../components/admin/ImageUploader.vue'
import { normalizeArticleVisibility } from '../../utils/articleVisibility'
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

onMounted(async () => {
  const tr = await getTags()
  tags.value = tr.data || []
  if (isEdit.value) {
    const r = await getAdminArticle(route.params.id)
    const a = r.data
    form.title = a.title
    form.content = a.content
    form.summary = a.summary || ''
    form.coverImage = a.coverImage || ''
    form.status = a.status || 'draft'
    form.visibility = normalizeArticleVisibility(a.visibility)
    form.tagIds = (a.tags || []).map(t => t.id)
  }
})

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
.article-form {
  margin-top: 16px;
}

.tag-select {
  width: 100%;
}
</style>
