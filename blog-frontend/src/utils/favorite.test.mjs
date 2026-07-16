import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import {
  buildFavoriteListParams,
  buildFavoriteLoginRedirect,
  clearFavoriteIntentQuery,
  hasFavoriteIntent
} from './favorite.js'

const favoriteApiSource = readFileSync(new URL('../api/favorite.js', import.meta.url), 'utf8')

const favoriteApiContracts = [
  "export const getFavorites = params => request.get('/my/favorites', { params })",
  'export const getFavoriteStatus = articleId => request.get(`/my/favorites/${articleId}/status`, { skipErrorMessage: true })',
  'export const favoriteArticle = articleId => request.put(`/my/favorites/${articleId}`)',
  'export const unfavoriteArticle = articleId => request.delete(`/my/favorites/${articleId}`)'
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
