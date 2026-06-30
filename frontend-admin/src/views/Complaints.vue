<template>
  <div>
    <!-- 工具栏 -->
    <div class="toolbar">
      <h2 class="page-title">投诉建议</h2>
      <div class="actions">
        <input v-model="keyword" class="input search" placeholder="搜索标题..." @keyup.enter="load" />
        <select v-model="typeFilter" class="select" @change="load">
          <option :value="null">全部类型</option>
          <option value="COMPLAINT">📢 投诉</option>
          <option value="SUGGESTION">💡 建议</option>
        </select>
        <select v-model="statusFilter" class="select" @change="load">
          <option :value="null">全部状态</option>
          <option value="PENDING">⏳ 待处理</option>
          <option value="PROCESSING">🔄 处理中</option>
          <option value="DONE">✅ 已完成</option>
          <option value="APPROVED">👍 已采纳</option>
          <option value="REJECTED">🚫 已驳回</option>
        </select>
        <button class="btn btn-primary" @click="openForm()">+ 新增</button>
      </div>
    </div>

    <!-- 统计栏 -->
    <div class="stats-bar">
      <span>共 <strong>{{ filteredList.length }}</strong> 条</span>
      <span>⏳ 待处理 <strong>{{ statusStats.pending }}</strong></span>
      <span>🔄 处理中 <strong>{{ statusStats.processing }}</strong></span>
      <span>✅ 已完成 <strong>{{ statusStats.done }}</strong></span>
      <span>👍 已采纳 <strong>{{ statusStats.approved }}</strong></span>
      <span>🚫 已驳回 <strong>{{ statusStats.rejected }}</strong></span>
    </div>

    <div class="card">
      <table class="table">
        <thead>
        <tr>
          <th>序号</th>
          <th>标题</th>
          <th>类型</th>
          <th>业主</th>
          <th>房屋</th>
          <th>状态</th>
          <th>回复</th>
          <th>创建时间</th>
          <th>操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="(item, index) in filteredList" :key="item.id">
          <td>{{ index + 1 }}</td>
          <td><strong>{{ item.title }}</strong></td>
          <td><span :class="['tag', item.type === 'COMPLAINT' ? 'tag-complaint' : 'tag-suggestion']">{{ typeLabel(item.type) }}</span></td>
          <td>{{ ownerLabel(item.ownerId) }}</td>
          <td>{{ unitLabel(item.roomId) }}</td>
          <td><span :class="['tag', statusClass(item.status)]">{{ statusLabel(item.status) }}</span></td>
          <td>{{ item.replyCount > 0 ? `💬 ${item.replyCount}条回复` : '-' }}</td>
          <td>{{ formatDate(item.createdAt) }}</td>
          <td>
            <!-- 待处理/处理中 → 回复 -->
            <button v-if="item.status === 'PENDING' || item.status === 'PROCESSING'" class="btn btn-primary" @click="openReply(item)">回复</button>
            <!-- 已采纳 → 只读 -->
            <button v-else-if="item.status === 'APPROVED'" class="btn btn-ghost" disabled style="opacity:0.5;cursor:not-allowed;">已采纳</button>
            <!-- 已完成/已驳回 → 编辑 -->
            <button v-else class="btn btn-ghost" @click="openForm(item)">编辑</button>
            <div class="dropdown" :class="{ 'show': activeDropdown === item.id }">
              <button class="btn btn-ghost dropdown-toggle" @click.stop="toggleDropdown(item.id)">
                更多 ▼
              </button>
              <div class="dropdown-menu">
                <button class="dropdown-item" @click="openForm(item)">编辑</button>
                <button v-if="item.status !== 'APPROVED'" class="dropdown-item" @click="openStatusChange(item)">变更状态</button>
                <button class="dropdown-item text-danger" @click="remove(item)">删除</button>
              </div>
            </div>
          </td>
        </tr>
        <tr v-if="!filteredList.length"><td colspan="9" class="empty">暂无数据</td></tr>
        </tbody>
      </table>
    </div>

    <!-- 新增/编辑弹窗 -->
    <div v-if="showModal" class="modal-mask" @click.self="showModal = false">
      <div class="modal">
        <h3>{{ form.id ? '✏️ 编辑投诉' : '➕ 新增投诉' }}</h3>
        <div class="form-group">
          <label>标题 <span class="required">*</span></label>
          <input v-model="form.title" class="input" maxlength="100" placeholder="请输入标题" />
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>类型</label>
            <select v-model="form.type" class="select" @change="onTypeChange">
              <option value="COMPLAINT">📢 投诉</option>
              <option value="SUGGESTION">💡 建议</option>
            </select>
          </div>
          <div class="form-group">
            <label>状态</label>
            <select v-model="form.status" class="select">
              <!-- 建议类型 -->
              <template v-if="form.type === 'SUGGESTION'">
                <option value="PENDING">⏳ 待处理</option>
                <option value="APPROVED">👍 已采纳</option>
                <option value="REJECTED">🚫 已驳回</option>
              </template>
              <!-- 投诉类型 -->
              <template v-else>
                <option value="PENDING">⏳ 待处理</option>
                <option value="PROCESSING">🔄 处理中</option>
                <option value="DONE">✅ 已完成</option>
                <option value="REJECTED">🚫 已驳回</option>
              </template>
            </select>
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>业主 <span class="required">*</span></label>
            <select v-model.number="form.ownerId" class="select">
              <option v-for="o in owners" :key="o.id" :value="o.id">{{ o.name }}</option>
            </select>
          </div>
          <div class="form-group">
            <label>房屋 <span class="required">*</span></label>
            <select v-model.number="form.roomId" class="select">
              <option v-for="u in units" :key="u.id" :value="u.id">{{ unitOptionLabel(u) }}</option>
            </select>
          </div>
        </div>
        <div class="form-group">
          <label>内容</label>
          <textarea v-model="form.content" class="input textarea" rows="4" placeholder="请详细描述问题或建议"></textarea>
        </div>
        <!-- 历史回复列表（编辑弹窗） -->
        <div v-if="editReplyList.length > 0" class="reply-history">
          <label>回复记录</label>
          <div v-for="reply in editReplyList" :key="reply.id" class="reply-history-item">
            <div class="reply-history-header">
              <span class="reply-time">{{ formatDate(reply.createdAt) }}</span>
            </div>
            <div class="reply-history-content">{{ reply.replyContent }}</div>
          </div>
        </div>
        <p v-if="formError" class="err">{{ formError }}</p>
        <div class="modal-actions">
          <button class="btn btn-ghost" @click="showModal = false">取消</button>
          <button class="btn btn-primary" @click="save">保存</button>
        </div>
      </div>
    </div>

    <!-- 回复弹窗 -->
    <div v-if="showReplyModal" class="modal-mask" @click.self="showReplyModal = false">
      <div class="modal reply-modal">
        <h3>💬 回复投诉</h3>
        <div class="reply-item">
          <span class="reply-item-title">{{ replyItem?.title }}</span>
          <span class="reply-item-status" :class="statusClass(replyItem?.status)">{{ statusLabel(replyItem?.status) }}</span>
        </div>
        <!-- 历史回复列表 -->
        <div v-if="replyList.length > 0" class="reply-history">
          <label>历史回复</label>
          <div v-for="(reply, idx) in replyList" :key="reply.id" class="reply-history-item">
          <div class="reply-history-header">
            <span class="reply-role" :class="reply.ownerId ? 'role-owner' : 'role-admin'">{{ replyLabel(reply) }}</span>
            <span class="reply-read" :class="reply.adminRead ? 'read' : 'unread'">{{ reply.adminRead ? '✓ 已读' : '● 未读' }}</span>
            <span class="reply-time">{{ formatDate(reply.createdAt) }}</span>
          </div>
          <div class="reply-history-content">{{ reply.replyContent }}</div>
        </div>
        </div>
        <div class="form-group">
          <label>新增回复 <span class="required">*</span></label>
          <textarea v-model="replyContent" class="input textarea" rows="4" placeholder="请输入回复内容..."></textarea>
        </div>
        <p v-if="replyError" class="err">{{ replyError }}</p>
        <div class="modal-actions">
          <button class="btn btn-ghost" @click="showReplyModal = false">关闭</button>
          <button class="btn btn-primary" @click="submitReply">提交回复</button>
        </div>
      </div>
    </div>

    <!-- 变更状态弹窗 -->
    <div v-if="showStatusModal" class="modal-mask" @click.self="showStatusModal = false">
      <div class="modal">
        <h3>📝 变更状态</h3>
        <div class="form-group">
          <label>新状态</label>
          <select v-model="statusChangeValue" class="select">
            <!-- 根据类型动态显示 -->
            <template v-if="statusChangeItem?.type === 'SUGGESTION'">
              <option value="PENDING">⏳ 待处理</option>
              <option value="APPROVED">👍 已采纳</option>
              <option value="REJECTED">🚫 已驳回</option>
            </template>
            <template v-else>
              <option value="PENDING">⏳ 待处理</option>
              <option value="PROCESSING">🔄 处理中</option>
              <option value="DONE">✅ 已完成</option>
              <option value="REJECTED">🚫 已驳回</option>
            </template>
          </select>
        </div>
        <p v-if="statusChangeError" class="err">{{ statusChangeError }}</p>
        <div class="modal-actions">
          <button class="btn btn-ghost" @click="showStatusModal = false">取消</button>
          <button class="btn btn-primary" @click="confirmStatusChange">确认变更</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, reactive, ref, computed, watch } from 'vue'
