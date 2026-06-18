import { defineStore } from 'pinia'
import { usePreferredDark } from '@vueuse/core'

export const useThemeStore = defineStore('theme', {
  state: () => ({ isDark: false }),
  actions: {
    init() {
      const s = localStorage.getItem('theme')
      this.isDark = s ? s === 'dark' : usePreferredDark().value
      this.apply()
    },
    toggle() { this.isDark = !this.isDark; localStorage.setItem('theme', this.isDark ? 'dark' : 'light'); this.apply() },
    apply() { document.documentElement.setAttribute('data-theme', this.isDark ? 'dark' : 'light') }
  }
})
