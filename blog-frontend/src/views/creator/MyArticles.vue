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
          </div>

          <el-table v-loading="loading" :data="articles" stripe class="article-table">
            <el-table-column prop="title" label="标题" min-width="220" />
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
            <el-table-column prop="reviewReason" label="审核反馈" min-width="180">
              <template #default="{ row }">{{ row.reviewReason || '-' }}</template>
            </el-table-column>
            <el-table-column label="更新时间" width="180">
              <template #default="{ row }">{{ formatDate(row.updatedAt || row.createdAt) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="380">
              <template #default="{ row }">
                <el-button
                  size="small"
                  :loading="rowAction[row.id] === 'groups'"
                  :disabled="isRowBusy(row.id)"
                  @click="openGroupAssign(row)"
                >
                  分组
                </el-button>
                <el-button
                  v-if="row.status !== 'pending'"
                  size="small"
                  :disabled="isRowBusy(row.id)"
                  @click="$router.push(`/creator/articles/edit/${row.id}`)"
                >
                  编辑
                </el-button>
                <el-button
                  v-if="row.status === 'draft' || row.status === 'rejected'"
                  size="small"
                  type="success"
                  :loading="rowAction[row.id] === 'submit'"
                  :disabled="isRowBusy(row.id)"
                  @click="submit(row.id)"
                >
                  提交审核
                </el-button>
                <el-button
                  v-if="row.status === 'pending'"
                  size="small"
                  :loading="rowAction[row.id] === 'withdraw'"
                  :disabled="isRowBusy(row.id)"
                  @click="withdraw(row.id)"
                >
                  撤回
                </el-button>
                <el-button
                  v-if="row.status === 'published'"
                  size="small"
                  :disabled="isRowBusy(row.id)"
                  @click="$router.push(`/article/${row.id}`)"
                >
                  查看已发布
                </el-button>
                <el-button
                  size="small"
                  type="danger"
                  :loading="rowAction[row.id] === 'delete'"
                  :disabled="isRowBusy(row.id)"
                  @click="remove(row.id)"
                >
                  删除
                </el-button>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import AppHeader from '../../components/AppHeader.vue'
import { createArticleGroup, deleteArticleGroup, getArticleGroups, updateArticleGroup } from '../../api/articleGroup'
import { deleteMyArticle, getMyArticles, submitMyArticle, updateMyArticleGroups, withdrawMyArticle } from '../../api/myArticle'
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
const selectedGroup = ref(GROUP_FILTER_ALL)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const rowAction = ref({})
const groupAssignLoading = ref(false)
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

function selectGroup(groupKey) {
  selectedGroup.value = groupKey
  page.value = 1
  load()
}

function isRowBusy(id) {
  return !!rowAction.value[id]
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
