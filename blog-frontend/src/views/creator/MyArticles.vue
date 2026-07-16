<template>
  <div class="layout">
    <AppHeader />
    <el-main class="main">
      <div class="page-header">
        <div>
          <span class="page-kicker">创作中心</span>
          <h1>我的文章</h1>
        </div>
        <el-button type="primary" @click="$router.push('/creator/articles/create')">写文章</el-button>
      </div>

      <div class="workspace">
        <aside v-loading="groupsLoading" class="group-panel">
          <div class="group-panel-head">
            <span>文章分组</span>
            <el-button size="small" text @click="createGroup">新建</el-button>
          </div>
          <button
            type="button"
            class="group-item"
            :class="{ 'is-active': selectedGroup === GROUP_FILTER_ALL }"
            @click="selectGroup(GROUP_FILTER_ALL)"
          >
            <span>全部文章</span>
          </button>
          <button
            type="button"
            class="group-item"
            :class="{ 'is-active': selectedGroup === GROUP_FILTER_UNGROUPED }"
            @click="selectGroup(GROUP_FILTER_UNGROUPED)"
          >
            <span>未分组</span>
          </button>
          <div class="group-list">
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
          <div class="filters">
            <div>
              <strong>{{ activeGroupTitle }}</strong>
              <span class="article-count">共 {{ total }} 篇</span>
            </div>
            <el-select v-model="status" placeholder="全部状态" clearable @change="handleStatusChange">
              <el-option label="草稿" value="draft" />
              <el-option label="待审核" value="pending" />
              <el-option label="已发布" value="published" />
              <el-option label="已驳回" value="rejected" />
            </el-select>
            <el-select v-model="visibility" placeholder="全部可见性" clearable @change="handleVisibilityChange">
              <el-option label="公开" value="public" />
              <el-option label="仅自己可见" value="private" />
            </el-select>
          </div>

          <el-table v-loading="loading" :data="articles" stripe class="article-table">
            <el-table-column prop="title" label="标题" min-width="220">
              <template #default="{ row }">
                <button type="button" class="article-title-button" @click="openPreview(row)">
                  {{ row.title }}
                </button>
              </template>
            </el-table-column>
            <el-table-column label="分组" min-width="130">
              <template #default="{ row }">
                <span class="group-cell" :class="{ 'is-empty': !row.groups?.length }">
                  {{ formatArticleGroupNames(row.groups || []) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="110">
              <template #default="{ row }">
                <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="visibility" label="可见性" width="130">
              <template #default="{ row }">
                <el-tag :type="articleVisibilityType(row.visibility)" effect="plain">
                  {{ articleVisibilityText(row.visibility) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="reviewReason" label="审核反馈" min-width="180">
              <template #default="{ row }">{{ row.reviewReason || '-' }}</template>
            </el-table-column>
            <el-table-column label="更新时间" width="180">
              <template #default="{ row }">{{ formatDate(row.updatedAt || row.createdAt) }}</template>
            </el-table-column>
            <el-table-column
              label="操作"
              width="124"
              fixed="right"
              class-name="article-action-column"
              header-class-name="article-action-header"
            >
              <template #default="{ row }">
                <div class="article-actions">
                  <el-button
                    size="small"
                    class="article-primary-action"
                    :class="`article-primary-action--${primaryAction(row).tone}`"
                    :loading="rowAction[row.id] === primaryAction(row).loading"
                    :disabled="isRowBusy(row.id)"
                    @click="handlePrimaryAction(row)"
                  >
                    {{ primaryAction(row).text }}
                  </el-button>
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
                        <el-dropdown-item v-if="row.status === 'published'" command="edit">编辑</el-dropdown-item>
                        <el-dropdown-item
                          v-if="row.status === 'draft' || row.status === 'rejected'"
                          command="submit"
                        >
                          提交审核
                        </el-dropdown-item>
                        <el-dropdown-item command="visibility">
                          {{ nextVisibilityText(row.visibility) }}
                        </el-dropdown-item>
                        <el-dropdown-item command="delete" class="danger-command">删除</el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
              </template>
            </el-table-column>
          </el-table>

          <el-empty v-if="!articles.length && !loading" description="暂无文章" />
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
      </div>

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
  articleVisibilityType,
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

const activeGroupTitle = computed(() => {
  const parsed = parseGroupFilterKey(selectedGroup.value)
  if (parsed.type === GROUP_FILTER_UNGROUPED) return '未分组'
  if (parsed.type === 'group') {
    return articleGroups.value.find(group => group.id === parsed.groupId)?.name || '文章分组'
  }
  return '全部文章'
})

onMounted(() => {
  loadGroups()
  load()
})

function statusText(value) {
  return statusMap[value]?.text || value
}

function statusType(value) {
  return statusMap[value]?.type || 'info'
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

function primaryAction(row) {
  if (row.status === 'pending') {
    return { text: '撤回', tone: 'withdraw', loading: 'withdraw', command: 'withdraw' }
  }
  if (canOpenPublicArticle(row)) {
    return { text: '查看', tone: 'view', loading: '', command: 'view' }
  }
  return { text: '编辑', tone: 'edit', loading: '', command: 'edit' }
}

function canOpenPublicArticle(row) {
  return row.status === 'published' && normalizeArticleVisibility(row.visibility) === ARTICLE_VISIBILITY_PUBLIC
}

function openPreview(row) {
  const target = getCreatorPreviewRoute(row)
  if (target) router.push(target)
}

async function handlePrimaryAction(row) {
  await handleArticleCommand(row, primaryAction(row).command)
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
  width: min(100%, var(--content-width));
  margin: 0 auto;
  padding: 32px 24px 64px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
  margin-bottom: 18px;
}

.page-kicker {
  color: var(--accent-color);
  font-size: 12px;
  font-weight: 800;
}

.page-header h1 {
  margin: 4px 0 0;
  color: var(--text-color);
  font-size: 30px;
}

.filters {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  margin-bottom: 16px;
}

.filters .el-select {
  width: 180px;
}

.workspace {
  display: grid;
  grid-template-columns: 248px minmax(0, 1fr);
  gap: 18px;
  align-items: start;
}

.group-panel {
  position: sticky;
  top: calc(var(--app-header-height) + 16px);
  padding: 14px;
  border: 1px solid var(--soft-border-color);
  border-radius: var(--radius-md);
  background: var(--panel-bg);
}

.group-panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
  color: var(--text-color);
  font-weight: 800;
}

.group-list {
  display: grid;
  gap: 4px;
  margin-top: 6px;
}

.group-item {
  display: flex;
  width: 100%;
  min-height: 38px;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  border: 0;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--muted-text-color);
  text-align: left;
  cursor: pointer;
}

.group-item:hover,
.group-item.is-active {
  background: color-mix(in srgb, var(--primary-color) 10%, var(--panel-bg));
  color: var(--text-color);
}

.group-item.is-active {
  box-shadow: inset 3px 0 0 var(--accent-color);
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

.article-count {
  margin-left: 8px;
  color: var(--muted-text-color);
  font-size: 13px;
}

.article-table {
  margin-top: 12px;
}

.article-title-button {
  max-width: 100%;
  padding: 0;
  border: 0;
  background: transparent;
  color: var(--text-color);
  font: inherit;
  font-weight: 750;
  text-align: left;
  cursor: pointer;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.article-title-button:hover,
.article-title-button:focus-visible {
  color: var(--primary-color);
  text-decoration: underline;
  text-underline-offset: 3px;
}

.article-title-button:focus-visible {
  outline: 2px solid var(--primary-color);
  outline-offset: 2px;
}

.article-table :deep(th.el-table__cell) {
  text-align: center;
}

.article-table :deep(.article-action-header.el-table-fixed-column--right) {
  background-color: color-mix(in srgb, var(--primary-color) 6%, var(--panel-bg)) !important;
  box-shadow: -12px 0 22px -20px rgba(15, 23, 42, 0.5);
  z-index: 2;
}

.article-table :deep(.article-action-column.el-table-fixed-column--right) {
  background-color: var(--el-table-tr-bg-color) !important;
  box-shadow: -12px 0 22px -20px rgba(15, 23, 42, 0.5);
  z-index: 2;
}

.article-table :deep(.el-table__body tr.hover-row > .article-action-column.el-table-fixed-column--right),
.article-table :deep(.el-table__body tr:hover > .article-action-column.el-table-fixed-column--right) {
  background-color: color-mix(in srgb, var(--primary-color) 10%, var(--panel-bg)) !important;
}

.article-table :deep(.article-action-header),
.article-table :deep(.article-action-column) {
  border-left: 1px solid var(--soft-border-color);
}

.article-actions {
  position: relative;
  z-index: 2;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  width: 92px;
  margin: 0 auto;
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
  --action-border-color: color-mix(in srgb, #7c5cff 54%, transparent);
  --action-bg-color: color-mix(in srgb, #7c5cff 13%, var(--panel-bg));
  --action-text-color: #5d42d6;
}

.article-primary-action.article-primary-action--view,
.article-primary-action.article-primary-action--view:hover,
.article-primary-action.article-primary-action--view:focus {
  --action-border-color: color-mix(in srgb, #2f80ed 52%, transparent);
  --action-bg-color: color-mix(in srgb, #2f80ed 12%, var(--panel-bg));
  --action-text-color: #1f6fd8;
}

.article-primary-action.article-primary-action--withdraw,
.article-primary-action.article-primary-action--withdraw:hover,
.article-primary-action.article-primary-action--withdraw:focus {
  --action-border-color: color-mix(in srgb, #d97706 52%, transparent);
  --action-bg-color: color-mix(in srgb, #f59e0b 14%, var(--panel-bg));
  --action-text-color: #a85f00;
}

.article-primary-action:not(.is-disabled):hover {
  box-shadow: 0 8px 18px -14px currentColor;
  transform: translateY(-1px);
}

[data-theme="dark"] .article-primary-action.article-primary-action--edit,
[data-theme="dark"] .article-primary-action.article-primary-action--edit:hover,
[data-theme="dark"] .article-primary-action.article-primary-action--edit:focus {
  --action-border-color: color-mix(in srgb, #8b7cf6 58%, transparent);
  --action-bg-color: color-mix(in srgb, #8b7cf6 22%, var(--panel-bg));
  --action-text-color: #d9d5ff;
}

[data-theme="dark"] .article-primary-action.article-primary-action--view,
[data-theme="dark"] .article-primary-action.article-primary-action--view:hover,
[data-theme="dark"] .article-primary-action.article-primary-action--view:focus {
  --action-border-color: color-mix(in srgb, #4ea2ff 58%, transparent);
  --action-bg-color: color-mix(in srgb, #4ea2ff 22%, var(--panel-bg));
  --action-text-color: #d7ebff;
}

[data-theme="dark"] .article-primary-action.article-primary-action--withdraw,
[data-theme="dark"] .article-primary-action.article-primary-action--withdraw:hover,
[data-theme="dark"] .article-primary-action.article-primary-action--withdraw:focus {
  --action-border-color: color-mix(in srgb, #f2a64a 58%, transparent);
  --action-bg-color: color-mix(in srgb, #f2a64a 22%, var(--panel-bg));
  --action-text-color: #ffe6ba;
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
  transition: background 0.2s ease, color 0.2s ease, transform 0.2s ease;
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

.group-cell {
  color: var(--text-color);
}

.group-cell.is-empty {
  color: var(--muted-text-color);
}

.article-pagination {
  margin-top: 16px;
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

@media (max-width: 760px) {
  .main {
    padding: 24px 14px 48px;
  }

  .page-header {
    align-items: stretch;
    flex-direction: column;
  }

  .workspace {
    grid-template-columns: 1fr;
  }

  .group-panel {
    position: static;
  }

  .filters {
    align-items: stretch;
    flex-direction: column;
  }

  .filters .el-select {
    width: 100%;
  }
}
</style>
