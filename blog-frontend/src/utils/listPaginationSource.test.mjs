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

  assert.match(source, /getUsers\(\{\s*page:\s*page\.value,\s*size:\s*size\.value\s*\}\)/)
  assert.match(source, /total\s*=\s*ref\(0\)/)
  assert.match(source, /class="admin-pagination"/)
  assert.match(source, /本页/)
  assert.doesNotMatch(source, /getUsers\(\{\s*size:\s*100\s*\}\)/)
})
