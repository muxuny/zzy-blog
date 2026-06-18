<template>
  <div class="editor-wrapper">
    <el-input
      v-model="content"
      type="textarea"
      :rows="20"
      placeholder="使用 Markdown 语法编写..."
      class="editor-textarea"
      @input="emit"
    />
    <div class="editor-preview">
      <div class="preview-header">预览</div>
      <MarkdownRenderer :content="content" />
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import MarkdownRenderer from '../MarkdownRenderer.vue'

const props = defineProps({ modelValue: { type: String, default: '' } })
const emit = defineEmits(['update:modelValue'])
const content = ref(props.modelValue)

watch(() => props.modelValue, v => content.value = v)
watch(content, v => emit('update:modelValue', v))
</script>

<style scoped>
.editor-wrapper {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 16px;
  min-height: 500px;
}

.editor-textarea :deep(textarea) {
  min-height: 500px;
  font-family: SFMono-Regular, Consolas, "Liberation Mono", Menlo, monospace;
  font-size: 14px;
  line-height: 1.7;
}

.editor-preview {
  max-height: 600px;
  overflow-y: auto;
  border: 1px solid var(--soft-border-color);
  border-radius: var(--radius-md);
  background: var(--panel-bg);
}

.preview-header {
  padding: 10px 12px;
  border-bottom: 1px solid var(--soft-border-color);
  color: var(--muted-text-color);
  font-size: 13px;
  font-weight: 750;
  background: rgba(47, 128, 237, 0.06);
}

.editor-preview :deep(.article-content) {
  padding: 16px;
}

@media (max-width: 980px) {
  .editor-wrapper {
    grid-template-columns: 1fr;
  }
}
</style>
