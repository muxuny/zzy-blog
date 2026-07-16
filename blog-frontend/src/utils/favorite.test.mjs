import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import * as favoriteUtils from './favorite.js'

const {
  buildFavoriteListParams,
  buildFavoriteLoginRedirect,
  clearFavoriteIntentQuery,
  hasFavoriteIntent
} = favoriteUtils

const favoriteApiSource = readFileSync(new URL('../api/favorite.js', import.meta.url), 'utf8')

const favoriteApiContracts = [
  "export const getFavorites = params => request.get('/my/favorites', { params })",
  'export const getFavoriteStatus = (articleId, config = {}) => request.get(`/my/favorites/${articleId}/status`, { skipErrorMessage: true, ...config })',
  'export const favoriteArticle = (articleId, config = {}) => request.put(`/my/favorites/${articleId}`, undefined, config)',
  'export const unfavoriteArticle = (articleId, config = {}) => request.delete(`/my/favorites/${articleId}`, config)'
]

function assertFavoriteApiContract(source) {
  const normalizedSource = source.replace(/\s+/g, ' ').trim()
  for (const contract of favoriteApiContracts) {
    assert.ok(normalizedSource.includes(contract), `Missing API contract: ${contract}`)
  }
}

test('favorite API contract detects missing wrappers', () => {
  assert.throws(
    () => assertFavoriteApiContract("import request from './request'"),
    /Missing API contract/
  )
})

test('favorite API wrappers preserve methods paths params and status error handling', () => {
  assertFavoriteApiContract(favoriteApiSource)
})

test('favorite login intent returns to article and is consumed once', () => {
  assert.equal(buildFavoriteLoginRedirect('758902345678901401'),
    '/article/758902345678901401?favorite=1')
  assert.equal(hasFavoriteIntent({ favorite: '1' }), true)
  assert.deepEqual(clearFavoriteIntentQuery({ favorite: '1', from: 'home' }), { from: 'home' })
})

test('clearing favorite intent does not mutate the original query', () => {
  const query = { favorite: '1', from: 'home' }

  clearFavoriteIntentQuery(query)

  assert.deepEqual(query, { favorite: '1', from: 'home' })
})

test('favorite intent only accepts the string value 1', () => {
  assert.equal(hasFavoriteIntent({ favorite: 1 }), false)
  assert.equal(hasFavoriteIntent({ favorite: true }), false)
  assert.equal(hasFavoriteIntent({ favorite: 'true' }), false)
  assert.equal(hasFavoriteIntent({}), false)
})

test('favorite request context rejects deleted refreshed and stale auth state', () => {
  assert.equal(typeof favoriteUtils.isFavoriteRequestContextCurrent, 'function')
  const context = {
    componentActive: true,
    requestArticleId: '758902345678901401',
    currentArticleId: '758902345678901401',
    requestLoadToken: 3,
    currentLoadToken: 3,
    requestStateVersion: 7,
    currentStateVersion: 7,
    requestAuthToken: 'token-a',
    currentAuthToken: 'token-a'
  }

  assert.equal(favoriteUtils.isFavoriteRequestContextCurrent(context), true)
  assert.equal(favoriteUtils.isFavoriteRequestContextCurrent({
    ...context,
    currentAuthToken: null
  }), false)
  assert.equal(favoriteUtils.isFavoriteRequestContextCurrent({
    ...context,
    currentAuthToken: 'token-b'
  }), false)
  assert.equal(favoriteUtils.isFavoriteRequestContextCurrent({
    ...context,
    requestAuthToken: ''
  }), false)
})

test('favorite request context rejects component article load and state changes', () => {
  assert.equal(typeof favoriteUtils.isFavoriteRequestContextCurrent, 'function')
  const context = {
    componentActive: true,
    requestArticleId: 'article-a',
    currentArticleId: 'article-a',
    requestLoadToken: 2,
    currentLoadToken: 2,
    requestStateVersion: 4,
    currentStateVersion: 4,
    requestAuthToken: 'token-a',
    currentAuthToken: 'token-a'
  }
  const changes = [
    { componentActive: false },
    { currentArticleId: 'article-b' },
    { currentLoadToken: 3 },
    { currentStateVersion: 5 }
  ]

  for (const change of changes) {
    assert.equal(favoriteUtils.isFavoriteRequestContextCurrent({ ...context, ...change }), false)
  }
})