import request from '../api/request'
import { showConfirm } from '../composables/useConfirm'

const list = ref([])
const owners = ref([])
const units = ref([])
const buildings = ref([])
const keyword = ref('')
const typeFilter = ref(null)
const statusFilter = ref(null)
const showModal = ref(false)
const showReplyModal = ref(false)
const showStatusModal = ref(false)
const activeDropdown = ref(null)
const formError = ref('')
const replyError = ref('')
const statusChangeError = ref('')
const replyContent = ref('')
const replyItem = ref(null)
const replyList = ref([])
const editReplyList = ref([])
const statusChangeItem = ref(null)
const statusChangeValue = ref('')

const form = reactive({
  id: null,
  title: '',
  type: 'COMPLAINT',
  ownerId: null,
  roomId: null,
  content: '',
  status: 'PENDING',
  reply: '',
  replyDate: ''
})

// ============ 状态标签映射（含 APPROVED） ============
const STATUS_LABELS = {
  PENDING: '⏳ 待处理',
  PROCESSING: '🔄 处理中',
  DONE: '✅ 已完成',
  APPROVED: '👍 已采纳',
  REJECTED: '🚫 已驳回'
}

const STATUS_CLASSES = {
  PENDING: 'tag-pending',
  PROCESSING: 'tag-processing',
  DONE: 'tag-done',
  APPROVED: 'tag-approved',
  REJECTED: 'tag-rejected'
}

