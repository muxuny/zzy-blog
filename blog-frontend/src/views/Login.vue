<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <h1>登录</h1>
      <p class="auth-subtitle">进入后台，继续管理你的内容。</p>
      <el-form :model="form" :rules="rules" ref="formRef">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" class="auth-submit">登录</el-button>
        </el-form-item>
      </el-form>
      <div class="auth-link">还没有账号？<router-link to="/register">立即注册</router-link></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { login } from '../api/auth'
import { useAuthStore } from '../stores/auth'
import { resolveLoginRedirect } from '../utils/authRedirect'
import { ElMessage } from 'element-plus'

const router = useRouter(), route = useRoute(), authStore = useAuthStore(), loading = ref(false), formRef = ref(null)
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => {})
  if (!valid) return
  loading.value = true
  try {
    const r = await login(form)
    authStore.setToken(r.data.token)
    await authStore.fetchUser()
    ElMessage.success('登录成功')
    router.push(resolveLoginRedirect(authStore.user, route.query.redirect))
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 32px 16px;
  background:
    linear-gradient(90deg, rgba(47, 128, 237, 0.06) 1px, transparent 1px),
    linear-gradient(180deg, rgba(47, 128, 237, 0.06) 1px, transparent 1px),
    var(--bg-color);
  background-size: 34px 34px;
}

.auth-card {
  width: min(100%, 420px);
  border: 1px solid var(--soft-border-color);
}

.auth-card :deep(.el-card__body) {
  padding: 30px;
}

h1 {
  margin-bottom: 6px;
  color: var(--text-color);
  font-size: 30px;
  line-height: 1.2;
  font-weight: 850;
  text-align: center;
}

.auth-subtitle {
  margin-bottom: 24px;
  color: var(--muted-text-color);
  text-align: center;
}

.auth-submit {
  width: 100%;
  min-height: 40px;
}

.auth-notice {
  margin-bottom: 14px;
  padding: 10px 12px;
  border: 1px solid rgba(245, 158, 11, 0.3);
  border-radius: var(--radius-sm);
  color: #9a5a00;
  background: rgba(245, 158, 11, 0.11);
  text-align: center;
}

[data-theme="dark"] .auth-notice {
  color: #ffd28a;
}

.auth-link {
  margin-top: 4px;
  color: var(--muted-text-color);
  font-size: 14px;
  text-align: center;
}
</style>
