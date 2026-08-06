<template>
  <div class="page-copy-page">
    <div class="page-head">
      <div>
        <span class="page-eyebrow">站点配置</span>
        <h2>页面文案</h2>
        <p class="page-description">统一维护功能页头部导语，页面加载失败时仍回退到本地默认文案。</p>
      </div>
      <div class="page-actions">
        <button class="tool-button" type="button" :disabled="resetting || saving" @click="resetAll">
          {{ resetting ? '恢复中' : '恢复默认' }}
        </button>
        <button class="tool-button is-primary" type="button" :disabled="!canSave" @click="saveCurrent">
          {{ saving ? '保存中' : '保存这一页' }}
        </button>
      </div>
    </div>

    <el-alert
      v-if="loadError"
      class="page-copy-alert"
      type="warning"
      :title="loadError"
      show-icon
      :closable="false"
    />

    <section class="page-copy-summary">
      <div class="summary-tile is-primary">
        <span class="section-eyebrow">配置范围</span>
        <strong>{{ pageCopyStore.adminCopies.length }}</strong>
        <p>页面级 eyebrow、title、description</p>
      </div>
      <div v-for="group in groupedAdminCopies" :key="group.group" class="summary-tile">
        <span class="tiny-label">{{ group.group }}</span>
        <strong>{{ group.items.length }}</strong>
        <p>{{ group.items.map(item => item.pageName).join(' / ') }}</p>
      </div>
    </section>

    <section class="page-copy-grid" v-loading="pageCopyStore.adminLoading">
      <aside class="surface copy-list-panel">
        <div class="panel-head">
          <div>
            <span class="section-eyebrow">页面列表</span>
            <h3>选择导语</h3>
          </div>
          <el-select
            v-model="groupFilter"
            class="copy-group-filter"
            size="small"
            placeholder="全部页面"
            aria-label="页面分区筛选"
          >
            <el-option
              v-for="option in groupFilterOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </div>

        <div class="copy-groups">
          <section v-for="group in filteredGroupedAdminCopies" :key="group.group" class="copy-group">
            <span v-if="showGroupLabels" class="tiny-label">{{ group.group }}</span>
            <button
              v-for="copy in group.items"
              :key="copy.copyKey"
              class="copy-row"
              :class="{ 'is-active': selectedKey === copy.copyKey }"
              type="button"
              @click="selectCopy(copy)"
            >
              <strong>{{ copy.pageName }}</strong>
              <span>{{ copy.copyKey }}</span>
              <small>{{ copy.title }}</small>
            </button>
          </section>
        </div>
      </aside>

      <section class="surface copy-editor-panel">
        <div class="panel-head">
          <div>
            <span class="section-eyebrow">编辑内容</span>
            <h3>{{ selectedCopy?.pageName || '页面文案' }}</h3>
          </div>
          <span class="chip" :class="{ 'is-warning': hasChanges }">
            {{ hasChanges ? '有修改' : '已同步' }}
          </span>
        </div>

        <el-form label-position="top" class="copy-form">
          <div class="field-grid">
            <el-form-item label="页面标识">
              <el-input :model-value="selectedCopy?.copyKey || ''" readonly />
            </el-form-item>
            <el-form-item label="页面分区">
              <el-input :model-value="selectedCopy?.pageGroup || ''" readonly />
            </el-form-item>
          </div>
          <el-form-item label="眉标">
            <el-input v-model="form.eyebrow" maxlength="80" show-word-limit />
          </el-form-item>
          <el-form-item label="主标题">
            <el-input v-model="form.title" maxlength="160" show-word-limit />
          </el-form-item>
          <el-form-item label="描述">
            <el-input v-model="form.description" type="textarea" :rows="5" maxlength="500" show-word-limit />
          </el-form-item>
        </el-form>
      </section>

      <aside class="surface copy-preview-panel">
        <div class="panel-head">
          <div>
            <span class="section-eyebrow">即时预览</span>
            <h3>页面头部效果</h3>
          </div>
        </div>

        <div class="copy-preview">
          <span class="page-eyebrow">{{ previewCopy.eyebrow }}</span>
          <h2>{{ previewCopy.title }}</h2>
          <p v-if="previewCopy.description">{{ previewCopy.description }}</p>
        </div>

        <div class="preview-meta">
          <span class="tiny-label">当前页面</span>
          <strong>{{ selectedCopy?.pageName || '-' }}</strong>
          <p>{{ selectedCopy?.copyKey || '-' }}</p>
        </div>
      </aside>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { usePageCopyStore } from '../../stores/pageCopy'
