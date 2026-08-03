import request from './request'

export const getAdminDashboardOverview = () => request.get('/admin/dashboard/overview')
