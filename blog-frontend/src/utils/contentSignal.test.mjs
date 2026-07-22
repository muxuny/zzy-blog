import test from 'node:test'
import assert from 'node:assert/strict'
import { buildContentSignal } from './contentSignal.js'

test('buildContentSignal assigns same-day articles to the local today bucket', () => {
  const signal = buildContentSignal({
    now: new Date('2026-07-22T12:00:00+08:00'),
    articles: [
      {
        updatedAt: '2026-07-22T09:00:00+08:00',
        tags: [{ name: 'Vue' }]
      }
    ]
  })

  const today = signal.bars.at(-1)
  assert.equal(today.key, '2026-07-22')
  assert.equal(today.label, '7/22')
  assert.equal(today.count, 1)
  assert.equal(signal.updatedCount, 1)
})

test('buildContentSignal prefers updatedAt over createdAt for weekly buckets', () => {
  const signal = buildContentSignal({
    now: new Date('2026-07-22T12:00:00+08:00'),
    articles: [
      {
        createdAt: '2026-07-22T10:00:00+08:00',
        updatedAt: '2026-07-01T10:00:00+08:00',
        tags: [{ name: 'Old Update' }]
      }
    ]
  })

  assert.equal(signal.updatedCount, 0)
  assert.equal(signal.bars.at(-1).count, 0)
})

test('buildContentSignal ignores invalid article dates', () => {
  const signal = buildContentSignal({
    now: new Date('2026-07-22T12:00:00+08:00'),
    articles: [
      {
        updatedAt: 'not-a-date',
        createdAt: '2026-07-22T10:00:00+08:00',
        tags: [{ name: 'Invalid Date' }]
      }
    ]
  })

  assert.equal(signal.updatedCount, 0)
})

test('buildContentSignal falls back to top tag and then empty topic text', () => {
  const topTagSignal = buildContentSignal({
    now: new Date('2026-07-22T12:00:00+08:00'),
    topTags: [{ name: 'Design' }]
  })
  const emptySignal = buildContentSignal({
    now: new Date('2026-07-22T12:00:00+08:00')
  })

  assert.equal(topTagSignal.activeTopic, 'Design')
  assert.equal(topTagSignal.summary, '本周暂无新更新，可从专题继续阅读')
  assert.equal(emptySignal.activeTopic, '暂无主题')
})

test('buildContentSignal keeps every bar at least sixteen percent high', () => {
  const signal = buildContentSignal({
    now: new Date('2026-07-22T12:00:00+08:00')
  })

  assert.equal(signal.bars.length, 7)
  assert.ok(signal.bars.every(day => Number.parseInt(day.height, 10) >= 16))
})
