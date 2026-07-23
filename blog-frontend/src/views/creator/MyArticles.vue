<template>
  <div class="layout">
    <AppHeader />
    <el-main class="main creator-shell">
      <header class="page-head">
        <div class="head-copy">
          <span class="eyebrow">Creator console</span>
          <h1>创作空间保留控制台感，但触感更轻。</h1>
          <p>创作者最需要效率，所以动态只用于聚焦当前行、状态筛选和预览反馈。它应该让后台工作更顺手，而不是更花。</p>
        </div>
        <aside class="head-meta" aria-label="创作摘要">
          <div class="meta-line">
            <span class="meta-label">全部文章</span>
            <span class="meta-value">{{ total }} 篇</span>
          </div>
          <div class="meta-line">
            <span class="meta-label">待处理</span>
            <span class="meta-value">{{ attentionText }}</span>
          </div>
          <div class="meta-line">
            <span class="meta-label">最近更新</span>
            <span class="meta-value">{{ latestUpdateText }}</span>
          </div>
        </aside>
      </header>

      <section class="creator-toolbar" aria-label="创作筛选">
        <div class="searchbox">
          <input
            id="creatorSearch"
            v-model="keyword"
            type="search"
            placeholder="搜索自己的文章"
            aria-label="搜索自己的文章"
            @keydown.esc="keyword = ''"
          >
        </div>
        <div class="status-filters">
          <button
            v-for="option in statusOptions"
            :key="option.value || 'all'"
            type="button"
            class="chip"
            :class="{ 'is-active': status === option.value }"
            @click="selectStatus(option.value)"
          >
            {{ option.label }}
          </button>
          <el-select
            v-model="visibility"
            class="visibility-filter"
            placeholder="全部可见性"
            clearable
            @change="handleVisibilityChange"
          >
            <el-option label="公开" value="public" />
            <el-option label="仅自己可见" value="private" />
          </el-select>
          <el-button class="primary-button" type="primary" @click="$router.push('/creator/articles/create')">写文章</el-button>
        </div>
      </section>

      <section class="creator-workspace">
        <aside v-loading="groupsLoading" class="group-rail">
          <div class="rail-head">
            <strong>文章分组</strong>
            <el-button class="mini-button" size="small" text @click="createGroup">新建</el-button>
          </div>
          <div class="group-list">
            <button
              type="button"
              class="group-item"
              :class="{ 'is-active': selectedGroup === GROUP_FILTER_ALL }"
              @click="selectGroup(GROUP_FILTER_ALL)"
            >
              <span>全部文章</span>
              <span>{{ total }}</span>
            </button>
            <button
              type="button"
              class="group-item"
              :class="{ 'is-active': selectedGroup === GROUP_FILTER_UNGROUPED }"
              @click="selectGroup(GROUP_FILTER_UNGROUPED)"
            >
              <span>未分组</span>
              <span>--</span>
            </button>
            <div
              v-for="group in articleGroups"
              :key="group.id"
              role="button"
              tabindex="0"
              class="group-item group-item-with-actions"
              :class="{ 'is-active': selectedGroup === createGroupFilterKey(group.id) }"
              @click="selectGroup(createGroupFilterKey(group.id))"
              @keydown.enter.prevent="selectGroup(createGroupFilterKey(group.id))"
              @keydown.space.prevent="selectGroup(createGroupFilterKey(group.id))"
            >
              <span class="group-name">{{ group.name }}</span>
              <span class="group-meta">{{ group.articleCount || 0 }}</span>
              <el-dropdown trigger="click" @command="command => handleGroupCommand(group, command)">
                <span class="group-more" @click.stop>更多</span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="rename">重命名</el-dropdown-item>
                    <el-dropdown-item command="delete" class="danger-command">删除</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </aside>

        <section class="article-section">
          <section v-loading="loading" class="article-flow-panel" aria-label="我的文章">
            <div class="creator-list">
              <article
                v-for="(row, index) in displayedArticles"
                :key="row.id"
                class="creator-article-card"
                :class="[`creator-article-card--${row.status || 'unknown'}`, { 'is-selected': index === 0 }]"
              >
                <div class="row-copy">
                  <div class="row-meta">
                    <span>{{ primaryGroupName(row) }}</span>
                    <span>{{ formatCreatorDate(row.updatedAt || row.createdAt) }}</span>
                    <span>{{ articleVisibilityText(row.visibility) }}</span>
                  </div>
                  <h3>
                    <button type="button" class="article-title-button" @click="openPreview(row)">
                      {{ row.title }}
                    </button>
                  </h3>
                  <p>{{ rowSummary(row) }}</p>
                </div>
                <div class="row-actions">
                  <span class="status-pill" :data-status="row.status">{{ statusText(row.status) }}</span>
                  <el-button size="small" class="ghost-button preview-action" @click="openPreview(row)">预览</el-button>
                  <el-dropdown
                    trigger="click"
                    popper-class="article-action-menu"
                    :disabled="isRowBusy(row.id)"
                    @command="command => handleArticleCommand(row, command)"
                  >
                    <button
                      type="button"
                      class="article-more-button"
                      :disabled="isRowBusy(row.id)"
                      aria-label="更多操作"
                    >
                      <el-icon><MoreFilled /></el-icon>
                    </button>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item command="groups">分组</el-dropdown-item>
                        <el-dropdown-item v-if="canOpenPublicArticle(row)" command="view">查看公开页</el-dropdown-item>
                        <el-dropdown-item v-if="row.status !== 'pending'" command="edit">编辑</el-dropdown-item>
                        <el-dropdown-item
                          v-if="row.status === 'draft' || row.status === 'rejected'"
                          command="submit"
                        >
                          提交审核
                        </el-dropdown-item>
                        <el-dropdown-item v-if="row.status === 'pending'" command="withdraw">撤回</el-dropdown-item>
                        <el-dropdown-item command="visibility">
                          {{ nextVisibilityText(row.visibility) }}
                        </el-dropdown-item>
                        <el-dropdown-item command="delete" class="danger-command">删除</el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
              </article>

              <article v-if="!displayedArticles.length && !loading" class="creator-article-card is-empty-card">
                <div>
                  <div class="row-meta">
                    <span>{{ keyword ? '当前搜索' : activeGroupTitle }}</span>
                  </div>
                  <h3>暂时没有匹配的文章</h3>
                  <p>可以调整搜索、状态、可见性或分组筛选，也可以直接写一篇新的。</p>
                </div>
                <div class="row-actions">
                  <el-button class="primary-button" type="primary" @click="$router.push('/creator/articles/create')">写文章</el-button>
                </div>
              </article>
            </div>
          </section>

          <el-pagination
            v-if="total > size"
            v-model:current-page="page"
            :total="total"
            :page-size="size"
            layout="prev,pager,next"
            class="article-pagination"
            @current-change="load"
          />
        </section>
      </section>

      <el-dialog v-model="groupAssignDialog.visible" title="调整文章分组" width="420px" @closed="closeGroupAssign">
        <p class="group-assign-intro">
          这里只调整你自己的文章归类，不会修改文章内容，也不会改变审核状态。
        </p>
        <el-select
          v-model="groupAssignDialog.selectedGroupId"
          class="group-assign-select"
          placeholder="未分组"
          filterable
          clearable
        >
          <el-option label="未分组" :value="''" />
          <el-option
            v-for="group in articleGroups"
            :key="group.id"
            :label="group.name"
            :value="group.id"
          />
        </el-select>
        <template #footer>
          <el-button @click="groupAssignDialog.visible = false">取消</el-button>
          <el-button type="primary" :loading="groupAssignLoading" @click="assignGroup">保存</el-button>
        </template>
      </el-dialog>
    </el-main>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MoreFilled } from '@element-plus/icons-vue'
