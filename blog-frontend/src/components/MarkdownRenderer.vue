<template>
  <div ref="viewerRootRef" class="article-content toast-viewer-host" />
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import Viewer from '@toast-ui/editor/viewer'
import '@toast-ui/editor/dist/toastui-editor-viewer.css'
import '@toast-ui/editor/dist/theme/toastui-editor-dark.css'
import { createHeadingId } from '../utils/reading'
import { useThemeStore } from '../stores/theme'

const props = defineProps({ content: { type: String, default: '' } })
const themeStore = useThemeStore()
const viewerRootRef = ref(null)
let viewer = null

onMounted(() => {
  viewer = new Viewer({
    el: viewerRootRef.value,
    initialValue: props.content || '',
    theme: themeStore.isDark ? 'dark' : 'default',
    usageStatistics: false
  })
  updateHeadingIds()
})

onBeforeUnmount(() => {
  viewer?.destroy()
  viewer = null
})

watch(() => props.content, value => {
  if (!viewer) return
  viewer.setMarkdown(value || '')
  updateHeadingIds()
})

watch(() => themeStore.isDark, isDark => {
  viewerRootRef.value?.classList.toggle('toastui-editor-dark', isDark)
})

function updateHeadingIds() {
  nextTick(() => {
    if (!viewerRootRef.value) return
    const seen = new Map()
    viewerRootRef.value.querySelectorAll('h1, h2, h3, h4, h5, h6').forEach(heading => {
      heading.id = createHeadingId(heading.textContent || '', seen)
    })
  })
}
</script>

<style scoped>
.toast-viewer-host {
  color: var(--text-color);
}

.toast-viewer-host :deep(.toastui-editor-contents) {
  color: var(--text-color);
  font-family: inherit;
  font-size: inherit;
}

.toast-viewer-host :deep(.toastui-editor-contents img) {
  max-width: 100%;
}
</style>
