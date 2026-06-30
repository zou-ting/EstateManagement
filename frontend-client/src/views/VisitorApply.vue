<template>
  <div>
    <h2 class="page-title">访客登记</h2>

    <!-- 状态筛选 -->
    <div class="status-tabs">
      <div
        v-for="tab in statusTabs"
        :key="tab.value"
        :class="['status-tab', { active: currentStatus === tab.value }]"
        @click="currentStatus = tab.value"
      >
        {{ tab.label }}
        <span class="tab-count">{{ getStatusCount(tab.value) }}</span>
      </div>
    </div>

    <!-- 新增预约按钮 -->
    <button class="btn btn-primary submit-btn" @click="showSubmitModal = true">
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M12 5v14M5 12h14"/>
      </svg>
      新增访客预约
    </button>

    <!-- 访客列表 -->
    <div v-if="loading && !filteredList.length" class="loading">加载中...</div>
    <div v-else-if="!filteredList.length" class="empty-card">
      <div class="empty-icon">👥</div>
      <p>暂无访客记录</p>
      <p class="hint">点击上方按钮添加访客预约</p>
    </div>
    <div v-else class="visitor-list">
      <div
        v-for="item in filteredList"
        :key="item.id"
        class="visitor-card"
        @click="viewDetail(item)"
      >
        <div class="visitor-header">
          <div class="visitor-name">{{ item.visitorName }}</div>
          <div class="visitor-status" :class="getStatusClass(item.status)">
            {{ getStatusText(item.status) }}
          </div>
        </div>
        <div class="visitor-meta">
          <span class="visitor-phone">{{ item.visitorPhone || '—' }}</span>
          <span class="visitor-date">{{ item.visitDate || '—' }}</span>
        </div>
        <div v-if="item.reason" class="visitor-reason">
          {{ item.reason }}
        </div>
      </div>
    </div>

    <!-- 新增预约弹窗 -->
    <div v-if="showSubmitModal" class="modal-mask" @click.self="showSubmitModal = false">
      <div class="modal">
        <h3>新增访客预约</h3>
        <div class="form-group">
          <label>访客姓名 *</label>
          <input v-model="submitForm.visitorName" class="input" maxlength="50" placeholder="请输入访客姓名" />
        </div>
        <div class="form-group">
          <label>联系电话</label>
          <input v-model="submitForm.visitorPhone" class="input" maxlength="11" pattern="[0-9]*" inputmode="numeric" placeholder="请输入11位手机号" />
        </div>
        <div class="form-group">
          <label>来访日期 *</label>
          <input v-model="submitForm.visitDate" class="input" type="date" />
        </div>
        <div class="form-group">
          <label>来访事由</label>
          <textarea v-model="submitForm.reason" class="input textarea" maxlength="200" rows="3" placeholder="请输入来访事由"></textarea>
        </div>
        <p v-if="submitError" class="error">{{ submitError }}</p>
        <div class="modal-actions">
          <button class="btn btn-ghost" @click="showSubmitModal = false">取消</button>
          <button class="btn btn-primary" :disabled="submitting" @click="submitVisitor">
            {{ submitting ? '提交中...' : '确认预约' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 详情弹窗 -->
    <div v-if="showDetailModal" class="modal-mask" @click.self="showDetailModal = false">
      <div class="modal detail-modal">
        <div class="detail-header">
          <h3>{{ currentVisitor.visitorName }}</h3>
          <div class="detail-status" :class="getStatusClass(currentVisitor.status)">
            {{ getStatusText(currentVisitor.status) }}
          </div>
        </div>

        <div class="detail-info">
          <div class="info-row">
            <label>联系电话</label>
            <span>{{ currentVisitor.visitorPhone || '—' }}</span>
          </div>
          <div class="info-row">
            <label>来访日期</label>
            <span>{{ currentVisitor.visitDate || '—' }}</span>
          </div>
          <div class="info-row">
            <label>预约时间</label>
            <span>{{ formatDateTime(currentVisitor.createdAt) }}</span>
          </div>
        </div>

        <div v-if="currentVisitor.reason" class="detail-desc">
          <label>来访事由</label>
          <p>{{ currentVisitor.reason }}</p>
        </div>

        <!-- 状态说明 -->
        <div class="status-tips">
          <template v-if="currentVisitor.status === 'PENDING'">
            <div class="tip tip-warn">
              <span class="tip-icon">⏳</span>
              <span>等待物业审核中，请耐心等候</span>
            </div>
          </template>
          <template v-else-if="currentVisitor.status === 'APPROVED'">
            <div class="tip tip-success">
              <span class="tip-icon">✅</span>
              <span>预约已通过，访客可正常来访</span>
            </div>
          </template>
          <template v-else-if="currentVisitor.status === 'REJECTED'">
            <div class="tip tip-danger">
              <span class="tip-icon">❌</span>
              <span>预约已被拒绝，如有疑问请联系物业</span>
            </div>
          </template>
          <template v-else-if="currentVisitor.status === 'COMPLETED'">
            <div class="tip tip-success">
              <span class="tip-icon">🏁</span>
              <span>访客已完成来访</span>
            </div>
          </template>
        </div>

        <div class="modal-actions">
          <button class="btn btn-ghost" @click="showDetailModal = false">关闭</button>
          <button
            v-if="currentVisitor.status === 'PENDING'"
            class="btn btn-danger"
            @click="cancelVisitor"
            :disabled="canceling"
          >
            {{ canceling ? '取消中...' : '取消预约' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted,watch,onActivated } from 'vue'
import { useRoute } from 'vue-router' 
import request from '../api/request'
import { useUserStore } from '../stores/user'

const route = useRoute()
const userStore = useUserStore()

// 状态
const loading = ref(true)
const visitors = ref([])
const currentStatus = ref('ALL')

// 弹窗
const showSubmitModal = ref(false)
const showDetailModal = ref(false)
const currentVisitor = ref({})
const submitting = ref(false)
const canceling = ref(false)
const submitError = ref('')

// 提交表单
const submitForm = reactive({
  visitorName: '',
  visitorPhone: '',
  visitDate: '',
  reason: ''
})

// 状态筛选标签
const statusTabs = [
  { label: '全部', value: 'ALL' },
  { label: '待审批', value: 'PENDING' },
  { label: '已批准', value: 'APPROVED' },
  { label: '已拒绝', value: 'REJECTED' },
  { label: '已完成', value: 'COMPLETED' }
]

// 状态映射
const STATUS_MAP = {
  PENDING: '待审批',
  APPROVED: '已批准',
  REJECTED: '已拒绝',
  COMPLETED: '已完成'
}

// 筛选后的列表
const filteredList = computed(() => {
  if (currentStatus.value === 'ALL') {
    return visitors.value
  }
  return visitors.value.filter(v => v.status === currentStatus.value)
})

// 获取各状态数量
function getStatusCount(status) {
  if (status === 'ALL') return visitors.value.length
  return visitors.value.filter(v => v.status === status).length
}

function getStatusText(status) {
  return STATUS_MAP[status] || status
}

function getStatusClass(status) {
  switch (status) {
    case 'PENDING': return 'status-pending'
    case 'APPROVED': return 'status-approved'
    case 'REJECTED': return 'status-rejected'
    case 'COMPLETED': return 'status-completed'
    default: return ''
  }
}

function formatDateTime(dateStr) {
  if (!dateStr) return '—'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}
// 加载数据函数（提取为独立函数）
async function loadData() {
  loading.value = true
  const ownerId = userStore.user?.ownerId
  if (!ownerId) {
    loading.value = false
    return
  }
  try {
    visitors.value = await request.get(`/visitors/owner/${ownerId}`)
  } catch (e) {
    console.error('加载访客记录失败', e)
    visitors.value = []
  } finally {
    loading.value = false
  }
}

// 监听路由变化
watch(
  () => route.fullPath,
  () => {
    console.log('🔄 VisitorApply 路由变化，重新加载数据')
    loadData()
  },
  { immediate: false }
)

// 使用 onActivated 处理 keep-alive 缓存情况
onActivated(() => {
  console.log('🔄 VisitorApply 被激活，重新加载数据')
  loadData()
})

// 加载访客列表
async function loadVisitors() {
  const ownerId = userStore.user?.ownerId
  if (!ownerId) return
  try {
    visitors.value = await request.get(`/visitors/owner/${ownerId}`)
  } catch (e) {
    console.error('加载访客记录失败', e)
  }
}

// 提交预约
async function submitVisitor() {
  submitError.value = ''

  if (!submitForm.visitorName?.trim()) {
    submitError.value = '请输入访客姓名'
    return
  }
  if (!submitForm.visitDate) {
    submitError.value = '请选择来访日期'
    return
  }
  if (submitForm.visitorPhone && !/^1[3-9]\d{9}$/.test(submitForm.visitorPhone)) {
    submitError.value = '请输入正确的11位手机号'
    return
  }

  submitting.value = true
  try {
    await request.post('/visitors', {
      ownerId: userStore.user.ownerId,
      visitorName: submitForm.visitorName.trim(),
      visitorPhone: submitForm.visitorPhone?.trim() || null,
      visitDate: submitForm.visitDate,
      reason: submitForm.reason?.trim() || null,
      status: 'PENDING'
    })

    // 重置表单
    submitForm.visitorName = ''
    submitForm.visitorPhone = ''
    submitForm.visitDate = ''
    submitForm.reason = ''

    showSubmitModal.value = false
    await loadVisitors()
  } catch (e) {
    submitError.value = e.message || '提交失败'
  } finally {
    submitting.value = false
  }
}

// 查看详情
function viewDetail(item) {
  currentVisitor.value = item
  showDetailModal.value = true
}

// 取消预约
async function cancelVisitor() {
  if (currentVisitor.value.status !== 'PENDING') return

  canceling.value = true
  try {
    await request.delete(`/visitors/${currentVisitor.value.id}`)
    showDetailModal.value = false
    await loadVisitors()
  } catch (e) {
    console.error('取消预约失败', e)
  } finally {
    canceling.value = false
  }
}

onMounted(async () => {
  await loadData()
})
</script>

<style scoped>
.page-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
}

/* 状态筛选 */
.status-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
  overflow-x: auto;
}

.status-tab {
  padding: 8px 14px;
  background: #fff;
  border-radius: 10px;
  font-size: 13px;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
  white-space: nowrap;
}

.status-tab.active {
  background: var(--color-primary);
  color: #fff;
}

.tab-count {
  margin-left: 6px;
  font-size: 12px;
  background: rgba(0, 0, 0, 0.1);
  padding: 2px 6px;
  border-radius: 8px;
}

.status-tab.active .tab-count {
  background: rgba(255, 255, 255, 0.2);
}

/* 提交按钮 */
.submit-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
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

/* 访客列表 */
.visitor-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.visitor-card {
  background: #fff;
  border-radius: var(--radius);
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.visitor-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.visitor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.visitor-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text);
}

