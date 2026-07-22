export const THEME_PALETTES = [
  { value: 'juniper', label: '松针绿', description: '清爽克制，默认主题' },
  { value: 'fog', label: '雾蓝灰', description: '冷静耐读，适合长文' },
  { value: 'copper', label: '暖铜绿', description: '更有温度，适合创作' }
]

export const THEME_MODES = [
  { value: 'light', label: '浅色' },
  { value: 'dark', label: '深色' },
  { value: 'system', label: '跟随系统' }
]

export const DEFAULT_APPEARANCE = {
  palette: 'juniper',
  mode: 'light'
}

export const APPEARANCE_STORAGE_KEY = 'themeAppearance'
export const LEGACY_THEME_STORAGE_KEY = 'theme'

const paletteValues = new Set(THEME_PALETTES.map(item => item.value))
const modeValues = new Set(THEME_MODES.map(item => item.value))

export function normalizeAppearance(value) {
  const source = value && typeof value === 'object' ? value : {}
  return {
    palette: paletteValues.has(source.palette) ? source.palette : DEFAULT_APPEARANCE.palette,
    mode: modeValues.has(source.mode) ? source.mode : DEFAULT_APPEARANCE.mode
  }
}

export function readStoredAppearance(storage) {
  try {
    const raw = storage?.getItem?.(APPEARANCE_STORAGE_KEY)
    if (raw) return normalizeAppearance(JSON.parse(raw))

    const legacyTheme = storage?.getItem?.(LEGACY_THEME_STORAGE_KEY)
    if (legacyTheme === 'dark' || legacyTheme === 'light') {
      return normalizeAppearance({ palette: DEFAULT_APPEARANCE.palette, mode: legacyTheme })
    }
  } catch {
    return DEFAULT_APPEARANCE
  }

  return DEFAULT_APPEARANCE
}

export function writeStoredAppearance(storage, appearance) {
  try {
    storage?.setItem?.(APPEARANCE_STORAGE_KEY, JSON.stringify(normalizeAppearance(appearance)))
    return true
  } catch {
    return false
  }
}

export function resolveThemeName({ mode, systemPrefersDark }) {
  const normalized = normalizeAppearance({ mode }).mode
  if (normalized === 'system') return systemPrefersDark ? 'dark' : 'light'
  return normalized
}
