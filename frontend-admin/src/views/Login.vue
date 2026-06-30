<template>
  <div class="login-page">
    <div class="login-overlay"></div>
    <div class="login-shell">
      <aside class="login-hero">
        <div class="hero-brand">
          <div class="brand-mark" v-html="brandIconSvg"></div>
          <div>
            <p class="hero-kicker">Property Management</p>
            <h2>物业管理系统</h2>
          </div>
        </div>
        <p class="hero-desc">智慧小区运营平台，覆盖楼栋房屋、业主档案、物业费收缴与报修工单全流程。</p>
        <ul class="feature-list">
          <li>楼栋与房屋档案可视化维护</li>
          <li>物业费账单生成与收缴跟踪</li>
          <li>报修受理、访客审批与巡检记录</li>
        </ul>
        <div class="hero-stats">
          <div class="stat-chip"><span class="stat-num">3</span><span class="stat-label">楼栋</span></div>
          <div class="stat-chip"><span class="stat-num">8+</span><span class="stat-label">房屋</span></div>
          <div class="stat-chip"><span class="stat-num">24h</span><span class="stat-label">工单响应</span></div>
        </div>
      </aside>
      <div class="login-card">
        <div class="login-header">
          <span class="portal-badge">管理端</span>
          <h1>欢迎登录</h1>
          <p>管理员与物业管家账号</p>
        </div>
        <form @submit.prevent="handleLogin">
          <div class="form-group">
            <label>用户名</label>
            <input v-model="form.username" class="input" placeholder="请输入用户名" autocomplete="username" required />
          </div>
          <div class="form-group">
            <label>密码</label>
            <div class="password-wrapper">
              <input
                  :type="showPassword ? 'text' : 'password'"
                  v-model="form.password"
                  class="input"
                  placeholder="请输入密码"
                  autocomplete="current-password"
                  required
              />
              <span class="eye-icon" @click="showPassword = !showPassword">
                {{ showPassword ? '🙈' : '🐵' }}
              </span>
            </div>
          </div>
          <p v-if="error" class="error">{{ error }}</p>
          <button class="btn btn-primary login-btn" type="submit" :disabled="loading">
            {{ loading ? '登录中...' : '进入管理端' }}
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { brandIconSvg } from '../utils/brandIcon'
import { View,Hide } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const error = ref('')
const showPassword = ref(false)
const form = reactive({ username: '', password: '' })

async function handleLogin() {
  loading.value = true
  error.value = ''
  try {
    await userStore.login(form.username, form.password)
    router.push('/admin/dashboard')
  } catch (e) {
    error.value = e.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  position: relative;
  background: url('/resources/images/login/admin-bg.svg') center/cover no-repeat fixed;
  background-color: #064e3b;
}

.login-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
      115deg,
      rgba(6, 78, 59, 0.72) 0%,
      rgba(6, 95, 70, 0.45) 38%,
      rgba(255, 255, 255, 0.06) 62%,
      rgba(255, 255, 255, 0.32) 100%
  );
  pointer-events: none;
}

.login-shell {
  position: relative;
  z-index: 1;
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(300px, 1fr) min(440px, 100%);
  gap: clamp(24px, 5vw, 64px);
  align-items: center;
  max-width: 1280px;
  margin: 0 auto;
  padding: clamp(24px, 5vw, 64px) clamp(24px, 6vw, 96px);
}

.login-hero {
  color: #ecfdf5;
  animation: fadeUp 0.5s ease-out;
}

.hero-brand {
  display: flex;
  align-items: center;
  gap: 18px;
  margin-bottom: 28px;
}

