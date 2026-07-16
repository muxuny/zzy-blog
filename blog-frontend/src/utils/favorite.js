export function buildFavoriteLoginRedirect(articleId) {
  return `/article/${articleId}?favorite=1`
}

export function hasFavoriteIntent(query = {}) {
  return query.favorite === '1'
}

export function clearFavoriteIntentQuery(query = {}) {
  const next = { ...query }
  delete next.favorite
  return next
}

export function buildFavoriteListParams({ page = 1, size = 10, keyword = '', tagId = '' } = {}) {
  const params = { page, size }
  const normalizedKeyword = keyword.trim()
  if (normalizedKeyword) params.keyword = normalizedKeyword
  if (tagId !== '' && tagId !== null && tagId !== undefined) params.tagId = tagId
  return params
}