import AppHeader from '../../components/AppHeader.vue'
import { createArticleGroup, deleteArticleGroup, getArticleGroups, updateArticleGroup } from '../../api/articleGroup'
import { deleteMyArticle, getMyArticles, submitMyArticle, updateMyArticleGroups, updateMyArticleVisibility, withdrawMyArticle } from '../../api/myArticle'
import { getCreatorPreviewRoute } from '../../utils/creatorPreview'
import {
  ARTICLE_VISIBILITY_PUBLIC,
  articleVisibilityText,
  normalizeArticleVisibility,
  nextArticleVisibility
} from '../../utils/articleVisibility'
import {
  GROUP_FILTER_ALL,
  GROUP_FILTER_UNGROUPED,
  buildArticleGroupIds,
  buildMyArticleGroupParams,
  createGroupFilterKey,
  formatArticleGroupNames,
  getFirstArticleGroupId,
  parseGroupFilterKey
} from '../../utils/articleGroups'
import { formatDate } from '../../utils'

const articles = ref([])
const articleGroups = ref([])
const loading = ref(false)
const groupsLoading = ref(false)
const keyword = ref('')
const status = ref('')
const visibility = ref('')
const selectedGroup = ref(GROUP_FILTER_ALL)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const rowAction = ref({})
const groupAssignLoading = ref(false)
const router = useRouter()
const groupAssignDialog = ref({
  visible: false,
  article: null,
  selectedGroupId: ''
})

const statusMap = {
  draft: { text: '草稿', type: 'info' },
  pending: { text: '待审核', type: 'warning' },
  published: { text: '已发布', type: 'success' },
  rejected: { text: '已驳回', type: 'danger' }
}
const statusOptions = [
  { label: '全部', value: '' },
  { label: '草稿', value: 'draft' },
  { label: '待审核', value: 'pending' },
  { label: '已发布', value: 'published' },
  { label: '已驳回', value: 'rejected' }
]

const activeGroupTitle = computed(() => {
  const parsed = parseGroupFilterKey(selectedGroup.value)
  if (parsed.type === GROUP_FILTER_UNGROUPED) return '未分组'
  if (parsed.type === 'group') {
    return articleGroups.value.find(group => group.id === parsed.groupId)?.name || '文章分组'
  }
  return '全部文章'
})
const attentionCount = computed(() =>
  articles.value.filter(article => article.status === 'pending' || article.status === 'rejected').length
)
const attentionText = computed(() => (attentionCount.value ? `${attentionCount.value} 篇待处理` : '暂无待处理'))
const latestUpdateText = computed(() => {
  const newest = articles.value
    .map(article => article.updatedAt || article.createdAt)
    .map(value => new Date(value))
    .filter(date => !Number.isNaN(date.getTime()))
    .sort((left, right) => right.getTime() - left.getTime())[0]

  return newest ? formatCreatorDate(newest) : '暂无更新'
})
const displayedArticles = computed(() => {
  const query = keyword.value.trim().toLowerCase()
  if (!query) return articles.value

  return articles.value.filter(article => articleSearchText(article).includes(query))
})

