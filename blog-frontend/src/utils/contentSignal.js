const hongKongDateFormatter = new Intl.DateTimeFormat('en-US', {
  timeZone: 'Asia/Hong_Kong',
  year: 'numeric',
  month: '2-digit',
  day: '2-digit'
})
const weekdayLabels = ['日', '一', '二', '三', '四', '五', '六']

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

function getHongKongWeekdayLabel(date) {
  return weekdayLabels[date.getUTCDay()]
}

function isValidDateParts(year, month, day) {
  const date = new Date(Date.UTC(year, month - 1, day))
  return date.getUTCFullYear() === year && date.getUTCMonth() === month - 1 && date.getUTCDate() === day
}

function getHongKongDateKeyFromValue(value) {
  if (typeof value === 'string') {
    const localMatch = value.match(/^(\d{4})-(\d{2})-(\d{2})(?:[T\s]|$)/)
    const hasTimezone = /(?:Z|[+-]\d{2}:?\d{2})$/i.test(value.trim())
    if (localMatch && !hasTimezone) {
      const [, yearText, monthText, dayText] = localMatch
      const year = Number(yearText)
      const month = Number(monthText)
      const day = Number(dayText)
      return isValidDateParts(year, month, day) ? `${yearText}-${monthText}-${dayText}` : null
    }
  }

  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? null : getHongKongDateKey(date)
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
      weekdayLabel: getHongKongWeekdayLabel(date),
      count: 0
    }
  })
  const dayMap = new Map(days.map(day => [day.key, day]))
  const topicCounts = new Map()

  safeArticles.forEach(article => {
    if (!article) return

    const time = article.updatedAt || article.createdAt
    if (time) {
      const key = getHongKongDateKeyFromValue(time)
      const day = key ? dayMap.get(key) : null
      if (day) {
        day.count += 1
        const articleTags = Array.isArray(article.tags) ? article.tags : []
        articleTags.forEach(tag => {
          const name = String(tag?.name || '').trim()
          if (name) topicCounts.set(name, (topicCounts.get(name) || 0) + 1)
        })
      }
    }
  })

  const maxCount = Math.max(1, ...days.map(day => day.count))
  const peakCount = Math.max(0, ...days.map(day => day.count))
  const fallbackTopic = safeTopTags.map(tag => String(tag?.name || '').trim()).find(Boolean)
  const activeTopic = [...topicCounts.entries()].sort((a, b) => b[1] - a[1])[0]?.[0] || fallbackTopic || '暂无主题'
  const updatedCount = days.reduce((sum, day) => sum + day.count, 0)
  return {
    activeTopic,
    latestTitle: safeArticles[0]?.title || '',
    updatedCount,
    chartLabel: `近 7 天更新分布，共 ${updatedCount} 篇更新`,
    summary: updatedCount
      ? `当前筛选下近 7 天更新 ${updatedCount} 篇，集中在 ${activeTopic}`
      : '当前筛选下近 7 天暂无更新，可以从专题继续阅读',
    bars: days.map(day => {
      const updateText = day.count ? `${day.count} 篇更新` : '暂无更新'
      return {
        ...day,
        updateText,
        tooltip: `${day.label} 周${day.weekdayLabel} · ${updateText}`,
        accessibleLabel: `${day.label} 周${day.weekdayLabel}，${updateText}`,
        isToday: day.key === days.at(-1)?.key,
        isPeak: day.count > 0 && day.count === peakCount,
        height: `${Math.max(16, Math.round((day.count / maxCount) * 100))}%`
      }
    })
  }
}
