import request from './request'

export const getFavorites = params => request.get('/my/favorites', { params })
export const getFavoriteStatus = articleId => request.get(`/my/favorites/${articleId}/status`, {
  skipErrorMessage: true
})
export const favoriteArticle = articleId => request.put(`/my/favorites/${articleId}`)
export const unfavoriteArticle = articleId => request.delete(`/my/favorites/${articleId}`)
