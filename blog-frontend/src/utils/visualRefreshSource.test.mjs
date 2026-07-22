import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'

function read(relativePath) {
  return readFileSync(new URL(relativePath, import.meta.url), 'utf8')
}

test('theme toggle renders an appearance popover with palette and mode choices', () => {
  const source = read('../components/ThemeToggle.vue')

  assert.match(source, /class="appearance-trigger"/)
  assert.match(source, /外观/)
  assert.match(source, /themeStore\.palettes/)
  assert.match(source, /themeStore\.modes/)
  assert.match(source, /setPalette/)
  assert.match(source, /setMode/)
  assert.match(source, /class="palette-swatch"/)
})
