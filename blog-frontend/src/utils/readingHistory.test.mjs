import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import {
  buildReadingHistoryParams,
  formatReadingTime,
  getReadingHistoryGroupKey,
  groupReadingHistory,
  getPageAfterHistoryDeletion
} from './readingHistory.js'

const readingApiSource = readFileSync(new URL('../api/reading.js', import.meta.url), 'utf8')

const readingApiContracts = [
  "export const getReadingOverview = () => request.get('/my/reading/overview')",
  "export const getReadingHistory = params => request.get('/my/reading/history', { params })",
  "export const deleteReadingHistory = articleId => request.delete(`/my/reading/history/${articleId}`)",
  "export const clearReadingHistory = () => request.delete('/my/reading/history')"
]

function assertReadingApiContract(source) {
  const normalizedSource = source.replace(/\s+/g, ' ').trim()
  for (const contract of readingApiContracts) {
    assert.ok(normalizedSource.includes(contract), `Missing API contract: ${contract}`)
  }
}

test('reading API contract detects missing wrappers', () => {
  assert.throws(
    () => assertReadingApiContract("import request from './request'"),
    /Missing API contract/
  )
})

test('reading API wrappers preserve methods paths and params', () => {
  assertReadingApiContract(readingApiSource)
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
