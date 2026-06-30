<template>
  <div>
    <!-- 工具栏 -->
    <div class="toolbar">
      <h2 class="page-title">报修管理</h2>
      <div class="actions">
        <input v-model="keyword" class="input search" placeholder="搜索标题/业主..." @keyup.enter="load" />
        <select v-model="statusFilter" class="select" @change="load">
          <option value="">全部状态 ({{ list.length }})</option>
          <option value="PENDING">⏳ 待处理 ({{ statusStats.pending }})</option>
          <option value="PROCESSING">🔄 处理中 ({{ statusStats.processing }})</option>
          <option value="DONE">✅ 已完成 ({{ statusStats.done }})</option>
          <option value="CANCELLED">❌ 已取消 ({{ statusStats.cancelled }})</option>
        </select>
        <select v-model="priorityFilter" class="select" @change="load">
          <option value="">全部优先级</option>
          <option value="URGENT">🔴 紧急</option>
          <option value="HIGH">🟠 高</option>
          <option value="MEDIUM">🟡 中</option>
          <option value="LOW">🟢 低</option>
        </select>
        
      </div>
    </div>

    <!-- 统计栏 -->
    <div class="stats-bar">
      <span>共 <strong>{{ filteredList.length }}</strong> 条</span>
      <span>⏳ 待处理 <strong>{{ statusStats.pending }}</strong></span>
      <span>🔄 处理中 <strong>{{ statusStats.processing }}</strong></span>
      <span>✅ 已完成 <strong>{{ statusStats.done }}</strong></span>
      <span v-if="statusStats.cancelled">❌ 已取消 <strong>{{ statusStats.cancelled }}</strong></span>
      <span v-if="priorityStats.urgent">🔴 紧急 <strong>{{ priorityStats.urgent }}</strong></span>
    </div>

    <!-- 表格 -->
    <div class="card">
      <table class="table">
        <thead>
        <tr>
          <th>序号</th>
          <th>标题</th>
          <th>业主</th>
          <th>房间</th>
          <th>优先级</th>
          <th>提交日期</th>
          <th>完成日期</th>
          <th>状态</th>
          <th>备注</th>
          <th>操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="(item, index) in filteredList" :key="item.id" :class="{ 'urgent-row': isUrgent(item) }">
          <td>{{ index + 1 }}</td>
          <td><strong>{{ item.title }}</strong></td>
          <td>{{ ownerMap[item.ownerId] || item.ownerId }}</td>
          <td>{{ unitMap[item.roomId] || item.roomId }}</td>
          <td><span :class="['tag', priorityClass(item.priority)]">{{ priorityLabel(item.priority) }}</span></td>
          <td>{{ item.submitDate || '—' }}</td>
          <td>{{ item.finishDate || '—' }}</td>
          <td><span :class="['tag', statusClass(item.status)]">{{ statusLabel(item.status) }}</span></td>
          <td>{{ item.remark || '—' }}</td>
          <td>
            <button v-if="item.status === 'PENDING'" class="btn btn-primary" @click="setStatus(item, 'PROCESSING')">受理</button>
            <button v-if="item.status === 'PROCESSING'" class="btn btn-primary" @click="setStatus(item, 'DONE')">完成</button>
            <button v-if="item.status === 'DONE'" class="btn btn-ghost" @click="openForm(item)">查看</button>
            <div class="dropdown" :class="{ 'show': activeDropdown === item.id }">
              <button class="btn btn-ghost dropdown-toggle" @click.stop="toggleDropdown(item.id)">
                更多 ▼
              </button>
              <div class="dropdown-menu">
                <button v-if="item.status === 'PENDING'" class="dropdown-item" @click="openForm(item)">编辑</button>
                <button v-if="item.status === 'PROCESSING'" class="dropdown-item" @click="openForm(item)">编辑</button>
                <button v-if="item.status === 'PENDING' || item.status === 'PROCESSING'" class="dropdown-item" @click="setStatus(item, 'CANCELLED')">取消</button>
                <button class="dropdown-item text-danger" @click="remove(item)">删除</button>
              </div>
            </div>
          </td>
        </tr>
        <tr v-if="!filteredList.length"><td colspan="10" class="empty">暂无数据</td></tr>
        </tbody>
      </table>
    </div>

    <!-- 新增报修弹窗 -->
    <div v-if="showModal" class="modal-mask" @click.self="showModal = false">
      <div class="modal">
        <h3>{{ form.id ? '✏️ 编辑报修' : '➕ 新增报修' }}</h3>
        <div class="form-group">
          <label>业主 <span class="required">*</span></label>
          <div v-if="form.id" class="readonly-value">{{ ownerMap[form.ownerId] || form.ownerId }}</div>
          <select v-else v-model="form.ownerId" class="select">
            <option v-for="o in owners" :key="o.id" :value="o.id">{{ o.name }}（{{ o.ownerNo }}）</option>
          </select>
        </div>
        <div class="form-group">
          <label>房屋 <span class="required">*</span></label>
          <div v-if="form.id" class="readonly-value">{{ unitMap[form.roomId] || form.roomId }}</div>
          <select v-else v-model="form.roomId" class="select">
            <option v-for="u in units" :key="u.id" :value="u.id">{{ unitMap[u.id] || u.id }}</option>
          </select>
        </div>
        <div class="form-group">
          <label>标题 <span class="required">*</span></label>
          <div v-if="form.id" class="readonly-value">{{ form.title }}</div>
          <input v-else v-model="form.title" class="input" maxlength="100" placeholder="请输入报修标题" />
        </div>
        <div class="form-group">
          <label>问题描述</label>
          <div v-if="form.id" class="readonly-value textarea">{{ form.description || '无' }}</div>
          <textarea v-else v-model="form.description" class="input textarea" maxlength="500" rows="3" placeholder="请描述具体问题"></textarea>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>优先级</label>
            <select v-model="form.priority" class="select">
              <option value="LOW">🟢 低</option>
              <option value="MEDIUM">🟡 中</option>
              <option value="HIGH">🟠 高</option>
              <option value="URGENT">🔴 紧急</option>
            </select>
          </div>
          <div class="form-group">
            <label>状态</label>
            <select v-model="form.status" class="select">
              <option value="PENDING">待处理</option>
              <option value="PROCESSING">处理中</option>
              <option value="DONE">已完成</option>
              <option value="CANCELLED">已取消</option>
            </select>
          </div>
        </div>
        <div class="form-group">
          <label>完成日期</label>
          <input v-model="form.finishDate" type="date" class="input" />
        </div>
        <div class="form-group">
          <label>处理备注</label>
          <textarea v-model="form.remark" class="input textarea" maxlength="200" rows="2" placeholder="处理备注信息"></textarea>
        </div>
        <p v-if="formError" class="err">{{ formError }}</p>
        <div class="modal-actions">
          <button class="btn btn-ghost" @click="showModal = false">取消</button>
          <button class="btn btn-primary" @click="save">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, reactive } from 'vue'
