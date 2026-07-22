import { defineStore } from 'pinia'
import {
  DEFAULT_APPEARANCE,
  THEME_MODES,
  THEME_PALETTES,
  normalizeAppearance,
  readStoredAppearance,
  resolveThemeName,
  writeStoredAppearance
} from '../utils/themeAppearance'

function getSystemMediaQuery() {
  if (typeof window === 'undefined' || typeof window.matchMedia !== 'function') return null
  return window.matchMedia('(prefers-color-scheme: dark)')
}

export const useThemeStore = defineStore('theme', {
  state: () => ({
    palette: DEFAULT_APPEARANCE.palette,
    mode: DEFAULT_APPEARANCE.mode,
    isDark: false,
    systemPrefersDark: false,
    palettes: THEME_PALETTES,
    modes: THEME_MODES,
    mediaQuery: null,
    mediaQueryHandler: null
  }),
  getters: {
    activePalette(state) {
      return state.palettes.find(item => item.value === state.palette) || state.palettes[0]
    },
    activeMode(state) {
      return state.modes.find(item => item.value === state.mode) || state.modes[0]
    }
  },
  actions: {
    init() {
      const stored = readStoredAppearance(typeof localStorage === 'undefined' ? null : localStorage)
      const next = normalizeAppearance(stored)
      this.palette = next.palette
      this.mode = next.mode
      this.bindSystemPreference()
      this.apply()
    },
    bindSystemPreference() {
      const query = getSystemMediaQuery()
      this.mediaQuery = query
      this.systemPrefersDark = !!query?.matches
      if (!query || this.mediaQueryHandler) return

      this.mediaQueryHandler = event => {
        this.systemPrefersDark = !!event.matches
        if (this.mode === 'system') this.apply(false)
      }

      if (typeof query.addEventListener === 'function') {
        query.addEventListener('change', this.mediaQueryHandler)
      } else if (typeof query.addListener === 'function') {
        query.addListener(this.mediaQueryHandler)
      }
    },
    persist() {
      writeStoredAppearance(typeof localStorage === 'undefined' ? null : localStorage, {
        palette: this.palette,
        mode: this.mode
      })
    },
    setPalette(palette) {
      this.palette = normalizeAppearance({ palette, mode: this.mode }).palette
      this.apply()
      this.persist()
    },
    setMode(mode) {
      this.mode = normalizeAppearance({ palette: this.palette, mode }).mode
      this.apply()
      this.persist()
    },
    toggle() {
      this.setMode(this.isDark ? 'light' : 'dark')
    },
    apply() {
      const theme = resolveThemeName({
        mode: this.mode,
        systemPrefersDark: this.systemPrefersDark
      })
      this.isDark = theme === 'dark'
      document.documentElement.setAttribute('data-palette', this.palette)
      document.documentElement.setAttribute('data-theme', theme)
    }
  }
})
