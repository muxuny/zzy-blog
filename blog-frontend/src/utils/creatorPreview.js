export function getCreatorPreviewRoute(article) {
  if (!article?.id) return ''
  return `/creator/articles/preview/${article.id}`
}

export function getCreatorEditRoute(article) {
  if (!article?.id) return ''
  return `/creator/articles/edit/${article.id}`
}
