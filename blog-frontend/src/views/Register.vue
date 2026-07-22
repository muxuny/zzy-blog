<template>
  <main class="auth-shell">
    <section class="auth-copy">
      <span class="eyebrow">新入口</span>
      <h1>给自己的内容留一个稳定身份。</h1>
      <p>注册后等待审核，通过后就可以继续阅读、写文章和进入对应的管理入口。</p>
      <div class="auth-index" aria-label="注册后可使用的区域">
        <div class="meta-line">
          <span class="meta-label">阅读</span>
          <span class="meta-value">保存阅读进度</span>
        </div>
        <div class="meta-line">
          <span class="meta-label">收藏</span>
          <span class="meta-value">整理常看的文章</span>
        </div>
        <div class="meta-line">
          <span class="meta-label">创作</span>
          <span class="meta-value">维护自己的文章</span>
        </div>
      </div>
    </section>

    <section class="auth-panel" aria-label="注册表单">
      <span class="panel-kicker">注册</span>
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
    </section>
  </main>
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
.auth-shell {
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(340px, 420px);
  gap: clamp(28px, 6vw, 72px);
  align-items: center;
  width: min(1120px, calc(100% - 36px));
  margin: 0 auto;
  padding: 48px 0;
  background:
    linear-gradient(90deg, var(--theme-grid-x) 1px, transparent 1px),
    linear-gradient(180deg, var(--theme-grid-y) 1px, transparent 1px),
    var(--bg-color);
  background-size: 44px 44px;
}

.auth-copy {
  min-width: 0;
}

.eyebrow {
  display: inline-flex;
  margin: 0 0 14px;
  color: var(--primary-color);
  font-size: 12px;
  font-weight: 760;
  letter-spacing: 0;
}

h1 {
  max-width: 700px;
  margin: 0 0 18px;
  color: var(--text-color);
  font-family: Georgia, "Times New Roman", serif;
  font-size: clamp(42px, 7vw, 74px);
  font-weight: 500;
  line-height: 0.98;
}

.auth-copy p {
  max-width: 600px;
  margin: 0;
  color: var(--muted-text-color);
  font-size: 17px;
  line-height: 1.85;
}

.auth-index {
  max-width: 430px;
  margin-top: 34px;
  border-left: 1px solid var(--border-color);
  padding-left: 18px;
}

.meta-line {
  display: grid;
  grid-template-columns: 72px minmax(0, 1fr);
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid var(--soft-border-color);
}

.meta-line:last-child {
  border-bottom: 0;
}

.meta-label {
  color: var(--accent-color);
  font-size: 12px;
  font-weight: 760;
}

.meta-value {
  color: var(--text-color);
  font-size: 13px;
}

.auth-panel {
  position: relative;
  overflow: hidden;
  padding: 28px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background: color-mix(in srgb, var(--panel-bg) 94%, transparent);
  box-shadow: var(--shadow-sm);
}

.auth-panel::before {
  position: absolute;
  inset: 0;
  background:
    repeating-linear-gradient(90deg, transparent 0 22px, var(--theme-grid-x) 22px 23px),
    linear-gradient(135deg, color-mix(in srgb, var(--accent-color) 10%, transparent), transparent 48%);
  content: '';
  opacity: 0.72;
  pointer-events: none;
}

.auth-panel > * {
  position: relative;
}

.panel-kicker {
  display: block;
  margin-bottom: 8px;
  color: var(--text-color);
  font-family: Georgia, "Times New Roman", serif;
  font-size: 30px;
  font-weight: 500;
  line-height: 1.1;
}

.auth-subtitle {
  margin: 0 0 24px;
  color: var(--muted-text-color);
  line-height: 1.7;
}

.auth-panel :deep(.el-input__wrapper) {
  min-height: 42px;
  border-radius: 999px;
  background: color-mix(in srgb, var(--panel-bg) 94%, transparent);
}

.auth-submit {
  width: 100%;
  min-height: 42px;
  border-radius: 999px;
}

.auth-notice {
  margin-bottom: 14px;
  padding: 10px 12px;
  border: 1px solid color-mix(in srgb, var(--warning-color) 32%, transparent);
  border-radius: var(--radius-sm);
  color: color-mix(in srgb, var(--warning-color) 76%, var(--text-color));
  background: color-mix(in srgb, var(--warning-color) 12%, transparent);
  text-align: center;
}

[data-theme="dark"] .auth-notice {
  color: var(--warning-color);
}

.auth-link {
  margin-top: 4px;
  color: var(--muted-text-color);
  font-size: 14px;
  text-align: center;
}

@media (max-width: 860px) {
  .auth-shell {
    grid-template-columns: 1fr;
    align-content: center;
  }

  .auth-index {
    max-width: none;
  }
}

@media (max-width: 520px) {
  .auth-shell {
    width: min(100% - 28px, 1120px);
    padding: 28px 0;
  }

  h1 {
    font-size: 42px;
  }

  .auth-panel {
    padding: 22px;
  }

  .meta-line {
    grid-template-columns: 1fr;
  }
}
</style>
