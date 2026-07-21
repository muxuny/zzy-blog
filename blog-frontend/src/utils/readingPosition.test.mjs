import test from 'node:test'
import assert from 'node:assert/strict'
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
