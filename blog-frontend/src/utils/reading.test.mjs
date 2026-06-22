import test from 'node:test'
import assert from 'node:assert/strict'
import {
  createHeadingId,
  extractMarkdownToc,
  getReadingStats,
  normalizeArticleMarkdown
} from './reading.js'

test('extractMarkdownToc returns level 2 and 3 headings with stable ids', () => {
  const markdown = [
    '# 标题',
    '## 第一部分',
    '正文',
    '### 细节 A',
    '```js',
    '## 代码里的标题',
    '```',
    '~~~md',
    '## 波浪围栏里的标题',
    '~~~',
    '## 第一部分'
  ].join('\n')

  assert.deepEqual(extractMarkdownToc(markdown), [
    { id: '第一部分', level: 2, text: '第一部分' },
    { id: '细节-a', level: 3, text: '细节 A' },
    { id: '第一部分-2', level: 2, text: '第一部分' }
  ])
})

test('extractMarkdownToc keeps ids aligned and includes level 4 headings', () => {
  const markdown = [
    '# Same',
    '## Same',
    '#### Same',
    '### Same'
  ].join('\n')

  assert.deepEqual(extractMarkdownToc(markdown), [
    { id: 'same-2', level: 2, text: 'Same' },
    { id: 'same-3', level: 4, text: 'Same' },
    { id: 'same-4', level: 3, text: 'Same' }
  ])
})

test('normalizeArticleMarkdown converts body level 1 headings to level 2', () => {
  const markdown = [
    '# 章节',
    '正文里的 # 不变',
    '## 小节',
    '### 细分'
  ].join('\n')

  assert.equal(normalizeArticleMarkdown(markdown), [
    '## 章节',
    '正文里的 # 不变',
    '## 小节',
    '### 细分'
  ].join('\n'))
})

test('normalizeArticleMarkdown keeps level 1 examples inside code fences', () => {
  const markdown = [
    '# 章节',
    '```md',
    '# 示例标题',
    '```',
    '~~~md',
    '# 另一段示例标题',
    '~~~',
    '正文'
  ].join('\n')

  assert.equal(normalizeArticleMarkdown(markdown), [
    '## 章节',
    '```md',
    '# 示例标题',
    '```',
    '~~~md',
    '# 另一段示例标题',
    '~~~',
    '正文'
  ].join('\n'))
})

test('createHeadingId keeps duplicate headings stable', () => {
  const seen = new Map()

  assert.equal(createHeadingId('Vue 组件设计', seen), 'vue-组件设计')
  assert.equal(createHeadingId('Vue 组件设计', seen), 'vue-组件设计-2')
})

test('getReadingStats strips markdown and returns readable numbers', () => {
  const markdown = [
    '## 阅读体验',
    '这是一段中文内容，用来测试阅读时间。',
    'It also has frontend notes and markdown links.',
    '[链接](https://example.com)',
    '`code`'
  ].join('\n')

  const stats = getReadingStats(markdown)

  assert.equal(stats.wordCount > 0, true)
  assert.equal(stats.readingMinutes >= 1, true)
  assert.equal(stats.readingTimeText.endsWith('分钟'), true)
})

test('getReadingStats strips fenced code blocks using backticks and tildes', () => {
  const stats = getReadingStats([
    '## 正文',
    '真实内容',
    '```js',
    'code words should not count',
    '```',
    '~~~md',
    'another code block should not count either',
    '~~~'
  ].join('\n'))

  assert.equal(stats.wordCount, 6)
})
