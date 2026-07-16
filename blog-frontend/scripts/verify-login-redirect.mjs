import { resolveLoginRedirect } from '../src/utils/authRedirect.js'

const cases = [
  [{ role: 'user' }, undefined, '/'],
  [{ role: 'user' }, '/article/1', '/article/1'],
  [{ role: 'user' }, '/admin/dashboard', '/'],
  [{ role: 'admin' }, undefined, '/admin/dashboard'],
  [{ role: 'admin' }, '/admin/tags', '/admin/tags'],
  [{ role: 'admin' }, 'https://example.com', '/admin/dashboard'],
  [{ role: 'admin' }, '/login', '/admin/dashboard'],
  [{ role: 'user' }, '//evil.example', '/'],
  [{ role: 'admin' }, '//evil.example', '/admin/dashboard'],
  [{ role: 'user' }, '/\\evil.example', '/'],
  [{ role: 'admin' }, '/\\evil.example', '/admin/dashboard'],
  [{ role: 'user' }, '/\\\\evil.example', '/'],
  [{ role: 'admin' }, '/\\\\evil.example', '/admin/dashboard'],
  [{ role: 'user' }, '///evil.example', '/'],
  [{ role: 'admin' }, '///evil.example', '/admin/dashboard'],
  [{ role: 'user' }, '/\t/evil.example', '/'],
  [{ role: 'admin' }, '/\t/evil.example', '/admin/dashboard'],
  [{ role: 'user' }, '/\r/evil.example', '/'],
  [{ role: 'admin' }, '/\r/evil.example', '/admin/dashboard'],
  [{ role: 'user' }, '/\n/evil.example', '/'],
  [{ role: 'admin' }, '/\n/evil.example', '/admin/dashboard']
]

for (const [user, redirect, expected] of cases) {
  const actual = resolveLoginRedirect(user, redirect)
  if (actual !== expected) {
    throw new Error(`Expected ${expected}, got ${actual} for ${JSON.stringify({ user, redirect })}`)
  }
}

console.log('login redirect rules verified')
