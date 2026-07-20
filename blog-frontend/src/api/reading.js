import request from './request'

export const getReadingOverview = () => request.get('/my/reading/overview')
export const getReadingHistory = params => request.get('/my/reading/history', { params })
export const deleteReadingHistory = articleId => request.delete(`/my/reading/history/${articleId}`)
export const clearReadingHistory = () => request.delete('/my/reading/history')
