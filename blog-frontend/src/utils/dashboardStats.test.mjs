import test from 'node:test'
import assert from 'node:assert/strict'
import { buildDashboardStats } from './dashboardStats.js'

test('buildDashboardStats groups counts, percentages, review queues, and tag summary', () => {
  const result = buildDashboardStats({
    allArticles: [
      { id: 1, title: '草稿文章', status: 'draft', createdBy: 'admin' },
      { id: 2, title: '待审文章', status: 'pending', createdBy: 'alice' },
      { id: 3, title: '已发布文章', status: 'published', visibility: 'public', createdBy: 'bob' },
      { id: 4, title: '驳回文章', status: 'rejected', createdBy: 'carol' },
      { id: 5, title: '第二篇待审', status: 'pending', createdBy: 'dave' },
      { id: 6, title: '私密已发布', status: 'published', visibility: 'private', createdBy: 'erin' },
      { id: 7, title: '私密草稿', status: 'draft', visibility: 'private', createdBy: 'frank' }
    ],
    tags: [{ id: 1, name: 'Vue' }, { id: 2, name: 'Java' }],
    users: [
      { id: 10, username: 'new-user', nickname: '新用户', status: 'pending' },
      { id: 11, username: 'active-user', nickname: '老用户', status: 'active' }
    ]
  })

  assert.equal(result.metrics.totalArticles, 7)
  assert.equal(result.metrics.publishedArticles, 2)
  assert.equal(result.metrics.publicPublishedArticles, 1)
  assert.equal(result.metrics.privateArticles, 1)
  assert.equal(result.metrics.pendingArticles, 2)
  assert.equal(result.metrics.pendingUsers, 1)

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

  assert.deepEqual(result.pendingArticles.map(item => item.title), ['待审文章', '第二篇待审'])
  assert.deepEqual(result.pendingUsers.map(item => item.username), ['new-user'])
  assert.equal(result.tagSummary.total, 2)
  assert.deepEqual(result.tagSummary.items.map(item => item.name), ['Vue', 'Java'])
})

test('buildDashboardStats handles empty input safely', () => {
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
  assert.deepEqual(result.tagSummary.items, [])
})
