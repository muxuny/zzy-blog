import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { createPinia, setActivePinia } from 'pinia'
import {
  APPEARANCE_STORAGE_KEY,
  DEFAULT_APPEARANCE,
  LEGACY_THEME_STORAGE_KEY,
  THEME_MODES,
  THEME_PALETTES,
  normalizeAppearance,
  readStoredAppearance,
  resolveThemeName,
  writeStoredAppearance
} from './themeAppearance.js'

function createStorage(initial = {}) {
  const data = new Map(Object.entries(initial))
  return {
    getItem(key) {
      return data.has(key) ? data.get(key) : null
    },
    setItem(key, value) {
      data.set(key, String(value))
    },
    removeItem(key) {
      data.delete(key)
    },
    snapshot() {
      return Object.fromEntries(data.entries())
    }
  }
}

function installThemeStoreGlobals({ storage = createStorage(), localStorageDescriptor, systemPrefersDark = false } = {}) {
  const originalDocument = Object.getOwnPropertyDescriptor(globalThis, 'document')
  const originalWindow = Object.getOwnPropertyDescriptor(globalThis, 'window')
  const originalLocalStorage = Object.getOwnPropertyDescriptor(globalThis, 'localStorage')
  const attributes = new Map()
  const listeners = new Set()
  const mediaQuery = {
    matches: systemPrefersDark,
    addEventListener(eventName, handler) {
      if (eventName === 'change') listeners.add(handler)
    },
    removeEventListener(eventName, handler) {
      if (eventName === 'change') listeners.delete(handler)
    },
    dispatchChange(matches) {
      this.matches = matches
      for (const handler of listeners) handler({ matches })
    }
  }

  Object.defineProperty(globalThis, 'document', {
    configurable: true,
    value: {
      documentElement: {
        setAttribute(name, value) {
          attributes.set(name, String(value))
        },
        getAttribute(name) {
          return attributes.get(name) ?? null
        }
      }
    }
  })
  Object.defineProperty(globalThis, 'window', {
    configurable: true,
    value: {
      matchMedia(query) {
        assert.equal(query, '(prefers-color-scheme: dark)')
        return mediaQuery
      }
    }
  })
  Object.defineProperty(globalThis, 'localStorage', localStorageDescriptor || {
    configurable: true,
    value: storage
  })

  return {
    attributes,
    mediaQuery,
    storage,
    restore() {
      restoreGlobalDescriptor('document', originalDocument)
      restoreGlobalDescriptor('window', originalWindow)
      restoreGlobalDescriptor('localStorage', originalLocalStorage)
    }
  }
}

function restoreGlobalDescriptor(name, descriptor) {
  if (descriptor) {
    Object.defineProperty(globalThis, name, descriptor)
  } else {
    delete globalThis[name]
  }
}

test('theme constants expose three palettes and three modes', () => {
  assert.deepEqual(THEME_PALETTES.map(item => item.value), ['juniper', 'fog', 'copper'])
  assert.deepEqual(
    THEME_PALETTES.map(({ label, description }) => ({ label, description })),
    [
      { label: '松针绿', description: '清爽克制，默认主题' },
      { label: '雾蓝灰', description: '冷静耐读，适合长文' },
      { label: '暖铜绿', description: '更有温度，适合创作' }
    ]
  )
  assert.deepEqual(THEME_MODES.map(item => item.value), ['light', 'dark', 'system'])
  assert.deepEqual(THEME_MODES.map(item => item.label), ['浅色', '深色', '跟随系统'])
  assert.deepEqual(DEFAULT_APPEARANCE, { palette: 'juniper', mode: 'light' })
  assert.equal(APPEARANCE_STORAGE_KEY, 'themeAppearance')
  assert.equal(LEGACY_THEME_STORAGE_KEY, 'theme')
})

test('normalizeAppearance falls back to defaults for unknown values', () => {
  assert.deepEqual(normalizeAppearance({ palette: 'unknown', mode: 'future' }), DEFAULT_APPEARANCE)
  assert.deepEqual(normalizeAppearance({ palette: 'fog', mode: 'dark' }), { palette: 'fog', mode: 'dark' })
  assert.deepEqual(normalizeAppearance(null), DEFAULT_APPEARANCE)
})

test('readStoredAppearance prefers new storage and migrates legacy theme value', () => {
  const current = createStorage({
    themeAppearance: JSON.stringify({ palette: 'copper', mode: 'system' }),
    theme: 'dark'
  })
  const legacy = createStorage({ theme: 'dark' })

  assert.deepEqual(readStoredAppearance(current), { palette: 'copper', mode: 'system' })
  assert.deepEqual(readStoredAppearance(legacy), { palette: 'juniper', mode: 'dark' })
})

test('readStoredAppearance handles invalid JSON and unavailable storage', () => {
  const invalid = createStorage({ themeAppearance: '{bad json' })
  const broken = {
    getItem() {
      throw new Error('blocked')
    }
  }

  assert.deepEqual(readStoredAppearance(invalid), DEFAULT_APPEARANCE)
  assert.deepEqual(readStoredAppearance(broken), DEFAULT_APPEARANCE)
})

