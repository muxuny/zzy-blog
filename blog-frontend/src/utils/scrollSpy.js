export function getActiveHeadingId(headings = [], scrollY = 0, offset = 120) {
  if (!Array.isArray(headings) || headings.length === 0) return ''

  const marker = Number(scrollY) + Number(offset)
  if (!Number.isFinite(marker)) return ''

  return headings.reduce((activeId, heading) => {
    if (!heading || typeof heading.id !== 'string' || !Number.isFinite(heading.top)) {
      return activeId
    }
    return heading.top <= marker ? heading.id : activeId
  }, '')
}

export function getScrollTopForVisibleItem({
  containerScrollTop = 0,
  containerHeight = 0,
  itemTop = 0,
  itemHeight = 0,
  padding = 12
} = {}) {
  const scrollTop = Number(containerScrollTop)
  const height = Number(containerHeight)
  const top = Number(itemTop)
  const itemSize = Number(itemHeight)
  const edgePadding = Number(padding)

  if (![scrollTop, height, top, itemSize, edgePadding].every(Number.isFinite) || height <= 0) {
    return scrollTop
  }

  const visibleTop = scrollTop + edgePadding
  const visibleBottom = scrollTop + height - edgePadding
  const itemBottom = top + itemSize

  if (top < visibleTop) {
    return Math.max(0, top - edgePadding)
  }
  if (itemBottom > visibleBottom) {
    return Math.max(0, itemBottom - height + edgePadding)
  }
  return scrollTop
}
