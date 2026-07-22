import request from './request.js'

export const getReadingOverview = () => request.get('/my/reading/overview')
export const getReadingHistory = params => request.get('/my/reading/history', { params })
export const deleteReadingHistory = articleId => request.delete(`/my/reading/history/${articleId}`)
export const saveReadingPosition = (articleId, data, config = {}) =>
  request.put(`/my/reading/history/${articleId}/position`, data, config)
export const clearReadingHistory = () => request.delete('/my/reading/history')
