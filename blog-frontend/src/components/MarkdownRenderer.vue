<template>
  <div class="article-content" v-html="rendered" />
</template>

<script setup>
import { computed } from 'vue'
import { marked } from 'marked'
import hljs from 'highlight.js'
import { createHeadingId } from '../utils/reading'
import 'highlight.js/styles/github.css'

function createRenderer() {
  const renderer = new marked.Renderer()
  const seen = new Map()

  renderer.heading = (text, level, raw) => {
    const id = createHeadingId(raw || text, seen)
    return `<h${level} id="${id}">${text}</h${level}>`
  }

  return renderer
}

marked.setOptions({
  highlight(code, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try { return hljs.highlight(code, { language: lang }).value }
      catch {} }
    return hljs.highlightAuto(code).value
  }
})

const props = defineProps({ content: { type: String, default: '' } })
const rendered = computed(() => marked(props.content || '', { renderer: createRenderer() }))
</script>
