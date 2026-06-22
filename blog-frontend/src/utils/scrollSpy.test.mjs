import test from 'node:test'
import assert from 'node:assert/strict'
import { getActiveHeadingId, getScrollTopForVisibleItem } from './scrollSpy.js'

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

test('keeps toc scroll unchanged when active item is already visible', () => {
  assert.equal(getScrollTopForVisibleItem({
    containerScrollTop: 120,
    containerHeight: 300,
    itemTop: 180,
    itemHeight: 36,
    padding: 12
  }), 120)
})

test('scrolls toc down when active item is below visible area', () => {
  assert.equal(getScrollTopForVisibleItem({
    containerScrollTop: 0,
    containerHeight: 300,
    itemTop: 420,
    itemHeight: 36,
    padding: 12
  }), 168)
})

test('scrolls toc up when active item is above visible area', () => {
  assert.equal(getScrollTopForVisibleItem({
    containerScrollTop: 300,
    containerHeight: 300,
    itemTop: 220,
    itemHeight: 36,
    padding: 12
  }), 208)
})
