export const ARTICLE_STATUS = [
  { key: 'draft', label: '草稿', tone: 'neutral' },
  { key: 'pending', label: '待审核', tone: 'warning' },
  { key: 'published', label: '已发布', tone: 'success' },
  { key: 'rejected', label: '已驳回', tone: 'danger' }
]

function list(value) {
  return Array.isArray(value) ? value : []
}

function percent(count, total) {
  if (!total) return 0
  return Math.round((count / total) * 100)
}

function isPrivateArticle(article) {
  return article?.visibility === 'private'
}

function isPublicPublishedArticle(article) {
  return article?.status === 'published' && !isPrivateArticle(article)
}

function isPrivatePublishedArticle(article) {
  return article?.status === 'published' && isPrivateArticle(article)
}

export function buildDashboardStats(payload = {}) {
  const allArticles = list(payload.allArticles)
  const tags = list(payload.tags)
  const users = list(payload.users)
  const pendingUsers = users.filter(user => user.status === 'pending')
  const totalArticles = allArticles.length
  const publicPublishedArticles = allArticles.filter(isPublicPublishedArticle).length
  const privateArticles = allArticles.filter(isPrivatePublishedArticle).length

  const articleStatus = ARTICLE_STATUS.map(item => {
    const count = allArticles.filter(article => article.status === item.key).length
    return {
      ...item,
      count,
      percent: percent(count, totalArticles)
    }
  })

  const pendingArticles = allArticles
    .filter(article => article.status === 'pending')
    .slice(0, 5)

  return {
    metrics: {
      totalArticles,
      publishedArticles: articleStatus.find(item => item.key === 'published')?.count || 0,
      publicPublishedArticles,
      privateArticles,
      pendingArticles: articleStatus.find(item => item.key === 'pending')?.count || 0,
      pendingUsers: pendingUsers.length
    },
    articleStatus,
    pendingArticles,
    pendingUsers: pendingUsers.slice(0, 5),
    tagSummary: {
      total: tags.length,
      items: tags.slice(0, 12)
    }
  }
}
