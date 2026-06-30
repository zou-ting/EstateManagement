<template>
  <div class="login-page">
    <div class="login-overlay"></div>
    <div class="login-shell">
      <div class="login-card">
        <div class="login-header">
          <div class="brand-mark" v-html="brandIconSvg"></div>
          <span class="portal-badge">业主端</span>
          <h1>物业管理系统</h1>
          <p>登录后查看房屋信息、物业费账单与在线报修</p>
        </div>
        <form @submit.prevent="handleLogin">
          <div class="form-group">
            <label>业主账号</label>
            <input v-model="form.username" class="input" placeholder="owner001" autocomplete="username" required />
          </div>
          <div class="form-group">
            <label>密码</label>
            <div class="password-input-wrapper">
              <input v-model="form.password" :type="showPassword ? 'text' : 'password'" class="input" placeholder="请输入密码" autocomplete="current-password" required />
              <button type="button" class="password-toggle" @click="showPassword = !showPassword">
                <svg v-if="!showPassword" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                  <circle cx="12" cy="12" r="3"/>
                </svg>
                <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-5.95 5.05M15 11a3 3 0 1 1-6 0 3 3 0 0 1 6 0z"/>
                </svg>
              </button>
            </div>
          </div>
          <p v-if="error" class="error">{{ error }}</p>
          <button class="btn btn-primary" type="submit" :disabled="loading">
            {{ loading ? '登录中...' : '进入业主中心' }}
          </button>
        </form>
        <p class="register-link">还没有账号？<router-link to="/register">业主注册</router-link></p>
      </div>
      <aside class="login-hero">
        <p class="hero-kicker">Smart · Secure · Home</p>
        <h2>您的专属<br />智慧物业服务</h2>
        <ul class="feature-list">
          <li>随时查看房屋与产权信息</li>
          <li>在线提交报修与访客预约</li>
          <li>物业费账单与公告一站查阅</li>
        </ul>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { brandIconSvg } from '../utils/brandIcon'

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
    router.push('/home')
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
  background: url('/resources/images/login/client-bg.svg') center/cover no-repeat fixed;
  background-color: #ecfdf5;
}

.login-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    105deg,
    rgba(255, 255, 255, 0.94) 0%,
    rgba(255, 255, 255, 0.78) 34%,
    rgba(255, 255, 255, 0.18) 58%,
    rgba(5, 150, 105, 0.08) 100%
  );
  pointer-events: none;
}

.login-shell {
  position: relative;
  z-index: 1;
  min-height: 100vh;
  display: grid;
  grid-template-columns: min(440px, 100%) minmax(280px, 1fr);
  gap: clamp(24px, 5vw, 72px);
  align-items: center;
  max-width: 1180px;
  margin: 0 auto;
  padding: clamp(24px, 5vw, 64px) clamp(24px, 6vw, 96px);
}

.login-card {
  width: 100%;
  padding: 38px 34px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(18px) saturate(1.15);
  border: 1px solid rgba(255, 255, 255, 0.95);
  border-radius: 22px;
  box-shadow: 0 20px 56px rgba(5, 150, 105, 0.14), 0 8px 24px rgba(15, 23, 42, 0.06);
  animation: slideIn 0.45s ease-out;
}

@keyframes slideIn {
  from { opacity: 0; transform: translateX(-20px); }
  to { opacity: 1; transform: translateX(0); }
}

.login-header { margin-bottom: 28px; }

.brand-mark {
  width: 52px;
  height: 52px;
  margin-bottom: 16px;
  background: linear-gradient(135deg, #34d399, #059669);
  color: #fff;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10px 28px rgba(5, 150, 105, 0.28);
}

.portal-badge {
  display: inline-block;
  padding: 4px 12px;
  margin-bottom: 14px;
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
  color: #064e3b;
  margin-bottom: 8px;
  letter-spacing: -0.02em;
}

.login-header p {
  font-size: 14px;
  color: #64748b;
  line-height: 1.6;
}

.form-group { margin-bottom: 18px; }

.form-group label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #475569;
  margin-bottom: 8px;
}

.password-input-wrapper {
  position: relative;
}

.input {
  background: rgba(255, 255, 255, 0.98);
  border-color: #a7f3d0;
  padding: 12px 14px;
  padding-right: 44px;
}

.password-toggle {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
  color: #9CA3AF;
  padding: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color 0.2s ease;
}

.password-toggle:hover {
  color: #059669;
}

.input:focus {
  border-color: #059669;
  box-shadow: 0 0 0 3px rgba(5, 150, 105, 0.12);
}

.btn-primary {
  width: 100%;
  margin-top: 4px;
  padding: 13px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 12px;
  background: linear-gradient(135deg, #34d399, #059669);
  border: none;
}

.error {
  color: #dc2626;
  font-size: 13px;
  margin-bottom: 10px;
  padding: 8px 12px;
  background: rgba(220, 38, 38, 0.06);
  border-radius: 8px;
}

.register-link {
  text-align: center;
  margin-top: 22px;
  font-size: 14px;
  color: #64748b;
}

.register-link a {
  color: #059669;
  font-weight: 600;
  text-decoration: none;
}

.register-link a:hover { text-decoration: underline; }

.login-hero {
  color: #064e3b;
  animation: fadeUp 0.55s ease-out 0.08s both;
}

.hero-kicker {
  font-size: 13px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: #059669;
  margin-bottom: 16px;
  font-weight: 600;
}

.login-hero h2 {
  font-size: clamp(30px, 4vw, 42px);
  font-weight: 800;
  line-height: 1.25;
  margin-bottom: 28px;
  letter-spacing: -0.03em;
}

.feature-list {
  list-style: none;
  display: grid;
  gap: 16px;
}

.feature-list li {
  position: relative;
  padding: 14px 16px 14px 44px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(5, 150, 105, 0.12);
  border-radius: 14px;
  font-size: 15px;
  color: #065f46;
  box-shadow: 0 8px 24px rgba(5, 150, 105, 0.08);
}

.feature-list li::before {
  content: '';
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #059669;
  box-shadow: 0 0 0 4px rgba(5, 150, 105, 0.15);
}

@keyframes fadeUp {
  from { opacity: 0; transform: translateY(18px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 960px) {
  .login-shell {
    grid-template-columns: 1fr;
    max-width: 480px;
  }
  .login-hero { display: none; }
  .login-overlay {
    background: linear-gradient(180deg, rgba(255, 255, 255, 0.92) 0%, rgba(255, 255, 255, 0.72) 100%);
  }
}
</style>
