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
