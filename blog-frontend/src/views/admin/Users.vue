<template>
  <div class="users-page">
    <div class="page-head">
      <div>
        <span class="page-eyebrow">账号审核</span>
        <h2>用户管理</h2>
      </div>
    </div>

    <section class="surface" v-loading="loading">
      <div class="panel-head">
        <div>
          <span class="section-eyebrow">用户管理</span>
          <div class="help-heading">
            <h3>账号状态</h3>
            <span class="help-popover" :class="{ 'is-open': helpOpen }">
              <button
                class="help-trigger"
                type="button"
                aria-label="查看账号处理规则"
                :aria-expanded="helpOpen"
                @click="helpOpen = !helpOpen"
              >
                ?
              </button>
              <span class="help-card" role="tooltip">
                通过后用户可登录创作中心和个人阅读页。禁用只停用账号，不清理历史内容。
              </span>
            </span>
          </div>
        </div>
        <span class="chip" :class="{ 'is-warning': pendingCount > 0, 'is-success': pendingCount === 0 }">
          本页 {{ pendingCount }} 个待审核
        </span>
      </div>

      <div v-if="users.length" class="row-list">
        <article v-for="user in users" :key="user.id" class="data-row">
          <div class="row-title">
            <span>{{ user.nickname || user.username }}</span>
            <span class="chip" :class="userStatusClass(user.status)">{{ userStatusText(user.status) }}</span>
          </div>
          <div class="row-meta">
            {{ user.username }} / {{ user.email || '未填写邮箱' }} / {{ user.role || 'user' }}
          </div>
          <div class="row-actions">
            <button
              v-if="user.status === 'pending'"
              class="row-action-button is-success"
              type="button"
              @click="handleApprove(user.id)"
            >
              通过
            </button>
            <button
              v-if="user.status === 'active' && !isAdminUser(user)"
              class="row-action-button is-warning"
              type="button"
              @click="handleDisable(user.id)"
            >
              禁用
            </button>
            <span v-else-if="user.status === 'active'" class="row-meta">管理员账号</span>
          </div>
        </article>
      </div>
      <div v-else class="empty-state">暂无用户</div>
    </section>

    <el-pagination
      v-if="showPagination"
      v-model:current-page="page"
      :total="total"
      :page-size="size"
      layout="prev,pager,next"
      class="admin-pagination"
      @current-change="handlePageChange"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { approveUser, disableUser, getUsers } from '../../api/user'
import { normalizePageResult, shouldShowPagination } from '../../utils/pagination'

const users = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const loading = ref(false)
const helpOpen = ref(false)

const pendingCount = computed(() => users.value.filter(user => user.status === 'pending').length)
const showPagination = computed(() => shouldShowPagination(total.value, size.value))

onMounted(loadUsers)

function isAdminUser(user) {
  return user.role?.toLowerCase() === 'admin'
}

function userStatusText(status) {
  if (status === 'active') return '已启用'
  if (status === 'pending') return '待审核'
  if (status === 'disabled') return '已禁用'
  return status || '-'
}

function userStatusClass(status) {
  if (status === 'active') return 'is-success'
  if (status === 'pending') return 'is-warning'
  if (status === 'disabled') return 'is-danger'
  return ''
}

async function loadUsers() {
  loading.value = true
  try {
    const result = await getUsers({ page: page.value, size: size.value })
    const pageResult = normalizePageResult(result, size.value)
    const maxPage = Math.max(1, Math.ceil(pageResult.total / size.value))
    if (page.value > maxPage) {
      page.value = maxPage
      await loadUsers()
      return
    }
    users.value = pageResult.records
    total.value = pageResult.total
  } finally {
    loading.value = false
  }
}

function handlePageChange(nextPage) {
  page.value = nextPage
  void loadUsers()
}

async function handleApprove(id) {
  await approveUser(id)
  ElMessage.success('已通过')
  await loadUsers()
}

async function handleDisable(id) {
  await disableUser(id)
  ElMessage.success('已禁用')
  await loadUsers()
}
</script>

<style scoped>
.users-page {
  display: grid;
  gap: 16px;
}

.row-list .data-row {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(0, 1.4fr) auto;
  align-items: center;
}

.row-list .row-actions {
  justify-content: flex-end;
}

@media (max-width: 780px) {
  .row-list .data-row {
    grid-template-columns: 1fr;
  }

  .row-list .row-actions {
    justify-content: flex-start;
  }
}
</style>
