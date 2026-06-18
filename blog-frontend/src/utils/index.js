export function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
}
export function truncate(text, len = 100) {
  if (!text) return ''
  return text.length > len ? text.substring(0, len) + '...' : text
}
