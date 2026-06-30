<template>
  <div class="layout">
    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ collapsed: isCollapsed }">
      <!-- 折叠按钮 -->
      <div class="sidebar-header">
        <button class="collapse-btn" @click="toggleCollapse" :title="isCollapsed ? '展开' : '收起'">
          <div class="quick-icon house" :class="{ collapsed: isCollapsed }">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" :stroke="isCollapsed ? '#EF4444' : '#047857'" stroke-width="2">
              <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
              <polyline points="9 22 9 12 15 12 15 22"/>
            </svg>
          </div>
        </button>
        <div v-if="!isCollapsed" class="sidebar-brand">物业管理系统</div>
      </div>

      <!-- 主导航 -->
      <div class="sidebar-section">
        <p v-if="!isCollapsed" class="sidebar-section-title">导航</p>
        <ul class="sidebar-nav">
          <li v-for="tab in visibleTabs" :key="tab.path">
            <router-link 
              :to="tab.path" 
              :title="isCollapsed ? tab.label : ''"
              active-class="active"
            >
              <span class="sidebar-icon" v-html="tab.icon"></span>
              <span v-if="!isCollapsed">{{ tab.label }}</span>
              <span v-if="tab.badge" class="badge">{{ tab.badge }}</span>
            </router-link>
          </li>
        </ul>
      </div>

      <!-- 更多功能 -->
      <div class="sidebar-section">
        <p v-if="!isCollapsed" class="sidebar-section-title">更多功能</p>
        <ul class="sidebar-nav">
          <li v-for="item in visibleMoreItems" :key="item.path">
            <router-link 
              :to="item.path" 
              :title="isCollapsed ? item.label : ''"
              active-class="active"
            >
              <span class="sidebar-icon" v-html="item.icon"></span>
              <span v-if="!isCollapsed">{{ item.label }}</span>
            </router-link>
          </li>
        </ul>
      </div>

      <!-- 底部 -->
      <div class="sidebar-footer">
        <button class="logout-btn" @click="handleLogout" :title="isCollapsed ? '退出登录' : ''">
          <span class="sidebar-icon" v-html="logoutIcon"></span>
          <span v-if="!isCollapsed">退出登录</span>
        </button>
        <p v-if="!isCollapsed" class="version">v2.1.0</p>
      </div>
    </aside>

    <!-- 主内容区 -->
    <div class="content-wrapper" :class="{ expanded: isCollapsed }">
      <header class="app-header">
        <div class="header-inner">
          <div class="brand">
            <div class="brand-icon" v-html="brandIconSvg"></div>
            <div>
              <div class="brand-title">物业管理系统</div>
              <div class="brand-sub">业主端 · {{ userStore.user?.name || '业主' }}</div>
            </div>
          </div>
          <div class="header-right">
            <!-- 点击头像跳转“我的”页面 -->
            <span class="avatar-wrapper" @click="goToProfile" title="我的">
              <UserAvatar :name="userStore.user?.name" :avatar="userStore.user?.avatar" size="md" variant="light" />
            </span>
          </div>
        </div>
      </header>
      <main class="main">
        <router-view v-slot="{ Component, route }">
          <transition name="fade">
            <component :is="Component" :key="route.path" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { brandIconSvg } from '../utils/brandIcon'
import UserAvatar from '../components/UserAvatar.vue'

// 状态
const router = useRouter()
const userStore = useUserStore()
const isCollapsed = ref(false)



// 图标
const logoutIcon = '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>'

// 动态菜单（badge 根据实际数据计算）
const allTabs = computed(() => [
  { 
    path: '/home', 
    label: '首页', 
    icon: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/></svg>' 
  },
  { 
    path: '/pay-online', 
    label: '缴费', 
    icon: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="1" y="4" width="22" height="16" rx="2"/><path d="M1 10h22"/><circle cx="6" cy="14" r="1.5"/><circle cx="18" cy="14" r="1.5"/></svg>',
    badge: userStore.unpaidCount > 0 ? `${userStore.unpaidCount}项` : ''
  },
  { 
    path: '/property-fees', 
    label: '费用详情', 
    icon: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2"/><circle cx="12" cy="12" r="10"/></svg>' 
  },
  { 
    path: '/my-house-purchases', 
    label: '我的购房', 
    icon: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><path d="M9 22V12h6v10"/><path d="M12 6v4"/></svg>' 
  },
  { 
    path: '/my-property', 
    label: '房屋', 
    icon: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>' 
  },
  { 
    path: '/repair', 
    label: '报修', 
    icon: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/></svg>' 
  },
  { 
    path: '/profile', 
    label: '我的', 
    icon: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>' 
  }
])

