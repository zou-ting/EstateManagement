<template>
  <div>
    <h2 class="page-title">公告通知</h2>

    <!-- 标签切换 -->
    <div class="tab-bar">
      <div
        class="tab-item"
        :class="{ active: activeTab === 'announcement' }"
        @click="activeTab = 'announcement'"
      >
        📢 小区公告
        <span v-if="unreadAnnouncementCount > 0" class="tab-badge">{{ unreadAnnouncementCount }}</span>
      </div>
      <div
        class="tab-item"
        :class="{ active: activeTab === 'inspection' }"
        @click="activeTab = 'inspection'"
      >
        🔍 巡检记录
      </div>
    </div>

    <!-- 公告列表 -->
    <div v-if="activeTab === 'announcement'">
      <!-- 搜索框 -->
      <div class="search-bar">
        <input
          v-model="searchKeyword"
          type="text"
          class="search-input"
          placeholder="搜索公告标题..."
          @input="handleSearch"
        />
        <button class="search-btn" @click="handleSearch">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/>
            <path d="M21 21l-4.35-4.35"/>
          </svg>
        </button>
      </div>

      <div v-if="loading && !filteredList.length" class="loading">加载中...</div>
      <div v-else-if="!filteredList.length" class="empty-card">
        <div class="empty-icon">📢</div>
        <p>暂无公告通知</p>
        <p class="hint">物业发布的公告将在这里显示</p>
      </div>
      <div v-else class="announcement-list">
        <div
          v-for="item in filteredList"
          :key="item.id"
          class="announcement-card"
          @click="viewDetail(item)"
        >
          <div class="card-header">
            <div class="card-title">{{ item.title }}</div>
            <span class="new-badge" v-if="isNew(item)">新</span>
          </div>
          <div class="card-content">
            <p>{{ truncateContent(item.content) }}</p>
          </div>
          <div class="card-footer">
            <span class="publisher">{{ item.publisher || '物业管理员' }}</span>
            <span class="publish-date">{{ formatDate(item.publishDate) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 巡检记录列表 -->
    <div v-if="activeTab === 'inspection'">
      <div v-if="inspectionLoading" class="loading">加载中...</div>
      <div v-else-if="!inspectionRecords.length" class="empty-card">
        <div class="empty-icon">🔍</div>
        <p>暂无巡检记录</p>
        <p class="hint">您房屋的巡检记录将在这里显示</p>
      </div>
      <div v-else class="inspection-list">
        <div
          v-for="item in inspectionRecords"
          :key="item.id"
          class="inspection-card"
          @click="viewInspectionDetail(item)"
        >
          <div class="inspection-header">
            <div class="inspection-title">{{ unitMap[item.roomId] || item.roomId }}</div>
            <span :class="['result-tag', resultClass(item.result)]">{{ resultLabel(item.result) }}</span>
          </div>
          <div class="inspection-info">
            <div class="info-item">
              <span class="info-label">检查员</span>
              <span class="info-value">{{ item.inspectorName || '—' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">得分</span>
              <span class="info-value score" :class="scoreClass(item.score)">{{ item.score }}分</span>
            </div>
            <div class="info-item">
              <span class="info-label">检查日期</span>
              <span class="info-value">{{ item.checkDate || '—' }}</span>
            </div>
          </div>
          <div v-if="item.issues" class="inspection-issues">
            <span class="issues-label">问题描述：</span>
            <span class="issues-content">{{ item.issues }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 公告详情弹窗 -->
    <div v-if="showDetailModal" class="modal-mask" @click.self="showDetailModal = false">
      <div class="modal detail-modal">
        <div class="detail-header">
          <h3>{{ currentAnnouncement.title }}</h3>
          <button class="close-btn" @click="showDetailModal = false">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
        </div>

        <div class="detail-meta">
          <span class="publisher-info">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>
            </svg>
            {{ currentAnnouncement.publisher || '物业管理员' }}
          </span>
          <span class="date-info">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/>
            </svg>
            {{ formatDate(currentAnnouncement.publishDate) }}
          </span>
        </div>

        <div class="detail-content">
          <p>{{ currentAnnouncement.content || '暂无内容' }}</p>
        </div>

        <div class="modal-actions">
          <button class="btn btn-primary" @click="showDetailModal = false">关闭</button>
        </div>
      </div>
    </div>

    <!-- 巡检记录详情弹窗 -->
    <div v-if="showInspectionModal" class="modal-mask" @click.self="showInspectionModal = false">
      <div class="modal detail-modal">
        <div class="detail-header">
          <h3>巡检记录详情</h3>
          <button class="close-btn" @click="showInspectionModal = false">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
        </div>

        <div class="detail-meta">
          <span class="publisher-info">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
            </svg>
            {{ unitMap[currentInspection.roomId] || currentInspection.roomId }}
          </span>
          <span :class="['result-tag', resultClass(currentInspection.result)]">{{ resultLabel(currentInspection.result) }}</span>
        </div>

        <div class="inspection-detail">
          <div class="detail-row">
            <span class="detail-label">检查员</span>
            <span class="detail-value">{{ currentInspection.inspectorName || '—' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">检查得分</span>
            <span class="detail-value score" :class="scoreClass(currentInspection.score)">{{ currentInspection.score }}分</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">检查日期</span>
            <span class="detail-value">{{ currentInspection.checkDate || '—' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">所属楼栋</span>
            <span class="detail-value">{{ buildingMap[currentInspection.buildingId] || '—' }}</span>
          </div>
          <div v-if="currentInspection.issues" class="detail-row issues-row">
            <span class="detail-label">问题描述</span>
            <span class="detail-value issues-text">{{ currentInspection.issues }}</span>
          </div>
        </div>

        <div class="modal-actions">
          <button class="btn btn-primary" @click="showInspectionModal = false">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted ,watch,onActivated} from 'vue'
import { useRoute } from 'vue-router'
import request from '../api/request'
import { useUserStore } from '../stores/user'

const route = useRoute()
const userStore = useUserStore()
// 状态
const activeTab = ref('announcement')
const loading = ref(true)
const announcements = ref([])
const searchKeyword = ref('')
const showDetailModal = ref(false)
const currentAnnouncement = ref({})
const readAnnouncementIds = ref([])

// 巡检记录相关
const inspectionLoading = ref(false)
const inspectionRecords = ref([])
const units = ref([])
const buildings = ref([])
const showInspectionModal = ref(false)
const currentInspection = ref({})

const unitMap = computed(() => {
  const bm = Object.fromEntries(buildings.value.map(b => [b.id, b.buildingName]))
  return Object.fromEntries(units.value.map(u => [u.id, `${bm[u.buildingId] || ''} ${u.unitNo}`.trim()]))
})

const buildingMap = computed(() => Object.fromEntries(buildings.value.map(b => [b.id, b.buildingName])))

// 未读公告数量
const unreadAnnouncementCount = computed(() => {
  return announcements.value.filter(a => !readAnnouncementIds.value.includes(a.id)).length
})

// 巡检结果标签
const RESULT_LABELS = { PASS: '✅ 合格', RECTIFY: '⚠️ 待整改', FAIL: '❌ 不合格' }
function resultLabel(r) { return RESULT_LABELS[r] || r }
function resultClass(r) {
  return { PASS: 'result-pass', RECTIFY: 'result-rectify', FAIL: 'result-fail' }[r] || ''
}
function scoreClass(score) {
  if (score >= 90) return 'score-high'
  if (score >= 70) return 'score-medium'
  return 'score-low'
}

// 从localStorage加载已读记录
function loadReadRecords() {
  const ownerId = userStore.user?.ownerId || 'guest'
  const key = `read_announcements_${ownerId}`
  try {
    readAnnouncementIds.value = JSON.parse(localStorage.getItem(key) || '[]')
  } catch (e) {
    readAnnouncementIds.value = []
  }
}

// 保存已读记录
function saveReadRecord(announcementId) {
  if (!readAnnouncementIds.value.includes(announcementId)) {
    readAnnouncementIds.value.push(announcementId)
    const ownerId = userStore.user?.ownerId || 'guest'
    const key = `read_announcements_${ownerId}`
    localStorage.setItem(key, JSON.stringify(readAnnouncementIds.value))
  }
}

// 判断是否已读
function isRead(item) {
  return readAnnouncementIds.value.includes(item.id)
}

// 筛选后的列表（按发布时间倒序）
const filteredList = computed(() => {
  let list = [...announcements.value]
  
  // 按发布时间倒序排序
  list.sort((a, b) => {
    const dateA = a.publishDate || a.createdAt || ''
    const dateB = b.publishDate || b.createdAt || ''
    return dateB.localeCompare(dateA)
  })
  
  // 搜索过滤
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.toLowerCase()
    list = list.filter(item => 
      item.title?.toLowerCase().includes(keyword) ||
      item.content?.toLowerCase().includes(keyword)
    )
  }
  
  return list
})

// 判断是否是新公告（未读）
function isNew(item) {
  return !isRead(item)
}

// 截取内容
function truncateContent(content) {
  if (!content) return '暂无内容'
  return content.length > 100 ? content.substring(0, 100) + '...' : content
}

// 格式化日期
function formatDate(dateStr) {
  if (!dateStr) return '—'
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 加载公告列表
async function loadAnnouncements() {
  try {
    loading.value = true
    const data = await request.get('/announcements', { params: { status: 'PUBLISHED' } })
    announcements.value = data
  } catch (e) {
    console.error('加载公告失败', e)
    announcements.value = []
  } finally {
    loading.value = false
  }
}

// 搜索
function handleSearch() {
}

// 查看详情
function viewDetail(item) {
  currentAnnouncement.value = item
  showDetailModal.value = true
  saveReadRecord(item.id)
}

// 加载巡检记录
async function loadInspectionRecords() {
  try {
    inspectionLoading.value = true
    const ownerId = userStore.user?.ownerId
    if (!ownerId) {
      inspectionRecords.value = []
      return
    }
    // 加载房屋和楼栋数据
    const [unitsData, buildingsData, records] = await Promise.all([
      request.get('/units'),
      request.get('/buildings'),
      request.get('/inspection-records')
    ])
    units.value = unitsData
    buildings.value = buildingsData
    // 过滤出该业主房屋的巡检记录
    const ownerUnitIds = unitsData
      .filter(u => u.ownerId === ownerId)
      .map(u => u.id)
    inspectionRecords.value = records
      .filter(r => ownerUnitIds.includes(r.roomId))
      .sort((a, b) => (b.checkDate || '').localeCompare(a.checkDate || ''))
  } catch (e) {
    console.error('加载巡检记录失败', e)
    inspectionRecords.value = []
  } finally {
    inspectionLoading.value = false
  }
}

// 查看巡检详情
function viewInspectionDetail(item) {
  currentInspection.value = item
  showInspectionModal.value = true
}

// 加载数据函数（提取为独立函数）
async function loadData() {
  try {
    loading.value = true
    const data = await request.get('/announcements', { params: { status: 'PUBLISHED' } })
    announcements.value = data
  } catch (e) {
    console.error('加载公告失败', e)
    announcements.value = []
  } finally {
    loading.value = false
  }
}
// 监听路由变化
watch(
  () => route.fullPath,
  () => {
    console.log('🔄 Announcements 路由变化，重新加载数据')
    loadData()
    loadInspectionRecords()
  },
  { immediate: false }
)
onActivated(() => {
  console.log('🔄 Announcements 被激活，重新加载数据')
  loadReadRecords()
  loadData()
  loadInspectionRecords()
})

onMounted(async () => {
  loadReadRecords()
  await Promise.all([loadData(), loadInspectionRecords()])
})
</script>

<style scoped>
.page-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
}

/* 搜索框 */
.search-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.search-input {
  flex: 1;
  max-width: 300px;
  padding: 10px 14px;
  border: 1px solid var(--color-border);
  border-radius: 10px;
  font-size: 14px;
}

.search-input:focus {
  border-color: var(--color-primary);
  outline: none;
  box-shadow: 0 0 0 3px rgba(5, 150, 105, 0.1);
}

.search-btn {
  padding: 10px 16px;
  background: var(--color-primary);
  color: #fff;
  border: none;
  border-radius: 10px;
  cursor: pointer;
}

.search-btn:hover {
  background: #047857;
}

/* 加载和空状态 */
.loading {
  text-align: center;
  padding: 40px;
  color: var(--color-text-secondary);
}

.empty-card {
  text-align: center;
  padding: 48px 24px;
  background: #fff;
  border-radius: var(--radius);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-card p {
  margin: 0 0 8px 0;
  color: var(--color-text-secondary);
}

.empty-card p.hint {
  font-size: 13px;
}

/* 公告列表 */
.announcement-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.announcement-card {
  background: #fff;
  border-radius: var(--radius);
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border-left: 4px solid var(--color-primary);
}

.announcement-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text);
}

.new-badge {
  background: var(--color-primary);
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 10px;
}

.card-content {
  margin-bottom: 10px;
}

.card-content p {
  font-size: 14px;
  color: var(--color-text-secondary);
  line-height: 1.6;
  margin: 0;
}

.card-footer {
  display: flex;
  justify-content: space-between;
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
  max-width: 520px;
  max-height: 80vh;
  overflow-y: auto;
}

.detail-modal {
  position: relative;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.detail-header h3 {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
  flex: 1;
}

.close-btn {
  background: transparent;
  border: none;
  color: var(--color-text-secondary);
  cursor: pointer;
  padding: 4px;
}

.close-btn:hover {
  color: var(--color-text);
}

.detail-meta {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--color-border);
}

.publisher-info, .date-info {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--color-text-secondary);
}

.detail-content {
  margin-bottom: 20px;
}

.detail-content p {
  font-size: 15px;
  color: var(--color-text);
  line-height: 1.8;
  margin: 0;
  white-space: pre-wrap;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
}

/* 标签切换 */
.tab-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  background: #fff;
  padding: 6px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  width: fit-content;
}

.tab-item {
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #6B7280;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 6px;
}

.tab-item:hover {
  background: #F3F4F6;
}

.tab-item.active {
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
  color: #fff;
}

.tab-badge {
  background: #EF4444;
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  padding: 1px 7px;
  border-radius: 10px;
  line-height: 1.4;
}

/* 巡检记录列表 */
.inspection-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.inspection-card {
  background: #fff;
  border-radius: var(--radius);
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border-left: 4px solid #3B82F6;
}

.inspection-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.inspection-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.inspection-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text);
}

.result-tag {
  font-size: 12px;
  font-weight: 500;
  padding: 4px 12px;
  border-radius: 12px;
}

.result-pass {
  background: #D1FAE5;
  color: #065F46;
}

.result-rectify {
  background: #FEF3C7;
  color: #92400E;
}

.result-fail {
  background: #FEE2E2;
  color: #991B1B;
}

.inspection-info {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 10px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-size: 12px;
  color: #9CA3AF;
}

.info-value {
  font-size: 14px;
  color: #374151;
  font-weight: 500;
}

.score-high {
  color: #059669;
}

.score-medium {
  color: #D97706;
}

.score-low {
  color: #DC2626;
}

.inspection-issues {
  padding-top: 10px;
  border-top: 1px solid #F3F4F6;
  font-size: 13px;
  color: #6B7280;
}

.issues-label {
  color: #9CA3AF;
}

.issues-content {
  color: #374151;
}

/* 巡检详情 */
.inspection-detail {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  padding-bottom: 10px;
  border-bottom: 1px solid #F3F4F6;
}

.detail-row:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.detail-label {
  font-size: 13px;
  color: #6B7280;
}

.detail-value {
  font-size: 14px;
  color: #1F2937;
  font-weight: 500;
}

.issues-row {
  flex-direction: column;
  gap: 6px;
}

.issues-text {
  white-space: pre-wrap;
  line-height: 1.6;
  font-weight: 400;
}

@media (max-width: 640px) {
  .search-input {
    max-width: 100%;
  }

  .detail-meta {
    flex-direction: column;
    gap: 8px;
  }

  .inspection-info {
    grid-template-columns: 1fr;
  }

  .tab-bar {
    width: 100%;
  }

  .tab-item {
    flex: 1;
    justify-content: center;
  }
}
</style>