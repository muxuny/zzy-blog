<template>
  <el-container class="admin-layout">
    <el-aside width="220px" class="admin-sidebar">
      <div class="admin-logo">Blog Admin</div>
      <el-menu :router="true" :default-active="route.path">
        <el-menu-item index="/admin/dashboard"><el-icon><DataAnalysis /></el-icon><span>仪表盘</span></el-menu-item>
        <el-menu-item index="/admin/articles"><el-icon><Document /></el-icon><span>文章管理</span></el-menu-item>
        <el-menu-item index="/admin/tags"><el-icon><PriceTag /></el-icon><span>标签管理</span></el-menu-item>
        <el-menu-item index="/admin/images"><el-icon><Picture /></el-icon><span>图片管理</span></el-menu-item>
        <el-menu-item index="/admin/users" v-if="authStore.isAdmin"><el-icon><User /></el-icon><span>用户管理</span></el-menu-item>
        <el-menu-item index="/admin/profile"><el-icon><Setting /></el-icon><span>个人资料</span></el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="admin-header">
        <span>欢迎回来，{{ authStore.user?.nickname || authStore.user?.username }}</span>
        <div>
          <el-button text @click="$router.push('/')">返回博客</el-button>
          <el-button text @click="logout">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="admin-main"><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { DataAnalysis, Document, PriceTag, Picture, User, Setting } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
function logout() { authStore.logout(); router.push('/login') }
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
  background: var(--bg-color);
}

.admin-sidebar {
  background: #172334;
  border-right: 1px solid rgba(255, 255, 255, 0.08);
}

.admin-logo {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 22px;
  color: #fff;
  font-size: 18px;
  font-weight: 850;
  letter-spacing: 0;
}

.admin-sidebar :deep(.el-menu) {
  border-right: 0;
  background: transparent;
}

.admin-sidebar :deep(.el-menu-item) {
  height: 46px;
  margin: 4px 10px;
  border-radius: var(--radius-sm);
  color: rgba(255, 255, 255, 0.74);
}

.admin-sidebar :deep(.el-menu-item:not(.is-active):hover),
.admin-sidebar :deep(.el-menu-item:not(.is-active):focus) {
  color: #fff;
  background: rgba(255, 255, 255, 0.08);
}

.admin-sidebar :deep(.el-menu-item.is-active) {
  position: relative;
  color: #fff;
  background: rgba(47, 128, 237, 0.2);
}

.admin-sidebar :deep(.el-menu-item.is-active::before) {
  content: "";
  position: absolute;
  left: 0;
  top: 10px;
  bottom: 10px;
  width: 3px;
  border-radius: 999px;
  background: var(--accent-color);
}

.admin-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--soft-border-color);
  background: var(--panel-bg);
  color: var(--text-color);
}

.admin-main {
  padding: 28px;
  background: var(--bg-color);
}
</style>
