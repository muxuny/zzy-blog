export function createHeadingId(text, seen = new Map()) {
  const base = String(text || '')
    .trim()
    .toLowerCase()
    .replace(/<[^>]*>/g, '')
    .replace(/[`*_~[\]()]/g, '')
    .replace(/[^\u4e00-\u9fa5a-z0-9\s-]/g, '')
    .replace(/\s+/g, '-')
    .replace(/-+/g, '-')
    .replace(/^-|-$/g, '') || 'section'

  const count = (seen.get(base) || 0) + 1
  seen.set(base, count)
  return count === 1 ? base : `${base}-${count}`
}

export function extractMarkdownToc(markdown = '') {
  const seen = new Map()
  const toc = []
  let inFence = false

  String(markdown).split(/\r?\n/).forEach(line => {
    if (/^\s*```/.test(line)) {
      inFence = !inFence
      return
    }
    if (inFence) return

    const match = /^(#{2,3})\s+(.+?)\s*#*\s*$/.exec(line)
    if (!match) return

    const text = match[2].replace(/[`*_~]/g, '').trim()
    toc.push({
      id: createHeadingId(text, seen),
      level: match[1].length,
      text
    })
  })

  return toc
}

export function getReadingStats(markdown = '') {
  const plain = String(markdown)
    .replace(/```[\s\S]*?```/g, ' ')
    .replace(/`[^`]*`/g, ' ')
    .replace(/!\[[^\]]*]\([^)]*\)/g, ' ')
    .replace(/\[([^\]]+)]\([^)]*\)/g, '$1')
    .replace(/<[^>]*>/g, ' ')
    .replace(/[#>*_~\-[\]()]/g, ' ')

  const cjkCount = (plain.match(/[\u4e00-\u9fa5]/g) || []).length
  const latinCount = (plain.replace(/[\u4e00-\u9fa5]/g, ' ').match(/[a-zA-Z0-9]+(?:[-'][a-zA-Z0-9]+)*/g) || []).length
  const wordCount = cjkCount + latinCount
  const readingMinutes = Math.max(1, Math.ceil(wordCount / 450))

  return {
    wordCount,
    readingMinutes,
    readingTimeText: `约 ${readingMinutes} 分钟`
  }
}
