export function buildFavoriteLoginRedirect(articleId, shouldFavorite = true) {
  return shouldFavorite
    ? `/article/${articleId}?favorite=1`
    : `/article/${articleId}`
}

export function hasFavoriteIntent(query = {}) {
  return query.favorite === '1'
}

export function clearFavoriteIntentQuery(query = {}) {
  const next = { ...query }
  delete next.favorite
  return next
}

export function isFavoriteRequestContextCurrent({
  componentActive,
  requestArticleId,
  currentArticleId,
  requestLoadToken,
  currentLoadToken,
  requestStateVersion,
  currentStateVersion,
  requestAuthToken,
  currentAuthToken
} = {}) {
  return componentActive === true &&
    requestArticleId === currentArticleId &&
    requestLoadToken === currentLoadToken &&
    requestStateVersion === currentStateVersion &&
    typeof requestAuthToken === 'string' &&
    requestAuthToken.length > 0 &&
    requestAuthToken === currentAuthToken
}

export function canConsumeFavoriteIntent({
  componentActive,
  intentArticleId,
  currentArticleId,
  intentLoadToken,
  currentLoadToken,
  intentFullPath,
  currentFullPath,
  intentPath,
  currentPath,
  browserPathname,
  intentAuthToken,
  currentAuthToken,
  hasIntent
} = {}) {
  return componentActive === true &&
    intentArticleId === currentArticleId &&
    intentLoadToken === currentLoadToken &&
    intentFullPath === currentFullPath &&
    intentPath === currentPath &&
    intentPath === browserPathname &&
    typeof intentAuthToken === 'string' &&
    intentAuthToken.length > 0 &&
    intentAuthToken === currentAuthToken &&
    hasIntent === true
}

export function buildFavoriteListParams({ page = 1, size = 10, keyword = '', tagId = '' } = {}) {
  const params = { page, size }
  const normalizedKeyword = keyword.trim()
  if (normalizedKeyword) params.keyword = normalizedKeyword
  if (tagId !== '' && tagId !== null && tagId !== undefined) params.tagId = tagId
  return params
}
