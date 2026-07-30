<template>
  <div class="profile-page">
    <div class="page-head">
      <div>
        <span class="page-eyebrow">个人资料</span>
        <h2>个人资料</h2>
      </div>
    </div>

    <div class="profile-layout">
      <section class="surface profile-card">
        <div class="profile-mark">{{ profileInitial }}</div>
        <span class="section-eyebrow">个人资料</span>
        <h3>{{ user?.nickname || user?.username || '管理员' }}</h3>
        <p class="panel-caption">管理员账号。这里保持轻，不做多余装饰。</p>
      </section>

      <section class="surface">
        <div class="panel-head">
          <div>
            <span class="section-eyebrow">账号信息</span>
            <h3>当前权限</h3>
          </div>
          <span class="chip" :class="{ 'is-success': user?.status === 'active', 'is-warning': user?.status !== 'active' }">
            {{ userStatusText }}
          </span>
        </div>
        <div class="info-grid">
          <div class="info-item">
            <span class="tiny-label">用户名</span>
            <strong>{{ user?.username || '-' }}</strong>
          </div>
          <div class="info-item">
            <span class="tiny-label">昵称</span>
            <strong>{{ user?.nickname || '-' }}</strong>
          </div>
          <div class="info-item">
            <span class="tiny-label">邮箱</span>
            <strong>{{ user?.email || '-' }}</strong>
          </div>
          <div class="info-item">
            <span class="tiny-label">角色</span>
            <strong>{{ user?.role || '-' }}</strong>
          </div>
          <div class="info-item">
            <span class="tiny-label">后台入口</span>
            <strong>{{ user?.role === 'admin' ? '全部可见' : '不可见' }}</strong>
          </div>
          <div class="info-item">
            <span class="tiny-label">注册时间</span>
            <strong>{{ formatDate(user?.createdAt) || '-' }}</strong>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useAuthStore } from '../../stores/auth'
import { formatDate } from '../../utils'

const authStore = useAuthStore()
const user = computed(() => authStore.user)

const profileInitial = computed(() => {
  const name = user.value?.nickname || user.value?.username || 'A'
  return String(name).slice(0, 1).toUpperCase()
})

const userStatusText = computed(() => {
  if (user.value?.status === 'active') return '已启用'
  if (user.value?.status === 'pending') return '待审核'
  if (user.value?.status === 'disabled') return '已禁用'
  return user.value?.status || '-'
})
</script>

<style scoped>
.profile-page {
  display: grid;
  gap: 16px;
}

.profile-layout {
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  gap: 16px;
}

.profile-card {
  display: grid;
  align-content: start;
  gap: 8px;
}

.profile-mark {
  display: grid;
  place-items: center;
  width: 68px;
  height: 68px;
  margin-bottom: 8px;
  border: 1px solid color-mix(in srgb, var(--primary-color) 28%, var(--border-color));
  border-radius: var(--radius-md);
  color: var(--primary-color);
  background:
    linear-gradient(135deg, color-mix(in srgb, var(--primary-color) 14%, transparent), transparent 64%),
    color-mix(in srgb, var(--panel-bg) 92%, var(--bg-color));
  font-family: Georgia, "Times New Roman", serif;
  font-size: 34px;
  line-height: 1;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.info-item {
  display: grid;
  gap: 6px;
  min-height: 74px;
  padding: 13px;
  border: 1px solid var(--soft-border-color);
  border-radius: var(--radius-sm);
  background: color-mix(in srgb, var(--panel-bg) 72%, transparent);
}

.info-item strong {
  overflow-wrap: anywhere;
  color: var(--text-color);
  font-size: 15px;
}

@media (max-width: 860px) {
  .profile-layout,
  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
