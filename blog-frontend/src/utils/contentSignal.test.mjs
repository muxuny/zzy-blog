import test from 'node:test'
import assert from 'node:assert/strict'
import { buildContentSignal } from './contentSignal.js'

test('buildContentSignal assigns same-day articles to the Hong Kong today bucket', () => {
  const signal = buildContentSignal({
    now: new Date('2026-07-22T12:00:00+08:00'),
    articles: [
      {
        updatedAt: '2026-07-22T00:30:00+08:00',
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

test('buildContentSignal annotates bars with readable day labels and update counts', () => {
  const signal = buildContentSignal({
    now: new Date('2026-07-22T12:00:00+08:00'),
    articles: [
      {
        updatedAt: '2026-07-20T10:00:00+08:00',
        tags: [{ name: 'CSS' }]
      },
      {
        updatedAt: '2026-07-22T09:00:00+08:00',
        tags: [{ name: 'Vue' }]
      },
      {
        updatedAt: '2026-07-22T10:00:00+08:00',
        tags: [{ name: 'Vue' }]
      }
    ]
  })

  const emptyDay = signal.bars[0]
  const peakDay = signal.bars.at(-1)
  assert.equal(signal.chartLabel, '近 7 天更新分布，共 3 篇更新')
  assert.equal(emptyDay.weekdayLabel, '四')
  assert.equal(emptyDay.updateText, '暂无更新')
  assert.equal(emptyDay.tooltip, '7/16 周四 · 暂无更新')
  assert.equal(emptyDay.accessibleLabel, '7/16 周四，暂无更新')
  assert.equal(emptyDay.isToday, false)
  assert.equal(emptyDay.isPeak, false)
  assert.equal(peakDay.weekdayLabel, '三')
  assert.equal(peakDay.updateText, '2 篇更新')
  assert.equal(peakDay.tooltip, '7/22 周三 · 2 篇更新')
  assert.equal(peakDay.accessibleLabel, '7/22 周三，2 篇更新')
  assert.equal(peakDay.isToday, true)
  assert.equal(peakDay.isPeak, true)
})

test('buildContentSignal treats offsetless API timestamps as Hong Kong local dates', () => {
  const signal = buildContentSignal({
    now: new Date('2026-07-22T12:00:00+08:00'),
    articles: [
      {
        updatedAt: '2026-07-15T23:30:00',
        tags: [{ name: 'Boundary Old' }]
      },
      {
        updatedAt: '2026-07-22T00:30:00',
        tags: [{ name: 'Boundary Today' }]
      }
    ]
  })

  const today = signal.bars.at(-1)
  const firstDay = signal.bars[0]
  assert.equal(firstDay.key, '2026-07-16')
  assert.equal(today.key, '2026-07-22')
  assert.equal(today.count, 1)
  assert.equal(signal.updatedCount, 1)
  assert.equal(signal.activeTopic, 'Boundary Today')
})

test('buildContentSignal prefers updatedAt over createdAt for seven-day buckets', () => {
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
  assert.equal(topTagSignal.summary, '当前筛选下近 7 天暂无更新，可以从专题继续阅读')
  assert.equal(emptySignal.activeTopic, '暂无主题')
})

test('buildContentSignal keeps every bar at least sixteen percent high', () => {
  const signal = buildContentSignal({
    now: new Date('2026-07-22T12:00:00+08:00')
  })

  assert.equal(signal.bars.length, 7)
  assert.ok(signal.bars.every(day => Number.parseInt(day.height, 10) >= 16))
})

test('buildContentSignal tolerates null article and tag collections', () => {
  let signal
  assert.doesNotThrow(() => {
    signal = buildContentSignal({
      articles: null,
      topTags: null,
      now: new Date('2026-07-22T12:00:00+08:00')
    })
  })

  assert.equal(signal.activeTopic, '暂无主题')
  assert.equal(signal.updatedCount, 0)
  assert.equal(signal.bars.length, 7)
})

test('buildContentSignal skips null articles and trims tag names', () => {
  let signal
  assert.doesNotThrow(() => {
    signal = buildContentSignal({
      now: new Date('2026-07-22T12:00:00+08:00'),
      articles: [
        null,
        {
          updatedAt: '2026-07-22T09:00:00+08:00',
          tags: [null, { name: '   ' }, { name: '  Vue  ' }]
        }
      ]
    })
  })

  assert.equal(signal.activeTopic, 'Vue')
  assert.equal(signal.summary, '当前筛选下近 7 天更新 1 篇，集中在 Vue')
  assert.equal(signal.updatedCount, 1)
})

test('buildContentSignal ignores truthy non-array tag collections', () => {
  const signal = buildContentSignal({
    now: new Date('2026-07-22T12:00:00+08:00'),
    topTags: [{ name: 'Fallback' }],
    articles: [
      {
        updatedAt: '2026-07-22T09:00:00+08:00',
        tags: 'Vue'
      }
    ]
  })

  assert.equal(signal.updatedCount, 1)
  assert.equal(signal.activeTopic, 'Fallback')
  assert.equal(signal.summary, '当前筛选下近 7 天更新 1 篇，集中在 Fallback')
})

test('buildContentSignal only counts current seven-day tags for the active topic', () => {
  const signal = buildContentSignal({
    now: new Date('2026-07-22T12:00:00+08:00'),
    articles: [
      {
        updatedAt: '2026-07-22T09:00:00+08:00',
        tags: [{ name: 'Weekly' }]
      },
      {
        updatedAt: '2026-07-01T09:00:00+08:00',
        tags: [{ name: 'Old' }]
      },
      {
        updatedAt: '2026-07-02T09:00:00+08:00',
        tags: [{ name: 'Old' }]
      },
      {
        updatedAt: 'not-a-date',
        tags: [{ name: 'Invalid' }]
      }
    ]
  })

  assert.equal(signal.updatedCount, 1)
  assert.equal(signal.activeTopic, 'Weekly')
  assert.equal(signal.summary, '当前筛选下近 7 天更新 1 篇，集中在 Weekly')
})
