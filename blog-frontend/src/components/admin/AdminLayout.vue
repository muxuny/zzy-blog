<template>
  <div class="admin-shell">
    <section class="admin-frame" aria-label="后台管理">
      <aside class="admin-sidebar">
        <RouterLink class="brand-lockup" to="/admin/dashboard" aria-label="回到后台仪表盘">
          <span class="brand-eyebrow">ADMIN CONSOLE</span>
          <span class="brand-title">管理中枢</span>
          <span class="brand-subtitle">审核、用户、标签和素材都在这里收口。</span>
        </RouterLink>

        <nav class="nav-stack" aria-label="后台导航">
          <RouterLink
            class="nav-item"
            :class="{ 'is-active': isPathActive('/admin/dashboard') }"
            to="/admin/dashboard"
          >
            <span class="nav-icon">D</span>
            <span>仪表盘</span>
            <span class="nav-badge">总览</span>
          </RouterLink>
          <RouterLink
            class="nav-item"
            :class="{ 'is-active': route.path.startsWith('/admin/articles') }"
            to="/admin/articles"
          >
            <span class="nav-icon">A</span>
            <span>文章管理</span>
            <span class="nav-badge">审核</span>
          </RouterLink>
          <RouterLink
            class="nav-item"
            :class="{ 'is-active': isResourcesActive }"
            to="/admin/resources"
          >
            <span class="nav-icon">R</span>
            <span>资源管理</span>
            <span class="nav-badge">素材</span>
          </RouterLink>
          <RouterLink
            class="nav-item"
            :class="{ 'is-active': isPathActive('/admin/page-copies') }"
            to="/admin/page-copies"
          >
            <span class="nav-icon">T</span>
            <span>页面文案</span>
            <span class="nav-badge">配置</span>
          </RouterLink>
          <RouterLink
            class="nav-item"
            :class="{ 'is-active': isPathActive('/admin/users') }"
            to="/admin/users"
          >
            <span class="nav-icon">U</span>
            <span>用户管理</span>
            <span class="nav-badge">账号</span>
          </RouterLink>
          <RouterLink
            class="nav-item"
            :class="{ 'is-active': isPathActive('/admin/profile') }"
            to="/admin/profile"
          >
            <span class="nav-icon">P</span>
            <span>个人资料</span>
            <span class="nav-badge">-</span>
          </RouterLink>
        </nav>

        <div class="sidebar-note">
          <span class="tiny-label">当前身份</span>
          <strong>{{ authStore.user?.nickname || authStore.user?.username || '管理员' }}</strong>
          <p>后台只保留管理动作，内容阅读和创作入口回到前台。</p>
        </div>
      </aside>

      <main class="main-surface">
        <header class="topbar">
          <div class="welcome-line">
            欢迎回来，<span>{{ authStore.user?.nickname || authStore.user?.username || '管理员' }}</span>
          </div>
          <div class="toolbar">
            <ThemeToggle />
            <button class="tool-button is-text" type="button" @click="router.push('/')">返回博客</button>
            <button class="tool-button" type="button" @click="logout">退出登录</button>
          </div>
        </header>

        <div class="admin-page-body">
          <router-view />
        </div>
      </main>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter, RouterLink } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import ThemeToggle from '../ThemeToggle.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const isResourcesActive = computed(() => (
  ['/admin/resources', '/admin/tags', '/admin/images'].some(path => route.path.startsWith(path))
))

function isPathActive(path) {
  return route.path === path
}

