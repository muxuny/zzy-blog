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
  ['favorite intent cleanup', /router\.replace\s*\(\s*\{\s*query:\s*clearFavoriteIntentQuery\(route\.query\)\s*\}\s*\)/]
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
