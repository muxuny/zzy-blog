export function resolveLoginRedirect(user, redirect) {
  const isAdmin = user?.role === 'admin'
  const safeRedirect = typeof redirect === 'string' && redirect.startsWith('/') ? redirect : ''

  if (safeRedirect && !safeRedirect.startsWith('/login') && !safeRedirect.startsWith('/register')) {
    if (!safeRedirect.startsWith('/admin') || isAdmin) return safeRedirect
  }

  return isAdmin ? '/admin/dashboard' : '/'
}