test('readStoredAppearance returns fresh fallback appearance objects', () => {
  const fallback = readStoredAppearance(createStorage())

  assert.notEqual(fallback, DEFAULT_APPEARANCE)

  fallback.palette = 'copper'
  fallback.mode = 'dark'

  assert.deepEqual(readStoredAppearance(createStorage()), { palette: 'juniper', mode: 'light' })
  assert.deepEqual(DEFAULT_APPEARANCE, { palette: 'juniper', mode: 'light' })
})

test('writeStoredAppearance persists normalized appearance and ignores storage errors', () => {
  const storage = createStorage()
  const broken = {
    setItem() {
      throw new Error('blocked')
    }
  }

  assert.equal(writeStoredAppearance(storage, { palette: 'fog', mode: 'dark' }), true)
  assert.deepEqual(JSON.parse(storage.snapshot().themeAppearance), { palette: 'fog', mode: 'dark' })
  assert.equal(writeStoredAppearance(broken, { palette: 'copper', mode: 'system' }), false)
  assert.equal(writeStoredAppearance({}, { palette: 'fog', mode: 'dark' }), false)
  assert.equal(writeStoredAppearance(undefined, { palette: 'fog', mode: 'dark' }), false)
})

test('resolveThemeName respects explicit and system modes', () => {
  assert.equal(resolveThemeName({ mode: 'light', systemPrefersDark: true }), 'light')
  assert.equal(resolveThemeName({ mode: 'dark', systemPrefersDark: false }), 'dark')
  assert.equal(resolveThemeName({ mode: 'system', systemPrefersDark: true }), 'dark')
  assert.equal(resolveThemeName({ mode: 'system', systemPrefersDark: false }), 'light')
})

test('resolveThemeName defaults missing appearance to light', () => {
  assert.equal(resolveThemeName(), 'light')
  assert.equal(resolveThemeName({}), 'light')
})

test('theme store applies palette and resolved theme attributes', () => {
  const source = readFileSync(new URL('../stores/theme.js', import.meta.url), 'utf8')

  assert.match(source, /getLocalStorage/)
  assert.match(source, /palette:\s*DEFAULT_APPEARANCE\.palette/)
  assert.match(source, /mode:\s*DEFAULT_APPEARANCE\.mode/)
  assert.match(source, /setPalette\(palette\)/)
  assert.match(source, /setMode\(mode\)/)
  assert.match(source, /document\.documentElement\.setAttribute\('data-palette'/)
  assert.match(source, /document\.documentElement\.setAttribute\('data-theme'/)
  assert.match(source, /resolveThemeName/)
  assert.doesNotMatch(source, /this\.apply\(false\)/)
})

test('theme store actions apply appearance and persist normalized settings', async () => {
  const globals = installThemeStoreGlobals({
    storage: createStorage({ theme: 'dark' }),
    systemPrefersDark: false
  })

  try {
    setActivePinia(createPinia())
    const { useThemeStore } = await import('../stores/theme.js')
    const store = useThemeStore()

    store.init()

    assert.equal(store.mode, 'dark')
    assert.equal(store.isDark, true)
    assert.equal(globalThis.document.documentElement.getAttribute('data-theme'), 'dark')
    assert.equal(globalThis.document.documentElement.getAttribute('data-palette'), 'juniper')

    store.setPalette('fog')

    assert.equal(globalThis.document.documentElement.getAttribute('data-palette'), 'fog')
    assert.deepEqual(JSON.parse(globals.storage.snapshot().themeAppearance), { palette: 'fog', mode: 'dark' })

    store.setMode('system')

    assert.equal(store.isDark, false)
    assert.equal(globalThis.document.documentElement.getAttribute('data-theme'), 'light')
    assert.deepEqual(JSON.parse(globals.storage.snapshot().themeAppearance), { palette: 'fog', mode: 'system' })

    globals.mediaQuery.dispatchChange(true)

    assert.equal(store.isDark, true)
    assert.equal(globalThis.document.documentElement.getAttribute('data-theme'), 'dark')

    store.toggle()

    assert.equal(store.mode, 'light')
    assert.equal(store.isDark, false)
    assert.equal(globalThis.document.documentElement.getAttribute('data-theme'), 'light')
    assert.deepEqual(JSON.parse(globals.storage.snapshot().themeAppearance), { palette: 'fog', mode: 'light' })
  } finally {
    globals.restore()
  }
})

test('theme store keeps applying when localStorage access throws', async () => {
  const globals = installThemeStoreGlobals({
    localStorageDescriptor: {
      configurable: true,
      get() {
        throw new DOMException('blocked', 'SecurityError')
      }
    }
  })

  try {
    setActivePinia(createPinia())
    const { useThemeStore } = await import('../stores/theme.js')
    const store = useThemeStore()

    assert.doesNotThrow(() => store.init())
    assert.equal(globalThis.document.documentElement.getAttribute('data-theme'), 'light')
    assert.equal(globalThis.document.documentElement.getAttribute('data-palette'), 'juniper')

    assert.doesNotThrow(() => store.setMode('dark'))
    assert.equal(store.isDark, true)
    assert.equal(globalThis.document.documentElement.getAttribute('data-theme'), 'dark')
  } finally {
    globals.restore()
  }
})
