import test from 'node:test'
import assert from 'node:assert/strict'
import {
  createHeadingId,
  extractMarkdownToc,
  getReadingStats
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
    '## 第一部分'
  ].join('\n')

  assert.deepEqual(extractMarkdownToc(markdown), [
    { id: '第一部分', level: 2, text: '第一部分' },
    { id: '细节-a', level: 3, text: '细节 A' },
    { id: '第一部分-2', level: 2, text: '第一部分' }
  ])
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