// ============ 方法 ============
function typeLabel(s) { return s === 'COMPLAINT' ? '📢 投诉' : '💡 建议' }
function statusLabel(s) { return STATUS_LABELS[s] || s }
function statusClass(s) { return STATUS_CLASSES[s] || '' }

function replyLabel(reply) {
  if (reply.ownerId) {
    const owner = owners.value.find(o => o.id === reply.ownerId)
    return `业主-${owner?.name || ''}`
  }
  return '管理员'
}

// ============ 筛选列表 ============
const filteredList = computed(() => {
  let result = list.value
  if (keyword.value.trim()) {
    const kw = keyword.value.trim().toLowerCase()
    result = result.filter(item => item.title?.toLowerCase().includes(kw))
  }
  if (typeFilter.value) {
    result = result.filter(item => item.type === typeFilter.value)
  }
  if (statusFilter.value) {
    result = result.filter(item => item.status === statusFilter.value)
  }
  return result
})

// ============ 状态统计（含 APPROVED） ============
const statusStats = computed(() => {
  const stats = { pending: 0, processing: 0, done: 0, approved: 0, rejected: 0 }
  list.value.forEach(item => {
    if (item.status === 'PENDING') stats.pending++
    else if (item.status === 'PROCESSING') stats.processing++
    else if (item.status === 'DONE') stats.done++
    else if (item.status === 'APPROVED') stats.approved++
    else if (item.status === 'REJECTED') stats.rejected++
  })
  return stats
})

// ============ 业主/房屋相关 ============
function ownerLabel(id) {
  return id ? owners.value.find(o => o.id === id)?.name || `#${id}` : '-'
}