onMounted(() => {
  loadGroups()
  load()
})

function statusText(value) {
  return statusMap[value]?.text || value
}

async function load() {
  loading.value = true
  try {
    const baseParams = { page: page.value, size: size.value }
    const params = buildMyArticleGroupParams(baseParams, selectedGroup.value)
    if (status.value) params.status = status.value
    if (visibility.value) params.visibility = visibility.value
    const result = await getMyArticles(params)
    articles.value = result.data || []
    total.value = result.total || 0
  } finally {
    loading.value = false
  }
}

async function loadGroups() {
  groupsLoading.value = true
  try {
    const result = await getArticleGroups()
    articleGroups.value = result.data || []
  } finally {
    groupsLoading.value = false
  }
}

function handleStatusChange() {
  page.value = 1
  load()
}

function selectStatus(value) {
  if (status.value === value) return
  status.value = value
  handleStatusChange()
}

function handleVisibilityChange() {
  page.value = 1
  load()
}

function selectGroup(groupKey) {
  selectedGroup.value = groupKey
  page.value = 1
  load()
}

function isRowBusy(id) {
  return !!rowAction.value[id]
}

function canOpenPublicArticle(row) {
  return row.status === 'published' && normalizeArticleVisibility(row.visibility) === ARTICLE_VISIBILITY_PUBLIC
}

function openPreview(row) {
  const target = getCreatorPreviewRoute(row)
  if (target) router.push(target)
}

function primaryGroupName(row) {
  return formatArticleGroupNames(row.groups || []) || '未分组'
}

function rowSummary(row) {
  if (row.status === 'published') {
    return row.reviewReason || '已发布，可从预览或公开页检查展示'
  }
  if (row.status === 'pending') {
    return row.reviewReason || '待审核，等待管理员确认'
  }
  if (row.status === 'rejected') {
    return row.reviewReason || '已驳回，需要调整后重新提交'
  }
  return row.summary || '草稿，继续补齐内容后提交审核'
}

function articleSearchText(article) {
  return [
    article.title,
    article.summary,
    article.reviewReason,
    primaryGroupName(article),
    statusText(article.status),
    articleVisibilityText(article.visibility),
    rowSummary(article)
  ]
    .filter(Boolean)
    .join(' ')
    .toLowerCase()
}

function formatCreatorDate(value) {
  const date = value instanceof Date ? value : new Date(value)
  if (Number.isNaN(date.getTime())) return ''

  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const target = new Date(date.getFullYear(), date.getMonth(), date.getDate())
  const timeText = date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', hour12: false })
  const diffDays = Math.round((today.getTime() - target.getTime()) / 86400000)

  if (diffDays === 0) return `今天 ${timeText}`
  if (diffDays === 1) return `昨天 ${timeText}`
  return formatDate(date)
}

async function handleArticleCommand(row, command) {
  if (command === 'groups') {
    openGroupAssign(row)
  }
  if (command === 'edit') {
    router.push(`/creator/articles/edit/${row.id}`)
  }
  if (command === 'submit') {
    await submit(row.id)
  }
  if (command === 'withdraw') {
    await withdraw(row.id)
  }
  if (command === 'view') {
    router.push(`/article/${row.id}`)
  }
  if (command === 'visibility') {
    await toggleVisibility(row)
  }
  if (command === 'delete') {
    await remove(row.id)
  }
}

async function runRowAction(id, action, request) {
  if (isRowBusy(id)) return
  rowAction.value = { ...rowAction.value, [id]: action }
  try {
    await request()
    await loadGroups()
    await load()
  } finally {
    const next = { ...rowAction.value }
    delete next[id]
    rowAction.value = next
  }
}

async function submit(id) {
  await runRowAction(id, 'submit', async () => {
    await submitMyArticle(id)
    ElMessage.success('已提交审核')
  })
}

async function withdraw(id) {
  await runRowAction(id, 'withdraw', async () => {
    await withdrawMyArticle(id)
    ElMessage.success('已撤回为草稿')
  })
}

function nextVisibilityText(currentVisibility) {
  return nextArticleVisibility(currentVisibility) === 'private' ? '设为私密' : '设为公开'
}

async function toggleVisibility(row) {
  const nextVisibility = nextArticleVisibility(row.visibility)
  await runRowAction(row.id, 'visibility', async () => {
    await updateMyArticleVisibility(row.id, { visibility: nextVisibility })
    ElMessage.success(nextVisibility === 'private' ? '已设为仅自己可见' : '已设为公开')
  })
}

function openGroupAssign(article) {
  groupAssignDialog.value = {
    visible: true,
    article,
    selectedGroupId: getFirstArticleGroupId(article.groups)
  }
}

function closeGroupAssign() {
  if (groupAssignLoading.value) return
  groupAssignDialog.value = {
    visible: false,
    article: null,
    selectedGroupId: ''
  }
}

async function assignGroup() {
  const article = groupAssignDialog.value.article
  if (!article || groupAssignLoading.value) return
  groupAssignLoading.value = true
  try {
    await runRowAction(article.id, 'groups', async () => {
      await updateMyArticleGroups(article.id, {
        groupIds: buildArticleGroupIds(groupAssignDialog.value.selectedGroupId)
      })
      groupAssignDialog.value.visible = false
      ElMessage.success('分组已更新')
    })
  } finally {
    groupAssignLoading.value = false
    if (!groupAssignDialog.value.visible) {
      closeGroupAssign()
    }
  }
}

