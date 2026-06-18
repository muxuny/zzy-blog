import request from './request'
export const uploadImage = file => {
  const fd = new FormData()
  fd.append('file', file)
  return request.post('/admin/upload/image', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
}
export const deleteImage = id => request.delete(`/admin/upload/image/${id}`)
export const getImages = params => request.get('/admin/upload/images', { params })
