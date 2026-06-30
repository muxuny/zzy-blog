export const ARTICLE_VISIBILITY_PUBLIC = 'public'
export const ARTICLE_VISIBILITY_PRIVATE = 'private'

const visibilityMap = {
  [ARTICLE_VISIBILITY_PUBLIC]: { text: '公开', type: 'success' },
  [ARTICLE_VISIBILITY_PRIVATE]: { text: '仅自己可见', type: 'info' }
}

export function normalizeArticleVisibility(visibility) {
  if (visibility === ARTICLE_VISIBILITY_PRIVATE) {
    return ARTICLE_VISIBILITY_PRIVATE
  }
  return ARTICLE_VISIBILITY_PUBLIC
}

export function articleVisibilityText(visibility) {
  return visibilityMap[normalizeArticleVisibility(visibility)].text
}

export function articleVisibilityType(visibility) {
  return visibilityMap[normalizeArticleVisibility(visibility)].type
}

export function nextArticleVisibility(visibility) {
  return normalizeArticleVisibility(visibility) === ARTICLE_VISIBILITY_PRIVATE
    ? ARTICLE_VISIBILITY_PUBLIC
    : ARTICLE_VISIBILITY_PRIVATE
}
