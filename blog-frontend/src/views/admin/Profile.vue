<template>
  <div class="admin-page">
    <h2>个人资料</h2>

    <el-descriptions :column="1" border class="profile-descriptions">
      <el-descriptions-item label="用户名">{{ user?.username }}</el-descriptions-item>
      <el-descriptions-item label="昵称">{{ user?.nickname }}</el-descriptions-item>
      <el-descriptions-item label="邮箱">{{ user?.email || '-' }}</el-descriptions-item>
      <el-descriptions-item label="角色">{{ user?.role }}</el-descriptions-item>
      <el-descriptions-item label="状态">
        <el-tag :type="user?.status === 'active' ? 'success' : 'warning'">{{ user?.status }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="注册时间">{{ formatDate(user?.createdAt) }}</el-descriptions-item>
    </el-descriptions>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useAuthStore } from '../../stores/auth'
import { formatDate } from '../../utils'

const authStore = useAuthStore()
const user = computed(() => authStore.user)
</script>

<style scoped>
.profile-descriptions {
  margin-top: 16px;
}
</style>
