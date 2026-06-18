import fs from 'node:fs'

const read = file => (fs.existsSync(file) ? fs.readFileSync(file, 'utf8') : '')

const files = {
  router: read('src/router/index.js'),
  articleDetail: read('src/views/ArticleDetail.vue'),
  myArticles: read('src/views/creator/MyArticles.vue'),
  articleWrite: read('src/views/creator/ArticleWrite.vue'),
  appHeader: read('src/components/AppHeader.vue')
}

let failed = false

function check(condition, message) {
  if (!condition) {
    console.error(`缺少内容：${message}`)
    failed = true
  }
}

function checkIncludes(fileName, content, text) {
  check(content.includes(text), `${fileName} -> ${text}`)
}

const creatorRoutes = [
  '/creator/articles',
  '/creator/articles/create',
  '/creator/articles/edit/:id'
]

for (const route of creatorRoutes) {
  const index = files.router.indexOf(`path: '${route}'`)
  check(index >= 0, `src/router/index.js -> ${route}`)
  if (index >= 0) {
    const routeBlock = files.router.slice(index, files.router.indexOf('\n  }', index) + 5)
    check(routeBlock.includes('requiresAuth: true'), `src/router/index.js -> ${route} requiresAuth: true`)
  }
}

const adminRouteIndex = files.router.indexOf("path: '/admin'")
check(adminRouteIndex >= 0, 'src/router/index.js -> /admin')
if (adminRouteIndex >= 0) {
  const adminRouteBlock = files.router.slice(adminRouteIndex, files.router.indexOf('\n    children:', adminRouteIndex))
  check(adminRouteBlock.includes('requiresAuth: true'), 'src/router/index.js -> /admin requiresAuth: true')
  check(adminRouteBlock.includes('requiresAdmin: true'), 'src/router/index.js -> /admin requiresAdmin: true')
}

const guardChecks = [
  'to.meta.requiresAdmin',
  'authStore.isAdmin',
  "next({ path: '/' })"
]

for (const text of guardChecks) {
  checkIncludes('src/router/index.js', files.router, text)
}

const articleDetailChecks = [
  'loading',
  'errorMessage',
  'try',
  'catch',
  'finally',
  '文章不存在或暂不可见',
  'v-if="loading"',
  'v-else-if="errorMessage"'
]

for (const text of articleDetailChecks) {
  checkIncludes('src/views/ArticleDetail.vue', files.articleDetail, text)
}

const myArticleChecks = [
  'el-pagination',
  'total',
  'current-page',
  'page.value = 1',
  'deleteMyArticle',
  'submitMyArticle',
  'withdrawMyArticle'
]

for (const text of myArticleChecks) {
  checkIncludes('src/views/creator/MyArticles.vue', files.myArticles, text)
}

const articleWriteChecks = [
  'v-loading',
  ':disabled',
  ':loading',
  'savingStatus',
  'saving',
  'try',
  'finally',
  "article.status === 'pending'",
  '待审核文章请先撤回再编辑'
]

for (const text of articleWriteChecks) {
  checkIncludes('src/views/creator/ArticleWrite.vue', files.articleWrite, text)
}

const requiredCopy = [
  '创作中心',
  '我的文章',
  '写文章',
  '全部状态',
  '草稿',
  '待审核',
  '已发布',
  '已驳回',
  '审核反馈',
  '更新时间',
  '操作',
  '编辑',
  '提交审核',
  '撤回',
  '查看已发布',
  '删除',
  '保存草稿',
  '取消'
]

const copyCorpus = `${files.myArticles}\n${files.articleWrite}\n${files.appHeader}`
for (const text of requiredCopy) {
  check(copyCorpus.includes(text), `关键中文文案 -> ${text}`)
}

if (failed) process.exit(1)
console.log('创作中心页面静态校验通过')
