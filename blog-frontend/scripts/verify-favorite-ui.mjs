import { existsSync, readFileSync } from 'node:fs'

const articleDetailPath = new URL('../src/views/ArticleDetail.vue', import.meta.url)
const source = readFileSync(articleDetailPath, 'utf8')
const normalizedSource = source.replace(/\s+/g, ' ')

const favoriteFiles = {
  page: new URL('../src/views/Favorites.vue', import.meta.url),
  item: new URL('../src/components/FavoriteArticleItem.vue', import.meta.url),
  router: new URL('../src/router/index.js', import.meta.url),
  header: new URL('../src/components/AppHeader.vue', import.meta.url),
  request: new URL('../src/api/request.js', import.meta.url)
}

const missingFiles = Object.entries(favoriteFiles)
  .filter(([, path]) => !existsSync(path))
  .map(([name]) => name)

if (missingFiles.length) {
  console.error('favorite list UI files are missing:')
  for (const name of missingFiles) console.error(`- file: ${name}`)
  process.exit(1)
}

const pageSource = readFileSync(favoriteFiles.page, 'utf8')
const itemSource = readFileSync(favoriteFiles.item, 'utf8')
const routerSource = readFileSync(favoriteFiles.router, 'utf8')
const headerSource = readFileSync(favoriteFiles.header, 'utf8')
const requestSource = readFileSync(favoriteFiles.request, 'utf8')
const normalizedPageSource = pageSource.replace(/\s+/g, ' ')
const normalizedItemSource = itemSource.replace(/\s+/g, ' ')

function extractBraceBlock(fileSource, marker) {
  const markerIndex = fileSource.indexOf(marker)
  const start = fileSource.indexOf('{', markerIndex + marker.length)
  if (markerIndex < 0 || start < 0) return ''

  let depth = 0
  for (let index = start; index < fileSource.length; index += 1) {
    if (fileSource[index] === '{') depth += 1
    if (fileSource[index] === '}') depth -= 1
    if (depth === 0) return fileSource.slice(start + 1, index)
  }
  return ''
}

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

const requiredRoutes = ["path: '/favorites'", 'requiresAuth: true']
const requiredPageText = [
  'getFavorites',
  'unfavoriteArticle',
  'buildFavoriteListParams',
  '关键词',
  '标签',
  '重置',
  'el-pagination',
  '尚未收藏文章',
  '当前筛选无结果'
]
const requiredItemText = [
  'item.available',
  '该文章暂未公开',
  'favoritedAt',
  '取消收藏',
  'el-tooltip'
]

const availableTemplateMatch = itemSource.match(/<template v-if="item\.available">([\s\S]*?)<\/template>/)
const availableTemplate = availableTemplateMatch?.[1] || ''
const availableTemplateEnd = availableTemplateMatch
  ? itemSource.indexOf(availableTemplateMatch[0]) + availableTemplateMatch[0].length
  : -1
const itemOutsideAvailableTemplate = availableTemplateMatch
  ? itemSource.replace(availableTemplateMatch[0], '')
  : itemSource
