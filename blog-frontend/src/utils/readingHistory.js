function normalizePositiveInteger(value, fallback) {
  const number = Number(value)
  return Number.isInteger(number) && number > 0 ? number : fallback
}

function localDaySerial(date) {
  return Date.UTC(date.getFullYear(), date.getMonth(), date.getDate()) / 86400000
}

export function buildReadingHistoryParams(options = {}) {
  const { page = 1, size = 10 } = options ?? {}
  const normalizedPage = normalizePositiveInteger(page, 1)
  const normalizedSize = normalizePositiveInteger(size, 10)

  return {
    page: normalizedPage,
    size: normalizedSize <= 100 ? normalizedSize : 10
  }
}

export function formatReadingTime(value) {
  if (value == null || value === '') return ''

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''

  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false
  }).format(date)
}

export function getReadingHistoryGroupKey(lastReadAt, now = new Date()) {
  const lastReadDate = new Date(lastReadAt)
  if (Number.isNaN(lastReadDate.getTime())) return 'earlier'

  const dayDifference = localDaySerial(now) - localDaySerial(lastReadDate)
  if (dayDifference <= 0) return 'today'
  if (dayDifference === 1) return 'yesterday'
  return 'earlier'
}

export function groupReadingHistory(items = [], now = new Date()) {
  if (!Array.isArray(items)) return []

  const groups = [
    { key: 'today', label: '今天', items: [] },
    { key: 'yesterday', label: '昨天', items: [] },
    { key: 'earlier', label: '更早', items: [] }
  ]
  const groupsByKey = Object.fromEntries(groups.map(group => [group.key, group]))

  for (const item of items) {
    groupsByKey[getReadingHistoryGroupKey(item?.lastReadAt, now)].items.push(item)
  }

  return groups.filter(group => group.items.length > 0)
}

export function getPageAfterHistoryDeletion({ page, size, total, deletedCount = 1 } = {}) {
  const normalizedPage = normalizePositiveInteger(page, 1)
  const normalizedSize = normalizePositiveInteger(size, 10)
  const pageSize = normalizedSize <= 100 ? normalizedSize : 10
  const totalNumber = Number(total)
  const currentTotal = Number.isFinite(totalNumber) && totalNumber > 0 ? Math.floor(totalNumber) : 0
  const deletedNumber = Number(deletedCount)
  const removed = Number.isInteger(deletedNumber) && deletedNumber > 0 ? deletedNumber : 1
  const nextTotal = Math.max(0, currentTotal - removed)
  const maxPage = Math.max(1, Math.ceil(nextTotal / pageSize))

  return Math.min(normalizedPage, maxPage)
}
