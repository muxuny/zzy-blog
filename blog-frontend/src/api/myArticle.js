import request from './request'

export const getMyArticles = params => request.get('/my/articles', { params })
export const getMyArticle = id => request.get(`/my/articles/${id}`)
export const createMyArticle = data => request.post('/my/articles', data)
export const updateMyArticle = (id, data) => request.put(`/my/articles/${id}`, data)
export const updateMyArticleGroups = (id, data) => request.put(`/my/articles/${id}/groups`, data)
export const deleteMyArticle = id => request.delete(`/my/articles/${id}`)
export const submitMyArticle = id => request.put(`/my/articles/${id}/submit`)
export const withdrawMyArticle = id => request.put(`/my/articles/${id}/withdraw`)
