export const ARTICLE_STATUS = [
  { key: 'draft', label: '草稿', tone: 'neutral' },
  { key: 'pending', label: '待审核', tone: 'warning' },
  { key: 'published', label: '已发布', tone: 'success' },
  { key: 'rejected', label: '已驳回', tone: 'danger' }
]

const PROGRESS_LABELS = ['浅读', '阅读中', '接近读完', '已读完']

function list(value) {
  return Array.isArray(value) ? value : []
}

function number(value) {
  const parsed = Number(value)
  return Number.isFinite(parsed) && parsed >= 0 ? parsed : 0
}

function percent(count, total) {
  if (!total) return 0
  return Math.round((count / total) * 100)
}

function metric(metrics, key, fallback = 0) {
  if (Object.prototype.hasOwnProperty.call(metrics, key)) return number(metrics[key])
  return fallback
}

function normalizeDateBuckets(value) {
  return list(value).map(item => ({
    date: typeof item?.date === 'string' ? item.date : '',
    count: number(item?.count)
  }))
}

function normalizeProgressBuckets(value) {
  const items = list(value)
  const total = items.reduce((sum, item) => sum + number(item?.count), 0)
  const byLabel = new Map(items.map(item => [item?.label, item]))
  return PROGRESS_LABELS
    .map(label => {
      const item = byLabel.get(label) || {}
      const count = number(item.count)
      return { label, count, percent: percent(count, total) }
    })
    .filter(item => item.count > 0 || total > 0)
}

function normalizeTopArticles(value) {
  return list(value)
    .slice(0, 5)
    .map(item => ({
      id: item?.id ?? null,
      title: typeof item?.title === 'string' ? item.title : '未命名文章',
      viewCount: number(item?.viewCount)
    }))
}

function normalizeFavoriteTopArticles(value) {
  return list(value)
    .slice(0, 5)
    .map(item => ({
      articleId: item?.articleId ?? null,
      title: typeof item?.title === 'string' ? item.title : '未命名文章',
      favoriteCount: number(item?.favoriteCount)
    }))
}

export function buildDashboardStats(payload = {}) {
  const metricsSource = payload.metrics || {}
  const statusItems = list(payload.articleStatus)
  const statusByKey = new Map(statusItems.map(item => [item.key, item]))
  const totalArticles = metric(metricsSource, 'totalArticles')

  const articleStatus = ARTICLE_STATUS.map(item => {
    const backendItem = statusByKey.get(item.key) || {}
    const count = metric(metricsSource, `${item.key}Articles`, number(backendItem.count))
    return {
      ...item,
      count,
      percent: percent(count, totalArticles)
    }
  })

  const publishedArticles = metric(
    metricsSource,
    'publishedArticles',
    articleStatus.find(item => item.key === 'published')?.count || 0
  )
  const pendingArticles = metric(
    metricsSource,
    'pendingArticles',
    articleStatus.find(item => item.key === 'pending')?.count || 0
  )
  const rejectedArticles = metric(
    metricsSource,
    'rejectedArticles',
    articleStatus.find(item => item.key === 'rejected')?.count || 0
  )
  const tagSummary = payload.tagSummary || {}
  const tagItems = list(tagSummary.items).slice(0, 12)
  const trafficSummary = payload.trafficSummary || {}
  const readingSummary = payload.readingSummary || {}
  const favoriteSummary = payload.favoriteSummary || {}
  const resourceSummary = payload.resourceSummary || {}

  return {
    metrics: {
      totalArticles,
      draftArticles: metric(metricsSource, 'draftArticles'),
      pendingArticles,
      publishedArticles,
      rejectedArticles,
      publicPublishedArticles: metric(metricsSource, 'publicPublishedArticles'),
      privateArticles: metric(metricsSource, 'privateArticles'),
      totalUsers: metric(metricsSource, 'totalUsers'),
      pendingUsers: metric(metricsSource, 'pendingUsers'),
      activeUsers: metric(metricsSource, 'activeUsers'),
      disabledUsers: metric(metricsSource, 'disabledUsers'),
      totalTags: metric(metricsSource, 'totalTags')
    },
    articleStatus,
    pendingArticles: list(payload.pendingArticles).slice(0, 5),
    pendingUsers: list(payload.pendingUsers).slice(0, 5),
    tagSummary: {
      total: Object.prototype.hasOwnProperty.call(tagSummary, 'total')
        ? number(tagSummary.total)
        : tagItems.length,
      items: tagItems
    },
    trafficSummary: {
      totalViews: number(trafficSummary.totalViews),
      averageViews: number(trafficSummary.averageViews),
      lowViewArticles: number(trafficSummary.lowViewArticles),
      topArticles: normalizeTopArticles(trafficSummary.topArticles)
    },
    readingSummary: {
      recent7Days: number(readingSummary.recent7Days),
      recent30Days: number(readingSummary.recent30Days),
      activeReaders30Days: number(readingSummary.activeReaders30Days),
      averageProgress: number(readingSummary.averageProgress),
      progressBuckets: normalizeProgressBuckets(readingSummary.progressBuckets),
      dailyReads: normalizeDateBuckets(readingSummary.dailyReads)
    },
    favoriteSummary: {
      total: number(favoriteSummary.total),
      recent7Days: number(favoriteSummary.recent7Days),
      topArticles: normalizeFavoriteTopArticles(favoriteSummary.topArticles),
      dailyFavorites: normalizeDateBuckets(favoriteSummary.dailyFavorites)
    },
    resourceSummary: {
      totalImages: number(resourceSummary.totalImages),
      totalImageSize: number(resourceSummary.totalImageSize),
      recent7DaysImages: number(resourceSummary.recent7DaysImages),
      totalTags: number(resourceSummary.totalTags)
    }
  }
}
