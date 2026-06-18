<template>
  <div class="admin-page">
    <h2>用户管理</h2>

    <el-table :data="users" stripe class="admin-table">
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="role" label="角色" width="100">
        <template #default="{ row }">
          <el-tag :type="row.role === 'admin' ? 'danger' : 'info'">{{ row.role }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'active' ? 'success' : row.status === 'pending' ? 'warning' : 'info'">
            {{ row.status === 'active' ? '已激活' : row.status === 'pending' ? '待审核' : '已禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button v-if="row.status === 'pending'" size="small" type="success" @click="handleApprove(row.id)">
            审核通过
          </el-button>
          <el-button
            v-if="row.status === 'active' && !isAdminUser(row)"
            size="small"
            type="warning"
            @click="handleDisable(row.id)"
          >
            禁用
          </el-button>
          <el-text v-else-if="row.status === 'active'" type="info" size="small">不可禁用</el-text>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getUsers, approveUser, disableUser } from '../../api/user'
import { ElMessage } from 'element-plus'

const users = ref([])

function isAdminUser(user) {
  return user.role?.toLowerCase() === 'admin'
}

onMounted(async () => {
  const r = await getUsers({ size: 100 })
  users.value = r.data || []
})

async function handleApprove(id) {
  await approveUser(id)
  ElMessage.success('已通过')
  const r = await getUsers({ size: 100 })
  users.value = r.data || []
}

async function handleDisable(id) {
  await disableUser(id)
  ElMessage.success('已禁用')
  const r = await getUsers({ size: 100 })
  users.value = r.data || []
}
</script>

<style scoped>
.admin-table {
  margin-top: 16px;
}
</style>
