import request from './request'

export const getArticles = params => request.get('/articles', { params })
export const getArticle = id => request.get(`/articles/${id}`)
export const getArticleNeighbors = id => request.get(`/articles/${id}/neighbors`, { skipErrorMessage: true })
export const getRelatedArticles = (id, size = 4) => request.get(`/articles/${id}/related`, {
  params: { size },
  skipErrorMessage: true
})

export const getAdminArticles = params => request.get('/admin/articles', { params })
export const getAdminArticle = id => request.get(`/admin/articles/${id}`)
export const createAdminArticle = data => request.post('/admin/articles', data)
export const updateAdminArticle = (id, data) => request.put(`/admin/articles/${id}`, data)
export const deleteAdminArticle = id => request.delete(`/admin/articles/${id}`)
export const approveArticle = id => request.put(`/admin/articles/${id}/approve`)
export const rejectArticle = (id, reason) => request.put(`/admin/articles/${id}/reject`, { reason })

export const createArticle = createAdminArticle
export const updateArticle = updateAdminArticle
export const deleteArticle = deleteAdminArticle
