import test from 'node:test'
import assert from 'node:assert/strict'
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const srcRoot = path.resolve(__dirname, '..')

function read(relativePath) {
  return fs.readFileSync(path.resolve(srcRoot, relativePath), 'utf8')
}

test('tag article page uses paged article requests and total count', () => {
  const source = read('views/TagArticles.vue')

  assert.match(source, /page\s*=\s*ref\(1\)/)
  assert.match(source, /total\s*=\s*ref\(0\)/)
  assert.match(source, /<el-pagination/)
  assert.match(source, /tagId:\s*resolvedTagId\.value/)
  assert.match(source, /page:\s*page\.value/)
  assert.match(source, /size:\s*size\.value/)
  assert.doesNotMatch(source, /size:\s*100/)
})

test('admin users page consumes backend pagination', () => {
  const source = read('views/admin/Users.vue')

  assert.match(source, /const params = \{ page: page\.value, size: size\.value \}/)
  assert.match(source, /if \(status\.value\) params\.status = status\.value/)
  assert.match(source, /getUsers\(params\)/)
  assert.match(source, /total\s*=\s*ref\(0\)/)
  assert.match(source, /class="admin-pagination"/)
  assert.match(source, /本页/)
  assert.doesNotMatch(source, /getUsers\(\{\s*size:\s*100\s*\}\)/)
})

test('admin users page reads status query and passes status to backend params', () => {
  const source = read('views/admin/Users.vue')

  assert.match(source, /import \{ computed, onMounted, ref, watch \} from 'vue'/)
  assert.match(source, /import \{ useRoute \} from 'vue-router'/)
  assert.match(source, /const route = useRoute\(\)/)
  assert.match(source, /const userStatusOptions = \[/)
  assert.match(source, /value: 'pending', label: '待审核'/)
  assert.match(source, /value: 'active', label: '已启用'/)
  assert.match(source, /value: 'disabled', label: '已禁用'/)
  assert.match(source, /const status = ref\(normalizeUserStatusQuery\(route\.query\.status\)\)/)
  assert.match(source, /function normalizeUserStatusQuery\(value\)/)
  assert.match(source, /watch\(\s*\(\) => route\.query\.status/)
  assert.match(source, /class="filter-field user-status-filter"/)
  assert.match(source, /aria-label="用户状态"/)
  assert.match(source, /@change="handleFilterChange"/)
})

test('admin resources page pages tags and images separately', () => {
  const source = read('views/admin/Resources.vue')

  assert.match(source, /getAdminTags\(\{\s*page:\s*tagPage\.value,\s*size:\s*tagSize\.value\s*\}\)/)
  assert.match(source, /getImages\(\{\s*page:\s*imagePage\.value,\s*size:\s*imageSize\.value\s*\}\)/)
  assert.match(source, /tagTotal/)
  assert.match(source, /imageTotal/)
  assert.match(source, /class="resource-pagination"/)
  assert.doesNotMatch(source, /getTags\(\)/)
  assert.doesNotMatch(source, /size:\s*100/)
})

test('legacy admin tag and image pages are not kept as fixed-size list sources', () => {
  assert.equal(fs.existsSync(path.resolve(srcRoot, 'views/admin/Tags.vue')), false)
  assert.equal(fs.existsSync(path.resolve(srcRoot, 'views/admin/Images.vue')), false)
})

test('admin dashboard uses aggregate overview endpoint', () => {
  const source = read('views/admin/Dashboard.vue')

  assert.match(source, /getAdminDashboardOverview/)
  assert.doesNotMatch(source, /getAdminArticles/)
  assert.doesNotMatch(source, /getUsers/)
  assert.doesNotMatch(source, /getTags/)
  assert.doesNotMatch(source, /size:\s*200/)
  assert.doesNotMatch(source, /size:\s*100/)
})
