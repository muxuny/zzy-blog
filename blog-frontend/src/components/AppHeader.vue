<template>
  <el-header class="app-header">
    <div class="header-inner">
      <router-link to="/" class="logo" aria-label="返回首页">
        <span class="logo-mark" aria-hidden="true">Z</span>
        <span class="logo-copy">
          <span class="logo-text">ZZY Blog</span>
          <span class="logo-kicker">Private index</span>
        </span>
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
  position: relative;
  display: inline-grid;
  grid-template-columns: 38px max-content;
  align-items: center;
  gap: 12px;
  min-height: 46px;
  padding: 4px 14px 4px 4px;
  border-radius: 999px;
  color: var(--text-color);
  font-weight: 700;
  isolation: isolate;
}

.logo::before {
  position: absolute;
  inset: 0;
  z-index: -1;
  border: 1px solid color-mix(in srgb, var(--border-color) 78%, transparent);
  border-radius: inherit;
  background: color-mix(in srgb, var(--panel-bg) 68%, transparent);
  box-shadow: 0 14px 36px color-mix(in srgb, var(--theme-glow-color) 28%, transparent);
  content: '';
  opacity: 0.82;
  transform: scaleX(0.96);
  transform-origin: left center;
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.logo:hover::before {
  opacity: 1;
  transform: scaleX(1);
}

.logo:focus-visible {
  outline: 2px solid var(--primary-color);
  outline-offset: 3px;
}

.logo-mark {
  position: relative;
  width: 38px;
  height: 38px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1px solid color-mix(in srgb, var(--primary-color) 32%, var(--border-color));
  border-radius: 50%;
  background: color-mix(in srgb, var(--primary-color) 10%, var(--panel-bg));
  color: var(--primary-color);
  font-family: Georgia, "Times New Roman", serif;
  font-size: 20px;
  font-weight: 700;
  line-height: 1;
  box-shadow:
    inset 0 0 0 4px color-mix(in srgb, var(--panel-bg) 68%, transparent),
    0 10px 24px color-mix(in srgb, var(--theme-glow-color) 40%, transparent);
  transition: border-color 0.2s ease, transform 0.2s ease;
}

.logo-mark::after {
  position: absolute;
  inset: 6px;
  border: 1px solid color-mix(in srgb, var(--accent-color) 30%, transparent);
  border-radius: 50%;
  content: '';
  pointer-events: none;
}

.logo:hover .logo-mark {
  border-color: color-mix(in srgb, var(--primary-color) 54%, var(--border-color));
  transform: translateY(-1px);
}

.logo-copy {
  display: grid;
  gap: 2px;
  min-width: 0;
  line-height: 1;
}

.logo-text {
  color: var(--text-color);
  font-family: Georgia, "Times New Roman", serif;
  font-size: 20px;
  font-weight: 600;
  line-height: 0.95;
}

.logo-kicker {
  color: var(--muted-text-color);
  font-size: 11px;
  font-weight: 650;
  line-height: 1.1;
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

  .logo {
    grid-template-columns: 38px;
    padding: 4px;
  }

  .logo-copy {
    display: none;
  }
}
</style>
