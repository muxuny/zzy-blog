import request from './request'

export const getTags = () => request.get('/tags')
export const getAdminTags = params => request.get('/admin/tags', { params })
export const createTag = data => request.post('/admin/tags', data)
export const deleteTag = id => request.delete(`/admin/tags/${id}`)
