import test from 'node:test'
import assert from 'node:assert/strict'
import { buildDashboardStats } from './dashboardStats.js'

test('buildDashboardStats normalizes backend overview counts and previews', () => {
  const result = buildDashboardStats({
    metrics: {
      totalArticles: 7,
      draftArticles: 2,
      pendingArticles: 2,
      publishedArticles: 2,
      rejectedArticles: 1,
      publicPublishedArticles: 1,
      privateArticles: 1,
      pendingUsers: 2
    },
    articleStatus: [
      { key: 'draft', count: 2 },
      { key: 'pending', count: 2 },
      { key: 'published', count: 2 },
      { key: 'rejected', count: 1 }
    ],
    pendingArticles: [
      { id: 1, title: '待审 1' },
      { id: 2, title: '待审 2' },
      { id: 3, title: '待审 3' },
      { id: 4, title: '待审 4' },
      { id: 5, title: '待审 5' },
      { id: 6, title: '待审 6' }
    ],
    pendingUsers: [
      { id: 10, username: 'new-user' },
      { id: 11, username: 'next-user' }
    ],
    tagSummary: {
      total: 12,
      items: [{ id: 20, name: 'Vue' }, { id: 21, name: 'Java' }]
    }
  })

  assert.equal(result.metrics.totalArticles, 7)
  assert.equal(result.metrics.publishedArticles, 2)
  assert.equal(result.metrics.publicPublishedArticles, 1)
  assert.equal(result.metrics.privateArticles, 1)
  assert.equal(result.metrics.pendingArticles, 2)
  assert.equal(result.metrics.pendingUsers, 2)

  assert.deepEqual(
    result.articleStatus.map(item => ({
      key: item.key,
      count: item.count,
      percent: item.percent
    })),
    [
      { key: 'draft', count: 2, percent: 29 },
      { key: 'pending', count: 2, percent: 29 },
      { key: 'published', count: 2, percent: 29 },
      { key: 'rejected', count: 1, percent: 14 }
    ]
  )

  assert.deepEqual(result.pendingArticles.map(item => item.title), ['待审 1', '待审 2', '待审 3', '待审 4', '待审 5'])
  assert.deepEqual(result.pendingUsers.map(item => item.username), ['new-user', 'next-user'])
  assert.equal(result.tagSummary.total, 12)
  assert.deepEqual(result.tagSummary.items.map(item => item.name), ['Vue', 'Java'])
})

test('buildDashboardStats handles empty overview safely', () => {
  const result = buildDashboardStats()

  assert.equal(result.metrics.totalArticles, 0)
  assert.equal(result.metrics.publishedArticles, 0)
  assert.equal(result.metrics.publicPublishedArticles, 0)
  assert.equal(result.metrics.privateArticles, 0)
  assert.equal(result.metrics.pendingArticles, 0)
  assert.equal(result.metrics.pendingUsers, 0)
  assert.equal(result.articleStatus.every(item => item.percent === 0), true)
  assert.deepEqual(result.pendingArticles, [])
  assert.deepEqual(result.pendingUsers, [])
  assert.equal(result.tagSummary.total, 0)
  assert.deepEqual(result.tagSummary.items, [])
})