const allMoreItems = [
  { 
    path: '/my-parking', 
    label: '我的车位', 
    icon: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/><path d="M7 3v18"/><path d="M17 3v18"/><path d="M3 12h18"/></svg>' 
  },
  { 
    path: '/community-houses', 
    label: '小区房源', 
    icon: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>' 
  },
  { 
    path: '/complaint', 
    label: '投诉建议', 
    icon: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 12l2 2 4-4"/><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>' 
  },
  { 
    path: '/visitor-apply', 
    label: '来访登记', 
    icon: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><line x1="20" y1="8" x2="20" y2="14"/><line x1="23" y1="11" x2="17" y2="11"/></svg>' 
  },
  { 
    path: '/announcements', 
    label: '公告通知', 
    icon: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>' 
  }
]

// 权限控制
const visibleTabs = computed(() => allTabs.value)
const visibleMoreItems = computed(() => allMoreItems)

// 方法
function handleLogout() {
  userStore.logout()
  router.push('/login')
}

function toggleCollapse() {
  isCollapsed.value = !isCollapsed.value
  localStorage.setItem('sidebarCollapsed', JSON.stringify(isCollapsed.value))
}

// 点击头像跳转到“我的”页面
function goToProfile() {
  router.push('/profile')
}



// 键盘快捷键：Ctrl + B
function handleKeydown(e) {
  if ((e.ctrlKey || e.metaKey) && e.key === 'b') {
    e.preventDefault()
    toggleCollapse()
  }
}

// 响应式：小屏幕自动折叠
function handleResize() {
  if (window.innerWidth < 768) {
    isCollapsed.value = true
  }
}

// 生命周期
onMounted(() => {
  const saved = localStorage.getItem('sidebarCollapsed')
  if (saved !== null) {
    isCollapsed.value = JSON.parse(saved)
  }
  handleResize()
  window.addEventListener('resize', handleResize)
  window.addEventListener('keydown', handleKeydown)

  userStore.fetchUnpaidCount()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  window.removeEventListener('keydown', handleKeydown)
})

// 监听业主切换
watch(() => userStore.user?.ownerId, () => {
  userStore.fetchUnpaidCount()
})
</script>

<style scoped>
/* ===== 布局 ===== */
.layout {
  min-height: 100vh;
  display: flex;
  background: #F5F5F5;
}

/* ===== 侧边栏 ===== */
.sidebar {
  width: 200px;
  background: #FFFFFF;
  display: flex;
  flex-direction: column;
  position: fixed;
  top: 0;
  left: 0;
  height: 100vh;
  overflow-y: auto;
  overflow-x: hidden;
  box-shadow: 2px 0 12px rgba(0, 0, 0, 0.06);
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 100;
  scrollbar-width: none;
  -ms-overflow-style: none;
}
.sidebar::-webkit-scrollbar {
  width: 0;
  height: 0;
}
.sidebar::-webkit-scrollbar-track {
  background: transparent;
}
.sidebar::-webkit-scrollbar-thumb {
  background: transparent;
}
.sidebar.collapsed {
  width: 64px;
}

/* ===== 侧边栏头部 ===== */
.sidebar-header {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #F3F4F6;
  min-height: 72px;
}
.sidebar-brand {
  font-size: 16px;
  font-weight: 700;
  color: #047857;
  margin-left: 12px;
  white-space: nowrap;
}
.collapse-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: greenyellow;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: green;
  transition: all 0.2s ease;
  flex-shrink: 0;
}
.collapse-btn:hover {
  background:gold;
  color: #047857;
}
.sidebar.collapsed .collapse-btn {
  margin: 0 auto;
}

/* ===== 侧边栏区块 ===== */
.sidebar-section {
  padding: 8px 0;
  flex-shrink: 0;
}
.sidebar-section-title {
  padding: 8px 20px;
  font-size: 11px;
  font-weight: 600;
  color: #9CA3AF;
  text-transform: uppercase;
  letter-spacing: 0.8px;
  margin: 0;
}
.sidebar.collapsed .sidebar-section-title {
  display: none;
}

