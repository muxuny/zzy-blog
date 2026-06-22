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
