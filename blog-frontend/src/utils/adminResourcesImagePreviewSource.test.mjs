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

test('admin resources image cards expose preview triggers', () => {
  const source = read('views/admin/Resources.vue')

  assert.match(source, /class="mini-image preview-thumb-button"/)
  assert.match(source, /@click="openImagePreview\(image\)"/)
  assert.match(source, /class="image-card-actions"/)
  assert.match(source, />预览<\/button>/)
})

test('admin resources image preview dialog shows large image and metadata', () => {
  const source = read('views/admin/Resources.vue')

  assert.match(source, /<el-dialog[\s\S]*class="image-preview-dialog"/)
  assert.match(source, /v-model="previewDialog\.visible"/)
  assert.match(source, /class="image-preview-stage"/)
  assert.match(source, /:src="previewImage\.url"/)
  assert.match(source, /class="image-preview-meta"/)
  for (const label of ['文件名', '文件大小', '上传人', '上传时间', '图片地址']) {
    assert.match(source, new RegExp(label))
  }
})

test('admin resources image preview supports copy and dialog deletion', () => {
  const source = read('views/admin/Resources.vue')

  assert.match(source, /const previewDialog = ref\(/)
  assert.match(source, /const previewImage = computed\(/)
  assert.match(source, /function openImagePreview\(image\)/)
  assert.match(source, /async function copyImageUrl\(\)/)
  assert.match(source, /navigator\.clipboard\?\.writeText/)
  assert.match(source, /async function handleDeletePreviewImage\(\)/)
  assert.match(source, /handleDeleteImage\(previewImage\.value\?\.id,\s*\{\s*closePreview:\s*true\s*\}\)/)
})