import { PAGE_COPY_GROUPS, resolvePageCopy } from '../../utils/pageCopy'

const pageCopyStore = usePageCopyStore()
const selectedKey = ref('home.hero')
const ALL_PAGE_COPY_GROUP = 'all'
const groupFilter = ref(ALL_PAGE_COPY_GROUP)
const saving = ref(false)
const resetting = ref(false)
const loadError = ref('')
const form = reactive({
  eyebrow: '',
  title: '',
  description: ''
})

const groupedAdminCopies = computed(() => pageCopyStore.groupedAdminCopies)
const groupFilterOptions = computed(() => [
  { label: '全部页面', value: ALL_PAGE_COPY_GROUP },
  ...PAGE_COPY_GROUPS.map(group => ({ label: group, value: group }))
])
const filteredGroupedAdminCopies = computed(() => {
  if (groupFilter.value === ALL_PAGE_COPY_GROUP) return groupedAdminCopies.value
  return groupedAdminCopies.value.filter(group => group.group === groupFilter.value)
})
const showGroupLabels = computed(() => groupFilter.value === ALL_PAGE_COPY_GROUP)
const selectedCopy = computed(() => (
  pageCopyStore.adminCopies.find(item => item.copyKey === selectedKey.value)
    || pageCopyStore.adminCopies[0]
    || null
))
const hasChanges = computed(() => {
  if (!selectedCopy.value) return false
  return form.eyebrow !== selectedCopy.value.eyebrow
    || form.title !== selectedCopy.value.title
    || form.description !== selectedCopy.value.description
})
const canSave = computed(() => !!selectedCopy.value && !!form.title.trim() && hasChanges.value && !saving.value)
const previewCopy = computed(() => resolvePageCopy(selectedKey.value, { tagName: '当前标签' }, [{
  ...(selectedCopy.value || {}),
  eyebrow: form.eyebrow,
  title: form.title,
  description: form.description
}]))

onMounted(async () => {
  try {
    await pageCopyStore.loadAdminCopies({ force: true, throwOnError: true })
    if (!pageCopyStore.adminCopies.some(item => item.copyKey === selectedKey.value)) {
      selectedKey.value = pageCopyStore.adminCopies[0]?.copyKey || 'home.hero'
    }
  } catch (error) {
    loadError.value = error.response?.data?.message || error.message || '页面文案配置加载失败'
  }
})

watch(selectedCopy, copy => {
  if (!copy) return
  form.eyebrow = copy.eyebrow || ''
  form.title = copy.title || ''
  form.description = copy.description || ''
}, { immediate: true })

watch(groupFilter, () => {
  if (groupFilter.value === ALL_PAGE_COPY_GROUP || selectedCopy.value?.pageGroup === groupFilter.value) return
  selectedKey.value = filteredGroupedAdminCopies.value[0]?.items[0]?.copyKey || selectedKey.value
})

function selectCopy(copy) {
  selectedKey.value = copy.copyKey
}

async function saveCurrent() {
  if (!selectedCopy.value || !canSave.value) return
  saving.value = true
  try {
    await pageCopyStore.saveAdminCopy(selectedCopy.value.copyKey, {
      eyebrow: form.eyebrow,
      title: form.title,
      description: form.description
    })
    ElMessage.success('页面文案已保存')
  } finally {
    saving.value = false
  }
}

async function resetAll() {
  await ElMessageBox.confirm('恢复后，所有页面文案会回到当前默认值。', '恢复默认', {
    type: 'warning',
    confirmButtonText: '恢复默认',
    cancelButtonText: '取消'
  })
  resetting.value = true
  try {
    await pageCopyStore.resetAdminCopies()
    ElMessage.success('页面文案已恢复默认')
  } finally {
    resetting.value = false
  }
}
</script>

<style scoped>
.page-copy-page {
  display: grid;
  gap: 16px;
}

.page-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}

.page-copy-alert {
  border-radius: var(--radius-md);
}