function unitOptionLabel(u) {
  const b = buildings.value.find(x => x.id === u.buildingId)
  return `${b?.buildingName || ''} ${u.unitNo}`.trim()
}

function unitLabel(id) {
  const u = units.value.find(x => x.id === id)
  return u ? unitOptionLabel(u) : `#${id}`
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return dateStr.replace('T', ' ').slice(0, 16)
}

// ============ 下拉菜单 ============
function toggleDropdown(id) {
  activeDropdown.value = activeDropdown.value === id ? null : id
}

function closeDropdown() {
  activeDropdown.value = null
}

// ============ 数据加载 ============
async function loadMeta() {
  owners.value = await request.get('/owners')
  units.value = await request.get('/units')
  buildings.value = await request.get('/buildings')
}

async function load() {
  const data = await request.get('/complaints')
  // replyCount 由后端返回，直接使用
  list.value = data
}

// ============ 类型切换（状态联动） ============
function onTypeChange() {
  if (form.type === 'SUGGESTION') {
    if (!['PENDING', 'APPROVED', 'REJECTED'].includes(form.status)) {
      form.status = 'PENDING'
    }
  } else {
    if (!['PENDING', 'PROCESSING', 'DONE', 'REJECTED'].includes(form.status)) {
      form.status = 'PENDING'
    }
  }
}

// ============ 新增/编辑 ============
async function openForm(item) {
  formError.value = ''
  editReplyList.value = []
  Object.assign(form, {
    id: null,
    title: '',
    type: 'COMPLAINT',
    ownerId: owners.value[0]?.id ?? null,
    roomId: units.value[0]?.id ?? null,
    content: '',
    status: 'PENDING',
    reply: '',
    replyDate: ''
  })
  if (item) {
    Object.assign(form, item)
    // 加载该投诉的回复列表
    try {
      editReplyList.value = await request.get(`/complaints/${item.id}/replies`)
    } catch (e) {
      editReplyList.value = []
    }
  }
  onTypeChange()
  showModal.value = true
}

async function save() {
  if (!form.title?.trim() || !form.ownerId || !form.roomId) {
    formError.value = '标题、业主和房屋不能为空'
    return
  }
  try {
    if (form.id) {
      await request.put(`/complaints/${form.id}`, form)
    } else {
      await request.post('/complaints', form)
    }
    showModal.value = false
    load()
  } catch (e) {
    formError.value = e.message
  }
}

// ============ 回复 ============
async function openReply(item) {
  replyItem.value = item
  replyContent.value = ''
  replyError.value = ''
  replyList.value = []
  showReplyModal.value = true
  await loadReplies(item.id)
}

async function loadReplies(complaintId) {
  try {
    replyList.value = await request.get(`/complaints/${complaintId}/replies`)
  } catch (e) {
    replyList.value = []
  }
}

async function submitReply() {
  if (!replyContent.value?.trim()) {
    replyError.value = '回复内容不能为空'
    return
  }
  try {
    await request.post(`/complaints/${replyItem.value.id}/reply`, { reply: replyContent.value })
    replyContent.value = ''
    replyError.value = ''
    await loadReplies(replyItem.value.id)
    load()
  } catch (e) {
    replyError.value = e.message
  }
}

// ============ 变更状态 ============
function openStatusChange(item) {
  statusChangeItem.value = item
  statusChangeValue.value = item.status
  statusChangeError.value = ''
  showStatusModal.value = true
}

async function confirmStatusChange() {
  if (!statusChangeValue.value) {
    statusChangeError.value = '请选择状态'
    return
  }
  try {
    await request.put(`/complaints/${statusChangeItem.value.id}/status`, { status: statusChangeValue.value })
    showStatusModal.value = false
    load()
  } catch (e) {
    statusChangeError.value = e.message
  }
}

// ============ 删除 ============
async function remove(item) {
  const ok = await showConfirm('删除确认', `确定删除「${item.title}」吗？`)
  if (!ok) return
  await request.delete(`/complaints/${item.id}`)
  load()
}

// ============ 生命周期 ============
onMounted(async () => {
  await loadMeta()
  load()
  document.addEventListener('click', closeDropdown)
})

