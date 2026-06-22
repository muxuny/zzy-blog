import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'

const source = readFileSync(new URL('./MarkdownRenderer.vue', import.meta.url), 'utf8')

test('MarkdownRenderer uses Toast UI Viewer for article rendering', () => {
  assert.match(source, /@toast-ui\/editor\/viewer/)
  assert.doesNotMatch(source, /from 'marked'/)
})