.visitor-status {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.status-pending {
  background: rgba(251, 191, 36, 0.15);
  color: #d97706;
}

.status-approved {
  background: rgba(5, 150, 105, 0.15);
  color: #059669;
}

.status-rejected {
  background: rgba(239, 68, 68, 0.15);
  color: #dc2626;
}

.status-completed {
  background: rgba(59, 130, 246, 0.15);
  color: #2563eb;
}

.visitor-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-bottom: 8px;
}

.visitor-reason {
  font-size: 13px;
  color: var(--color-text);
  line-height: 1.5;
  padding: 8px 12px;
  background: rgba(0, 0, 0, 0.03);
  border-radius: 8px;
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
  max-height: 85vh;
  overflow-y: auto;
}

.modal h3 {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 20px 0;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 8px;
}

.input, .select {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid var(--color-border);
  border-radius: 10px;
  font-size: 14px;
  font-family: inherit;
  background: #fff;
}

.input:focus, .select:focus {
  border-color: var(--color-primary);
  outline: none;
  box-shadow: 0 0 0 3px rgba(5, 150, 105, 0.1);
}

.textarea {
  resize: vertical;
  min-height: 80px;
}

.error {
  color: #dc2626;
  font-size: 13px;
  margin-bottom: 12px;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 20px;
}