async function createGroup() {
  try {
    const prompt = await ElMessageBox.prompt('请输入分组名称', '新建分组', {
      confirmButtonText: '创建',
      cancelButtonText: '取消',
      inputPlaceholder: '例如：项目复盘',
      inputValidator: value => {
        if (!value || !value.trim()) return false
        return true
      },
      inputErrorMessage: '分组名称不能为空'
    })
    const result = await createArticleGroup({ name: prompt.value.trim() })
    await loadGroups()
    selectedGroup.value = createGroupFilterKey(result.data.id)
    page.value = 1
    await load()
    ElMessage.success('分组已创建')
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    throw error
  }
}

async function handleGroupCommand(group, command) {
  if (command === 'rename') {
    await renameGroup(group)
  }
  if (command === 'delete') {
    await removeGroup(group)
  }
}

async function renameGroup(group) {
  try {
    const prompt = await ElMessageBox.prompt('请输入新的分组名称', '重命名分组', {
      confirmButtonText: '保存',
      cancelButtonText: '取消',
      inputValue: group.name,
      inputValidator: value => {
        if (!value || !value.trim()) return false
        return true
      },
      inputErrorMessage: '分组名称不能为空'
    })
    await updateArticleGroup(group.id, { name: prompt.value.trim() })
    await loadGroups()
    ElMessage.success('分组已更新')
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    throw error
  }
}

