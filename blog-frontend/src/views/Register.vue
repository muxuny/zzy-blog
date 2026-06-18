<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <h1>注册</h1>
      <p class="auth-subtitle">创建账号后，需要管理员审核才能登录后台。</p>
      <el-form :model="form" :rules="rules" ref="formRef">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" show-password />
        </el-form-item>
        <el-form-item prop="nickname">
          <el-input v-model="form.nickname" placeholder="昵称（选填）" />
        </el-form-item>
        <div class="auth-notice">注册后需管理员审核方可登录。</div>
        <el-form-item>
          <el-button type="primary" @click="handleRegister" :loading="loading" class="auth-submit">注册</el-button>
        </el-form-item>
      </el-form>
      <div class="auth-link">已有账号？<router-link to="/login">立即登录</router-link></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '../api/auth'
import { ElMessage } from 'element-plus'

const router = useRouter(), loading = ref(false), formRef = ref(null)
const form = reactive({ username: '', password: '', nickname: '' })
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '长度为 3-50 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码不少于 6 位', trigger: 'blur' }
  ]
}

async function handleRegister() {
  const valid = await formRef.value.validate().catch(() => {})
  if (!valid) return
  loading.value = true
  try {
    await register(form)
    ElMessage.success('注册成功，等待管理员审核')
    router.push('/login')
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
