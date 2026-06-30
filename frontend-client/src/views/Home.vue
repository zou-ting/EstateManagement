<template>
  <div>
    <!-- 欢迎区域 -->
    <div class="hero">
      <p class="hero-greet">你好，{{ userStore.user?.name || '业主' }}</p>
      <h2 class="hero-title">智慧业主服务</h2>
      <p class="hero-desc">查看房屋、提交报修、维护个人信息</p>
    </div>

    <!-- 我的房屋列表 -->
    <div class="section">
      <h3 class="section-title">我的房屋</h3>
      <div v-if="loading && !units.length" class="loading">加载中...</div>
      <div v-else-if="!units.length" class="empty-card">
        <p>暂未绑定房屋</p>
      </div>
      <div v-else class="unit-list">
        <div
          v-for="unit in units"
          :key="unit.id"
          :class="['unit-card', { active: selectedUnit?.id === unit.id }]"
          @click="selectUnit(unit)"
        >
          <div class="unit-icon">🏠</div>
          <div class="unit-info">
            <div class="unit-no">{{ unit.unitNo }}</div>
            <div class="unit-detail">{{ unit.areaSqm }}㎡ · {{ unit.unitType || '普通住宅' }}</div>
          </div>
          <div v-if="selectedUnit?.id === unit.id" class="unit-check">✓</div>
        </div>
      </div>
    </div>

    <!-- 待办事项聚合 -->
    <div class="section">
      <h3 class="section-title">待办事项</h3>
      <div class="stats-grid">
        <div class="stat-card" @click="goToPage('/pay-online')">
          <div class="stat-icon unpaid">💳</div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.pendingFees }}</span>
            <span class="stat-label">待缴费账单</span>
          </div>
        </div>
        <div class="stat-card" @click="goToPage('/repair')">
          <div class="stat-icon repair">🔧</div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.processingRepairs }}</span>
            <span class="stat-label">进行中报修</span>
          </div>
        </div>
        <div class="stat-card" @click="goToPage('/complaint')">
          <div class="stat-icon complaint">📝</div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.pendingComplaints }}</span>
            <span class="stat-label">待处理投诉</span>
          </div>
        </div>
        <div class="stat-card" @click="goToPage('/decoration')">
          <div class="stat-icon decoration">🎨</div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.pendingDecoration }}</span>
            <span class="stat-label">待审批装修</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 最新公告 -->
    <div class="section">
      <div class="section-header">
        <h3 class="section-title">最新公告</h3>
        <router-link to="/announcements" class="view-all">查看全部 →</router-link>
      </div>
      <div v-if="loading && !announcements.length" class="loading">加载中...</div>
      <div v-else-if="!announcements.length" class="empty-card">
        <p>暂无公告</p>
      </div>
      <div v-else class="announcement-list">
        <div v-for="item in announcements" :key="item.id" class="announcement-card" @click="viewAnnouncement(item)">
          <div class="announcement-badge">{{ item.type || '公告' }}</div>
          <div class="announcement-title">{{ item.title }}</div>
          <div class="announcement-meta">
            <span>{{ item.publishDate || item.createdAt?.slice(0, 10) }}</span>
            <span>{{ item.publisher || '物业管理员' }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 公告详情弹窗 -->
    <div v-if="showAnnouncement" class="modal-mask" @click.self="showAnnouncement = false">
      <div class="modal">
        <h3>{{ currentAnnouncement.title }}</h3>
        <div class="modal-meta">
          <span>{{ currentAnnouncement.publishDate || currentAnnouncement.createdAt?.slice(0, 10) }}</span>
          <span>{{ currentAnnouncement.publisher || '物业管理员' }}</span>
        </div>
        <div class="modal-content">
          <p>{{ currentAnnouncement.content }}</p>
        </div>
        <div class="modal-actions">
          <button class="btn btn-ghost" @click="showAnnouncement = false">关闭</button>
        </div>
      </div>
    </div>

    <!-- 快捷入口 -->
    <div class="section">
      <h3 class="section-title">快捷服务</h3>
      <div class="quick-grid">
        <router-link to="/my-property" class="quick-card">
          <div class="quick-icon dorm">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
          </div>
          <div class="quick-body">
            <div class="quick-title">我的房屋</div>
            <div class="quick-desc">房屋 · 楼栋 · 产权</div>
          </div>
          <span class="quick-arrow">→</span>
        </router-link>
        <router-link to="/repair" class="quick-card accent">
          <div class="quick-icon repair">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/></svg>
          </div>
          <div class="quick-body">
            <div class="quick-title">报修申请</div>
            <div class="quick-desc">设施故障一键上报</div>
          </div>
          <span class="quick-arrow">→</span>
        </router-link>
        <router-link to="/pay-online" class="quick-card">
          <div class="quick-icon pay">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="1" y="4" width="22" height="16" rx="2"/><path d="M1 10h22"/><circle cx="6" cy="14" r="1.5"/><circle cx="18" cy="14" r="1.5"/></svg>
          </div>
          <div class="quick-body">
            <div class="quick-title">在线缴费</div>
            <div class="quick-desc">物业费账单一键支付</div>
          </div>
          <span class="quick-arrow">→</span>
        </router-link>
        <router-link to="/profile" class="quick-card">
          <div class="quick-icon profile">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
          </div>
          <div class="quick-body">
            <div class="quick-title">个人信息</div>
            <div class="quick-desc">资料维护与退出登录</div>
          </div>
          <span class="quick-arrow">→</span>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import request from '../api/request'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const units = ref([])
const selectedUnit = ref(null)
const buildings = ref([])
const announcements = ref([])
const stats = reactive({
  pendingFees: 0,
  processingRepairs: 0,
  pendingComplaints: 0,
  pendingDecoration: 0
})

// 公告弹窗
const showAnnouncement = ref(false)
const currentAnnouncement = ref({})

function goToPage(path) {
  router.push(path)
}

function selectUnit(unit) {
  selectedUnit.value = unit
  loadStats()
}

function viewAnnouncement(item) {
  currentAnnouncement.value = item
  showAnnouncement.value = true
}

// 加载房屋列表
async function loadUnits() {
  const ownerId = userStore.user?.ownerId
  if (!ownerId) return
  try {
    units.value = await request.get(`/units/owner/${ownerId}`)
    if (units.value.length > 0) {
      selectedUnit.value = units.value[0]
    }
  } catch (e) {
    console.error('加载房屋失败', e)
  }
}

// 加载楼栋信息
async function loadBuildings() {
  try {
    buildings.value = await request.get('/buildings')
  } catch (e) {
    console.error('加载楼栋失败', e)
  }
}

// 加载待办事项统计
async function loadStats() {
  const ownerId = userStore.user?.ownerId
  const roomId = selectedUnit.value?.id

  // 重置统计数据
  stats.pendingFees = 0
  stats.processingRepairs = 0
  stats.pendingComplaints = 0
  stats.pendingDecoration = 0

  if (!ownerId) return

  // 加载待缴费账单
  try {
    const fees = await request.get('/property-fees', { params: { roomId } })
    stats.pendingFees = fees.filter(f => f.payStatus === 'UNPAID' || f.payStatus === 'OVERDUE').length
  } catch (e) {
    console.error('加载账单失败', e)
  }

  // 加载进行中报修
  try {
    const repairs = await request.get(`/repairs/owner/${ownerId}`)
    stats.processingRepairs = repairs.filter(r => r.status === 'PROCESSING').length
  } catch (e) {
    console.error('加载报修失败', e)
  }

  // 加载待处理投诉
  try {
    const complaints = await request.get(`/complaints/owner/${ownerId}`)
    stats.pendingComplaints = complaints.filter(c => c.status === 'PENDING' || c.status === 'PROCESSING').length
  } catch (e) {
    console.error('加载投诉失败', e)
  }

  // 加载待审批装修
  try {
    const decorations = await request.get(`/decoration/owner/${ownerId}`)
    stats.pendingDecoration = decorations.filter(d => d.status === 'PENDING').length
  } catch (e) {
    console.error('加载装修失败', e)
  }
}

// 加载最新公告
async function loadAnnouncements() {
  try {
    const data = await request.get('/announcements')
    announcements.value = data.slice(0, 3)
  } catch (e) {
    console.error('加载公告失败', e)
  }
}

onMounted(async () => {
  loading.value = true
  await Promise.all([loadUnits(), loadBuildings(), loadAnnouncements()])
  if (selectedUnit.value) {
    await loadStats()
  }
  loading.value = false
})
</script>

<style scoped>
.hero {
  background: linear-gradient(135deg, #ECFDF5 0%, #D1FAE5 100%);
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  padding: 24px;
  margin-bottom: 20px;
}

.hero-greet {
  font-size: 13px;
  color: var(--color-primary);
  font-weight: 500;
  margin-bottom: 6px;
}

.hero-title {
  font-size: 22px;
  font-weight: 700;
  margin-bottom: 8px;
  color: var(--color-text);
}

.hero-desc {
  font-size: 14px;
  color: var(--color-text-secondary);
  line-height: 1.5;
}

.section {
  margin-bottom: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
  color: var(--color-text);
}

.view-all {
  font-size: 13px;
  color: var(--color-primary);
  text-decoration: none;
}

.loading, .empty-card {
  text-align: center;
  padding: 24px;
  color: var(--color-text-secondary);
  background: #fff;
  border-radius: var(--radius);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.empty-card p {
  margin: 0;
}

/* 房屋列表 */
.unit-list {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding-bottom: 4px;
}

.unit-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  background: #fff;
  border: 2px solid var(--color-border);
  border-radius: var(--radius);
  cursor: pointer;
  transition: all var(--transition);
  min-width: 200px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.unit-card:hover {
  border-color: rgba(5, 150, 105, 0.3);
}

.unit-card.active {
  border-color: var(--color-primary);
  background: rgba(5, 150, 105, 0.05);
}

.unit-icon {
  font-size: 24px;
}

.unit-info {
  flex: 1;
}

.unit-no {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 4px;
}

.unit-detail {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.unit-check {
  width: 24px;
  height: 24px;
  background: var(--color-primary);
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

/* 待办事项统计 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #fff;
  border-radius: var(--radius);
  cursor: pointer;
  transition: all var(--transition);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}

.stat-icon.unpaid {
  background: #FEE2E2;
}

.stat-icon.repair {
  background: #DBEAFE;
}

.stat-icon.complaint {
  background: #FEF3C7;
}

.stat-icon.decoration {
  background: #E0E7FF;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-text);
}

.stat-label {
  font-size: 12px;
  color: var(--color-text-secondary);
  margin-top: 2px;
}

/* 公告列表 */
.announcement-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.announcement-card {
  padding: 14px 16px;
  background: #fff;
  border-radius: var(--radius);
  cursor: pointer;
  transition: all var(--transition);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.announcement-card:hover {
  background: #F8FAFC;
}

.announcement-badge {
  display: inline-block;
  padding: 2px 8px;
  font-size: 11px;
  background: rgba(5, 150, 105, 0.1);
  color: var(--color-primary);
  border-radius: 4px;
  margin-bottom: 8px;
}

.announcement-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 8px;
  color: var(--color-text);
}

.announcement-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: var(--color-text-secondary);
}

/* 弹窗 */
.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.modal {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  width: 90%;
  max-width: 480px;
  max-height: 80vh;
  overflow-y: auto;
}

.modal h3 {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 12px 0;
}

.modal-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--color-border);
}

.modal-content {
  font-size: 14px;
  line-height: 1.7;
  color: var(--color-text);
}

.modal-content p {
  margin: 0;
  white-space: pre-wrap;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 20px;
}

/* 快捷入口 */
.quick-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.quick-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  text-decoration: none;
  color: inherit;
  box-shadow: var(--shadow);
  transition: all var(--transition);
}

.quick-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  border-color: #6EE7B7;
}

.quick-card.accent {
  background: linear-gradient(135deg, #ECFDF5, #D1FAE5);
}

.quick-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: var(--color-primary);
}

.quick-icon.dorm { background: rgba(5, 150, 105, 0.15); }
.quick-icon.repair { background: rgba(217, 119, 6, 0.15); }
.quick-icon.pay { background: rgba(139, 92, 246, 0.15); }
.quick-icon.profile { background: rgba(59, 130, 246, 0.15); }

.quick-body { flex: 1; }

.quick-title {
  font-weight: 600;
  font-size: 15px;
  margin-bottom: 4px;
}

.quick-desc {
  font-size: 13px;
  color: var(--color-text-secondary);
}

.quick-arrow {
  color: var(--color-primary);
  font-size: 18px;
  opacity: 0.6;
}

@media (max-width: 640px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .unit-list {
    flex-direction: column;
  }

  .unit-card {
    min-width: auto;
  }
}
</style>
