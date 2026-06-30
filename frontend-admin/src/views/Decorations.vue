<template>
  <div>
    <!-- 工具栏 -->
    <div class="toolbar">
      <h2 class="page-title">装修管理</h2>
      <div class="actions">
        <input v-model="keyword" class="input search" placeholder="搜索房屋/业主..." @keyup.enter="load" />
        <select v-model="statusFilter" class="select" @change="load">
          <option :value="null">全部状态</option>
          <option value="PENDING">待批</option>
          <option value="APPROVED">已批准</option>
          <option value="REJECTED">已驳回</option>
          <option value="IN_PROGRESS">施工中</option>
          <option value="DONE">已完工</option>
        </select>
        <button class="btn btn-primary" @click="openForm()">+ 新增申请</button>
      </div>
    </div>

    <!-- 统计栏 -->
    <div class="stats-bar">
      <span>共 <strong>{{ filteredList.length }}</strong> 条申请</span>
      <span>🟡 待批 {{ statusStats.pending }} 条</span>
      <span>🟢 已批准 {{ statusStats.approved }} 条</span>
      <span>🔵 施工中 {{ statusStats.inProgress }} 条</span>
      <span>✅ 已完工 {{ statusStats.done }} 条</span>
      <span>⛔ 已驳回 {{ statusStats.rejected }} 条</span>
    </div>

    <div class="card">
      <table class="table">
        <thead>
        <tr>
          <th>序号</th>
          <th>房屋</th>
          <th>业主</th>
          <th>装修类型</th>
          <th>开工日期</th>
          <th>完工日期</th>
          <th>状态</th>
          <th>施工单位</th>
          <th>监理人</th>
          <th>操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="(item, index) in filteredList" :key="item.id">
          <td>{{ index + 1 }}</td>
          <td>{{ unitLabel(item.roomId) }}</td>
          <td>{{ ownerLabel(item.ownerId) }}</td>
          <td><span :class="['tag', typeClass(item.decorationType)]">{{ typeLabel(item.decorationType) }}</span></td>
          <td>{{ item.startDate || '-' }}</td>
          <td>{{ item.endDate || '-' }}</td>
          <td><span :class="['tag', statusClass(item.status)]">{{ statusLabel(item.status) }}</span></td>
          <td>{{ item.contractor || '-' }}</td>
          <td>{{ item.supervisor || '-' }}</td>
          <td>
            <button class="btn btn-ghost btn-sm" @click="openForm(item)">编辑</button>
            <button v-if="item.status === 'PENDING'" class="btn btn-success btn-sm" @click="approve(item)">批准</button>
            <button v-if="item.status === 'PENDING'" class="btn btn-danger btn-sm" @click="reject(item)">驳回</button>
            <button class="btn btn-danger btn-sm" @click="remove(item)">删除</button>
          </td>
        </tr>
        <tr v-if="!filteredList.length"><td colspan="10" class="empty">暂无数据</td></tr>
        </tbody>
      </table>
    </div>

    <!-- 弹窗表单 -->
    <div v-if="showModal" class="modal-mask" @click.self="showModal = false">
      <div class="modal">
        <h3>{{ form.id ? '✏️ 编辑装修申请' : '➕ 新增装修申请' }}</h3>
        <div class="form-row">
          <div class="form-group">
            <label>房屋 <span class="required">*</span></label>
            <select v-model.number="form.roomId" class="select">
              <option v-for="u in units" :key="u.id" :value="u.id">{{ unitOptionLabel(u) }}</option>
            </select>
          </div>
          <div class="form-group">
            <label>业主 <span class="required">*</span></label>
            <select v-model.number="form.ownerId" class="select">
              <option v-for="o in owners" :key="o.id" :value="o.id">{{ o.name }}</option>
            </select>
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>装修类型</label>
            <select v-model="form.decorationType" class="select">
              <option value="REMODELING">翻新</option>
              <option value="RENOVATION">改造</option>
              <option value="NEW">新建</option>
            </select>
          </div>
          <div class="form-group">
            <label>状态</label>
            <select v-model="form.status" class="select">
              <option value="PENDING">待批</option>
              <option value="APPROVED">已批准</option>
              <option value="REJECTED">已驳回</option>
              <option value="IN_PROGRESS">施工中</option>
              <option value="DONE">已完工</option>
            </select>
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>开工日期 <span class="required">*</span></label>
            <input v-model="form.startDate" type="date" class="input" />
          </div>
          <div class="form-group">
            <label>完工日期</label>
            <input v-model="form.endDate" type="date" class="input" />
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>施工单位</label>
            <input v-model="form.contractor" class="input" placeholder="如 装修公司A" />
          </div>
          <div class="form-group">
            <label>监理人</label>
            <input v-model="form.supervisor" class="input" placeholder="如 张监理" />
          </div>
        </div>
        <div v-if="form.approvalDate" class="form-group">
          <label>批准日期</label>
          <input v-model="form.approvalDate" type="date" class="input" readonly />
        </div>
        <div class="form-group">
          <label>备注</label>
          <textarea v-model="form.remark" class="input textarea" rows="2" placeholder="备注信息"></textarea>
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
import { onMounted, reactive, ref, computed } from 'vue'
import request from '../api/request'
import { showConfirm } from '../composables/useConfirm'

const list = ref([])
const units = ref([])
const buildings = ref([])
const owners = ref([])
const keyword = ref('')
const statusFilter = ref(null)
const showModal = ref(false)
const formError = ref('')

