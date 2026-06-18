<template><router-view /></template>
<script setup>
import { useThemeStore } from './stores/theme'
import { onMounted } from 'vue'
const themeStore = useThemeStore()
onMounted(() => themeStore.init())
</script>
