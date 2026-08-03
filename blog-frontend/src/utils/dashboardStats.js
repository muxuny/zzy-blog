export const ARTICLE_STATUS = [
  { key: 'draft', label: '草稿', tone: 'neutral' },
  { key: 'pending', label: '待审核', tone: 'warning' },
  { key: 'published', label: '已发布', tone: 'success' },
  { key: 'rejected', label: '已驳回', tone: 'danger' }
]

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
  const tagSummary = payload.tagSummary || {}
  const tagItems = list(tagSummary.items).slice(0, 12)

  return {
    metrics: {
      totalArticles,
      publishedArticles,
      publicPublishedArticles: metric(metricsSource, 'publicPublishedArticles'),
      privateArticles: metric(metricsSource, 'privateArticles'),
      pendingArticles,
      pendingUsers: metric(metricsSource, 'pendingUsers')
    },
    articleStatus,
    pendingArticles: list(payload.pendingArticles).slice(0, 5),
    pendingUsers: list(payload.pendingUsers).slice(0, 5),
    tagSummary: {
      total: Object.prototype.hasOwnProperty.call(tagSummary, 'total') ? number(tagSummary.total) : tagItems.length,
      items: tagItems
    }
  }
}