const form = reactive({
  id: null,
  roomId: null,
  ownerId: null,
  decorationType: 'REMODELING',
  startDate: '',
  endDate: '',
  status: 'PENDING',
  approvalDate: '',
  contractor: '',
  supervisor: '',
  remark: ''
})

// 筛选后的列表
const filteredList = computed(() => {
  let result = list.value
  if (keyword.value.trim()) {
    const kw = keyword.value.trim().toLowerCase()
    result = result.filter(item => {
      const unit = units.value.find(u => u.id === item.roomId)
      const owner = owners.value.find(o => o.id === item.ownerId)
      const unitName = unit ? unit.unitNo : ''
      const ownerName = owner ? owner.name : ''
      return unitName.toLowerCase().includes(kw) || ownerName.toLowerCase().includes(kw)
    })
  }
  if (statusFilter.value) {
    result = result.filter(item => item.status === statusFilter.value)
  }
  return result
})

// 状态统计
const statusStats = computed(() => {
  const stats = { pending: 0, approved: 0, rejected: 0, inProgress: 0, done: 0 }
  list.value.forEach(item => {
    if (item.status === 'PENDING') stats.pending++
    else if (item.status === 'APPROVED') stats.approved++
    else if (item.status === 'REJECTED') stats.rejected++
    else if (item.status === 'IN_PROGRESS') stats.inProgress++
    else if (item.status === 'DONE') stats.done++
  })
  return stats
})

function typeLabel(s) {
  const map = { REMODELING: '翻新', RENOVATION: '改造', NEW: '新建' }
  return map[s] || s
}

function typeClass(s) {
  const map = { REMODELING: 'tag-remodeling', RENOVATION: 'tag-renovation', NEW: 'tag-new' }
  return map[s] || ''
}

function statusLabel(s) {
  const map = { PENDING: '待批', APPROVED: '已批准', REJECTED: '已驳回', IN_PROGRESS: '施工中', DONE: '已完工' }
  return map[s] || s
}

function statusClass(s) {
  const map = {
    PENDING: 'tag-pending',
    APPROVED: 'tag-approved',
    REJECTED: 'tag-rejected',
    IN_PROGRESS: 'tag-progress',
    DONE: 'tag-done'
  }
  return map[s] || ''
}

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

async function loadMeta() {
  units.value = await request.get('/units')
  buildings.value = await request.get('/buildings')
  owners.value = await request.get('/owners')
}

async function load() {
  list.value = await request.get('/decoration')
}

function openForm(item) {
  formError.value = ''
  Object.assign(form, {
    id: null,
    roomId: units.value[0]?.id ?? null,
    ownerId: owners.value[0]?.id ?? null,
    decorationType: 'REMODELING',
    startDate: '',
    endDate: '',
    status: 'PENDING',
    approvalDate: '',
    contractor: '',
    supervisor: '',
    remark: ''
  })
  if (item) Object.assign(form, item)
  showModal.value = true
}

async function save() {
  if (!form.roomId || !form.ownerId || !form.startDate) {
    formError.value = '房屋、业主和开工日期不能为空'
    return
  }
  try {
    if (form.id) {
      await request.put(`/decoration/${form.id}`, form)
    } else {
      await request.post('/decoration', form)
    }
    showModal.value = false
    load()
  } catch (e) {
    formError.value = e.message
  }
}

async function approve(item) {
  const ok = await showConfirm('批准确认', `确定批准「${unitLabel(item.roomId)}」的装修申请吗？`)
  if (!ok) return
  await request.put(`/decoration/${item.id}/approve`)
  load()
}

async function reject(item) {
  const ok = await showConfirm('驳回确认', `确定驳回「${unitLabel(item.roomId)}」的装修申请吗？`)
  if (!ok) return
  await request.put(`/decoration/${item.id}/reject`)
  load()
}

async function remove(item) {
  const ok = await showConfirm('删除确认', `确定删除「${unitLabel(item.roomId)}」的装修记录吗？`)
  if (!ok) return
  await request.delete(`/decoration/${item.id}`)
  load()
}

onMounted(async () => {
  await loadMeta()
  load()
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
.btn-success { background: #059669; color: #fff; }
.btn-success:hover { background: #047857; }

.tag { padding: 2px 12px; border-radius: 12px; font-size: 12px; }

.tag-remodeling { background: #DBEAFE; color: #1D4ED8; }
.tag-renovation { background: #FEF3C7; color: #B45309; }
.tag-new { background: #E0E7FF; color: #3730A3; }

.tag-pending { background: #FEF3C7; color: #B45309; }
.tag-approved { background: #D1FAE5; color: #065F46; }
.tag-rejected { background: #FEE2E2; color: #991B1B; }
.tag-progress { background: #DBEAFE; color: #1D4ED8; }
.tag-done { background: #D1FAE5; color: #065F46; }

.empty { text-align: center; color: var(--color-text-secondary); padding: 24px !important; }
.err { color: var(--color-danger); font-size: 13px; }
.required { color: #DC2626; }

.textarea { resize: vertical; min-height: 60px; }

.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }

@media (max-width: 640px) {
  .form-row { grid-template-columns: 1fr; }
  .toolbar { flex-direction: column; align-items: stretch; }
  .toolbar .actions { flex-wrap: wrap; }
  .toolbar .search { width: 100%; }
}
</style>