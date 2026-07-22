function padDatePart(value) {
  return String(value).padStart(2, '0')
}

function getLocalDateKey(date) {
  return [
    date.getFullYear(),
    padDatePart(date.getMonth() + 1),
    padDatePart(date.getDate())
  ].join('-')
}

function getLocalDateLabel(date) {
  return `${date.getMonth() + 1}/${date.getDate()}`
}

export function buildContentSignal({ articles = [], topTags = [], now = new Date() } = {}) {
  const safeArticles = Array.isArray(articles) ? articles : []
  const safeTopTags = Array.isArray(topTags) ? topTags : []
  const startDate = new Date(now)
  startDate.setHours(0, 0, 0, 0)

  const days = Array.from({ length: 7 }, (_, index) => {
    const date = new Date(startDate)
    date.setDate(startDate.getDate() - (6 - index))
    return {
      key: getLocalDateKey(date),
      label: getLocalDateLabel(date),
      count: 0
    }
  })
  const dayMap = new Map(days.map(day => [day.key, day]))
  const topicCounts = new Map()

  safeArticles.forEach(article => {
    if (!article) return

    const time = article.updatedAt || article.createdAt
    if (time) {
      const date = new Date(time)
      if (!Number.isNaN(date.getTime())) {
        const day = dayMap.get(getLocalDateKey(date))
        if (day) {
          day.count += 1
          ;(article.tags || []).forEach(tag => {
            const name = String(tag?.name || '').trim()
            if (name) topicCounts.set(name, (topicCounts.get(name) || 0) + 1)
          })
        }
      }
    }
  })

  const maxCount = Math.max(1, ...days.map(day => day.count))
  const fallbackTopic = safeTopTags.map(tag => String(tag?.name || '').trim()).find(Boolean)
  const activeTopic = [...topicCounts.entries()].sort((a, b) => b[1] - a[1])[0]?.[0] || fallbackTopic || '暂无主题'
  const updatedCount = days.reduce((sum, day) => sum + day.count, 0)
  return {
    activeTopic,
    latestTitle: safeArticles[0]?.title || '',
    updatedCount,
    summary: updatedCount
      ? `本周更新 ${updatedCount} 篇，集中在 ${activeTopic}`
      : '本周暂无新更新，可从专题继续阅读',
    bars: days.map(day => ({
      ...day,
      height: `${Math.max(16, Math.round((day.count / maxCount) * 100))}%`
    }))
  }
}
