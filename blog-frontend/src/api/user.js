import request from './request'
export const getUsers = params => request.get('/admin/users', { params })
export const getUser = id => request.get(`/admin/users/${id}`)
export const approveUser = id => request.put(`/admin/users/${id}/approve`)
export const disableUser = id => request.put(`/admin/users/${id}/disable`)