async function removeGroup(group) {
  try {
    await ElMessageBox.confirm('删除分组后，分组内文章会变为未分组，文章本身不会被删除。', '删除分组', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteArticleGroup(group.id)
    if (selectedGroup.value === createGroupFilterKey(group.id)) {
      selectedGroup.value = GROUP_FILTER_UNGROUPED
      page.value = 1
    }
    await loadGroups()
    await load()
    ElMessage.success('分组已删除')
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    throw error
  }
}

async function remove(id) {
  await ElMessageBox.confirm('确定删除这篇文章吗？', '提示')
  await runRowAction(id, 'delete', async () => {
    await deleteMyArticle(id)
    ElMessage.success('删除成功')
  })
}
</script>

<style scoped>
.main {
  width: min(1180px, calc(100% - 36px));
  margin: 0 auto;
  padding: 22px 0 72px;
}

.creator-shell {
  position: relative;
  isolation: isolate;
}

.creator-shell::before {
  position: fixed;
  inset: 0;
  z-index: -1;
  background:
    linear-gradient(90deg, var(--theme-grid-x) 1px, transparent 1px),
    linear-gradient(180deg, var(--theme-grid-y) 1px, transparent 1px);
  background-size: 44px 44px;
  content: '';
  opacity: 0.52;
  pointer-events: none;
}

.page-head {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(280px, 360px);
  gap: 30px;
  align-items: end;
  margin-bottom: 26px;
  padding-top: 22px;
}

.head-copy {
  min-width: 0;
}

.eyebrow {
  display: inline-flex;
  margin: 0 0 12px;
  color: var(--primary-color);
  font-size: 12px;
  font-weight: 760;
  letter-spacing: 0;
}

.page-head h1 {
  max-width: 820px;
  margin: 0 0 18px;
  color: var(--text-color);
  font-family: Georgia, "Times New Roman", serif;
  font-size: clamp(42px, 7vw, 76px);
  font-weight: 500;
  line-height: 0.98;
}

.head-copy p {
  max-width: 660px;
  margin: 0;
  color: var(--muted-text-color);
  font-size: 17px;
  line-height: 1.85;
}

.head-meta {
  border-left: 1px solid var(--border-color);
  padding-left: 18px;
}

.meta-line {
  display: grid;
  grid-template-columns: 78px minmax(0, 1fr);
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid var(--soft-border-color);
}

.meta-line:last-child {
  border-bottom: 0;
}

.meta-label {
  color: var(--accent-color);
  font-size: 12px;
  font-weight: 760;
}

.meta-value {
  min-width: 0;
  overflow: hidden;
  color: var(--text-color);
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.creator-toolbar {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 16px;
  align-items: center;
  margin-bottom: 20px;
  padding: 14px 0;
  border-top: 1px solid var(--soft-border-color);
  border-bottom: 1px solid var(--soft-border-color);
}

.toolbar-title {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: 8px 12px;
  min-width: 0;
}

.toolbar-title strong {
  color: var(--text-color);
  font-size: 16px;
}

.toolbar-controls {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
}

.toolbar-controls .el-select {
  width: 174px;
}

.toolbar-controls :deep(.el-select__wrapper) {
  min-height: 40px;
  border-radius: 999px;
  background: color-mix(in srgb, var(--panel-bg) 94%, transparent);
}

.article-count {
  color: var(--muted-text-color);
  font-size: 13px;
}

.creator-workspace {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  gap: 24px;
  align-items: start;
}

.group-rail {
  position: sticky;
  top: calc(var(--app-header-height) + 20px);
  min-width: 0;
  padding-right: 18px;
  border-right: 1px solid var(--border-color);
}

.rail-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  color: var(--text-color);
  font-weight: 800;
}

.group-list {
  display: grid;
  gap: 6px;
  margin-top: 6px;
}

.group-item {
  position: relative;
  display: flex;
  overflow: hidden;
  width: 100%;
  min-height: 38px;
  align-items: center;
  gap: 8px;
  padding: 8px 10px 8px 12px;
  border: 1px solid transparent;
  border-radius: var(--radius-md);
  background: transparent;
  color: var(--muted-text-color);
  text-align: left;
  cursor: pointer;
  transition:
    transform 0.22s ease,
    border-color 0.22s ease,
    background-color 0.22s ease,
    color 0.22s ease;
}

.group-item::before {
  position: absolute;
  top: 9px;
  bottom: 9px;
  left: 0;
  width: 3px;
  border-radius: 999px;
  background: linear-gradient(180deg, var(--primary-color), var(--accent-color));
  content: '';
  opacity: 0;
  pointer-events: none;
  transform: scaleY(0.55);
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.group-item:hover,
.group-item.is-active {
  transform: translateX(2px);
  border-color: color-mix(in srgb, var(--primary-color) 30%, transparent);
  background: var(--surface-wash-color);
  color: var(--text-color);
}

.group-item:hover::before,
.group-item.is-active::before {
  opacity: 1;
  transform: scaleY(1);
}

.group-item-with-actions {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto auto;
}

.group-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.group-meta {
  min-width: 24px;
  padding: 1px 7px;
  border-radius: 999px;
  background: color-mix(in srgb, var(--muted-text-color) 12%, transparent);
  color: var(--muted-text-color);
  font-size: 12px;
  text-align: center;
}

.group-more {
  color: var(--muted-text-color);
  font-size: 12px;
}

.article-section {
  min-width: 0;
}

.table-wrap {
  min-width: 0;
  border-top: 1px solid var(--border-color);
}

.article-row {
  position: relative;
  display: grid;
  grid-template-columns:
    minmax(230px, 1.45fr)
    minmax(90px, 0.7fr)
    92px
    106px
    minmax(130px, 0.9fr)
    122px
    112px;
  gap: 14px;
  align-items: center;
  min-height: 82px;
  padding: 14px 0;
  border-bottom: 1px solid var(--soft-border-color);
  color: var(--muted-text-color);
  transition:
    transform 0.22s ease,
    border-color 0.22s ease,
    background-color 0.22s ease,
    box-shadow 0.22s ease;
}

.article-row::before {
  position: absolute;
  top: 14px;
  bottom: 14px;
  left: -1px;
  width: 3px;
  border-radius: 999px;
  background: linear-gradient(180deg, var(--primary-color), var(--accent-color));
  content: '';
  opacity: 0;
  transform: scaleY(0.55);
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.article-row:not(.table-head):hover {
  transform: translateX(3px);
  border-bottom-color: color-mix(in srgb, var(--primary-color) 62%, var(--border-color));
  background: color-mix(in srgb, var(--panel-bg) 84%, transparent);
  box-shadow: 0 18px 42px -34px var(--theme-glow-color);
}

.article-row:not(.table-head):hover::before {
  opacity: 1;
  transform: scaleY(1);
}

.table-head {
  min-height: 42px;
  color: var(--accent-color);
  font-size: 12px;
  font-weight: 800;
}

.table-head::before {
  display: none;
}

.row-title {
  min-width: 0;
}

.article-title-button {
  max-width: 100%;
  padding: 0;
  overflow: hidden;
  border: 0;
  background: transparent;
  color: var(--text-color);
  font: inherit;
  font-weight: 780;
  text-align: left;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: pointer;
}

.article-title-button:hover,
.article-title-button:focus-visible {
  color: var(--primary-color);
  text-decoration: underline;
  text-underline-offset: 3px;
}

.article-title-button:focus-visible,
.article-more-button:focus-visible,
.group-item:focus-visible {
  outline: 2px solid var(--primary-color);
  outline-offset: 2px;
}

.row-title p {
  display: -webkit-box;
  margin: 5px 0 0;
  overflow: hidden;
  color: var(--muted-text-color);
  font-size: 12px;
  line-height: 1.55;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
}

.group-cell,
.visibility-cell,
.review-cell,
.time-cell {
  min-width: 0;
  overflow: hidden;
  color: var(--muted-text-color);
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.group-cell {
  color: var(--text-color);
}

.group-cell.is-empty {
  color: var(--muted-text-color);
}

.status-pill {
  display: inline-flex;
  width: max-content;
  min-height: 28px;
  align-items: center;
  justify-content: center;
  padding: 4px 10px;
  border-radius: 999px;
  background: color-mix(in srgb, var(--muted-text-color) 12%, transparent);
  color: var(--muted-text-color);
  font-size: 12px;
  font-weight: 800;
  white-space: nowrap;
}

.status-pill[data-status="draft"] {
  background: color-mix(in srgb, var(--muted-text-color) 12%, transparent);
  color: var(--muted-text-color);
}

.status-pill[data-status="pending"] {
  background: color-mix(in srgb, var(--warning-color) 18%, transparent);
  color: var(--warning-color);
}

.status-pill[data-status="published"] {
  background: color-mix(in srgb, var(--primary-color) 14%, transparent);
  color: var(--primary-color);
}

.status-pill[data-status="rejected"] {
  background: color-mix(in srgb, var(--danger-color) 14%, transparent);
  color: var(--danger-color);
}

.article-actions {
  position: relative;
  z-index: 2;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  width: 92px;
  margin-left: auto;
  padding: 3px;
  border: 1px solid color-mix(in srgb, var(--primary-color) 12%, var(--soft-border-color));
  border-radius: 999px;
  background: color-mix(in srgb, var(--panel-bg) 92%, var(--primary-color));
  box-shadow: 0 8px 20px -18px rgba(15, 23, 42, 0.55);
}

.article-actions :deep(.el-button + .el-button) {
  margin-left: 0;
}

.article-primary-action {
  min-width: 50px;
  height: 28px;
  padding: 0 10px;
  border-color: var(--action-border-color);
  border-radius: 999px;
  background: var(--action-bg-color);
  color: var(--action-text-color);
  font-weight: 700;
}

.article-primary-action.article-primary-action--edit,
.article-primary-action.article-primary-action--edit:hover,
.article-primary-action.article-primary-action--edit:focus {
  --action-border-color: color-mix(in srgb, var(--accent-color) 54%, transparent);
  --action-bg-color: color-mix(in srgb, var(--accent-color) 13%, var(--panel-bg));
  --action-text-color: color-mix(in srgb, var(--accent-color) 58%, var(--text-color));
}

.article-primary-action.article-primary-action--view,
.article-primary-action.article-primary-action--view:hover,
.article-primary-action.article-primary-action--view:focus {
  --action-border-color: color-mix(in srgb, var(--primary-color) 52%, transparent);
  --action-bg-color: color-mix(in srgb, var(--primary-color) 12%, var(--panel-bg));
  --action-text-color: color-mix(in srgb, var(--primary-color) 60%, var(--text-color));
}

.article-primary-action.article-primary-action--withdraw,
.article-primary-action.article-primary-action--withdraw:hover,
.article-primary-action.article-primary-action--withdraw:focus {
  --action-border-color: color-mix(in srgb, var(--warning-color) 52%, transparent);
  --action-bg-color: color-mix(in srgb, var(--warning-color) 14%, var(--panel-bg));
  --action-text-color: color-mix(in srgb, var(--warning-color) 55%, var(--text-color));
}

.article-primary-action:not(.is-disabled):hover {
  box-shadow: 0 8px 18px -14px currentColor;
  transform: translateY(-1px);
}

[data-theme="dark"] .article-primary-action.article-primary-action--edit,
[data-theme="dark"] .article-primary-action.article-primary-action--edit:hover,
[data-theme="dark"] .article-primary-action.article-primary-action--edit:focus,
[data-theme="dark"] .article-primary-action.article-primary-action--view,
[data-theme="dark"] .article-primary-action.article-primary-action--view:hover,
[data-theme="dark"] .article-primary-action.article-primary-action--view:focus,
[data-theme="dark"] .article-primary-action.article-primary-action--withdraw,
[data-theme="dark"] .article-primary-action.article-primary-action--withdraw:hover,
[data-theme="dark"] .article-primary-action.article-primary-action--withdraw:focus {
  --action-bg-color: color-mix(in srgb, currentColor 18%, var(--panel-bg));
}

.article-more-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  padding: 0;
  border: 0;
  border-radius: 999px;
  background: color-mix(in srgb, var(--muted-text-color) 10%, transparent);
  color: var(--muted-text-color);
  cursor: pointer;
  transition: background-color 0.2s ease, color 0.2s ease, transform 0.2s ease;
}

.article-more-button:hover,
.article-more-button:focus-visible {
  background: color-mix(in srgb, var(--primary-color) 16%, transparent);
  color: var(--primary-color);
  outline: none;
  transform: translateY(-1px);
}

.article-more-button:disabled {
  cursor: not-allowed;
  opacity: 0.55;
  transform: none;
}

:global(.article-action-menu) {
  min-width: 136px !important;
  padding: 6px !important;
  border: 1px solid color-mix(in srgb, var(--primary-color) 12%, var(--soft-border-color)) !important;
  border-radius: 14px !important;
  background: color-mix(in srgb, var(--panel-bg) 96%, var(--primary-color)) !important;
  box-shadow: 0 18px 42px -24px rgba(15, 23, 42, 0.55) !important;
}

:global(.article-action-menu .el-dropdown-menu__item) {
  height: 34px;
  border-radius: 10px;
  color: var(--text-color);
  font-size: 13px;
  line-height: 34px;
}

:global(.article-action-menu .el-dropdown-menu__item:not(.is-disabled):hover) {
  background: color-mix(in srgb, var(--primary-color) 12%, transparent);
  color: var(--primary-color);
}

:global(.article-action-menu .danger-command) {
  color: var(--danger-color, #f56c6c);
}

:global(.article-action-menu .danger-command:not(.is-disabled):hover) {
  background: color-mix(in srgb, #f56c6c 12%, transparent);
  color: #f56c6c;
}

.article-pagination {
  margin-top: 18px;
}

.group-assign-intro {
  margin: 0 0 14px;
  color: var(--muted-text-color);
  font-size: 14px;
  line-height: 1.7;
}

.group-assign-select {
  width: 100%;
}

@media (max-width: 1060px) {
  .creator-toolbar,
  .creator-workspace,
  .page-head {
    grid-template-columns: 1fr;
  }

  .head-meta {
    border-left: 0;
    border-top: 1px solid var(--border-color);
    padding-top: 12px;
    padding-left: 0;
  }

  .group-rail {
    position: static;
    padding-right: 0;
    padding-bottom: 16px;
    border-right: 0;
    border-bottom: 1px solid var(--border-color);
  }

  .toolbar-controls {
    justify-content: flex-start;
  }
}

@media (max-width: 900px) {
  .article-row {
    grid-template-columns: 1fr 1fr;
  }

  .table-head {
    display: none;
  }

  .article-actions {
    margin-left: 0;
  }
}

@media (max-width: 640px) {
  .main {
    width: min(100% - 28px, var(--content-width));
    padding: 16px 0 48px;
  }

  .page-head h1 {
    font-size: 42px;
  }

  .toolbar-controls,
  .toolbar-controls .el-select {
    width: 100%;
  }

  .toolbar-controls {
    display: grid;
    grid-template-columns: 1fr;
  }

  .article-row {
    grid-template-columns: 1fr;
    gap: 8px;
    padding: 16px 0;
  }
}

@media (prefers-reduced-motion: reduce) {
  .group-item,
  .group-item::before,
  .article-row,
  .article-row::before,
  .article-primary-action,
  .article-more-button {
    transition: none;
  }

  .group-item:hover,
  .article-row:not(.table-head):hover,
  .article-primary-action:not(.is-disabled):hover,
  .article-more-button:hover,
  .article-more-button:focus-visible {
    transform: none;
  }
}

/* Prototype-aligned creator console */
.page-head {
  gap: 42px;
  align-items: center;
  margin-bottom: 30px;
  padding-top: 30px;
}

.page-head h1 {
  max-width: 760px;
  font-size: clamp(54px, 6.2vw, 88px);
  line-height: 0.94;
}

.head-copy p {
  max-width: 760px;
}

.head-meta {
  align-self: end;
  width: min(100%, 360px);
  margin-bottom: 4px;
  padding: 14px 18px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background: color-mix(in srgb, var(--panel-bg) 94%, transparent);
  box-shadow: var(--shadow-soft);
}

.meta-line {
  grid-template-columns: minmax(72px, 1fr) auto;
  gap: 20px;
  padding: 8px 0;
  border-bottom: 0;
}

.meta-label {
  color: var(--muted-text-color);
  font-size: 13px;
  font-weight: 520;
}

.meta-value {
  color: var(--text-color);
  font-size: 16px;
  font-weight: 760;
  text-align: right;
}

.creator-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 22px;
  padding: 0;
  border: 0;
}

.searchbox {
  position: relative;
  flex: 1;
  min-width: 240px;
}

.searchbox input {
  width: 100%;
  height: 42px;
  padding: 0 18px 0 42px;
  border: 1px solid var(--border-color);
  border-radius: 999px;
  background: color-mix(in srgb, var(--panel-bg) 94%, transparent);
  color: var(--text-color);
  font-size: 15px;
  outline: none;
  transition:
    border-color 0.22s ease,
    background-color 0.22s ease,
    box-shadow 0.22s ease;
}

.searchbox input:focus {
  border-color: color-mix(in srgb, var(--primary-color) 62%, var(--border-color));
  background: var(--panel-bg);
  box-shadow: 0 0 0 4px color-mix(in srgb, var(--primary-color) 13%, transparent);
}

.searchbox::before {
  position: absolute;
  top: 50%;
  left: 17px;
  z-index: 1;
  color: var(--muted-text-color);
  content: '⌕';
  transform: translateY(-50%);
}

.searchbox::after {
  position: absolute;
  right: 18px;
  bottom: 7px;
  left: 42px;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--primary-color), transparent);
  content: '';
  opacity: 0;
  transform: scaleX(0.35);
  transition:
    opacity 0.22s ease,
    transform 0.22s ease;
}

.searchbox:focus-within::after {
  opacity: 1;
  transform: scaleX(1);
}

.status-filters {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}

.chip {
  min-height: 34px;
  padding: 0 15px;
  border: 1px solid var(--border-color);
  border-radius: 999px;
  background: color-mix(in srgb, var(--panel-bg) 88%, transparent);
  color: var(--muted-text-color);
  font-size: 14px;
  cursor: pointer;
  transition:
    border-color 0.22s ease,
    background-color 0.22s ease,
    color 0.22s ease,
    transform 0.22s ease;
}

.chip:hover,
.chip:focus-visible,
.chip.is-active {
  border-color: color-mix(in srgb, var(--primary-color) 58%, var(--border-color));
  background: color-mix(in srgb, var(--primary-color) 10%, var(--panel-bg));
  color: var(--primary-color);
  outline: none;
}

.chip.is-active {
  box-shadow: inset 0 0 0 1px color-mix(in srgb, var(--primary-color) 12%, transparent);
}

.visibility-filter {
  width: 148px;
}

.visibility-filter :deep(.el-select__wrapper) {
  min-height: 34px;
  border-radius: 999px;
  background: color-mix(in srgb, var(--panel-bg) 90%, transparent);
  box-shadow: 0 0 0 1px var(--border-color) inset;
}

.primary-button {
  min-height: 34px;
  border-radius: 999px;
  background: var(--primary-color);
  border-color: var(--primary-color);
  color: var(--button-text-color);
  font-weight: 760;
}

.creator-workspace {
  grid-template-columns: 230px minmax(0, 1fr);
  gap: 20px;
}

.group-rail,
.article-flow-panel {
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background: color-mix(in srgb, var(--panel-bg) 94%, transparent);
  box-shadow: var(--shadow-soft);
}

.group-rail {
  padding: 16px;
  border-right: 1px solid var(--border-color);
}

.rail-head {
  margin-bottom: 14px;
}

.mini-button {
  border: 1px solid var(--border-color);
  border-radius: 999px;
  color: var(--muted-text-color);
}

.group-list {
  gap: 9px;
  margin-top: 0;
}

.group-item {
  min-height: 38px;
  justify-content: space-between;
  padding: 8px 10px;
  border-color: var(--border-color);
  border-radius: var(--radius-md);
  background: color-mix(in srgb, var(--panel-bg) 84%, transparent);
}

.group-item::before {
  top: 8px;
  bottom: 8px;
  left: -1px;
  background: linear-gradient(180deg, var(--primary-color), var(--accent-color));
}

.group-item:hover,
.group-item.is-active {
  transform: none;
  border-color: color-mix(in srgb, var(--primary-color) 58%, var(--border-color));
  background: color-mix(in srgb, var(--primary-color) 8%, var(--panel-bg));
}

.group-item-with-actions {
  grid-template-columns: minmax(0, 1fr) auto auto;
}

.group-meta {
  background: color-mix(in srgb, var(--muted-text-color) 11%, transparent);
}

.article-flow-panel {
  min-width: 0;
  padding: 10px 12px;
}

.creator-list {
  display: grid;
  gap: 12px;
}

.creator-article-card {
  position: relative;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 16px;
  align-items: center;
  min-height: 106px;
  padding: 17px 16px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background: color-mix(in srgb, var(--panel-bg) 96%, transparent);
  transition:
    transform 0.22s ease,
    border-color 0.22s ease,
    background-color 0.22s ease,
    box-shadow 0.22s ease;
}

.creator-article-card::before {
  position: absolute;
  top: 14px;
  bottom: 14px;
  left: -1px;
  width: 3px;
  border-radius: 999px;
  background: linear-gradient(180deg, var(--primary-color), var(--accent-color));
  content: '';
  opacity: 0;
  transform: scaleY(0.55);
  transition:
    opacity 0.22s ease,
    transform 0.22s ease;
}

.creator-article-card:not(.is-empty-card):hover,
.creator-article-card.is-selected {
  border-color: color-mix(in srgb, var(--primary-color) 58%, var(--border-color));
  background: var(--panel-bg);
  box-shadow: var(--shadow-soft);
  transform: translateY(-2px);
}

.creator-article-card:not(.is-empty-card):hover::before,
.creator-article-card.is-selected::before {
  opacity: 1;
  transform: scaleY(1);
}

.row-copy {
  min-width: 0;
}

.row-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 14px;
  color: var(--muted-text-color);
  font-size: 13px;
}

.creator-article-card h3 {
  margin: 6px 0 4px;
  color: var(--text-color);
  font-size: 20px;
  line-height: 1.35;
}

.creator-article-card p {
  display: -webkit-box;
  margin: 0;
  overflow: hidden;
  color: var(--muted-text-color);
  font-size: 14px;
  line-height: 1.7;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.article-title-button {
  white-space: normal;
}

.row-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
}

.status-pill {
  min-height: 28px;
  border-radius: 999px;
}

.ghost-button,
.preview-action {
  min-height: 32px;
  padding: 0 13px;
  border: 1px solid var(--border-color);
  border-radius: 999px;
  background: color-mix(in srgb, var(--panel-bg) 90%, transparent);
  color: var(--muted-text-color);
  font-weight: 650;
}

.ghost-button:hover,
.ghost-button:focus {
  border-color: color-mix(in srgb, var(--primary-color) 42%, var(--border-color));
  background: color-mix(in srgb, var(--primary-color) 9%, var(--panel-bg));
  color: var(--primary-color);
}

.article-more-button {
  border: 1px solid color-mix(in srgb, var(--primary-color) 24%, var(--border-color));
  background: color-mix(in srgb, var(--panel-bg) 92%, transparent);
}

.article-more-button:hover,
.article-more-button:focus-visible {
  background: color-mix(in srgb, var(--primary-color) 12%, var(--panel-bg));
}

.article-pagination {
  justify-content: flex-end;
  margin-top: 18px;
}

@media (max-width: 1060px) {
  .page-head,
  .creator-workspace {
    grid-template-columns: 1fr;
  }

  .head-meta {
    width: 100%;
    border-left: 1px solid var(--border-color);
    border-top: 1px solid var(--border-color);
    padding: 14px 18px;
  }

  .group-rail {
    position: static;
    padding: 16px;
    border-right: 1px solid var(--border-color);
    border-bottom: 1px solid var(--border-color);
  }
}

@media (max-width: 780px) {
  .creator-toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .status-filters {
    justify-content: flex-start;
  }

  .visibility-filter {
    width: 100%;
  }

  .creator-article-card {
    grid-template-columns: 1fr;
  }

  .row-actions {
    justify-content: flex-start;
  }
}

@media (max-width: 640px) {
  .page-head h1 {
    font-size: 42px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .searchbox input,
  .searchbox::after,
  .chip,
  .creator-article-card,
  .creator-article-card::before {
    transition: none;
  }

  .creator-article-card:not(.is-empty-card):hover,
  .creator-article-card.is-selected {
    transform: none;
  }
}
</style>
