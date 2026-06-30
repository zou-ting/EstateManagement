<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-icon" v-html="brandIconSvg"></div>
        <div>
          <div class="brand-title">物业管理系统</div>
          <div class="brand-sub">管理端 · {{ roleLabel }}</div>
        </div>
      </div>
      <nav class="nav">
        <router-link v-for="item in navMenus" :key="item.path" :to="item.path" class="nav-item" @click.native="item.isComplaint && markAllRead()">
          <span class="nav-icon" v-html="item.icon"></span>
          <span>{{ item.label }}</span>
          <span v-if="item.isComplaint && complaintUnreadCount > 0" class="unread-badge">{{ complaintUnreadCount }}</span>
        </router-link>
      </nav>
      <div class="sidebar-footer">
        <router-link to="/admin/profile" class="user-chip">
          <UserAvatar :name="userStore.user?.name" :avatar="userStore.user?.avatar" size="sm" variant="light" />
          <span class="user-name">{{ userStore.user?.name }}</span>
        </router-link>
        <button class="btn-logout" @click="logout">退出</button>
      </div>
    </aside>
    <div class="main">
      <header class="top-bar">
        <h1 class="page-heading">{{ currentTitle }}</h1>
        <div class="top-bar-meta">小区物业管理</div>
      </header>
      <main class="content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { menuIcon } from '../utils/menuIcons'
import { brandIconSvg } from '../utils/brandIcon'
import UserAvatar from '../components/UserAvatar.vue'
import request from '../api/request'

const complaintUnreadCount = ref(0)
let unreadTimer = null

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const roleLabels = { ADMIN: '管理员', PROPERTY_MANAGER: '物业管家' }
const roleLabel = computed(() => roleLabels[userStore.user?.role] || '')

const navMenus = computed(() =>
    userStore.menus
        .filter(m => m.title !== '缴费记录' && m.path !== '/payments')
        .map(m => ({
          path: '/admin' + m.path,
          label: m.title,
          icon: menuIcon(m.icon),
          isComplaint: m.title === '投诉建议'
        }))
)

async function loadUnreadCount() {
  try {
    const res = await request.get('/complaints/admin/unread-count')
    complaintUnreadCount.value = res || 0
  } catch (e) {
    complaintUnreadCount.value = 0
  }
}

function markAllRead() {
  request.post('/complaints/admin/mark-all-read').catch(() => {})
  complaintUnreadCount.value = 0
}

const currentTitle = computed(() => '工作台')

onMounted(async () => {
  if (userStore.user && !userStore.menus.length) {
    await userStore.fetchMenus(userStore.user.role)
  }
  loadUnreadCount()
  unreadTimer = setInterval(loadUnreadCount, 30000)
})

onUnmounted(() => {
  if (unreadTimer) {
    clearInterval(unreadTimer)
  }
})

function logout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout {
  display: flex;
  min-height: 100vh;
}

.sidebar {
  width: var(--sidebar-width);
  background: linear-gradient(180deg, #064E3B 0%, #047857 55%, #065F46 100%);
  color: #fff;
  display: flex;
  flex-direction: column;
  position: fixed;
  top: 0;
  bottom: 0;
  left: 0;
  z-index: 40;
  box-shadow: 4px 0 24px rgba(4, 120, 87, 0.25);
}

.brand {
  display: flex;
  gap: 12px;
  align-items: center;
  padding: 24px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.12);
}

.brand-icon {
  width: 42px;
  height: 42px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.brand-title { font-weight: 600; font-size: 15px; line-height: 1.3; }
.brand-sub { font-size: 11px; opacity: 0.75; margin-top: 2px; }

.nav {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 16px 12px;
  overflow-y: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
}
.nav::-webkit-scrollbar {
  width: 0;
  height: 0;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 11px 14px;
  border-radius: 10px;
  color: rgba(255, 255, 255, 0.75);
  font-size: 14px;
  transition: all var(--transition);
  border-left: 3px solid transparent;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

.nav-item.router-link-active {
  background: rgba(255, 255, 255, 0.15);
  color: #fff;
  border-left-color: #6EE7B7;
  font-weight: 500;
}

.unread-badge {
  background: #EF4444;
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  padding: 1px 6px;
  border-radius: 10px;
  margin-left: auto;
  min-width: 18px;
  text-align: center;
}

.nav-icon { display: flex; opacity: 0.9; }

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.12);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.user-chip {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border-radius: 10px;
  color: #fff;
  text-decoration: none;
  background: rgba(255, 255, 255, 0.08);
}

.user-chip:hover { background: rgba(255, 255, 255, 0.14); }

.user-name { font-size: 13px; }

.btn-logout {
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.25);
  color: rgba(255, 255, 255, 0.85);
  padding: 8px;
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
  font-family: inherit;
  transition: all var(--transition);
}

.btn-logout:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

.main {
  flex: 1;
  margin-left: var(--sidebar-width);
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.top-bar {
  background: var(--color-surface);
  border-bottom: 1px solid var(--color-border);
  padding: 20px 28px;
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 16px;
}

.page-heading {
  font-size: 20px;
  font-weight: 600;
  color: var(--color-text);
}

.top-bar-meta {
  font-size: 13px;
  color: var(--color-text-secondary);
  padding: 4px 12px;
  background: #ECFDF5;
  border-radius: 20px;
}

.content {
  flex: 1;
  padding: 24px 28px;
}
</style>
