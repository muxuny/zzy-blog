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

function isFenceLine(line) {
  return /^\s*(```|~~~)/.test(line)
}

export function normalizeArticleMarkdown(markdown = '') {
  let inFence = false

  return String(markdown || '')
    .split(/(\r?\n)/)
    .map(part => {
      if (/^\r?\n$/.test(part)) return part

      if (isFenceLine(part)) {
        inFence = !inFence
        return part
      }

      if (inFence) return part
      return part.replace(/^(#)(?!#)(\s+)/, '##$2')
    })
    .join('')
}

export function extractMarkdownToc(markdown = '') {
  const seen = new Map()
  const toc = []
  let inFence = false

  String(markdown).split(/\r?\n/).forEach(line => {
    if (isFenceLine(line)) {
      inFence = !inFence
      return
    }
    if (inFence) return

    const match = /^(#{1,6})\s+(.+?)\s*#*\s*$/.exec(line)
    if (!match) return

    const text = match[2].replace(/[`*_~]/g, '').trim()
    const id = createHeadingId(text, seen)
    const level = match[1].length
    if (level < 2 || level > 4) return

    toc.push({
      id,
      level,
      text
    })
  })

  return toc
}

export function getReadingStats(markdown = '') {
  const plain = String(markdown)
    .replace(/(^|\n)\s*(```|~~~)[\s\S]*?\n\s*\2/g, ' ')
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
