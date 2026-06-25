import request from './request'

export const getArticleGroups = () => request.get('/my/article-groups')
export const createArticleGroup = data => request.post('/my/article-groups', data)
export const updateArticleGroup = (id, data) => request.put(`/my/article-groups/${id}`, data)
export const deleteArticleGroup = id => request.delete(`/my/article-groups/${id}`)
