import test from 'node:test'
import assert from 'node:assert/strict'
import {
  ARTICLE_VISIBILITY_PRIVATE,
  ARTICLE_VISIBILITY_PUBLIC,
  articleVisibilityText,
  articleVisibilityType,
  nextArticleVisibility,
  normalizeArticleVisibility
} from './articleVisibility.js'

test('normalizeArticleVisibility defaults unknown values to public', () => {
  assert.equal(normalizeArticleVisibility(undefined), ARTICLE_VISIBILITY_PUBLIC)
  assert.equal(normalizeArticleVisibility(''), ARTICLE_VISIBILITY_PUBLIC)
  assert.equal(normalizeArticleVisibility('friends'), ARTICLE_VISIBILITY_PUBLIC)
  assert.equal(normalizeArticleVisibility(ARTICLE_VISIBILITY_PRIVATE), ARTICLE_VISIBILITY_PRIVATE)
})

test('article visibility helpers return stable Chinese labels and tag types', () => {
  assert.equal(articleVisibilityText(ARTICLE_VISIBILITY_PUBLIC), '公开')
  assert.equal(articleVisibilityText(ARTICLE_VISIBILITY_PRIVATE), '仅自己可见')
  assert.equal(articleVisibilityType(ARTICLE_VISIBILITY_PUBLIC), 'success')
  assert.equal(articleVisibilityType(ARTICLE_VISIBILITY_PRIVATE), 'info')
})

test('nextArticleVisibility toggles public and private', () => {
  assert.equal(nextArticleVisibility(ARTICLE_VISIBILITY_PUBLIC), ARTICLE_VISIBILITY_PRIVATE)
  assert.equal(nextArticleVisibility(ARTICLE_VISIBILITY_PRIVATE), ARTICLE_VISIBILITY_PUBLIC)
})