test('favorite intent cleanup requires unchanged auth and route context', () => {
  assert.equal(typeof favoriteUtils.canConsumeFavoriteIntent, 'function')
  const context = {
    componentActive: true,
    intentArticleId: 'article-a',
    currentArticleId: 'article-a',
    intentLoadToken: 2,
    currentLoadToken: 2,
    intentFullPath: '/article/article-a?favorite=1&from=home',
    currentFullPath: '/article/article-a?favorite=1&from=home',
    intentPath: '/article/article-a',
    currentPath: '/article/article-a',
    browserPathname: '/article/article-a',
    intentAuthToken: 'token-a',
    currentAuthToken: 'token-a',
    hasIntent: true
  }

  assert.equal(favoriteUtils.canConsumeFavoriteIntent(context), true)
  assert.equal(favoriteUtils.canConsumeFavoriteIntent({ ...context, currentAuthToken: null }), false)
  assert.equal(favoriteUtils.canConsumeFavoriteIntent({ ...context, currentAuthToken: 'token-b' }), false)
  assert.equal(favoriteUtils.canConsumeFavoriteIntent({ ...context, intentAuthToken: '' }), false)
})

test('favorite intent cleanup rejects lifecycle route pathname and intent changes', () => {
  assert.equal(typeof favoriteUtils.canConsumeFavoriteIntent, 'function')
  const context = {
    componentActive: true,
    intentArticleId: 'article-a',
    currentArticleId: 'article-a',
    intentLoadToken: 2,
    currentLoadToken: 2,
    intentFullPath: '/article/article-a?favorite=1',
    currentFullPath: '/article/article-a?favorite=1',
    intentPath: '/article/article-a',
    currentPath: '/article/article-a',
    browserPathname: '/article/article-a',
    intentAuthToken: 'token-a',
    currentAuthToken: 'token-a',
    hasIntent: true
  }
  const changes = [
    { componentActive: false },
    { currentArticleId: 'article-b' },
    { currentLoadToken: 3 },
    { currentFullPath: '/article/article-a?from=home' },
    { currentPath: '/article/article-b' },
    { browserPathname: '/login' },
    { hasIntent: false }
  ]

  for (const change of changes) {
    assert.equal(favoriteUtils.canConsumeFavoriteIntent({ ...context, ...change }), false)
  }
})

test('favorite list params trim filters and preserve snowflake tag ids', () => {
  assert.deepEqual(buildFavoriteListParams({
    page: 2,
    size: 10,
    keyword: '  architecture  ',
    tagId: '758902345678901301'
  }), {
    page: 2,
    size: 10,
    keyword: 'architecture',
    tagId: '758902345678901301'
  })
})

test('favorite list params omit empty keyword and tag id', () => {
  assert.deepEqual(buildFavoriteListParams({
    page: 1,
    size: 10,
    keyword: '   ',
    tagId: ''
  }), {
    page: 1,
    size: 10
  })
})

test('favorite list params omit nullish tag ids and preserve zero', () => {
  assert.deepEqual(buildFavoriteListParams({ tagId: null }), {
    page: 1,
    size: 10
  })
  assert.deepEqual(buildFavoriteListParams({ tagId: undefined }), {
    page: 1,
    size: 10
  })
  assert.deepEqual(buildFavoriteListParams({ tagId: 0 }), {
    page: 1,
    size: 10,
    tagId: 0
  })
})

test('favorite list params preserve tag id strings unchanged', () => {
  assert.deepEqual(buildFavoriteListParams({
    tagId: '000758902345678901301'
  }), {
    page: 1,
    size: 10,
    tagId: '000758902345678901301'
  })
})
