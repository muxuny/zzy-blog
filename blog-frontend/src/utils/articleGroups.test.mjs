import test from 'node:test'
import assert from 'node:assert/strict'
import {
  GROUP_FILTER_ALL,
  GROUP_FILTER_UNGROUPED,
  buildArticleGroupIds,
  buildArticleGroupIdsForSave,
  buildMyArticleGroupParams,
  createGroupFilterKey,
  formatArticleGroupNames,
  getFirstArticleGroupId
} from './articleGroups.js'

test('buildMyArticleGroupParams keeps all articles unscoped', () => {
  assert.deepEqual(
    buildMyArticleGroupParams({ page: 2, size: 10, status: 'draft' }, GROUP_FILTER_ALL),
    { page: 2, size: 10, status: 'draft' }
  )
})

test('buildMyArticleGroupParams adds ungrouped flag for virtual group', () => {
  assert.deepEqual(
    buildMyArticleGroupParams({ page: 1, size: 10 }, GROUP_FILTER_UNGROUPED),
    { page: 1, size: 10, ungrouped: true }
  )
})

test('buildMyArticleGroupParams adds groupId for concrete group', () => {
  assert.deepEqual(
    buildMyArticleGroupParams({ page: 1, size: 10 }, createGroupFilterKey(42)),
    { page: 1, size: 10, groupId: 42 }
  )
})

test('buildArticleGroupIds maps optional selected group to backend list field', () => {
  assert.deepEqual(buildArticleGroupIds(''), [])
  assert.deepEqual(buildArticleGroupIds(null), [])
  assert.deepEqual(buildArticleGroupIds(8), [8])
})

test('buildArticleGroupIdsForSave preserves hidden existing groups when editing', () => {
  assert.deepEqual(buildArticleGroupIdsForSave(11, [{ id: 11 }, { id: 12 }]), [11, 12])
  assert.deepEqual(buildArticleGroupIdsForSave('', [{ id: 11 }, { id: 12 }]), [])
  assert.deepEqual(buildArticleGroupIdsForSave(13, [{ id: 11 }, { id: 12 }]), [13, 12])
})

test('getFirstArticleGroupId returns the first group id for edit form', () => {
  assert.equal(getFirstArticleGroupId([]), '')
  assert.equal(getFirstArticleGroupId([{ id: 11 }, { id: 12 }]), 11)
})

test('formatArticleGroupNames shows virtual ungrouped label when empty', () => {
  assert.equal(formatArticleGroupNames([]), '未分组')
  assert.equal(formatArticleGroupNames([{ name: '产品' }, { name: '复盘' }]), '产品 / 复盘')
})
