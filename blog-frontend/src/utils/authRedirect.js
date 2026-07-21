export function resolveLoginRedirect(user, redirect) {
  const isAdmin = user?.role === 'admin'
  const isInternalRedirect = typeof redirect === 'string' &&
    !/[\t\r\n]/.test(redirect) &&
    redirect.startsWith('/') &&
    !redirect.startsWith('//') &&
    !redirect.startsWith('/\\')
  const safeRedirect = isInternalRedirect ? redirect : ''

  if (safeRedirect && !safeRedirect.startsWith('/login') && !safeRedirect.startsWith('/register')) {
    if (!safeRedirect.startsWith('/admin') || isAdmin) return safeRedirect
  }

  return isAdmin ? '/admin/dashboard' : '/'
}