import request from '../api/request'
import { showConfirm } from '../composables/useConfirm'

const list = ref([])
const owners = ref([])
const units = ref([])
const buildings = ref([])
const statusFilter = ref('')
const priorityFilter = ref('')
const keyword = ref('')
const showModal = ref(false)
const formError = ref('')
const activeDropdown = ref(null)

const form = reactive({
  id: null,
  ownerId: null,
  roomId: null,
  title: '',
  description: '',
  priority: 'MEDIUM',
  status: 'PENDING',
  submitDate: '',
  finishDate: '',
  remark: ''
})

const ownerMap = computed(() => Object.fromEntries(owners.value.map(r => [r.id, `${r.name}（${r.ownerNo}）`])))
const unitMap = computed(() => {
  const bm = Object.fromEntries(buildings.value.map(b => [b.id, b.buildingName]))
  return Object.fromEntries(units.value.map(u => [u.id, `${bm[u.buildingId] || ''} ${u.unitNo}`.trim()]))
})

const STATUS_LABELS = { PENDING: '⏳待处理', PROCESSING: '🔄处理中', DONE: '✅已完成', CANCELLED: '❌已取消' }
const PRIORITY_LABELS = { URGENT: '🔴紧急', HIGH: '🟠高', MEDIUM: '🟡中', LOW: '🟢低' }
const PRIORITY_WEIGHT = { URGENT: 4, HIGH: 3, MEDIUM: 2, LOW: 1 }

function statusLabel(s) { return STATUS_LABELS[s] || s }
function priorityLabel(p) { return PRIORITY_LABELS[p] || p }

function statusClass(s) {
  return { PENDING: 'pending', PROCESSING: 'processing', DONE: 'done', CANCELLED: 'cancelled' }[s] || ''
}

function isUrgent(item) {
  if (item.status !== 'PENDING') return false
  if (!item.submitDate) return false
  const submitTime = new Date(item.submitDate).getTime()
  const now = Date.now()
  return (now - submitTime) > 24 * 60 * 60 * 1000
}

function toggleDropdown(id) {
  activeDropdown.value = activeDropdown.value === id ? null : id
}

function priorityClass(p) {
  return { URGENT: 'urgent', HIGH: 'high', MEDIUM: 'medium', LOW: 'low' }[p] || ''
}

// 筛选列表
const filteredList = computed(() => {
  let result = list.value
  if (keyword.value.trim()) {
    const kw = keyword.value.trim().toLowerCase()
    result = result.filter(item =>
        item.title?.toLowerCase().includes(kw) ||
        ownerMap.value[item.ownerId]?.toLowerCase().includes(kw)
    )
  }
  if (statusFilter.value) {
    result = result.filter(item => item.status === statusFilter.value)
  }
  if (priorityFilter.value) {
    result = result.filter(item => item.priority === priorityFilter.value)
  }
  result.sort((a, b) => {
    const weightA = PRIORITY_WEIGHT[a.priority] || 0
    const weightB = PRIORITY_WEIGHT[b.priority] || 0
    if (weightB !== weightA) return weightB - weightA
    return (new Date(a.submitDate || 0) - new Date(b.submitDate || 0))
  })
  return result
})