const sensitiveItemFields = ['item.summary', 'item.coverImage', 'item.authorName', 'item.viewCount', 'item.tags']
const loadBlock = extractBraceBlock(pageSource, 'async function load()')
const loadErrorBlock = extractBraceBlock(loadBlock, 'catch (error)')
const pageCorrectionBlock = extractBraceBlock(loadBlock, 'if (page.value > maxPage)')
const unmountBlock = extractBraceBlock(pageSource, 'onBeforeUnmount(() =>')
const submitSearchBlock = extractBraceBlock(pageSource, 'function submitSearch()')
const tagChangeBlock = extractBraceBlock(pageSource, 'function handleTagChange()')
const resetFiltersBlock = extractBraceBlock(pageSource, 'function resetFilters()')
const pageChangeBlock = extractBraceBlock(pageSource, 'function handlePageChange(nextPage)')
const retryLoadBlock = extractBraceBlock(pageSource, 'function retryLoad()')
const hasFiltersBlock = extractBraceBlock(pageSource, 'const hasFilters = computed(() =>')
const loadTagsBlock = extractBraceBlock(pageSource, 'async function loadTags()')
const loadTagsErrorBlock = extractBraceBlock(loadTagsBlock, 'catch')
const removeFavoriteBlock = extractBraceBlock(pageSource, 'async function removeFavorite(item)')
const removeFinallyBlock = extractBraceBlock(removeFavoriteBlock, 'finally')
const removeRequestGuardBlock = extractBraceBlock(pageSource, 'function isRemoveRequestCurrent(articleId, requestId)')
const openArticleBlock = extractBraceBlock(pageSource, 'function openArticle(item)')
const unauthorizedBlock = extractBraceBlock(requestSource, "if (error.response?.status === 401 && !error.config?.skipAuthRedirect)")
const favoritesRoutePattern = /\{\s*path:\s*'\/favorites',\s*name:\s*'Favorites',\s*component:\s*\(\)\s*=>\s*import\('\.\.\/views\/Favorites\.vue'\),\s*meta:\s*\{\s*requiresAuth:\s*true\s*\}\s*\}/s
const activeLoadGuard = 'if (!componentActive || requestId !== loadRequestVersion) return null'
const successStaleGuardIndex = loadBlock.indexOf(activeLoadGuard)
const recordsIndex = loadBlock.indexOf('const records = result.data || []')
const nextTotalIndex = loadBlock.indexOf('const nextTotal = Number(result.total) || 0')
const maxPageIndex = loadBlock.indexOf('const maxPage = Math.max(1, Math.ceil(nextTotal / size.value))')
const pageCorrectionIndex = loadBlock.indexOf('if (page.value > maxPage)')
const applyRecordsIndex = loadBlock.indexOf('items.value = records')
const appliedMetadataIndex = loadBlock.indexOf('return { applied: true, page: requestPage, recordCount: records.length }')
const errorStaleGuardIndex = loadErrorBlock.indexOf(activeLoadGuard)
const errorItemsIndex = loadErrorBlock.indexOf('items.value = []')
const errorTotalIndex = loadErrorBlock.indexOf('total.value = 0')
const errorMessageIndex = loadErrorBlock.indexOf('loadError.value =')
const errorReturnIndex = loadErrorBlock.lastIndexOf('return null')
const tagSuccessGuardIndex = loadTagsBlock.indexOf('if (!componentActive || requestId !== tagRequestVersion) return')
const tagApplyIndex = loadTagsBlock.indexOf('tags.value = result.data || []')
const tagErrorGuardIndex = loadTagsErrorBlock.indexOf('if (!componentActive || requestId !== tagRequestVersion) return')
const tagErrorApplyIndex = loadTagsErrorBlock.indexOf('tags.value = []')
const removeWriteAwaitIndex = removeFavoriteBlock.indexOf('await unfavoriteArticle(item.articleId)')
const removeRequestGuard = 'if (!isRemoveRequestCurrent(item.articleId, requestId)) return'
const removeFirstGuardIndex = removeFavoriteBlock.indexOf(removeRequestGuard)
const removeToastIndex = removeFavoriteBlock.indexOf("ElMessage.success('已取消收藏')")
const removeReloadIndex = removeFavoriteBlock.indexOf('await load()')
const removeSecondGuardIndex = removeFavoriteBlock.indexOf(
  removeRequestGuard,
  removeFirstGuardIndex + removeRequestGuard.length
)

