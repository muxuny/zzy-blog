import test from 'node:test'
import assert from 'node:assert/strict'
import {
  getPageAfterSingleDeletion,
  normalizePageResult,
  shouldShowPagination
} from './pagination.js'

test('normalizePageResult keeps records, total, page, and size usable', () => {
  assert.deepEqual(
    normalizePageResult({ data: [{ id: 1 }], total: '12', page: '2', size: '5' }),
    { records: [{ id: 1 }], total: 12, page: 2, size: 5 }
  )
})

test('normalizePageResult falls back to empty values for malformed input', () => {
  assert.deepEqual(
    normalizePageResult({ data: null, total: 'bad', page: 0, size: -1 }),
    { records: [], total: 0, page: 1, size: 10 }
  )
})

test('getPageAfterSingleDeletion backs up from an emptied last page', () => {
  assert.equal(getPageAfterSingleDeletion({ page: 3, size: 10, total: 21 }), 2)
  assert.equal(getPageAfterSingleDeletion({ page: 1, size: 10, total: 1 }), 1)
  assert.equal(getPageAfterSingleDeletion({ page: 2, size: 10, total: 30 }), 2)
})

test('shouldShowPagination only shows when total exceeds page size', () => {
  assert.equal(shouldShowPagination(11, 10), true)
  assert.equal(shouldShowPagination(10, 10), false)
})
