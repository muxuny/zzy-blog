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
  'intentFullPath',
  'intentPath',
  'intentAuthToken',
  "localStorage.getItem('token')",
  'window.location.pathname',
  '已收藏',
  '收藏'
]

const contracts = [
  ['favorite API imports', /import\s*\{[^}]*favoriteArticle[^}]*unfavoriteArticle[^}]*getFavoriteStatus[^}]*\}\s*from\s*['"]\.\.\/api\/favorite['"]/s],
  ['favorite intent imports', /import\s*\{[^}]*buildFavoriteLoginRedirect[^}]*hasFavoriteIntent[^}]*clearFavoriteIntentQuery[^}]*\}\s*from\s*['"]\.\.\/utils\/favorite['"]/s],
  ['favorite button click and loading state', /class="favorite-button"[^>]*:loading="favoriteLoading"[^>]*@click="handleFavorite"/s],
  ['favorite button icons', /<StarFilled\s+v-if="favorited"\s*\/>[\s\S]*<Star\s+v-else\s*\/>/],
  ['favorite handler', /async function handleFavorite\s*\(\)/],
  ['favorite initialization', /async function initializeFavorite\s*\(articleId, token\)/],
  ['favorite context guard', /return componentActive && token === loadToken\.value && article\.value\?\.id === articleId && stateVersion === favoriteStateVersion/],
  ['status request captures and validates state version', /const stateVersion = favoriteStateVersion[\s\S]*await getFavoriteStatus\(articleId\)[\s\S]*isCurrentFavoriteContext\(articleId, token, stateVersion\)/],
  ['favorite write invalidates status and owns loading state', /const stateVersion = \+\+favoriteStateVersion[\s\S]*const operationId = \+\+favoriteOperationId[\s\S]*if \(operationId === favoriteOperationId\) favoriteLoading\.value = false/],
  ['article load invalidates old favorite work', /async function loadArticle\s*\(\)\s*\{[\s\S]*favoriteStateVersion \+= 1[\s\S]*favoriteOperationId \+= 1[\s\S]*favoriteLoading\.value = false[\s\S]*favorited\.value = false/],
  ['unmount invalidates article and favorite work', /onBeforeUnmount\(\(\) => \{[\s\S]*componentActive = false[\s\S]*loadToken\.value \+= 1[\s\S]*favoriteStateVersion \+= 1[\s\S]*favoriteOperationId \+= 1[\s\S]*removeEventListener/],
  ['favorite intent captures route identity', /const intentFullPath = route\.fullPath[\s\S]*const intentPath = route\.path[\s\S]*const intentArticleId = articleId/],
  ['favorite intent requires an auth token snapshot', /if \(hasFavoriteIntent\(route\.query\)\) \{[\s\S]*const intentAuthToken = localStorage\.getItem\('token'\)[\s\S]*if \(!intentAuthToken\) return[\s\S]*await setFavorite\(articleId, true, token\)/],
  ['favorite intent cleanup validates lifecycle route browser path and auth token', /componentActive[\s\S]*article\.value\?\.id === intentArticleId[\s\S]*route\.fullPath === intentFullPath[\s\S]*route\.path === intentPath[\s\S]*window\.location\.pathname === intentPath[\s\S]*localStorage\.getItem\('token'\) === intentAuthToken[\s\S]*hasFavoriteIntent\(route\.query\)[\s\S]*router\.replace\s*\(\s*\{\s*query:\s*clearFavoriteIntentQuery\(route\.query\)\s*\}\s*\)/]
]

const missing = required.filter(text => !source.includes(text))
const failedContracts = contracts
  .filter(([, pattern]) => !pattern.test(normalizedSource))
  .map(([name]) => name)

if (missing.length || failedContracts.length) {
  console.error('src/views/ArticleDetail.vue is missing favorite UI requirements:')
  for (const text of missing) console.error(`- text: ${text}`)
  for (const name of failedContracts) console.error(`- contract: ${name}`)
  process.exit(1)
}

console.log('article detail favorite UI verified')