function logout() {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.admin-shell {
  min-height: 100vh;
  padding: 24px;
  background:
    radial-gradient(circle at 14% 8%, color-mix(in srgb, var(--primary-color) 10%, transparent), transparent 30%),
    radial-gradient(circle at 88% 0%, color-mix(in srgb, var(--accent-color) 10%, transparent), transparent 28%);
}

.admin-frame {
  display: grid;
  grid-template-columns: minmax(210px, 248px) minmax(0, 1fr);
  width: min(1380px, 100%);
  min-height: calc(100vh - 48px);
  margin: 0 auto;
  overflow: hidden;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background:
    radial-gradient(circle at 18% 6%, color-mix(in srgb, var(--primary-color) 13%, transparent), transparent 30%),
    radial-gradient(circle at 92% 2%, color-mix(in srgb, var(--accent-color) 11%, transparent), transparent 28%),
    color-mix(in srgb, var(--bg-color) 90%, var(--panel-bg));
  box-shadow: var(--shadow-md);
}

.admin-sidebar {
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding: 22px 14px;
  border-right: 1px solid var(--border-color);
  background:
    linear-gradient(180deg, color-mix(in srgb, var(--panel-bg) 92%, transparent), color-mix(in srgb, var(--panel-bg) 60%, transparent)),
    linear-gradient(90deg, var(--theme-grid-x) 1px, transparent 1px);
  background-size: auto, 36px 36px;
}

.brand-lockup {
  display: grid;
  padding: 2px 10px 16px;
  border-bottom: 1px solid var(--soft-border-color);
  color: inherit;
}

.brand-lockup:hover {
  color: inherit;
}

.brand-eyebrow {
  color: var(--muted-text-color);
  font-size: 12px;
  font-weight: 760;
}

.brand-title {
  display: block;
  margin-top: 6px;
  color: var(--text-color);
  font-family: Georgia, "Times New Roman", serif;
  font-size: 25px;
  font-weight: 500;
  line-height: 1.1;
}

.brand-subtitle {
  margin-top: 8px;
  color: var(--muted-text-color);
  font-size: 13px;
}

.nav-stack {
  display: grid;
  gap: 6px;
}

.nav-item {
  display: grid;
  grid-template-columns: 20px minmax(0, 1fr) auto;
  align-items: center;
  gap: 9px;
  width: 100%;
  padding: 10px 11px;
  border: 1px solid transparent;
  border-radius: var(--radius-md);
  color: var(--muted-text-color);
  background: transparent;
  text-align: left;
  transition: background 0.16s ease, border-color 0.16s ease, color 0.16s ease;
}

.nav-item:hover {
  color: var(--text-color);
  background: color-mix(in srgb, var(--accent-color) 8%, transparent);
}

.nav-item.is-active {
  color: var(--text-color);
  background: color-mix(in srgb, var(--primary-color) 11%, transparent);
  box-shadow: inset 3px 0 0 var(--primary-color);
}

.nav-icon {
  display: grid;
  place-items: center;
  width: 20px;
  height: 20px;
  border-radius: 999px;
  color: var(--primary-color);
  background: color-mix(in srgb, var(--primary-color) 12%, transparent);
  font-size: 11px;
  font-weight: 760;
}

.nav-badge {
  color: var(--muted-text-color);
  font-size: 12px;
}

.sidebar-note {
  margin-top: auto;
  padding: 14px 10px 0;
  border-top: 1px solid var(--soft-border-color);
}

.sidebar-note strong {
  display: block;
  margin: 6px 0 4px;
  color: var(--text-color);
  font-weight: 760;
}

.sidebar-note p {
  margin: 0;
  color: var(--muted-text-color);
  font-size: 13px;
}

.main-surface {
  min-width: 0;
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
  background: color-mix(in srgb, var(--bg-color) 76%, transparent);
}

.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  min-height: 64px;
  padding: 0 24px;
  border-bottom: 1px solid var(--soft-border-color);
  background: color-mix(in srgb, var(--panel-bg) 84%, transparent);
  backdrop-filter: blur(14px);
}

.welcome-line {
  color: var(--text-color);
  font-weight: 760;
}

.welcome-line span {
  color: var(--muted-text-color);
  font-weight: 500;
}

.admin-page-body {
  min-width: 0;
  padding: 24px;
}

@media (max-width: 860px) {
  .admin-shell {
    padding: 12px;
  }

  .admin-frame {
    grid-template-columns: 1fr;
    min-height: calc(100vh - 24px);
  }

  .admin-sidebar {
    border-right: 0;
    border-bottom: 1px solid var(--border-color);
  }

  .nav-stack {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .sidebar-note {
    display: none;
  }

  .topbar {
    align-items: stretch;
    flex-direction: column;
    padding: 14px 18px;
  }

  .toolbar {
    justify-content: flex-start;
  }
}

@media (max-width: 560px) {
  .admin-page-body {
    padding: 18px;
  }

  .nav-stack {
    grid-template-columns: 1fr;
  }
}
</style>
