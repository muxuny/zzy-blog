import test from 'node:test'
import assert from 'node:assert/strict'
import { getActiveHeadingId } from './scrollSpy.js'

test('keeps the first heading active before the second heading reaches offset', () => {
  const headings = [
    { id: 'intro', top: 100 },
    { id: 'details', top: 300 }
  ]

  assert.equal(getActiveHeadingId(headings, 179, 120), 'intro')
})

test('activates the second heading after it crosses offset', () => {
  const headings = [
    { id: 'intro', top: 100 },
    { id: 'details', top: 300 }
  ]

  assert.equal(getActiveHeadingId(headings, 181, 120), 'details')
})

test('returns an empty string for empty headings', () => {
  assert.equal(getActiveHeadingId([], 200, 120), '')
})