const favoriteContracts = [
  ['favorites route is protected', requiredRoutes.every(text => routerSource.includes(text)) && favoritesRoutePattern.test(routerSource)],
  ['favorites header entry', headerSource.includes('我的收藏') && headerSource.includes("$router.push('/favorites')")],
  ['item declares required prop and events', normalizedItemSource.includes('item: { type: Object, required: true }') && itemSource.includes("defineEmits(['open', 'remove'])")],
  ['available title opens only when available', normalizedItemSource.includes('<button v-if="item.available"') && normalizedItemSource.includes("@click=\"$emit('open', item)\"")],
  ['unavailable title uses focusable tooltip', normalizedItemSource.includes('<el-tooltip v-else') && itemSource.includes('content="该文章暂未公开"') && itemSource.includes(':trigger="[\'hover\', \'focus\']"') && itemSource.includes('class="title-snapshot" tabindex="0"')],
  ['sensitive fields stay in available branch', sensitiveItemFields.every(field => availableTemplate.includes(field)) && sensitiveItemFields.every(field => !itemOutsideAvailableTemplate.includes(field))],
  ['favorite date is always visible', itemSource.includes('<div class="item-foot">') && itemSource.indexOf('formatDate(item.favoritedAt)') > availableTemplateEnd],
  ['remove is isolated and accessible', itemSource.includes('@click.stop="$emit(\'remove\', item)"') && itemSource.includes('title="取消收藏"') && itemSource.includes('aria-label="取消收藏"')],
  ['remove button has stable dimensions', /\.remove-button\s*\{[^}]*width:\s*36px;[^}]*height:\s*36px;/s.test(itemSource)],
  ['favorites expose busy and live status', pageSource.includes('<main class="favorites-main" :aria-busy="loading">') && pageSource.includes('class="favorite-skeleton" role="status" aria-live="polite"') && pageSource.includes('class="empty-status" role="status" aria-live="polite"')],
  ['favorite page reads and normalizes PageResult', recordsIndex >= 0 && nextTotalIndex > recordsIndex && loadBlock.includes('items.value = records') && loadBlock.includes('total.value = nextTotal') && !loadBlock.includes('total.value = result.total')],
  ['favorite load corrects invalid pages before applying records', maxPageIndex > nextTotalIndex && pageCorrectionIndex > maxPageIndex && applyRecordsIndex > pageCorrectionIndex && pageCorrectionBlock.includes('page.value = maxPage') && pageCorrectionBlock.includes('listContextVersion += 1') && pageCorrectionBlock.includes('return await load()')],
  ['favorite load returns only active applied response metadata', loadBlock.includes('const requestPage = page.value') && successStaleGuardIndex >= 0 && recordsIndex > successStaleGuardIndex && appliedMetadataIndex > applyRecordsIndex],
  ['favorite load guards lifecycle and stale responses', pageSource.includes('let componentActive = true') && loadBlock.includes('const requestId = ++loadRequestVersion') && loadBlock.includes(activeLoadGuard) && loadBlock.includes('if (componentActive && requestId === loadRequestVersion) loading.value = false')],
  ['stale load errors cannot mutate current state', errorStaleGuardIndex >= 0 && errorItemsIndex > errorStaleGuardIndex && errorTotalIndex > errorItemsIndex && errorMessageIndex > errorTotalIndex && errorReturnIndex > errorMessageIndex],
  ['unmount invalidates favorite work', normalizedPageSource.includes("import { computed, onBeforeUnmount, onMounted, ref } from 'vue'") && unmountBlock.includes('componentActive = false') && unmountBlock.includes('loadRequestVersion += 1') && unmountBlock.includes('listContextVersion += 1') && unmountBlock.includes('tagRequestVersion += 1') && unmountBlock.includes('removeRequestVersions.clear()')],
  ['filter actions reset pagination', [submitSearchBlock, tagChangeBlock, resetFiltersBlock].every(block => block.includes('page.value = 1'))],
  ['filter and page actions advance list context', pageSource.includes('let listContextVersion = 0') && [submitSearchBlock, tagChangeBlock, resetFiltersBlock, pageChangeBlock].every(block => block.includes('listContextVersion += 1'))],
  ['keyword submission uses list param normalization', submitSearchBlock.includes('keyword.value = buildFavoriteListParams({ keyword: keywordInput.value }).keyword ||')],
  ['empty state filter detection uses list param normalization', hasFiltersBlock.includes('buildFavoriteListParams({') && hasFiltersBlock.includes('keyword: keyword.value') && hasFiltersBlock.includes('tagId: tagId.value') && hasFiltersBlock.includes("return 'keyword' in params || 'tagId' in params")],
  ['load errors can be retried through generic load', loadBlock.includes('loadError.value =') && pageSource.includes('@click="retryLoad"') && retryLoadBlock.includes('void load()')],
  ['tag loading does not block favorites and respects lifecycle', pageSource.includes('void loadTags()') && pageSource.includes('void load()') && loadTagsBlock.includes('const requestId = ++tagRequestVersion') && tagSuccessGuardIndex >= 0 && tagApplyIndex > tagSuccessGuardIndex && tagErrorGuardIndex >= 0 && tagErrorApplyIndex > tagErrorGuardIndex],
  ['remove awaits current write then uses generic load', removeFavoriteBlock.includes('if (isRemoving(item.articleId) || !componentActive) return') && removeWriteAwaitIndex >= 0 && removeFirstGuardIndex > removeWriteAwaitIndex && removeToastIndex > removeFirstGuardIndex && removeReloadIndex > removeToastIndex && removeSecondGuardIndex > removeReloadIndex && !removeFavoriteBlock.includes('recordCount') && !removeFavoriteBlock.includes('page.value -= 1')],
  ['remove finally keeps request ownership', removeRequestGuardBlock.includes('componentActive') && removeRequestGuardBlock.includes('removeRequestVersions.get(articleId) === requestId') && removeFinallyBlock.includes('if (isRemoveRequestCurrent(item.articleId, requestId))')],
  ['article navigation keeps snowflake ids and catches rejection', openArticleBlock.includes('if (!item.available) return') && openArticleBlock.includes("router.push('/article/' + item.articleId).catch(() => {})") && !pageSource.includes('Number(item.articleId)') && !pageSource.includes('Number(tagId.value)')],
  ['401 redirect preserves same-origin full path', unauthorizedBlock.includes("localStorage.removeItem('token')") && unauthorizedBlock.includes('const currentFullPath = `${window.location.pathname}${window.location.search}${window.location.hash}`') && unauthorizedBlock.includes('window.location.href = `/login?redirect=${encodeURIComponent(currentFullPath)}`')],
  ['favorite layout uses theme variables and responsive rules', itemSource.includes('var(--panel-bg)') && pageSource.includes('var(--content-width)') && itemSource.includes('@media (max-width: 640px)') && pageSource.includes('@media (max-width: 720px)')]
]

const missingFavoriteText = [
  ...requiredPageText.filter(text => !pageSource.includes(text)).map(text => `page text: ${text}`),
  ...requiredItemText.filter(text => !itemSource.includes(text)).map(text => `item text: ${text}`)
]
const failedFavoriteContracts = favoriteContracts
  .filter(([, passed]) => !passed)
  .map(([name]) => name)

if (missingFavoriteText.length || failedFavoriteContracts.length) {
  console.error('favorite list UI is missing requirements:')
  for (const text of missingFavoriteText) console.error(`- ${text}`)
  for (const name of failedFavoriteContracts) console.error(`- contract: ${name}`)
  process.exit(1)
}

console.log('favorite detail and list UI verified')
