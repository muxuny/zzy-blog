<template>
  <div class="profile-page">
    <div class="page-head">
      <div>
        <span class="page-eyebrow">个人资料</span>
        <h2>个人资料</h2>
      </div>
    </div>

    <section class="surface profile-overview">
      <div class="profile-hero">
        <div class="profile-mark">{{ profileInitial }}</div>
        <div class="profile-headline">
          <span class="section-eyebrow">当前账号</span>
          <h3>{{ displayName }}</h3>
          <div class="profile-tags">
            <span class="chip">{{ roleText }}</span>
            <span class="chip" :class="{ 'is-success': user?.status === 'active', 'is-warning': user?.status !== 'active' }">
              {{ userStatusText }}
            </span>
          </div>
        </div>
        <div class="profile-access">
          <span class="tiny-label">后台入口</span>
          <strong>{{ adminAccessText }}</strong>
        </div>
      </div>

      <div class="profile-details">
        <div class="panel-head">
          <div>
            <span class="section-eyebrow">账号信息</span>
            <h3>权限与联系信息</h3>
          </div>
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
            <span class="tiny-label">账号状态</span>
            <strong>{{ userStatusText }}</strong>
          </div>
          <div class="info-item">
            <span class="tiny-label">注册时间</span>
            <strong>{{ formatDate(user?.createdAt) || '-' }}</strong>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useAuthStore } from '../../stores/auth'
import { formatDate } from '../../utils'

const authStore = useAuthStore()
const user = computed(() => authStore.user)

const displayName = computed(() => user.value?.nickname || user.value?.username || '管理员')

const profileInitial = computed(() => {
  return String(displayName.value || 'A').slice(0, 1).toUpperCase()
})

const roleText = computed(() => user.value?.role || '-')

const adminAccessText = computed(() => (user.value?.role === 'admin' ? '全部可见' : '不可见'))

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

.profile-overview {
  display: grid;
  gap: 18px;
  overflow: hidden;
  background:
    linear-gradient(135deg, color-mix(in srgb, var(--primary-color) 10%, transparent), transparent 44%),
    linear-gradient(90deg, transparent, color-mix(in srgb, var(--accent-color) 8%, transparent)),
    color-mix(in srgb, var(--panel-bg) 94%, var(--bg-color));
}

.profile-hero {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: center;
  gap: 18px;
  padding-bottom: 18px;
  border-bottom: 1px solid var(--soft-border-color);
}

.profile-mark {
  display: grid;
  place-items: center;
  width: 78px;
  height: 78px;
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

.profile-headline {
  display: grid;
  gap: 8px;
  min-width: 0;
}

.profile-headline h3 {
  margin: 0;
  overflow-wrap: anywhere;
  color: var(--text-color);
  font-family: Georgia, "Times New Roman", serif;
  font-size: 28px;
  font-weight: 500;
  line-height: 1.15;
}

.profile-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.profile-access {
  display: grid;
  gap: 6px;
  min-width: 150px;
  padding-left: 18px;
  border-left: 1px solid var(--soft-border-color);
}

.profile-access strong {
  color: var(--text-color);
  font-size: 15px;
}

.profile-details {
  display: grid;
  gap: 14px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
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

@media (max-width: 980px) {
  .profile-hero {
    grid-template-columns: auto minmax(0, 1fr);
  }

  .profile-access {
    grid-column: 1 / -1;
    min-width: 0;
    padding: 12px 0 0;
    border-top: 1px solid var(--soft-border-color);
    border-left: 0;
  }

  .info-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 860px) {
  .profile-hero,
  .info-grid {
    grid-template-columns: 1fr;
  }

  .profile-mark {
    width: 68px;
    height: 68px;
  }
}
</style>
