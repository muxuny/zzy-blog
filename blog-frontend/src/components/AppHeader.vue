<template>
  <el-header class="app-header">
    <div class="header-inner">
      <router-link to="/" class="logo" aria-label="返回首页">
        <span class="logo-mark">B</span>
        <span class="logo-text">ZZY Blog</span>
      </router-link>
      <div class="header-right">
        <ThemeToggle />
        <template v-if="authStore.isLoggedIn">
          <el-dropdown trigger="click">
            <button type="button" class="user-info">
              <span class="user-name">{{ authStore.user?.nickname || authStore.user?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push('/creator/articles/create')">
                  写文章
                </el-dropdown-item>
                <el-dropdown-item @click="$router.push('/reading')">
                  我的阅读
                </el-dropdown-item>
                <el-dropdown-item @click="$router.push('/creator/articles')">
                  创作中心
                </el-dropdown-item>
                <el-dropdown-item v-if="authStore.isAdmin" @click="$router.push('/admin/dashboard')">
                  后台管理
                </el-dropdown-item>
                <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button text @click="$router.push('/login')">登录</el-button>
          <el-button text @click="$router.push('/register')">注册</el-button>
        </template>
      </div>
    </div>
  </el-header>
</template>

<script setup>
import { useAuthStore } from '../stores/auth'
import { useRouter } from 'vue-router'
import ThemeToggle from './ThemeToggle.vue'
import { ArrowDown } from '@element-plus/icons-vue'

const authStore = useAuthStore()
const router = useRouter()
function logout() { authStore.logout(); router.push('/') }
</script>

<style scoped>
.app-header {
  position: sticky;
  top: 0;
  z-index: 100;
  isolation: isolate;
  height: var(--app-header-height);
  padding: 0;
  border-bottom: 0;
  background: transparent;
}

.app-header::before {
  position: absolute;
  left: 0;
  top: 0;
  right: 0;
  bottom: -18px;
  z-index: -2;
  width: auto;
  background:
    linear-gradient(
      180deg,
      var(--header-backdrop-bg) 0%,
      color-mix(in srgb, var(--header-backdrop-bg) 84%, transparent) 62%,
      transparent 100%
    );
  backdrop-filter: blur(16px) saturate(1.08);
  content: '';
  mask-image: linear-gradient(180deg, #000 0%, #000 64%, transparent 100%);
  pointer-events: none;
  transform: none;
}

.app-header::after {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: -1;
  width: auto;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--header-divider-color), transparent);
  content: '';
  pointer-events: none;
  transform: none;
}

.header-inner {
  width: min(100%, var(--content-width));
  height: var(--app-header-height);
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.logo {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  color: var(--text-color);
  font-weight: 800;
}

.logo-mark {
  width: 32px;
  height: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-sm);
  background: var(--primary-color);
  color: #fff;
  box-shadow: 0 10px 24px var(--theme-glow-color);
}

.logo-text {
  font-size: 18px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.user-info {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: 36px;
  max-width: 220px;
  padding: 0 10px;
  border: 0;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--text-color);
  font: inherit;
  cursor: pointer;
}

.user-info:hover {
  background: color-mix(in srgb, var(--primary-color) 8%, transparent);
}

.user-info:focus-visible {
  outline: 2px solid var(--primary-color);
  outline-offset: 2px;
}

.user-name {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@media (max-width: 640px) {
  .header-inner {
    padding: 0 14px;
  }

  .logo-text {
    display: none;
  }
}
</style>