.brand-mark {
  width: 56px;
  height: 56px;
  flex-shrink: 0;
  background: linear-gradient(135deg, #34d399, #059669);
  color: #fff;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 12px 32px rgba(52, 211, 153, 0.35);
}

.hero-kicker {
  font-size: 13px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #a7f3d0;
  margin-bottom: 6px;
}

.login-hero h2 {
  font-size: clamp(28px, 4vw, 38px);
  font-weight: 700;
  line-height: 1.2;
  letter-spacing: -0.02em;
}

.hero-desc {
  max-width: 520px;
  font-size: 16px;
  line-height: 1.75;
  color: rgba(236, 253, 245, 0.92);
  margin-bottom: 28px;
}

.feature-list {
  list-style: none;
  display: grid;
  gap: 14px;
  margin-bottom: 32px;
}

.feature-list li {
  position: relative;
  padding-left: 22px;
  font-size: 15px;
  color: rgba(167, 243, 208, 0.95);
}

.feature-list li::before {
  content: '';
  position: absolute;
  left: 0;
  top: 9px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #34d399;
  box-shadow: 0 0 0 4px rgba(52, 211, 153, 0.22);
}

.hero-stats {
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
}

.stat-chip {
  padding: 12px 18px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(167, 243, 208, 0.25);
  border-radius: 14px;
  backdrop-filter: blur(8px);
}

.stat-num {
  display: block;
  font-size: 20px;
  font-weight: 700;
  color: #fff;
  line-height: 1.2;
}

.stat-label {
  font-size: 12px;
  color: rgba(167, 243, 208, 0.85);
}

.login-card {
  width: 100%;
  padding: 40px 36px;
  background: rgba(255, 255, 255, 0.94);
  backdrop-filter: blur(20px) saturate(1.15);
  border: 1px solid rgba(255, 255, 255, 0.88);
  border-radius: 22px;
  box-shadow: 0 24px 64px rgba(6, 78, 59, 0.28), 0 8px 24px rgba(15, 23, 42, 0.08);
  animation: slideIn 0.45s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(24px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes fadeUp {
  from {
    opacity: 0;
    transform: translateY(16px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-header {
  margin-bottom: 28px;
}

.portal-badge {
  display: inline-block;
  padding: 4px 12px;
  margin-bottom: 12px;
  font-size: 12px;
  font-weight: 600;
  color: #047857;
  background: rgba(5, 150, 105, 0.1);
  border: 1px solid rgba(5, 150, 105, 0.22);
  border-radius: 20px;
}

.login-header h1 {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 8px;
  color: #064e3b;
  letter-spacing: -0.02em;
}

.login-header p {
  font-size: 14px;
  color: #64748b;
}

.form-group {
  margin-bottom: 18px;
}

.form-group label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #475569;
  margin-bottom: 8px;
}

.input {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid #a7f3d0;
  border-radius: 8px;
  font-size: 14px;
  font-family: inherit;
  background: #fff;
  transition: border-color 0.2s, box-shadow 0.2s;
  box-sizing: border-box;
}

.input:focus {
  border-color: #059669;
  box-shadow: 0 0 0 3px rgba(5, 150, 105, 0.12);
  outline: none;
}

.password-wrapper {
  position: relative;
}

.password-wrapper .input {
  padding-right: 44px;
}

.eye-icon {
  position: absolute;
  right: 14px;
  top: 50%;
  transform: translateY(-50%);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  color: #94a3b8;
  transition: color 0.2s;
  z-index: 2;
}

.eye-icon:hover {
  color: #059669;
}

.eye-icon .el-icon {
  font-size: 20px;
}

.login-btn {
  width: 100%;
  margin-top: 4px;
  padding: 13px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 12px;
  background: linear-gradient(135deg, #34d399, #059669);
  border: none;
  color: #fff;
  cursor: pointer;
  transition: filter 0.2s;
}

.login-btn:hover:not(:disabled) {
  filter: brightness(1.05);
}

.login-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.error {
  color: #dc2626;
  font-size: 13px;
  margin-bottom: 10px;
  padding: 8px 12px;
  background: rgba(220, 38, 38, 0.06);
  border-radius: 8px;
}

@media (max-width: 960px) {
  .login-shell {
    grid-template-columns: 1fr;
    max-width: 480px;
  }

  .login-hero {
    display: none;
  }

  .login-overlay {
    background: linear-gradient(180deg, rgba(6, 78, 59, 0.4) 0%, rgba(255, 255, 255, 0.55) 100%);
  }
}
</style>