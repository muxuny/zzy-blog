import test from 'node:test'
import assert from 'node:assert/strict'
import {
  PAGE_COPY_DEFAULTS,
  PAGE_COPY_GROUPS,
  mergePageCopies,
  resolvePageCopy
} from './pageCopy.js'

test('page copy defaults cover the first configurable page scope', () => {
  assert.equal(PAGE_COPY_DEFAULTS.length, 17)
  assert.deepEqual(PAGE_COPY_GROUPS, ['公开与用户页', '创作中心', '后台管理'])
  assert.deepEqual(
    PAGE_COPY_DEFAULTS.map(item => item.copyKey),
    [
      'home.hero',
      'tag.index',
      'auth.login',
      'auth.register',
      'reading.overview',
      'reading.history',
      'favorites.index',
      'creator.articles',
      'creator.article.create',
      'creator.article.edit',
      'admin.dashboard',
      'admin.articles',
      'admin.article.create',
      'admin.article.edit',
      'admin.resources',
      'admin.users',
      'admin.profile'
    ]
  )
})

test('resolvePageCopy replaces placeholders and falls back for unknown keys', () => {
  assert.equal(resolvePageCopy('tag.index', { tagName: 'Vue' }).title, 'Vue')
  assert.equal(resolvePageCopy('tag.index').title, '标签')

  const missing = resolvePageCopy('missing.key')

  assert.equal(missing.copyKey, 'missing.key')
  assert.equal(missing.title, '页面')
  assert.equal(missing.description, '')
})

test('mergePageCopies overlays known remote copy and ignores unknown keys', () => {
  const merged = mergePageCopies([
    {
      copyKey: 'home.hero',
      pageName: '首页',
      pageGroup: '公开与用户页',
      eyebrow: '新的眉标',
      title: '新的首页标题',
      description: ''
    },
    {
      copyKey: 'unknown.key',
      title: '不应该出现'
    }
  ])

  assert.equal(merged.length, 17)
  assert.equal(merged.find(item => item.copyKey === 'home.hero').title, '新的首页标题')
  assert.equal(merged.find(item => item.copyKey === 'home.hero').description, '')
  assert.equal(merged.some(item => item.copyKey === 'unknown.key'), false)
})

test('resolvePageCopy can read from merged remote copy list', () => {
  const merged = mergePageCopies([
    {
      copyKey: 'admin.dashboard',
      eyebrow: '自定义概览',
      title: '自定义仪表盘',
      description: '自定义描述'
    }
  ])

  const copy = resolvePageCopy('admin.dashboard', {}, merged)

  assert.equal(copy.eyebrow, '自定义概览')
  assert.equal(copy.title, '自定义仪表盘')
  assert.equal(copy.description, '自定义描述')
})
