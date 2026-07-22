const hongKongDateFormatter = new Intl.DateTimeFormat('en-US', {
  timeZone: 'Asia/Hong_Kong',
  year: 'numeric',
  month: '2-digit',
  day: '2-digit'
})

function getHongKongDateParts(date) {
  const parts = Object.fromEntries(hongKongDateFormatter.formatToParts(date).map(part => [part.type, part.value]))
  return {
    year: Number(parts.year),
    month: Number(parts.month),
    day: Number(parts.day)
  }
}

function getHongKongDateKey(date) {
  const { year, month, day } = getHongKongDateParts(date)
  return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`
}

function getHongKongDateLabel(date) {
  const { month, day } = getHongKongDateParts(date)
  return `${month}/${day}`
}

export function buildContentSignal({ articles = [], topTags = [], now = new Date() } = {}) {
  const safeArticles = Array.isArray(articles) ? articles : []
  const safeTopTags = Array.isArray(topTags) ? topTags : []
  const { year, month, day } = getHongKongDateParts(new Date(now))
  const todayAnchor = Date.UTC(year, month - 1, day)

  const days = Array.from({ length: 7 }, (_, index) => {
    const date = new Date(todayAnchor - (6 - index) * 24 * 60 * 60 * 1000)
    return {
      key: getHongKongDateKey(date),
      label: getHongKongDateLabel(date),
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
        const day = dayMap.get(getHongKongDateKey(date))
        if (day) {
          day.count += 1
          const articleTags = Array.isArray(article.tags) ? article.tags : []
          articleTags.forEach(tag => {
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
      ? `当前列表本周更新 ${updatedCount} 篇，集中在 ${activeTopic}`
      : '当前列表本周暂无新更新，可从专题继续阅读',
    bars: days.map(day => ({
      ...day,
      height: `${Math.max(16, Math.round((day.count / maxCount) * 100))}%`
    }))
  }
}
