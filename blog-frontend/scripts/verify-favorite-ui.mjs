import { readFileSync } from 'node:fs'

const articleDetailPath = new URL('../src/views/ArticleDetail.vue', import.meta.url)
const source = readFileSync(articleDetailPath, 'utf8')
const normalizedSource = source.replace(/\s+/g, ' ')

const required = [
  'favoriteArticle',
  'unfavoriteArticle',
  'getFavoriteStatus',
  'buildFavoriteLoginRedirect',
  'hasFavoriteIntent',
  'clearFavoriteIntentQuery',
  'favoriteLoading',
  'componentActive',
  'favoriteStateVersion',
  'favoriteOperationId',
  'favoriteRequestConfig',
  'isFavoriteRequestContextCurrent',
  'canConsumeFavoriteIntent',
  'navigateToFavoriteLogin',
  'authStore.logout()',
  'error.response?.status === 401',
  'watch(() => authStore.token',
  'skipAuthRedirect: true',
  "localStorage.getItem('token')",
  '已收藏',
  '收藏'
]

const contracts = [
  ['favorite API imports', normalizedSource.includes("from '../api/favorite'")],
  ['favorite guard imports', normalizedSource.includes('isFavoriteRequestContextCurrent') && normalizedSource.includes('canConsumeFavoriteIntent')],
  ['favorite button wiring', source.includes('class="favorite-button"') && source.includes(':loading="favoriteLoading"') && source.includes('@click="handleFavorite"')],
  ['favorite status config', source.includes('getFavoriteStatus(articleId, favoriteRequestConfig)')],
  ['favorite write config', source.includes('favoriteArticle(articleId, favoriteRequestConfig)') && source.includes('unfavoriteArticle(articleId, favoriteRequestConfig)')],
  ['favorite request guard call', source.includes('isFavoriteRequestContextCurrent({')],
  ['favorite intent guard call', source.includes('canConsumeFavoriteIntent({')],
  ['safe favorite initialization', /void initializeFavorite\([^\n]+\)\.catch\(\(\) => \{\}\)/.test(source)],
  ['safe login navigation', source.includes('async function navigateToFavoriteLogin(articleId)')],
  ['safe intent replacement', source.includes('async function replaceFavoriteIntentQuery(query)')]
]

const missing = required.filter(text => !source.includes(text))
const failedContracts = contracts
  .filter(([, passed]) => !passed)
  .map(([name]) => name)

if (missing.length || failedContracts.length) {
  console.error('src/views/ArticleDetail.vue is missing favorite UI requirements:')
  for (const text of missing) console.error(`- text: ${text}`)
  for (const name of failedContracts) console.error(`- contract: ${name}`)
  process.exit(1)
}

console.log('article detail favorite UI verified')