// 状态统计
const statusStats = computed(() => {
  const stats = { pending: 0, processing: 0, done: 0, cancelled: 0 }
  list.value.forEach(item => {
    if (item.status === 'PENDING') stats.pending++
    else if (item.status === 'PROCESSING') stats.processing++
    else if (item.status === 'DONE') stats.done++
    else if (item.status === 'CANCELLED') stats.cancelled++
  })
  return stats
})

// 优先级统计
const priorityStats = computed(() => {
  const stats = { urgent: 0, high: 0, medium: 0, low: 0 }
  list.value.forEach(item => {
    if (item.priority === 'URGENT') stats.urgent++
    else if (item.priority === 'HIGH') stats.high++
    else if (item.priority === 'MEDIUM') stats.medium++
    else if (item.priority === 'LOW') stats.low++
  })
  return stats
})

async function loadMeta() {
  [owners.value, units.value, buildings.value] = await Promise.all([
    request.get('/owners'), request.get('/units'), request.get('/buildings')
  ])
}

async function load() {
  list.value = await request.get('/repairs', { params: { status: statusFilter.value || undefined } })
}

function openForm(item) {
  formError.value = ''
  Object.assign(form, {
    id: null,
    ownerId: owners.value[0]?.id || null,
    roomId: null,
    title: '',
    description: '',
    priority: 'MEDIUM',
    status: 'PENDING',
    submitDate: '',
    finishDate: '',
    remark: ''
  })
  if (item) Object.assign(form, item)
  showModal.value = true
}

async function save() {
  if (!form.ownerId) { formError.value = '请选择业主'; return }
  if (!form.roomId) { formError.value = '请选择房屋'; return }
  if (!form.title?.trim()) { formError.value = '请输入标题'; return }
  formError.value = ''
  try {
    if (form.id) {
      await request.put(`/repairs/${form.id}`, form)
    } else {
      await request.post('/repairs', form)
    }
    showModal.value = false
    load()
  } catch (e) {
    formError.value = e.message
  }
}

async function setStatus(item, status) {
  await request.put(`/repairs/${item.id}/status`, { status })
  load()
}

async function remove(item) {
  const ok = await showConfirm('删除报修', `确定删除「${item.title}」吗？`)
  if (!ok) return
  await request.delete(`/repairs/${item.id}`)
  load()
}

onMounted(async () => {
  await loadMeta()
  load()
  document.addEventListener('click', closeDropdown)
})

onUnmounted(() => {
  document.removeEventListener('click', closeDropdown)
})

function closeDropdown() {
  activeDropdown.value = null
}
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
}
.stats-bar strong { color: #064E3B; }

.table .btn { margin-right: 8px; padding: 4px 10px; font-size: 13px; }
.table th:nth-child(5), .table td:nth-child(5) { width: 100px; white-space: nowrap; }
.table th:nth-child(8), .table td:nth-child(8) { width: 100px; white-space: nowrap; }
.empty { text-align: center; color: var(--color-text-secondary); padding: 24px !important; }
.err { color: var(--color-danger); font-size: 13px; }
.required { color: #DC2626; }

.tag { display: inline-block; padding: 4px 12px; border-radius: 50px; font-size: 12px; font-weight: 500; }
.tag.pending { background: #FEF3C7; color: #B45309; }
.tag.processing { background: #DBEAFE; color: #1D4ED8; }
.tag.done { background: #D1FAE5; color: #065F46; }
.tag.cancelled { background: #F3F4F6; color: #6B7280; }

.tag.urgent { background: #FEE2E2; color: #991B1B; border-radius: 50px; padding: 4px 14px; }

.urgent-row {
  background: linear-gradient(90deg, rgba(254, 226, 226, 0.3) 0%, transparent 100%);
}
.urgent-row td:first-child::before {
  content: '⚠️';
  margin-right: 4px;
}

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
  min-width: 100px;
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

.tag.high { background: #FED7AA; color: #9A3412; border-radius: 50px; padding: 4px 14px; }
.tag.medium { background: #FEF3C7; color: #B45309; border-radius: 50px; padding: 4px 14px; }
.tag.low { background: #D1FAE5; color: #065F46; border-radius: 50px; padding: 4px 14px; }

.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.textarea { resize: vertical; min-height: 80px; }
.readonly-value {
  padding: 10px 14px;
  background: #F3F4F6;
  border: 1px solid #E5E7EB;
  border-radius: var(--radius);
  font-size: 14px;
  color: #374151;
}
.readonly-value.textarea {
  min-height: 80px;
  white-space: pre-wrap;
}

@media (max-width: 640px) {
  .form-row { grid-template-columns: 1fr; }
  .toolbar { flex-direction: column; align-items: stretch; }
  .toolbar .actions { flex-wrap: wrap; }
  .toolbar .search { width: 100%; }
}
</style>