<template>
  <div class="article-editor">
    <div class="editor-toolbar" aria-label="文章编辑工具">
      <el-select
        v-model="headingValue"
        class="heading-select"
        placeholder="标题"
        :teleported="false"
        @change="applyHeading"
      >
        <el-option label="普通段落" :value="0" />
        <el-option label="章节标题" :value="2" />
        <el-option label="小节标题" :value="3" />
        <el-option label="细分标题" :value="4" />
      </el-select>

      <div class="toolbar-actions">
        <el-button title="加粗" aria-label="加粗" @click="runCommand('bold')">
          <span class="toolbar-text-icon toolbar-text-icon--bold">B</span>
        </el-button>
        <el-button title="斜体" aria-label="斜体" @click="runCommand('italic')">
          <span class="toolbar-text-icon toolbar-text-icon--italic">I</span>
        </el-button>
        <el-button :icon="List" title="无序列表" aria-label="无序列表" @click="runCommand('bulletList')" />
        <el-button :icon="Tickets" title="有序列表" aria-label="有序列表" @click="runCommand('orderedList')" />
        <el-button :icon="ChatLineSquare" title="引用" aria-label="引用" @click="runCommand('blockQuote')" />
        <el-button :icon="Document" title="代码块" aria-label="代码块" @click="runCommand('codeBlock')" />
        <el-button :icon="Picture" :loading="uploading" title="插入图片" aria-label="插入图片" @click="openImagePicker" />
      </div>

      <input
        ref="imageInputRef"
        type="file"
        accept="image/*"
        class="image-input"
        @change="uploadPickedImage"
      />
    </div>

    <div ref="editorRootRef" class="toast-editor-host" />
  </div>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatLineSquare, Document, List, Picture, Tickets } from '@element-plus/icons-vue'
import Editor from '@toast-ui/editor'
import '@toast-ui/editor/dist/toastui-editor.css'
import '@toast-ui/editor/dist/theme/toastui-editor-dark.css'
import '@toast-ui/editor/dist/i18n/zh-cn'
import { uploadImage } from '../../api/image'
import { normalizeArticleMarkdown } from '../../utils/reading'
import { useThemeStore } from '../../stores/theme'

const props = defineProps({ modelValue: { type: String, default: '' } })
const emit = defineEmits(['update:modelValue'])
const themeStore = useThemeStore()

const editorRootRef = ref(null)
const imageInputRef = ref(null)
const uploading = ref(false)
const headingValue = ref(0)
let editor = null
let syncingFromEditor = false

onMounted(() => {
  editor = new Editor({
    el: editorRootRef.value,
    height: '560px',
    minHeight: '420px',
    initialValue: props.modelValue || '',
    initialEditType: 'wysiwyg',
    previewStyle: 'vertical',
    language: 'zh-CN',
    theme: themeStore.isDark ? 'dark' : 'default',
    usageStatistics: false,
    toolbarItems: [
      ['bold', 'italic', 'strike'],
      ['hr', 'quote'],
      ['ul', 'ol', 'task'],
      ['code', 'codeblock'],
      ['link', 'image'],
      ['scrollSync']
    ],
    hooks: {
      addImageBlobHook: handleImageBlob
    },
    events: {
      change: syncContent
    }
  })
})

onBeforeUnmount(() => {
  editor?.destroy()
  editor = null
})

watch(() => props.modelValue, value => {
  if (!editor || syncingFromEditor) return
  if ((value || '') !== editor.getMarkdown()) {
    editor.setMarkdown(value || '', false)
  }
})

watch(() => themeStore.isDark, isDark => {
  const ui = editorRootRef.value?.querySelector('.toastui-editor-defaultUI')
  ui?.classList.toggle('toastui-editor-dark', isDark)
})

function syncContent() {
  if (!editor) return
  const markdown = editor.getMarkdown()
  const normalizedMarkdown = normalizeArticleMarkdown(markdown)
  syncingFromEditor = true
  if (normalizedMarkdown !== markdown) {
    editor.setMarkdown(normalizedMarkdown, false)
  }
  emit('update:modelValue', normalizedMarkdown)
  nextTick(() => {
    syncingFromEditor = false
  })
}

function runCommand(command, payload) {
  if (!editor) return
  editor.exec(command, payload)
  editor.focus()
  syncContent()
}

function applyHeading(level) {
  headingValue.value = level
  runCommand('heading', { level })
}

function openImagePicker() {
  imageInputRef.value?.click()
}

async function uploadPickedImage(event) {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file || uploading.value) return

  uploading.value = true
  try {
    const result = await uploadImage(file)
    insertImage(result.data?.url, file.name)
  } catch {
    ElMessage.error('图片上传失败')
  } finally {
    uploading.value = false
  }
}

async function handleImageBlob(blob, callback) {
  try {
    const result = await uploadImage(blob)
    const url = result.data?.url
    if (url) callback(url, blob.name || '图片')
  } catch {
    ElMessage.error('图片上传失败')
  }
}

function insertImage(url, name = '图片') {
  if (!editor || !url) return
  editor.exec('addImage', { imageUrl: url, altText: name })
  editor.focus()
  syncContent()
}
</script>

<style scoped>
.article-editor {
  width: 100%;
  border: 1px solid var(--soft-border-color);
  border-radius: var(--radius-md);
  background: var(--panel-bg);
  overflow: hidden;
}

.editor-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border-bottom: 1px solid var(--soft-border-color);
  background:
    linear-gradient(90deg, color-mix(in srgb, var(--primary-color) 8%, transparent), transparent),
    var(--panel-bg);
}

.heading-select {
  width: 138px;
}

.toolbar-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.toolbar-actions :deep(.el-button) {
  width: 34px;
  height: 34px;
  padding: 0;
}

.toolbar-text-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 1em;
  line-height: 1;
}

.toolbar-text-icon--bold {
  font-weight: 700;
}

.toolbar-text-icon--italic {
  font-style: italic;
}

.image-input {
  display: none;
}

.toast-editor-host {
  color: var(--text-color);
}

.toast-editor-host :deep(.toastui-editor-defaultUI) {
  border: 0;
  color: var(--text-color);
  background: var(--panel-bg);
}

.toast-editor-host :deep(.toastui-editor-defaultUI-toolbar) {
  border-color: var(--soft-border-color);
  background: color-mix(in srgb, var(--panel-bg) 94%, var(--bg-color));
}

.toast-editor-host :deep(.toastui-editor-main),
.toast-editor-host :deep(.toastui-editor-md-container),
.toast-editor-host :deep(.toastui-editor-ww-container) {
  background: var(--panel-bg);
}

.toast-editor-host :deep(.toastui-editor-contents),
.toast-editor-host :deep(.ProseMirror),
.toast-editor-host :deep(.toastui-editor-md-preview) {
  color: var(--text-color);
  font-family: inherit;
}

.toast-editor-host :deep(.toastui-editor-md-tab-container),
.toast-editor-host :deep(.toastui-editor-tabs),
.toast-editor-host :deep(.toastui-editor-mode-switch) {
  background: var(--panel-bg);
  border-color: var(--soft-border-color);
}

@media (max-width: 760px) {
  .editor-toolbar {
    align-items: flex-start;
  }

  .heading-select {
    width: 100%;
  }
}
</style>