/* 详情弹窗 */
.detail-modal {
  max-width: 520px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.detail-header h3 {
  margin: 0;
}

.detail-status {
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.detail-info {
  margin-bottom: 16px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px dashed var(--color-border);
}

.info-row:last-child {
  border-bottom: none;
}

.info-row label {
  font-size: 13px;
  color: var(--color-text-secondary);
}

.info-row span {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text);
}

.detail-desc {
  margin-bottom: 16px;
}

.detail-desc label {
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-bottom: 8px;
  display: block;
}

.detail-desc p {
  font-size: 14px;
  color: var(--color-text);
  line-height: 1.6;
  margin: 0;
  padding: 12px;
  background: rgba(0, 0, 0, 0.03);
  border-radius: 8px;
}

/* 状态提示 */
.status-tips {
  margin-bottom: 16px;
}

.tip {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  border-radius: 10px;
  font-size: 13px;
}

.tip-warn {
  background: rgba(251, 191, 36, 0.1);
  color: #92400e;
}

.tip-success {
  background: rgba(5, 150, 105, 0.1);
  color: #065f46;
}

.tip-danger {
  background: rgba(239, 68, 68, 0.1);
  color: #991b1b;
}

.tip-icon {
  font-size: 16px;
}

/* 取消按钮 */
.btn-danger {
  background: #dc2626;
  color: #fff;
  border: none;
}

.btn-danger:hover {
  background: #b91c1c;
}

@media (max-width: 640px) {
  .status-tabs {
    flex-wrap: wrap;
  }

  .status-tab {
    flex: 1;
    min-width: 70px;
    text-align: center;
  }
}
</style>