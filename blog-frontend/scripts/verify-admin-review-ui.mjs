import fs from 'node:fs'
import path from 'node:path'

const root = process.cwd()
const articlesPath = path.join(root, 'src/views/admin/Articles.vue')
const editPath = path.join(root, 'src/views/admin/ArticleEdit.vue')

const articles = fs.readFileSync(articlesPath, 'utf8')
const edit = fs.readFileSync(editPath, 'utf8')

const failures = []

function check(condition, message) {
  if (!condition) failures.push(message)
}

function includesAll(source, values, label) {
  for (const value of values) {
    check(source.includes(value), `${label} should include ${value}`)
  }
}

function functionBody(source, name) {
  const start = source.indexOf(`async function ${name}`)
  if (start === -1) return ''
  const next = source.indexOf('\nasync function ', start + 1)
  const scriptEnd = source.indexOf('</script>', start)
  const end = next === -1 ? scriptEnd : Math.min(next, scriptEnd)
  return source.slice(start, end === -1 ? source.length : end)
}

includesAll(
  articles,
  ['approveArticle', 'rejectArticle', 'deleteAdminArticle', 'getAdminArticles'],
  'Articles.vue imports or usage',
)

includesAll(
  articles,
  ['全部状态', '草稿', '待审核', '已发布', '已驳回', '驳回原因', '通过', '驳回', '审核通过'],
  'Articles.vue copy',
)

check(/v-model="status"|v-model:.*="status"/.test(articles), 'Articles.vue should have a status filter model')
check(/@change="handleStatusChange"/.test(articles), 'Articles.vue should reset pagination when status changes')
check(/statusMap\s*=/.test(articles), 'Articles.vue should define statusMap')
includesAll(articles, ['draft', 'pending', 'published', 'rejected'], 'Articles.vue statusMap')
check(/function\s+statusText|const\s+statusText/.test(articles), 'Articles.vue should define statusText')
check(/function\s+statusType|const\s+statusType/.test(articles), 'Articles.vue should define statusType')
check(/function\s+handleApprove|const\s+handleApprove/.test(articles), 'Articles.vue should define handleApprove')
check(/function\s+handleReject|const\s+handleReject/.test(articles), 'Articles.vue should define handleReject')
check(/\.trim\(\)/.test(articles), 'Articles.vue reject prompt should trim and validate non-empty input')
check(/throw\s+new\s+Error|return\s+false/.test(articles), 'Articles.vue reject prompt should block empty reason')

const approveBody = functionBody(articles, 'handleApprove')
const rejectBody = functionBody(articles, 'handleReject')
const deleteBody = functionBody(articles, 'handleDelete')

for (const [name, body] of [
  ['handleApprove', approveBody],
  ['handleReject', rejectBody],
  ['handleDelete', deleteBody],
]) {
  check(body.includes('if (isBusy(id)) return'), `Articles.vue ${name} should return early when the row is busy`)
}

check(
  rejectBody.indexOf('setBusy(id, true)') !== -1 &&
    rejectBody.indexOf('ElMessageBox.prompt') !== -1 &&
    rejectBody.indexOf('setBusy(id, true)') < rejectBody.indexOf('ElMessageBox.prompt'),
  'Articles.vue handleReject should set row busy before opening the prompt',
)
check(rejectBody.includes('catch'), 'Articles.vue handleReject should catch prompt cancellation')
check(rejectBody.includes('finally'), 'Articles.vue handleReject should always clear busy state')

check(
  deleteBody.indexOf('setBusy(id, true)') !== -1 &&
    deleteBody.indexOf('ElMessageBox.confirm') !== -1 &&
    deleteBody.indexOf('setBusy(id, true)') < deleteBody.indexOf('ElMessageBox.confirm'),
  'Articles.vue handleDelete should set row busy before opening the confirmation',
)
check(deleteBody.includes('catch'), 'Articles.vue handleDelete should catch confirmation cancellation')
check(deleteBody.includes('finally'), 'Articles.vue handleDelete should always clear busy state')

includesAll(
  edit,
  ['getAdminArticle', 'createAdminArticle', 'updateAdminArticle'],
  'ArticleEdit.vue admin API usage',
)

for (const publicApi of ['getArticle', 'createArticle', 'updateArticle']) {
  check(!new RegExp(`\\b${publicApi}\\b`).test(edit), `ArticleEdit.vue should not use public ${publicApi}`)
}

includesAll(edit, ['保存草稿', '提交审核', '发布'], 'ArticleEdit.vue buttons')

const saveBody = functionBody(edit, 'save')
const saveButtons = edit.match(/<el-button[\s\S]*?@click="save\('[^']+'\)"[\s\S]*?>/g) || []
check(saveButtons.length >= 3, 'ArticleEdit.vue should render three save buttons')
check(
  saveButtons.every(button => button.includes(':disabled="!!savingStatus"') || button.includes(':disabled="Boolean(savingStatus)"')),
  'ArticleEdit.vue save buttons should be disabled while any save is in flight',
)
check(saveBody.includes('if (savingStatus.value) return'), 'ArticleEdit.vue save() should return early when saving')
check(saveBody.includes('const payload'), 'ArticleEdit.vue save() should copy form into a payload before calling API')
check(saveBody.includes('tagIds: [...form.tagIds]'), 'ArticleEdit.vue payload should copy tagIds array')
check(saveBody.includes('updateAdminArticle(route.params.id, payload)'), 'ArticleEdit.vue update should use copied payload')
check(saveBody.includes('createAdminArticle(payload)'), 'ArticleEdit.vue create should use copied payload')

if (failures.length) {
  console.error('Admin review UI verification failed:')
  for (const failure of failures) {
    console.error(`- ${failure}`)
  }
  process.exit(1)
}

console.log('Admin review UI verification passed.')