/* ===== 导航菜单 ===== */
.sidebar-nav {
  list-style: none;
  padding: 0;
  margin: 0;
}
.sidebar-nav li {
  margin-bottom: 2px;
}
.sidebar-nav a {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 10px 20px;
  color: #4B5563;
  text-decoration: none;
  font-size: 14px;
  transition: all 0.2s ease;
  border-left: 3px solid transparent;
  position: relative;
  white-space: nowrap;
}
.sidebar.collapsed .sidebar-nav a {
  padding: 10px 0;
  justify-content: center;
}
.sidebar-nav a:hover {
  background: rgba(5, 150, 105, 0.06);
  color: #059669;
}
.sidebar-nav a.active {
  background: rgba(5, 150, 105, 0.10);
  color: #047857;
  border-left-color: #047857;
  font-weight: 600;
}
.sidebar-nav a.active::after {
  content: '';
  position: absolute;
  right: 12px;
  width: 6px;
  height: 6px;
  background: #047857;
  border-radius: 50%;
}
.sidebar.collapsed .sidebar-nav a.active::after {
  right: 4px;
}
.sidebar-icon {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.sidebar-icon svg {
  width: 20px;
  height: 20px;
}

/* ===== Badge ===== */
.badge {
  margin-left: auto;
  background: #EF4444;
  color: #fff;
  font-size: 10px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 10px;
  line-height: 1.4;
}
.sidebar.collapsed .badge {
  display: none;
}

/* ===== 侧边栏底部 ===== */
.sidebar-footer {
  margin-top: auto;
  padding: 16px 20px;
  border-top: 1px solid #F3F4F6;
}
.sidebar.collapsed .sidebar-footer {
  padding: 16px 0;
}
.logout-btn {
  display: flex;
  align-items: center;
  gap: 14px;
  width: 100%;
  padding: 10px 16px;
  background: transparent;
  border: 1px solid #F3F4F6;
  border-radius: 8px;
  color: #EF4444;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
}
.sidebar.collapsed .logout-btn {
  justify-content: center;
  padding: 10px 0;
  border-color: transparent;
}
.logout-btn:hover {
  background: rgba(239, 68, 68, 0.08);
  border-color: #EF4444;
}
.version {
  text-align: center;
  font-size: 11px;
  color: #9CA3AF;
  margin: 12px 0 0 0;
  letter-spacing: 0.3px;
}
.sidebar.collapsed .version {
  display: none;
}

/* ===== 内容区域 ===== */
.content-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  margin-left: 200px;
  transition: margin-left 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  min-height: 100vh;
}
.content-wrapper.expanded {
  margin-left: 64px;
}

/* ===== 顶部导航 ===== */
.app-header {
  background: linear-gradient(135deg, #047857 0%, #059669 50%, #10B981 100%);
  color: #fff;
  position: sticky;
  top: 0;
  z-index: 50;
  box-shadow: 0 4px 20px rgba(4, 120, 87, 0.3);
}
.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 16px 24px;
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.brand {
  display: flex;
  align-items: center;
  gap: 12px;
}
.brand-icon {
  width: 44px;
  height: 44px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.brand-title {
  font-size: 19px;
  font-weight: 700;
}
.brand-sub {
  font-size: 13px;
  opacity: 0.85;
  margin-top: 2px;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
/* 头像点击区域 */
.avatar-wrapper {
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: opacity 0.2s;
}
.avatar-wrapper:hover {
  opacity: 0.8;
}

/* ===== 主内容 ===== */
.main {
  flex: 1;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 24px;
}

/* ===== 过渡动画 ===== */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}
.fade-enter-from {
  opacity: 0;
  transform: translateY(8px);
}
.fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .sidebar {
    width: 64px;
  }
  .sidebar:not(.collapsed) {
    width: 64px;
  }
  .sidebar .sidebar-nav a span:last-child,
  .sidebar .sidebar-section-title,
  .sidebar .sidebar-brand,
  .sidebar .logout-btn span:last-child,
  .sidebar .version,
  .sidebar .badge {
    display: none;
  }
  .sidebar .sidebar-nav a {
    justify-content: center;
    padding: 10px 0;
  }
  .sidebar .logout-btn {
    justify-content: center;
    padding: 10px 0;
    border-color: transparent;
  }
  .sidebar .collapse-btn {
    margin: 0 auto;
  }
  .content-wrapper {
    margin-left: 64px;
  }
  .brand-sub {
    display: none;
  }
  .brand-title {
    font-size: 16px;
  }
  .main {
    padding: 16px;
  }
}
@media (max-width: 480px) {
  .header-inner {
    padding: 12px 16px;
  }
  .brand-icon {
    width: 36px;
    height: 36px;
  }
  .brand-title {
    font-size: 14px;
  }
  .main {
    padding: 12px;
  }
}
</style>