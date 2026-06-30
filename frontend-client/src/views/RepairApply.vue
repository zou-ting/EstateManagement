<template>
  <div>
    <h2 class="page-title">报修管理</h2>

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

    <!-- 提交报修按钮 -->
    <button class="btn btn-primary submit-btn" @click="showSubmitModal = true">
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M12 5v14M5 12h14"/>
      </svg>
      提交报修
    </button>

    <!-- 报修列表 -->
    <div v-if="loading && !filteredList.length" class="loading">加载中...</div>
    <div v-else-if="!filteredList.length" class="empty-card">
      <div class="empty-icon">🔧</div>
      <p>暂无报修记录</p>
      <p class="hint">点击上方按钮提交报修申请</p>
    </div>
    <div v-else class="repair-list">
      <div
        v-for="item in filteredList"
        :key="item.id"
        class="repair-card"
        @click="viewDetail(item)"
      >
        <div class="repair-header">
          <div class="repair-title">{{ item.title }}</div>
          <div class="repair-status" :class="getStatusClass(item.status)">
            {{ getStatusText(item.status) }}
          </div>
        </div>
        <div class="repair-meta">
          <span class="repair-room">{{ getUnitName(item.roomId) }}</span>
          <span class="repair-priority" :class="getPriorityClass(item.priority)">
            {{ getPriorityText(item.priority) }}
          </span>
        </div>
        <div class="repair-time">
          <span>提交：{{ item.submitDate || '—' }}</span>
          <span v-if="item.finishDate">完成：{{ item.finishDate }}</span>
        </div>
      </div>
    </div>

    <!-- 提交报修弹窗 -->
    <div v-if="showSubmitModal" class="modal-mask" @click.self="showSubmitModal = false">
      <div class="modal">
        <h3>提交报修申请</h3>
        <div class="form-group">
          <label>选择房屋 *</label>
          <select v-model="submitForm.roomId" class="select" required>
            <option value="">请选择房屋</option>
            <option v-for="unit in units" :key="unit.id" :value="unit.id">
              {{ unit.unitNo }} - {{ unit.unitType || '普通住宅' }}
            </option>
          </select>
        </div>
        <div class="form-group">
          <label>报修标题 *</label>
          <input v-model="submitForm.title" class="input" maxlength="100" placeholder="如：水龙头漏水" />
        </div>
        <div class="form-group">
          <label>问题描述</label>
          <textarea v-model="submitForm.description" class="input textarea" maxlength="500" rows="4" placeholder="请描述具体问题"></textarea>
        </div>
        <div class="form-group">
          <label>优先级 *</label>
          <select v-model="submitForm.priority" class="select">
            <option value="URGENT">紧急（需要立即处理）</option>
            <option value="HIGH">高（尽快处理）</option>
            <option value="MEDIUM">中（正常处理）</option>
            <option value="LOW">低（可稍后处理）</option>
          </select>
        </div>
        <p v-if="submitError" class="error">{{ submitError }}</p>
        <div class="modal-actions">
          <button class="btn btn-ghost" @click="showSubmitModal = false">取消</button>
          <button class="btn btn-primary" :disabled="submitting" @click="submitRepair">
            {{ submitting ? '提交中...' : '提交报修' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 报修详情弹窗 -->
    <div v-if="showDetailModal" class="modal-mask" @click.self="showDetailModal = false">
      <div class="modal detail-modal">
        <div class="detail-header">
          <h3>{{ currentRepair.title }}</h3>
          <div class="detail-status" :class="getStatusClass(currentRepair.status)">
            {{ getStatusText(currentRepair.status) }}
          </div>
        </div>

        <div class="detail-info">
          <div class="info-row">
            <label>房屋</label>
            <span>{{ getUnitName(currentRepair.roomId) }}</span>
          </div>
          <div class="info-row">
            <label>优先级</label>
            <span :class="getPriorityClass(currentRepair.priority)">
              {{ getPriorityText(currentRepair.priority) }}
            </span>
          </div>
          <div class="info-row">
            <label>提交时间</label>
            <span>{{ currentRepair.submitDate || '—' }}</span>
          </div>
          <div class="info-row" v-if="currentRepair.finishDate">
            <label>完成时间</label>
            <span>{{ currentRepair.finishDate }}</span>
          </div>
        </div>

        <div class="detail-desc">
          <label>问题描述</label>
          <p>{{ currentRepair.description || '无详细描述' }}</p>
        </div>

        <!-- 处理进度 -->
        <div class="progress-section">
          <label>处理进度</label>
          <div class="progress-steps">
            <div class="progress-step" :class="{ active: true, done: currentRepair.status !== 'PENDING' }">
              <div class="step-dot"></div>
              <span>提交报修</span>
            </div>
            <div class="progress-step" :class="{ active: currentRepair.status !== 'PENDING', done: currentRepair.status === 'DONE' }">
              <div class="step-dot"></div>
              <span>正在处理</span>
            </div>
            <div class="progress-step" :class="{ active: currentRepair.status === 'DONE' }">
              <div class="step-dot"></div>
              <span>已完成</span>
            </div>
          </div>
        </div>

        <!-- 处理备注 -->
        <div v-if="currentRepair.remark" class="detail-remark">
          <label>处理备注</label>
          <p>{{ currentRepair.remark }}</p>
        </div>

        <div class="modal-actions">
          <button class="btn btn-ghost" @click="showDetailModal = false">关闭</button>
          <button
            v-if="currentRepair.status === 'PENDING'"
            class="btn btn-danger"
            @click="cancelRepair"
            :disabled="canceling"
          >
            {{ canceling ? '取消中...' : '取消报修' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import request from '../api/request'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()

// 状态
const loading = ref(true)
const repairs = ref([])
const units = ref([])
const currentStatus = ref('ALL')

// 弹窗
const showSubmitModal = ref(false)
const showDetailModal = ref(false)
const currentRepair = ref({})
const submitting = ref(false)
const canceling = ref(false)
const submitError = ref('')

// 提交表单
const submitForm = reactive({
  roomId: '',
  title: '',
  description: '',
  priority: 'MEDIUM'
})

// 状态筛选标签
const statusTabs = [
  { label: '全部', value: 'ALL' },
  { label: '待处理', value: 'PENDING' },
  { label: '处理中', value: 'PROCESSING' },
  { label: '已完成', value: 'DONE' }
]

// 状态映射
const STATUS_MAP = {
  PENDING: '待处理',
  PROCESSING: '处理中',
  DONE: '已完成'
}

// 优先级映射
const PRIORITY_MAP = {
  URGENT: '紧急',
  HIGH: '高',
  MEDIUM: '中',
  LOW: '低'
}

// 筛选后的列表
const filteredList = computed(() => {
  if (currentStatus.value === 'ALL') {
    return repairs.value
  }
  return repairs.value.filter(r => r.status === currentStatus.value)
})

// 获取各状态数量
function getStatusCount(status) {
  if (status === 'ALL') return repairs.value.length
  return repairs.value.filter(r => r.status === status).length
}

function getStatusText(status) {
  return STATUS_MAP[status] || status
}

function getStatusClass(status) {
  switch (status) {
    case 'PENDING': return 'status-pending'
    case 'PROCESSING': return 'status-processing'
    case 'DONE': return 'status-done'
    default: return ''
  }
}

function getPriorityText(priority) {
  return PRIORITY_MAP[priority] || '中'
}

function getPriorityClass(priority) {
  switch (priority) {
    case 'URGENT': return 'priority-urgent'
    case 'HIGH': return 'priority-high'
    case 'MEDIUM': return 'priority-medium'
    case 'LOW': return 'priority-low'
    default: return 'priority-medium'
  }
}

function getUnitName(roomId) {
  const unit = units.value.find(u => u.id === roomId)
  return unit?.unitNo || `房屋#${roomId}`
}

// 加载房屋列表
async function loadUnits() {
  const ownerId = userStore.user?.ownerId
  if (!ownerId) return
  try {
    units.value = await request.get(`/units/owner/${ownerId}`)
    if (units.value.length > 0) {
      submitForm.roomId = units.value[0].id
    }
  } catch (e) {
    console.error('加载房屋失败', e)
  }
}

// 加载报修列表
async function loadRepairs() {
  const ownerId = userStore.user?.ownerId
  if (!ownerId) return
  try {
    repairs.value = await request.get(`/repairs/owner/${ownerId}`)
  } catch (e) {
    console.error('加载报修失败', e)
  }
}

// 提交报修
async function submitRepair() {
  submitError.value = ''

  if (!submitForm.roomId) {
    submitError.value = '请选择房屋'
    return
  }
  if (!submitForm.title?.trim()) {
    submitError.value = '请填写报修标题'
    return
  }

  submitting.value = true
  try {
    await request.post('/repairs', {
      ownerId: userStore.user.ownerId,
      roomId: submitForm.roomId,
      title: submitForm.title.trim(),
      description: submitForm.description?.trim() || null,
      priority: submitForm.priority,
      status: 'PENDING'
    })

    // 重置表单
    submitForm.title = ''
    submitForm.description = ''
    submitForm.priority = 'MEDIUM'
    if (units.value.length > 0) {
      submitForm.roomId = units.value[0].id
    }

    showSubmitModal.value = false
    await loadRepairs()
  } catch (e) {
    submitError.value = e.message || '提交失败'
  } finally {
    submitting.value = false
  }
}

// 查看详情
function viewDetail(item) {
  currentRepair.value = item
  showDetailModal.value = true
}

// 取消报修
async function cancelRepair() {
  if (currentRepair.value.status !== 'PENDING') return

  canceling.value = true
  try {
    await request.delete(`/repairs/${currentRepair.value.id}`)
    showDetailModal.value = false
    await loadRepairs()
  } catch (e) {
    console.error('取消报修失败', e)
  } finally {
    canceling.value = false
  }
}

onMounted(async () => {
  loading.value = true
  await Promise.all([loadUnits(), loadRepairs()])
  loading.value = false
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
  gap: 12px;
  margin-bottom: 16px;
}

.status-tab {
  padding: 8px 16px;
  background: #fff;
  border-radius: 10px;
  font-size: 13px;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
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

/* 报修列表 */
.repair-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.repair-card {
  background: #fff;
  border-radius: var(--radius);
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.repair-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.repair-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.repair-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text);
}

.repair-status {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.status-pending {
  background: rgba(251, 191, 36, 0.15);
  color: #d97706;
}

.status-processing {
  background: rgba(59, 130, 246, 0.15);
  color: #2563eb;
}

.status-done {
  background: rgba(5, 150, 105, 0.15);
  color: #059669;
}

.repair-meta {
  display: flex;
  gap: 12px;
  margin-bottom: 8px;
}

.repair-room {
  font-size: 13px;
  color: var(--color-text-secondary);
}

.repair-priority {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
}

.priority-urgent {
  background: rgba(239, 68, 68, 0.15);
  color: #dc2626;
}

.priority-high {
  background: rgba(249, 115, 22, 0.15);
  color: #ea580c;
}

.priority-medium {
  background: rgba(251, 191, 36, 0.15);
  color: #d97706;
}

.priority-low {
  background: rgba(107, 114, 128, 0.15);
  color: #6b7280;
}

.repair-time {
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
  min-height: 100px;
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
  padding: 8px 0;
  border-bottom: 1px dashed var(--color-border);
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
  white-space: pre-wrap;
}

/* 处理进度 */
.progress-section {
  margin-bottom: 16px;
}

.progress-section label {
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-bottom: 12px;
  display: block;
}

.progress-steps {
  display: flex;
  align-items: center;
  gap: 8px;
}

.progress-step {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 12px;
  color: var(--color-text-secondary);
  background: rgba(0, 0, 0, 0.03);
}

.progress-step.active {
  color: var(--color-primary);
  background: rgba(5, 150, 105, 0.1);
}

.progress-step.done {
  background: rgba(5, 150, 105, 0.15);
}

.step-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--color-text-secondary);
}

.progress-step.active .step-dot {
  background: var(--color-primary);
}

/* 处理备注 */
.detail-remark {
  margin-bottom: 16px;
  padding: 12px;
  background: rgba(5, 150, 105, 0.05);
  border-radius: 10px;
}

.detail-remark label {
  font-size: 13px;
  color: var(--color-primary);
  margin-bottom: 8px;
  display: block;
}

.detail-remark p {
  font-size: 14px;
  color: var(--color-text);
  margin: 0;
  white-space: pre-wrap;
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
    min-width: 80px;
    text-align: center;
  }
}
</style>