import fs from 'node:fs'

const read = file => fs.readFileSync(file, 'utf8')
const checks = [
  ['src/api/myArticle.js', '/my/articles'],
  ['src/api/article.js', '/admin/articles/{id}/approve'.replace('{id}', '${id}')],
  ['src/api/article.js', '/admin/articles/{id}/reject'.replace('{id}', '${id}')],
  ['src/api/article.js', 'getAdminArticle'],
  ['src/api/article.js', 'createAdminArticle']
]

let failed = false
for (const [file, text] of checks) {
  if (!fs.existsSync(file) || !read(file).includes(text)) {
    console.error(`缺少内容：${file} -> ${text}`)
    failed = true
  }
}

if (failed) {
  process.exit(1)
}

console.log('文章审核 API 静态校验通过')
