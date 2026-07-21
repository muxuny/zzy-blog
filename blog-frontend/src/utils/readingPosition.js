export function calculateReadingProgress({
  scrollY = 0,
  articleTop = 0,
  articleHeight = 0,
  viewportHeight = 0
} = {}) {
  const top = Number(articleTop)
  const height = Number(articleHeight)
  const viewport = Number(viewportHeight)
  const scroll = Number(scrollY)

  if (![top, height, viewport, scroll].every(Number.isFinite) || height <= 0) return 0

  const travel = Math.max(1, height - Math.max(0, viewport))
  return clampProgress(((scroll - top) / travel) * 100)
}

export function clampProgress(value) {
  const number = Number(value)
  if (!Number.isFinite(number)) return 0
  return Math.max(0, Math.min(100, Math.round(number)))
}

export function buildReadingPositionPayload({
  progressPercent,
  scrollY,
  anchorId = '',
  anchorOffset = null,
  articleUpdatedAt
} = {}) {
  if (!articleUpdatedAt) return null
  const progress = clampProgress(progressPercent)
  const scroll = Math.max(0, Math.round(Number(scrollY) || 0))
  const normalizedAnchorId = typeof anchorId === 'string' ? anchorId.trim().slice(0, 160) : ''
  const offsetNumber = Number(anchorOffset)

  return {
    progressPercent: progress,
    scrollY: scroll,
    anchorId: normalizedAnchorId || null,
    anchorOffset: Number.isFinite(offsetNumber) && offsetNumber >= 0 ? Math.round(offsetNumber) : null,
    articleUpdatedAt
  }
}

export function shouldShowResumePrompt(position = {}) {
  return !!position?.canResume && Number(position.progressPercent) >= 5
}

export function shouldSaveReadingPosition({
  now = Date.now(),
  lastSavedAt = 0,
  intervalMs = 9000,
  force = false
} = {}) {
  if (force) return true
  const current = Number(now)
  const previous = Number(lastSavedAt)
  const interval = Number(intervalMs)
  return [current, previous, interval].every(Number.isFinite) && current - previous >= interval
}
