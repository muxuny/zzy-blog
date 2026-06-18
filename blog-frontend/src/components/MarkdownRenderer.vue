<template>
  <div class="article-content" v-html="rendered" />
</template>

<script setup>
import { computed } from 'vue'
import { marked } from 'marked'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'

marked.setOptions({
  highlight(code, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try { return hljs.highlight(code, { language: lang }).value }
      catch {} }
    return hljs.highlightAuto(code).value
  }
})

const props = defineProps({ content: { type: String, default: '' } })
const rendered = computed(() => marked(props.content || ''))
</script>
