import test from 'node:test'
import assert from 'node:assert/strict'
import { existsSync, readFileSync } from 'node:fs'
import request from '../api/request.js'
import {
  clearReadingHistory,
  deleteReadingHistory,
  getReadingHistory,
  getReadingOverview
} from '../api/reading.js'
import {
  buildReadingHistoryParams,
  formatReadingTime,
  getReadingHistoryGroupKey,
  groupReadingHistory,
  getPageAfterHistoryDeletion
} from './readingHistory.js'

const historyFiles = {
  page: new URL('../views/ReadingHistory.vue', import.meta.url),
  item: new URL('../components/ReadingHistoryItem.vue', import.meta.url),
  space: new URL('../views/ReadingSpace.vue', import.meta.url),
  router: new URL('../router/index.js', import.meta.url)
}

function readSource(path) {
  return existsSync(path) ? readFileSync(path, 'utf8') : ''
}

function withoutComments(source) {
  return source
    .replace(/<!--[\s\S]*?-->/g, '')
    .replace(/\/\*[\s\S]*?\*\//g, '')
    .replace(/^\s*\/\/.*$/gm, '')
}

const pageSource = withoutComments(readSource(historyFiles.page))
const itemSource = withoutComments(readSource(historyFiles.item))
const readingSpaceSource = withoutComments(readSource(historyFiles.space))
const routerSource = withoutComments(readSource(historyFiles.router))
const normalizedPageSource = pageSource.replace(/\s+/g, ' ')
const normalizedItemSource = itemSource.replace(/\s+/g, ' ')
const normalizedReadingSpaceSource = readingSpaceSource.replace(/\s+/g, ' ')

test('reading history view and item component exist', () => {
  assert.equal(existsSync(historyFiles.page), true, 'ReadingHistory.vue should exist')
  assert.equal(existsSync(historyFiles.item), true, 'ReadingHistoryItem.vue should exist')
})

test('reading history route is lazy loaded and protected', () => {
  assert.match(routerSource, /path:\s*['"]\/reading\/history['"]/)
  assert.match(routerSource, /name:\s*['"]ReadingHistory['"]/)
  assert.match(routerSource, /import\(['"]\.\.\/views\/ReadingHistory\.vue['"]\)/)
  assert.match(routerSource, /path:\s*['"]\/reading\/history['"][\s\S]*?meta:\s*\{\s*requiresAuth:\s*true\s*\}/)
})

test('reading space separates continuation history favorites and discovery', () => {
  assert.equal(existsSync(historyFiles.space), true, 'ReadingSpace.vue should exist')
  for (const contract of [
    'getReadingOverview()',
    '上次阅读',
    '最近阅读',
    '最近收藏',
    'to="/reading/history"',
    'to="/favorites"',
    'to="/"',
    '发现更多文章'
  ]) {
    assert.ok(readingSpaceSource.includes(contract), `Missing reading space contract: ${contract}`)
  }
  assert.match(normalizedReadingSpaceSource, /<AppHeader\s*\/?>/)
  assert.match(readingSpaceSource, /<el-skeleton\b/)
})

test('reading space treats cover images with repeated labels as decorative', () => {
  assert.match(
    normalizedReadingSpaceSource,
    /<img v-if="overview\.lastRead\.coverImage"[^>]*class="last-read-cover"[^>]*alt=""[^>]*\/?>/
  )
  assert.match(
    normalizedReadingSpaceSource,
    /<img v-if="item\.coverImage"[^>]*class="favorite-cover"[^>]*alt=""[^>]*\/?>/
  )
  assert.doesNotMatch(readingSpaceSource, /:alt="(?:overview\.lastRead\.title|item\.title)"/)
})

test('reading space guards whole-item links and private snapshots', () => {
  assert.match(readingSpaceSource, /<article\s+v-if="overview\.lastRead"[^>]*class="last-read"/)
  assert.match(readingSpaceSource, /v-if="overview\.lastRead\.available"[\s\S]*?class="card-open-link"/)
  assert.match(readingSpaceSource, /content="该文章暂未公开"/)
  assert.match(readingSpaceSource, /tabindex="0"/)
  assert.match(readingSpaceSource, /该文章暂未公开/)

  const previewUnavailableBranches = [
    ...readingSpaceSource.matchAll(
      /<template\s+v-else>\s*<div class="preview-copy">[\s\S]*?<\/div>\s*<\/template>/g
    )
  ].map(match => match[0])
  const unavailableBranches = [
    {
      name: 'last read',
      source: readingSpaceSource.match(/<template\s+v-else>\s*<div class="last-read-copy unavailable-copy">[\s\S]*?<\/template>/)?.[0] || '',
      fieldPattern: /overview\.lastRead\.([A-Za-z][A-Za-z0-9]*)/g,
      allowedFields: ['lastReadAt', 'title']
    },
    {
      name: 'history',
      source: previewUnavailableBranches.find(source => source.includes('item.lastReadAt')) || '',
      fieldPattern: /item\.([A-Za-z][A-Za-z0-9]*)/g,
      allowedFields: ['lastReadAt', 'title']
    },
    {
      name: 'favorites',
      source: previewUnavailableBranches.find(source => source.includes('item.favoritedAt')) || '',
      fieldPattern: /item\.([A-Za-z][A-Za-z0-9]*)/g,
      allowedFields: ['favoritedAt', 'title']
    }
  ]
  for (const branch of unavailableBranches) {
    assert.ok(branch.source, `${branch.name} needs an explicit unavailable branch`)
    assert.match(branch.source, /<el-tooltip\b/)
    assert.match(branch.source, /tabindex="0"/)
    assert.match(branch.source, /该文章暂未公开/)
    assert.doesNotMatch(branch.source, /<RouterLink\b/)
    const referencedFields = [
      ...new Set([...branch.source.matchAll(branch.fieldPattern)].map(match => match[1]))
    ].sort()
    assert.deepEqual(referencedFields, branch.allowedFields, `${branch.name} may only use snapshot fields`)
  }
})

test('reading space guards history and favorite preview links independently', () => {
  const previewArticles = [
    {
      name: 'history',
      source: readingSpaceSource.match(
        /<article\s+v-for="item in overview\.recentHistory"[\s\S]*?class="history-preview"[\s\S]*?<\/article>/
      )?.[0] || ''
    },
    {
      name: 'favorites',
      source: readingSpaceSource.match(
        /<article\s+v-for="item in overview\.recentFavorites"[\s\S]*?class="favorite-preview"[\s\S]*?<\/article>/
      )?.[0] || ''
    }
  ]

  for (const article of previewArticles) {
    assert.ok(article.source, `${article.name} preview article should exist`)
    assert.match(
      article.source,
      /<RouterLink\s+v-if="item\.available"\s+class="card-open-link"/
    )
    assert.equal(
      (article.source.match(/<RouterLink\b/g) || []).length,
      1,
      `${article.name} preview should have exactly one conditional detail link`
    )
    const unavailableBranch = article.source.match(/<template\s+v-else>[\s\S]*?<\/template>/)?.[0] || ''
    assert.ok(unavailableBranch, `${article.name} preview needs an explicit unavailable branch`)
    assert.doesNotMatch(unavailableBranch, /<RouterLink\b/)
  }
})

test('reading space normalizes overview and ignores stale or unmounted loads', () => {
  for (const contract of [
    'lastRead: null',
    'recentHistory: []',
    'historyTotal: 0',
    'recentFavorites: []',
    'favoriteTotal: 0'
  ]) {
    assert.ok(readingSpaceSource.includes(contract), `Missing overview default: ${contract}`)
  }
  assert.match(readingSpaceSource, /const data = result\.data/)
  assert.match(readingSpaceSource, /Array\.isArray\(data\.recentHistory\)/)
  assert.match(readingSpaceSource, /Array\.isArray\(data\.recentFavorites\)/)
  assert.match(readingSpaceSource, /const requestId = \+\+requestVersion/)
  assert.match(readingSpaceSource, /if \(!componentActive \|\| requestId !== requestVersion\) return null/)
  assert.match(readingSpaceSource, /onBeforeUnmount\([\s\S]*?componentActive = false[\s\S]*?requestVersion \+= 1/s)
  assert.match(readingSpaceSource, /error\.response\?\.data\?\.message \|\| error\.message \|\| '我的阅读加载失败，请重试'/)
  assert.match(readingSpaceSource, /@click="retryLoad"/)
})

test('reading space keeps previews responsive and focuses only actionable surfaces', () => {
  assert.match(readingSpaceSource, /\.card-open-link\s*\{[^}]*position:\s*absolute;[^}]*inset:\s*0;/s)
  assert.match(readingSpaceSource, /\.card-open-link:focus-visible\s*\{/)
  assert.match(readingSpaceSource, /\.last-read \.card-open-link:focus-visible\s*\{[^}]*outline-offset:\s*-3px;/s)
  assert.match(readingSpaceSource, /\.timeline-dot\s*\{[^}]*pointer-events:\s*none;/s)
  assert.match(readingSpaceSource, /\.favorite-grid\s*\{[^}]*grid-template-columns:\s*repeat\(2,\s*minmax\(0,\s*1fr\)\);/s)
  assert.match(readingSpaceSource, /overflow-wrap:\s*anywhere;/)
  assert.match(readingSpaceSource, /@media\s*\(max-width:[^)]+\)[\s\S]*?\.favorite-grid[\s\S]*?grid-template-columns:\s*minmax\(0,\s*1fr\);/s)
  assert.match(readingSpaceSource, /@media\s*\(prefers-reduced-motion:\s*reduce\)/)
  assert.match(readingSpaceSource, /@media\s*\(prefers-reduced-motion:\s*reduce\)[\s\S]*?:deep\(\.el-skeleton\.is-animated \.el-skeleton__item\)[\s\S]*?animation:\s*none;/s)
  assert.doesNotMatch(readingSpaceSource, /\.(?:preview-title|preview-meta|title-text|last-read-title)[^{]*(?::hover|:focus(?!-visible))/)
})

test('reading space route is lazy loaded and protected', () => {
  assert.match(routerSource, /path:\s*['"]\/reading['"]/)
  assert.match(routerSource, /name:\s*['"]ReadingSpace['"]/)
  assert.match(routerSource, /import\(['"]\.\.\/views\/ReadingSpace\.vue['"]\)/)
  assert.match(
    routerSource,
    /\{\s*path:\s*['"]\/reading['"],\s*name:\s*['"]ReadingSpace['"],\s*component:\s*\(\)\s*=>\s*import\(['"]\.\.\/views\/ReadingSpace\.vue['"]\),\s*meta:\s*\{\s*requiresAuth:\s*true\s*\}\s*\}/
  )
})

test('reading history item exposes a safe whole-item link only for available articles', () => {
  assert.match(normalizedItemSource, /<article[^>]*class="reading-history-item"[^>]*:class="\{ 'is-unavailable': !item\.available \}"/)
  assert.match(normalizedItemSource, /<RouterLink v-if="item\.available" class="item-open-link" :to="`\/article\/\$\{item\.articleId\}`"[^>]*\/?>/)
  assert.doesNotMatch(itemSource, /<RouterLink\b(?![^>]*v-if="item\.available")[^>]*>/)
  assert.match(itemSource, /\.reading-history-item\s*\{[^}]*position:\s*relative;/s)
  assert.match(itemSource, /\.item-open-link\s*\{[^}]*position:\s*absolute;[^}]*inset:\s*0;[^}]*z-index:\s*1;/s)
})

test('reading history item keeps unavailable snapshots private and focusable', () => {
  const availableBranch = itemSource.match(/<template\s+v-if="item\.available">([\s\S]*?)<\/template>/)?.[1] || ''
  const unavailableBranch = itemSource.match(/<template\s+v-else>([\s\S]*?)<\/template>/)?.[1] || ''

  assert.match(unavailableBranch, /<el-tooltip\b/)
  assert.match(unavailableBranch, /content="该文章暂未公开"/)
  assert.match(unavailableBranch, /tabindex="0"/)
  assert.match(unavailableBranch, /该文章暂未公开/)
  for (const field of ['item.summary', 'item.coverImage', 'item.authorName', 'item.viewCount']) {
    assert.ok(availableBranch.includes(field), `${field} should be inside the available branch`)
    assert.ok(!unavailableBranch.includes(field), `${field} must not leak into the unavailable branch`)
  }
})

test('reading history item formats times and isolates the accessible remove action', () => {
  for (const field of ['item.lastReadAt', 'item.firstReadAt']) {
    assert.ok(itemSource.includes(`formatReadingTime(${field})`))
  }
  assert.match(itemSource, /item\.readCount/)
  assert.match(normalizedItemSource, /item: \{ type: Object, required: true \}/)
  assert.match(normalizedItemSource, /removing: \{ type: Boolean, default: false \}/)
  assert.match(normalizedItemSource, /disabled: \{ type: Boolean, default: false \}/)
  assert.match(itemSource, /:disabled="disabled"/)
  assert.match(itemSource, /defineEmits\(\['remove'\]\)/)
  assert.match(itemSource, /@click\.stop="\$emit\('remove', item\)"/)
  assert.match(itemSource, /title="删除历史"/)
  assert.match(itemSource, /aria-label="删除历史"/)
  assert.match(itemSource, /\.remove-button\s*\{[^}]*z-index:\s*(?:[2-9]|\d{2,});/s)
})

test('reading history item has stable responsive media and root-only hover treatment', () => {
  assert.match(itemSource, /width:\s*144px;/)
  assert.match(itemSource, /height:\s*96px;/)
  assert.match(itemSource, /aspect-ratio:\s*16\s*\/\s*9;/)
  assert.match(itemSource, /overflow-wrap:\s*anywhere;/)
  assert.match(itemSource, /\.reading-history-item:not\(\.is-unavailable\):hover\s*\{[^}]*(?:border-color|box-shadow):/s)
  assert.doesNotMatch(itemSource, /\.(?:item-title|item-meta|title-text)[^{]*(?::hover|:focus(?!-visible))/)
})

test('reading history page renders grouped timeline states and pagination', () => {
  for (const text of ['阅读历史', '暂无阅读历史', '去发现文章', '阅读历史加载失败，请重试']) {
    assert.ok(pageSource.includes(text), `page should include ${text}`)
  }
  assert.match(normalizedPageSource, /<AppHeader\s*\/?>/)
  assert.match(normalizedPageSource, /<RouterLink[^>]*to="\/reading"/)
  assert.match(pageSource, /groupReadingHistory\(items\.value\)/)
  assert.match(pageSource, /v-for="group in groups"/)
  assert.match(pageSource, /<ReadingHistoryItem\b/)
  assert.match(pageSource, /<el-skeleton\b/)
  assert.match(pageSource, /class="sr-only"/)
  assert.match(pageSource, /v-if="total > size"/)
  assert.match(pageSource, /:page-size="size"/)
  assert.match(pageSource, /@current-change="handlePageChange"/)
})

test('reading history empty action avoids nested interaction and routes explicitly', () => {
  const emptyState = pageSource.match(/<div v-else class="empty-status"[\s\S]*?<\/div>/)?.[0] || ''

  assert.doesNotMatch(emptyState, /<RouterLink\b[\s\S]*?<el-button\b/)
  assert.match(emptyState, /<el-button[^>]*@click="goDiscover"[^>]*>去发现文章<\/el-button>/)
  assert.match(pageSource, /import \{ useRouter \} from 'vue-router'/)
  assert.match(pageSource, /const router = useRouter\(\)/)
  assert.match(pageSource, /function goDiscover\(\)\s*\{\s*router\.push\('\/'\)\s*\}/)
})

test('reading history page loads retries and corrects stale paginated responses', () => {
  assert.match(pageSource, /getReadingHistory\(params\)/)
  assert.match(pageSource, /buildReadingHistoryParams\(\{[\s\S]*?page:\s*requestPage,[\s\S]*?size:\s*size\.value/s)
  assert.match(pageSource, /const requestId = \+\+requestVersion/)
  assert.match(pageSource, /if \(!componentActive \|\| requestId !== requestVersion\) return null/)
  assert.match(pageSource, /const maxPage = Math\.max\(1, Math\.ceil\(nextTotal \/ size\.value\)\)/)
  assert.match(pageSource, /if \(page\.value > maxPage\)[\s\S]*?page\.value = maxPage[\s\S]*?return await load\(\)/)
  assert.match(pageSource, /@click="retryLoad"/)
  assert.match(pageSource, /function retryLoad\(\)[\s\S]*?void load\(\)/)
})

test('reading history deletion and clear actions are guarded and reload safely', () => {
  assert.match(pageSource, /deleteReadingHistory\(item\.articleId\)/)
  assert.match(pageSource, /getPageAfterHistoryDeletion\(\{[\s\S]*?page:\s*page\.value,[\s\S]*?size:\s*size\.value,[\s\S]*?total:\s*total\.value/s)
  assert.match(pageSource, /ElMessage\.success\('已删除阅读历史'\)/)
  assert.match(pageSource, /clearReadingHistory\(\)/)
  assert.match(pageSource, /ElMessageBox\.confirm\([\s\S]*?清空[\s\S]*?阅读历史/s)
  assert.match(pageSource, /ElMessage\.success\('阅读历史已清空'\)/)
  assert.match(pageSource, /async function clearAllHistory\(\)/)
  assert.match(pageSource, /if \(clearing\.value \|\| removingIds\.value\.size > 0 \|\| !componentActive\) return/)
  assert.match(pageSource, /if \(clearing\.value \|\| isRemoving\(item\.articleId\) \|\| !componentActive\) return/)
  assert.match(pageSource, /:disabled="total === 0 \|\| removingIds\.size > 0"/)
  assert.match(pageSource, /<ReadingHistoryItem[\s\S]*?:disabled="clearing"[\s\S]*?@remove="removeHistory"/)
  assert.match(pageSource, /onBeforeUnmount\([\s\S]*?componentActive = false[\s\S]*?requestVersion \+= 1/s)
})

test('reading API wrappers execute the expected requests', async () => {
  const capturedConfigs = []
  const originalAdapter = request.defaults.adapter
  const originalLocalStorage = Object.getOwnPropertyDescriptor(globalThis, 'localStorage')

  Object.defineProperty(globalThis, 'localStorage', {
    configurable: true,
    value: {
      getItem: () => null,
      removeItem: () => {},
      setItem: () => {},
      clear: () => {}
    }
  })
  request.defaults.adapter = async config => {
    capturedConfigs.push(config)
    return {
      status: 200,
      statusText: 'OK',
      headers: {},
      config,
      data: { code: 200, message: 'success', data: null }
    }
  }

  try {
    await getReadingOverview()
    await getReadingHistory({ page: 2, size: 10 })
    await deleteReadingHistory('758902345678901401')
    await clearReadingHistory()

    assert.deepEqual(capturedConfigs.map(({ method, url, params }) => ({ method, url, params })), [
      { method: 'get', url: '/my/reading/overview', params: undefined },
      { method: 'get', url: '/my/reading/history', params: { page: 2, size: 10 } },
      { method: 'delete', url: '/my/reading/history/758902345678901401', params: undefined },
      { method: 'delete', url: '/my/reading/history', params: undefined }
    ])
  } finally {
    request.defaults.adapter = originalAdapter
    if (originalLocalStorage) {
      Object.defineProperty(globalThis, 'localStorage', originalLocalStorage)
    } else {
      delete globalThis.localStorage
    }
  }
})

test('reading history params normalize numeric and numeric-string inputs', () => {
  assert.deepEqual(buildReadingHistoryParams({ page: 2, size: 25 }), { page: 2, size: 25 })
  assert.deepEqual(buildReadingHistoryParams({ page: '3', size: '50' }), { page: 3, size: 50 })
})

test('reading history params use defaults for null options', () => {
  assert.deepEqual(buildReadingHistoryParams(null), { page: 1, size: 10 })
})

test('reading history params replace invalid page inputs with defaults', () => {
  for (const page of [0, -1, 1.5, Number.NaN, null]) {
    assert.deepEqual(buildReadingHistoryParams({ page, size: 10 }), { page: 1, size: 10 })
  }
})

test('reading history params replace invalid size inputs with defaults', () => {
  for (const size of [0, -1, 1.5, Number.NaN, 101, null]) {
    assert.deepEqual(buildReadingHistoryParams({ page: 1, size }), { page: 1, size: 10 })
  }
})

test('format reading time returns the Chinese local date and time', () => {
  const formatted = formatReadingTime('2026-07-20T09:35:00')

  assert.match(formatted, /2026.*7.*20/)
  assert.match(formatted, /09[:：]35/)
})

test('format reading time returns empty text for invalid nullish and empty values', () => {
  for (const value of ['invalid', null, undefined, '']) {
    assert.equal(formatReadingTime(value), '')
  }
})

test('reading history group key handles local day boundaries', () => {
  const now = new Date(2026, 0, 1, 12)

  assert.equal(getReadingHistoryGroupKey(new Date(2026, 0, 1, 1), now), 'today')
  assert.equal(getReadingHistoryGroupKey(new Date(2025, 11, 31, 23), now), 'yesterday')
  assert.equal(getReadingHistoryGroupKey(new Date(2025, 11, 30, 23), now), 'earlier')
  assert.equal(getReadingHistoryGroupKey(new Date(2026, 0, 2, 1), now), 'today')
  assert.equal(getReadingHistoryGroupKey('invalid', now), 'earlier')
})

test('group reading history returns non-empty ordered groups without mutating items', () => {
  const now = new Date(2026, 6, 20, 12)
  const items = [
    { id: 'a', lastReadAt: new Date(2026, 6, 19, 15) },
    { id: 'b', lastReadAt: new Date(2026, 6, 20, 9) },
    { id: 'c', lastReadAt: 'invalid' },
    { id: 'd', lastReadAt: new Date(2026, 6, 19, 8) }
  ]
  const originalItems = structuredClone(items)

  assert.deepEqual(groupReadingHistory(items, now), [
    { key: 'today', label: '今天', items: [items[1]] },
    { key: 'yesterday', label: '昨天', items: [items[0], items[3]] },
    { key: 'earlier', label: '更早', items: [items[2]] }
  ])
  assert.deepEqual(items, originalItems)
})

test('group reading history safely returns an empty list for non-array input', () => {
  assert.deepEqual(groupReadingHistory(null), [])
})

test('deleting history moves an empty final page to the prior page', () => {
  assert.equal(getPageAfterHistoryDeletion({ page: 3, size: 10, total: 21 }), 2)
})

test('deleting the final history item keeps the first page', () => {
  assert.equal(getPageAfterHistoryDeletion({ page: 1, size: 10, total: 1 }), 1)
})

test('deleting history keeps a populated middle page', () => {
  assert.equal(getPageAfterHistoryDeletion({ page: 2, size: 10, total: 25 }), 2)
})

test('deleting multiple history items recalculates the maximum page', () => {
  assert.equal(getPageAfterHistoryDeletion({ page: 3, size: 10, total: 25, deletedCount: 6 }), 2)
})

test('deleting history uses safe pagination defaults for invalid values', () => {
  assert.equal(getPageAfterHistoryDeletion({ page: 3, size: 10, total: 21, deletedCount: -1 }), 2)
  assert.equal(getPageAfterHistoryDeletion({ page: 3, size: 10, total: 21, deletedCount: Number.NaN }), 2)
  assert.equal(getPageAfterHistoryDeletion({ page: Number.NaN, size: Number.NaN, total: Number.NaN }), 1)
})