onUnmounted(() => {
  document.removeEventListener('click', closeDropdown)
})
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  gap: 12px;
  flex-wrap: wrap;
}
.toolbar .page-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
  white-space: nowrap;
}
.toolbar .actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.toolbar .search {
  width: 200px;
}

.stats-bar {
  display: flex;
  gap: 20px;
  padding: 12px 16px;
  background: #F8FAFC;
  border-radius: 8px;
  margin-bottom: 16px;
  font-size: 13px;
  color: #64748B;
  flex-wrap: wrap;
}
.stats-bar strong { color: #064E3B; }

.table .btn { margin-right: 4px; padding: 4px 10px; font-size: 13px; }
.btn-sm { padding: 4px 10px; font-size: 12px; }

.tag { padding: 2px 12px; border-radius: 12px; font-size: 12px; }

.tag-complaint { background: #FEE2E2; color: #991B1B; }
.tag-suggestion { background: #DBEAFE; color: #1E40AF; }

.tag-pending { background: #FEF3C7; color: #B45309; }
.tag-processing { background: #DBEAFE; color: #1E40AF; }
.tag-done { background: #D1FAE5; color: #065F46; }
.tag-approved { background: #E0E7FF; color: #3730A3; }  /* 紫色，区别于已完成 */
.tag-rejected { background: #F3F4F6; color: #6B7280; }

.empty { text-align: center; color: var(--color-text-secondary); padding: 24px !important; }
.err { color: var(--color-danger); font-size: 13px; }
.required { color: #DC2626; }

.dropdown {
  position: relative;
  display: inline-block;
}

.dropdown-toggle {
  padding: 4px 8px;
  font-size: 13px;
}

.dropdown-menu {
  position: absolute;
  right: 0;
  top: 100%;
  min-width: 120px;
  background: white;
  border: 1px solid #E5E7EB;
  border-radius: 6px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  z-index: 100;
  display: none;
  padding: 4px 0;
}

.dropdown.show .dropdown-menu {
  display: block;
}

.dropdown-item {
  display: block;
  width: 100%;
  padding: 8px 16px;
  text-align: left;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 13px;
  color: #374151;
}

.dropdown-item:hover {
  background: #F3F4F6;
}

.dropdown-item.text-danger {
  color: #DC2626;
}

.dropdown-item.text-danger:hover {
  background: #FEF2F2;
}

.textarea { resize: vertical; min-height: 80px; }

.reply-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #F8FAFC;
  border-radius: 8px;
  margin-bottom: 16px;
}
.reply-item-title { font-weight: 600; }
.reply-item-status { font-size: 12px; padding: 2px 10px; border-radius: 10px; }

.reply-box {
  padding: 12px;
  background: #F8FAFC;
  border-radius: 8px;
  font-size: 14px;
  color: var(--color-text-secondary);
  line-height: 1.6;
}

.reply-modal {
  max-height: 80vh;
  overflow-y: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
}
.reply-modal::-webkit-scrollbar {
  display: none;
}

.reply-history {
  margin-bottom: 16px;
}

.reply-history label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 8px;
}

.reply-history-item {
  padding: 10px 12px;
  background: #F8FAFC;
  border-radius: 6px;
  margin-bottom: 8px;
  border-left: 3px solid #3B82F6;
}

.reply-history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.reply-role {
  font-size: 12px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 4px;
}

.role-admin {
  color: #1E40AF;
  background: #DBEAFE;
}

.role-owner {
  color: #065F46;
  background: #D1FAE5;
}

.reply-read {
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 4px;
}

.reply-read.read {
  color: #065F46;
  background: #D1FAE5;
}

.reply-read.unread {
  color: #B45309;
  background: #FEF3C7;
}

.reply-time {
  font-size: 12px;
  color: #9CA3AF;
}

.reply-history-content {
  font-size: 14px;
  color: #374151;
  line-height: 1.5;
}

.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }

@media (max-width: 640px) {
  .form-row { grid-template-columns: 1fr; }
  .toolbar { flex-direction: column; align-items: stretch; }
  .toolbar .actions { flex-wrap: wrap; }
  .toolbar .search { width: 100%; }
  .stats-bar { gap: 8px; }
}
</style>