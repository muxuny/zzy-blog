import { resolveLoginRedirect } from '../src/utils/authRedirect.js'
import { readFileSync } from 'node:fs'

const loginSource = readFileSync(new URL('../src/views/Login.vue', import.meta.url), 'utf8')
const articleSource = readFileSync(new URL('../src/views/ArticleDetail.vue', import.meta.url), 'utf8')
const normalizedLoginSource = loginSource.replace(/\s+/g, ' ')
const normalizedArticleSource = articleSource.replace(/\s+/g, ' ')

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

const historyContracts = [
  ['post-login navigation replaces the login history entry',
    normalizedLoginSource.includes('await router.replace(resolveLoginRedirect(authStore.user, route.query.redirect))')],
  ['favorite login navigation replaces the article history entry',
    normalizedArticleSource.includes("async function navigateToFavoriteLogin(articleId, nextValue = true) { try { await router.replace({ path: '/login'")]
]

for (const [name, passed] of historyContracts) {
  if (!passed) throw new Error(`Missing login history contract: ${name}`)
}

console.log('login redirect rules verified')
