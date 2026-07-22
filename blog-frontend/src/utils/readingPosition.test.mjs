import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import {
  buildReadingPositionPayload,
  calculateReadingProgress,
  shouldSaveReadingPosition,
  shouldShowResumePrompt
} from './readingPosition.js'

test('calculateReadingProgress clamps progress between 0 and 100', () => {
  assert.equal(calculateReadingProgress({ scrollY: 0, articleTop: 100, articleHeight: 1000, viewportHeight: 500 }), 0)
  assert.equal(calculateReadingProgress({ scrollY: 350, articleTop: 100, articleHeight: 1000, viewportHeight: 500 }), 50)
  assert.equal(calculateReadingProgress({ scrollY: 700, articleTop: 100, articleHeight: 1000, viewportHeight: 500 }), 100)
})

test('buildReadingPositionPayload normalizes safe payload fields', () => {
  assert.deepEqual(buildReadingPositionPayload({
    progressPercent: 42.8,
    scrollY: 3810.9,
    anchorId: ' section-2 ',
    anchorOffset: 120.2,
    articleUpdatedAt: '2026-07-21T16:40:00'
  }), {
    progressPercent: 43,
    scrollY: 3811,
    anchorId: 'section-2',
    anchorOffset: 120,
    articleUpdatedAt: '2026-07-21T16:40:00'
  })
})

test('resume prompt requires backend permission and at least five percent progress', () => {
  assert.equal(shouldShowResumePrompt({ canResume: true, progressPercent: 5 }), true)
  assert.equal(shouldShowResumePrompt({ canResume: true, progressPercent: 4 }), false)
  assert.equal(shouldShowResumePrompt({ canResume: false, progressPercent: 80 }), false)
})

test('save gate throttles non-forced saves and always allows forced saves', () => {
  assert.equal(shouldSaveReadingPosition({ now: 10000, lastSavedAt: 2000, intervalMs: 9000 }), false)
  assert.equal(shouldSaveReadingPosition({ now: 12000, lastSavedAt: 2000, intervalMs: 9000 }), true)
  assert.equal(shouldSaveReadingPosition({ now: 10000, lastSavedAt: 9999, intervalMs: 9000, force: true }), true)
})

test('article detail source wires resume prompt and silent position saving', () => {
  const source = readFileSync(new URL('../views/ArticleDetail.vue', import.meta.url), 'utf8')

  assert.match(source, /saveReadingPosition/)
  assert.match(source, /continueReadingFromSavedPosition/)
  assert.match(source, /resumePromptDismissed/)
  assert.match(source, /skipErrorMessage:\s*true/)
  assert.match(source, /document\.addEventListener\('visibilitychange'/)
  assert.match(source, /onBeforeRouteLeave/)
})

test('article detail uses current article updated time and active heading when saving position', () => {
  const source = readFileSync(new URL('../views/ArticleDetail.vue', import.meta.url), 'utf8')

  assert.match(source, /article\.value\.updatedAt/)
  assert.match(source, /activeHeadingId\.value/)
  assert.match(source, /buildReadingPositionPayload/)
  assert.match(source, /calculateReadingProgress/)
  assert.match(source, /shouldSaveReadingPosition/)
})

test('article detail does not auto jump and lets the reader dismiss the resume prompt', () => {
  const source = readFileSync(new URL('../views/ArticleDetail.vue', import.meta.url), 'utf8')

  assert.doesNotMatch(
    source,
    /watch\(\s*(?:\(\) => )?(?:readingPosition|showResumePrompt)[\s\S]*?continueReadingFromSavedPosition/
  )
  assert.match(source, /@click="continueReadingFromSavedPosition"/)
  assert.match(source, /@click="dismissResumePrompt"/)
})

test('article detail surfaces resume as a dismissible dialog', () => {
  const source = readFileSync(new URL('../views/ArticleDetail.vue', import.meta.url), 'utf8')

  assert.match(source, /<el-dialog[\s\S]*v-model="showResumeDialog"/)
  assert.match(source, /class="resume-reading-dialog"/)
  assert.match(source, /上次读到/)
  assert.match(source, /@click="continueReadingFromSavedPosition"/)
  assert.match(source, /@click="dismissResumePrompt"/)
  assert.doesNotMatch(source, /class="resume-reading-panel"/)
})

test('article detail offers a floating back action after scrolling and saves before leaving', () => {
  const source = readFileSync(new URL('../views/ArticleDetail.vue', import.meta.url), 'utf8')

  assert.match(source, /floating-back-button/)
  assert.match(source, /showFloatingBackButton/)
  assert.match(source, /@click="handleFloatingBack"/)
  assert.match(source, /function handleFloatingBack\(\)[\s\S]*?flushReadingPosition\(\)[\s\S]*?goBack\(\)/)
})
