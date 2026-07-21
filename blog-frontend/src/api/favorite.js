import request from './request'

export const getFavorites = params => request.get('/my/favorites', { params })
export const getFavoriteStatus = (articleId, config = {}) => request.get(`/my/favorites/${articleId}/status`, {
  skipErrorMessage: true,
  ...config
})
export const favoriteArticle = (articleId, config = {}) =>
  request.put(`/my/favorites/${articleId}`, undefined, config)
export const unfavoriteArticle = (articleId, config = {}) =>
  request.delete(`/my/favorites/${articleId}`, config)
