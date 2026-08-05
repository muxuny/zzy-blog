import request from './request'

export const getPageCopies = () => request.get('/page-copies', {
  skipAuthRedirect: true,
  skipErrorMessage: true
})

export const getAdminPageCopies = () => request.get('/admin/page-copies', {
  skipErrorMessage: true
})

export const updateAdminPageCopy = (copyKey, data) => (
  request.put(`/admin/page-copies/${encodeURIComponent(copyKey)}`, data)
)

export const resetAdminPageCopies = () => request.post('/admin/page-copies/reset')
