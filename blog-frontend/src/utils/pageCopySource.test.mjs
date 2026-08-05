import test from 'node:test'
import assert from 'node:assert/strict'
import { existsSync, readFileSync } from 'node:fs'

function read(relativePath) {
  return readFileSync(new URL(relativePath, import.meta.url), 'utf8')
}

function exists(relativePath) {
  return existsSync(new URL(relativePath, import.meta.url))
}

test('page copy api exposes public and admin configuration requests', () => {
  const source = read('../api/pageCopy.js')

  assert.match(source, /request\.get\('\/page-copies'/)
  assert.match(source, /skipAuthRedirect:\s*true/)
  assert.match(source, /skipErrorMessage:\s*true/)
  assert.match(source, /request\.get\('\/admin\/page-copies',\s*\{[\s\S]*skipErrorMessage:\s*true/)
  assert.match(source, /request\.put\(`\/admin\/page-copies\/\$\{encodeURIComponent\(copyKey\)\}`/)
  assert.match(source, /request\.post\('\/admin\/page-copies\/reset'\)/)
})

test('admin page copy route and sidebar entry are wired', () => {
  const router = read('../router/index.js')
  const layout = read('../components/admin/AdminLayout.vue')

  assert.match(router, /path:\s*'page-copies'/)
  assert.match(router, /PageCopies\.vue/)
  assert.match(layout, /to="\/admin\/page-copies"/)
  assert.match(layout, /页面文案/)
  assert.match(layout, /isPathActive\('\/admin\/page-copies'\)/)
})

test('admin page copy view follows the prototype editing structure', () => {
  assert.equal(exists('../views/admin/PageCopies.vue'), true)
  const source = read('../views/admin/PageCopies.vue')

  assert.match(source, /usePageCopyStore/)
  assert.match(source, /groupedAdminCopies/)
  assert.match(source, /saveAdminCopy/)
  assert.match(source, /resetAdminCopies/)
  assert.match(source, /class="page-copy-page"/)
  assert.match(source, /class="page-copy-grid"/)
  assert.match(source, /class="copy-preview"/)
  assert.match(source, /页面文案/)
  assert.match(source, /恢复默认/)
  assert.match(source, /保存这一页/)
})

test('page copy store falls back quietly while admin config page can surface load errors', () => {
  const store = read('../stores/pageCopy.js')
  const view = read('../views/admin/PageCopies.vue')

  assert.match(store, /catch\s*\(error\)/)
  assert.match(store, /options\.throwOnError/)
  assert.match(store, /throw error/)
  assert.match(store, /mergePageCopies\(this\.adminCopies \|\| PAGE_COPY_DEFAULTS\)/)
  assert.match(view, /loadAdminCopies\(\{\s*force:\s*true,\s*throwOnError:\s*true\s*\}\)/)
})

test('functional pages consume page copy keys from the store', () => {
  const publicPages = [
    ['../views/Home.vue', 'home.hero'],
    ['../views/TagArticles.vue', 'tag.index'],
    ['../views/Login.vue', 'auth.login'],
    ['../views/Register.vue', 'auth.register'],
    ['../views/ReadingSpace.vue', 'reading.overview'],
    ['../views/ReadingHistory.vue', 'reading.history'],
    ['../views/Favorites.vue', 'favorites.index'],
    ['../views/creator/MyArticles.vue', 'creator.articles'],
    ['../views/creator/ArticleWrite.vue', 'creator.article.create'],
    ['../views/creator/ArticleWrite.vue', 'creator.article.edit']
  ]

  for (const [file, key] of publicPages) {
    const source = read(file)
    assert.match(source, /usePageCopyStore/)
    assert.match(source, new RegExp(key.replace(/\./g, '\\.')))
    assert.match(source, /loadPublicCopies/)
    assert.match(source, /resolveCopy/)
  }

  const adminPages = [
    ['../views/admin/Dashboard.vue', 'admin.dashboard'],
    ['../views/admin/Articles.vue', 'admin.articles'],
    ['../views/admin/ArticleEdit.vue', 'admin.article.create'],
    ['../views/admin/ArticleEdit.vue', 'admin.article.edit'],
    ['../views/admin/Resources.vue', 'admin.resources'],
    ['../views/admin/Users.vue', 'admin.users'],
    ['../views/admin/Profile.vue', 'admin.profile']
  ]

  for (const [file, key] of adminPages) {
    const source = read(file)
    assert.match(source, /usePageCopyStore/)
    assert.match(source, new RegExp(key.replace(/\./g, '\\.')))
    assert.match(source, /loadAdminCopies/)
    assert.match(source, /resolveCopy/)
    assert.match(source, /page-description/)
  }
})
