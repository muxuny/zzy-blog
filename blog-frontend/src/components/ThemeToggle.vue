<template>
  <el-button
    class="theme-toggle"
    text
    :aria-label="themeStore.isDark ? '切换到亮色模式' : '切换到暗色模式'"
    :title="themeStore.isDark ? '切换到亮色模式' : '切换到暗色模式'"
    :icon="themeStore.isDark ? Moon : Sunny"
    @click="themeStore.toggle()"
  />
</template>

<script setup>
import { useThemeStore } from '../stores/theme'
import { Moon, Sunny } from '@element-plus/icons-vue'
const themeStore = useThemeStore()
</script>

<style scoped>
.theme-toggle {
  width: 36px;
  height: 36px;
  padding: 0;
}
</style>