.page-copy-summary {
  display: grid;
  grid-template-columns: 1.2fr repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.summary-tile {
  min-width: 0;
  min-height: 104px;
  padding: 16px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background: color-mix(in srgb, var(--panel-bg) 88%, transparent);
  box-shadow: var(--shadow-sm);
}

.summary-tile.is-primary {
  background:
    linear-gradient(135deg, color-mix(in srgb, var(--primary-color) 12%, transparent), transparent 68%),
    color-mix(in srgb, var(--panel-bg) 90%, transparent);
}

.summary-tile strong {
  display: block;
  margin-top: 6px;
  color: var(--text-color);
  font-family: Georgia, "Times New Roman", serif;
  font-size: 36px;
  font-weight: 500;
  line-height: 1;
}

.summary-tile p {
  margin: 8px 0 0;
  color: var(--muted-text-color);
  font-size: 13px;
  line-height: 1.55;
}

.page-copy-grid {
  display: grid;
  align-items: start;
  grid-template-columns: minmax(240px, 0.82fr) minmax(340px, 1.12fr) minmax(280px, 0.92fr);
  gap: 14px;
  min-width: 0;
}

.copy-list-panel,
.copy-editor-panel,
.copy-preview-panel {
  min-width: 0;
}

.copy-list-panel {
  position: sticky;
  top: 24px;
  display: flex;
  flex-direction: column;
  max-height: calc(100vh - 220px);
  overflow: hidden;
}

.copy-editor-panel,
.copy-preview-panel {
  position: sticky;
  top: 24px;
}

.copy-groups {
  display: grid;
  gap: 8px;
  min-height: 0;
  overflow-y: auto;
  overscroll-behavior: contain;
  padding-right: 4px;
  scrollbar-gutter: stable;
}

.copy-group-filter {
  width: min(148px, 48%);
  flex-shrink: 0;
}

.copy-group-filter :deep(.el-select__wrapper) {
  min-height: 34px;
  border-radius: 999px;
  background: color-mix(in srgb, var(--panel-bg) 72%, var(--bg-color));
  box-shadow: 0 0 0 1px var(--soft-border-color) inset;
}

.copy-group {
  display: grid;
  gap: 8px;
}

.copy-group + .copy-group {
  margin-top: 14px;
}

.copy-row {
  display: grid;
  gap: 4px;
  width: 100%;
  min-height: 78px;
  padding: 11px;
  border: 1px solid var(--soft-border-color);
  border-radius: var(--radius-sm);
  background: color-mix(in srgb, var(--panel-bg) 84%, var(--bg-color));
  color: var(--text-color);
  text-align: left;
}

.copy-row.is-active {
  border-color: color-mix(in srgb, var(--primary-color) 60%, var(--border-color));
  background: color-mix(in srgb, var(--primary-color) 11%, var(--panel-bg));
}

.copy-row strong,
.copy-row span,
.copy-row small {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.copy-row span,
.copy-row small {
  color: var(--muted-text-color);
  font-size: 12px;
}

.field-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.copy-form :deep(.el-form-item) {
  margin-bottom: 14px;
}

.copy-preview {
  min-height: 230px;
  padding: 22px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background:
    linear-gradient(135deg, color-mix(in srgb, var(--primary-color) 12%, transparent), transparent 68%),
    color-mix(in srgb, var(--code-bg) 58%, var(--panel-bg));
}

.copy-preview h2 {
  margin: 6px 0 0;
  color: var(--text-color);
  font-family: Georgia, "Times New Roman", serif;
  font-size: 38px;
  font-weight: 500;
  line-height: 1.06;
}

.copy-preview p,
.preview-meta p {
  margin: 10px 0 0;
  color: var(--muted-text-color);
  line-height: 1.65;
}

.preview-meta {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px solid var(--soft-border-color);
}

.preview-meta strong {
  display: block;
  margin-top: 6px;
  color: var(--text-color);
}

@media (max-width: 1180px) {
  .page-copy-summary,
  .page-copy-grid {
    grid-template-columns: 1fr;
  }

  .copy-list-panel,
  .copy-editor-panel,
  .copy-preview-panel {
    position: static;
    max-height: none;
  }

  .copy-groups {
    overflow: visible;
    padding-right: 0;
  }
}

@media (max-width: 720px) {
  .page-actions,
  .field-grid {
    grid-template-columns: 1fr;
  }

  .page-actions {
    justify-content: stretch;
  }

  .page-actions .tool-button {
    width: 100%;
  }

  .copy-preview h2 {
    font-size: 31px;
  }

  .copy-group-filter {
    width: 100%;
  }
}
</style>
