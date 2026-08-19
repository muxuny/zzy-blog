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

test('buildDashboardStats normalizes monitoring summaries', () => {
  const result = buildDashboardStats({
    metrics: {
      totalArticles: 100,
      publishedArticles: 80,
      pendingArticles: 9,
      rejectedArticles: 4,
      privateArticles: 19
    },
    trafficSummary: {
      totalViews: 92416,
      averageViews: 1100,
      lowViewArticles: 18,
      topArticles: [
        { id: 1, title: 'Spring', viewCount: 12800 },
        { id: 2, title: 'Vue', viewCount: 9600 }
      ]
    },
    readingSummary: {
      recent7Days: 96,
      recent30Days: 384,
      activeReaders30Days: 41,
      averageProgress: 62,
      progressBuckets: [
        { label: '阅读中', count: 46 },
        { label: '已读完', count: 11 }
      ],
      dailyReads: [
        { date: '2026-08-06', count: 3 },
        { date: '2026-08-07', count: 5 }
      ]
    },
    favoriteSummary: {
      total: 318,
      recent7Days: 24,
      topArticles: [{ articleId: 1, title: '收藏文章', favoriteCount: 12 }],
      dailyFavorites: [{ date: '2026-08-07', count: 4 }]
    },
    resourceSummary: {
      totalImages: 246,
      totalImageSize: 2576980480,
      recent7DaysImages: 18,
      totalTags: 36
    }
  })

  assert.equal(result.metrics.rejectedArticles, 4)
  assert.equal(result.trafficSummary.totalViews, 92416)
  assert.equal(result.trafficSummary.averageViews, 1100)
  assert.equal(result.trafficSummary.lowViewArticles, 18)
  assert.deepEqual(result.trafficSummary.topArticles.map(item => item.title), ['Spring', 'Vue'])
  assert.equal(result.readingSummary.recent7Days, 96)
  assert.equal(result.readingSummary.recent30Days, 384)
  assert.equal(result.readingSummary.activeReaders30Days, 41)
  assert.equal(result.readingSummary.averageProgress, 62)
  assert.deepEqual(result.readingSummary.progressBuckets.map(item => item.label), [
    '浅读',
    '阅读中',
    '接近读完',
    '已读完'
  ])
  assert.equal(result.readingSummary.progressBuckets[1].count, 46)
  assert.equal(result.favoriteSummary.total, 318)
  assert.equal(result.favoriteSummary.recent7Days, 24)
  assert.equal(result.favoriteSummary.dailyFavorites[0].count, 4)
  assert.equal(result.resourceSummary.totalImages, 246)
  assert.equal(result.resourceSummary.totalImageSize, 2576980480)
  assert.equal(result.resourceSummary.recent7DaysImages, 18)
  assert.equal(result.resourceSummary.totalTags, 36)
})

test('buildDashboardStats handles missing monitoring summaries safely', () => {
  const result = buildDashboardStats()

  assert.equal(result.trafficSummary.totalViews, 0)
  assert.deepEqual(result.trafficSummary.topArticles, [])
  assert.deepEqual(result.readingSummary.progressBuckets, [])
  assert.deepEqual(result.readingSummary.dailyReads, [])
  assert.equal(result.favoriteSummary.total, 0)
  assert.deepEqual(result.favoriteSummary.topArticles, [])
  assert.equal(result.resourceSummary.totalImages, 0)
  assert.equal(result.resourceSummary.totalImageSize, 0)
})
