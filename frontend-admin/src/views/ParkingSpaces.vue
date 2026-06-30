<template>
  <div>
    <!-- 工具栏 -->
    <div class="toolbar">
      <h2 class="page-title">车位管理</h2>
      <div class="actions">
        <input v-model="keyword" class="input search" placeholder="搜索车位编号..." @keyup.enter="load" />
        <select v-model="statusFilter" class="select" @change="load">
          <option :value="null">全部状态</option>
          <option :value="1">空闲</option>
          <option :value="2">占用</option>
          <option :value="0">停用</option>
        </select>
        <button class="btn btn-warning" @click="showPending = !showPending" style="position:relative;">
          📋 待审批
          <span v-if="pendingCount > 0" class="badge">{{ pendingCount }}</span>
        </button>
        <button class="btn btn-primary" @click="openForm()">+ 新增车位</button>
      </div>
    </div>

    <!-- 统计栏 -->
    <div class="stats-bar">
      <span>共 <strong>{{ filteredList.length }}</strong> 个车位</span>
      <span>🟢 空闲 {{ statusStats.free }} 个</span>
      <span>🔴 占用 {{ statusStats.occupied }} 个</span>
      <span>⚪ 停用 {{ statusStats.disabled }} 个</span>
    </div>

    <!-- 待审批列表 -->
    <div v-if="showPending" class="card pending-card">
      <div class="pending-header">
        <h3 class="pending-title">📋 待审批申请</h3>
        <button class="btn btn-ghost" @click="showPending = false">收起</button>
      </div>
      <table class="table">
        <thead>
          <tr>
            <th>申请ID</th>
            <th>车位编号</th>
            <th>申请业主</th>
            <th>申请类型</th>
            <th>车牌号</th>
            <th>租赁方式</th>
            <th>申请时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="app in pendingList" :key="app.id">
            <td>#{{ app.id }}</td>
            <td><strong>{{ getSpaceNo(app.parkingSpaceId) }}</strong></td>
            <td>{{ getOwnerName(app.ownerId) }}</td>
            <td><span :class="['tag', appTypeClass(app.applicationType)]">{{ appTypeLabel(app.applicationType) }}</span></td>
            <td>{{ app.plateNumber || '-' }}</td>
            <td>{{ durationLabel(app.rentalDuration) }}</td>
            <td>{{ formatDate(app.createdAt) }}</td>
            <td>
              <button class="btn btn-success btn-sm" @click="approveApplication(app, true)">批准</button>
              <button class="btn btn-danger btn-sm" @click="approveApplication(app, false)">驳回</button>
            </td>
          </tr>
          <tr v-if="!pendingList.length">
            <td colspan="8" class="empty">🎉 暂无待审批申请</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 车位列表 -->
    <div class="card">
      <table class="table">
        <thead>
        <tr>
          <th>序号</th>
          <th>车位编号</th>
          <th>所属楼栋</th>
          <th>类型</th>
          <th>状态</th>
          <th>业主</th>
          <th>面积</th>
          <th>月租费</th>
          <th>购买费用</th>
          <th>操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="(item, index) in filteredList" :key="item.id">
          <td>{{ index + 1 }}</td>
          <td><strong>{{ item.spaceNo }}</strong></td>
          <td>{{ buildingLabel(item.buildingId) }}</td>
          <td><span :class="['tag', typeClass(item.type)]">{{ typeLabel(item.type) }}</span></td>
          <td><span :class="['tag', statusClass(item.status)]">{{ statusLabel(item.status) }}</span></td>
          <td>{{ ownerLabel(item.ownerId) }}</td>
          <td>{{ item.areaSqm ? item.areaSqm + '㎡' : '-' }}</td>
          <td class="amount">{{ item.type === 'MONTHLY' ? '¥' + (item.monthlyFee ?? 0) + '/月' : '—' }}</td>
          <td class="amount">{{ item.type === 'FIXED' ? '¥' + (item.purchasePrice ?? 0) : '—' }}</td>
          <td>
            <button class="btn btn-ghost" @click="openForm(item)">编辑</button>
            <button class="btn btn-danger" @click="remove(item)">删除</button>
          </td>
        </tr>
        <tr v-if="!filteredList.length"><td colspan="10" class="empty">暂无数据</td></tr>
        </tbody>
      </table>
    </div>

    <!-- 弹窗表单 -->
    <div v-if="showModal" class="modal-mask" @click.self="showModal = false">
      <div class="modal">
        <h3>{{ form.id ? '✏️ 编辑车位' : '➕ 新增车位' }}</h3>
        <div class="form-group">
          <label>车位编号 <span class="required">*</span></label>
          <input v-model="form.spaceNo" class="input" placeholder="如 CH1-001" />
        </div>
        <div class="form-group">
          <label>所属楼栋 <span class="required">*</span></label>
          <select v-model.number="form.buildingId" class="select">
            <option v-for="b in buildings" :key="b.id" :value="b.id">{{ b.buildingName }}</option>
          </select>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>类型</label>
            <select v-model="form.type" class="select">
              <option value="FIXED">购买</option>
              <option value="MONTHLY">月租</option>
              <option value="TEMP">临时</option>
            </select>
          </div>
          <div class="form-group">
            <label>状态</label>
            <select v-model.number="form.status" class="select">
              <option :value="1">🟢 空闲</option>
              <option :value="2">🔴 占用</option>
              <option :value="0">⚪ 停用</option>
            </select>
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>关联业主</label>
            <select v-model.number="form.ownerId" class="select">
              <option :value="null">无</option>
              <option v-for="o in owners" :key="o.id" :value="o.id">{{ o.name }}</option>
            </select>
          </div>
          <div class="form-group">
            <label>面积(㎡)</label>
            <input v-model.number="form.areaSqm" type="number" min="0" step="0.01" class="input" />
          </div>
        </div>
        <div class="form-group" v-if="form.type === 'MONTHLY'">
          <label>月租费(元) <span class="required">*</span></label>
          <input v-model.number="form.monthlyFee" type="number" min="0" step="0.01" class="input" placeholder="请输入月租费" />
        </div>
        <div class="form-group" v-else-if="form.type === 'FIXED'">
          <label>购买金额(元) <span class="required">*</span></label>
          <input v-model.number="form.purchasePrice" type="number" min="0" step="0.01" class="input" placeholder="请输入购买金额" />
        </div>
        <div class="form-group" v-else>
          <label>费用</label>
          <input class="input" :value="'—'" disabled />
        </div>
        <p v-if="formError" class="err">{{ formError }}</p>
        <div class="modal-actions">
          <button class="btn btn-ghost" @click="showModal = false">取消</button>
          <button class="btn btn-primary" @click="save">保存</button>
        </div>
      </div>
    </div>

    <!-- 审批弹窗 -->
    <div v-if="showApproveModal" class="modal-mask" @click.self="showApproveModal = false">
      <div class="modal">
        <h3>{{ approveAction === 'approve' ? '✅ 批准申请' : '❌ 驳回申请' }}</h3>
        <p class="approve-info">
          <strong>车位：</strong>{{ approveTarget?.spaceNo }}<br/>
          <strong>业主：</strong>{{ getOwnerName(approveTarget?.ownerId) }}<br/>
          <strong>类型：</strong>{{ appTypeLabel(approveTarget?.applicationType) }}<br/>
          <strong v-if="isPurchaseApprove()">参考价格：</strong>
          <span v-if="isPurchaseApprove()" class="text-success">¥{{ getSpacePurchasePrice(approveTarget?.parkingSpaceId) }}</span>
        </p>
        <div class="form-group">
          <label>审批备注</label>
          <textarea v-model="approveRemark" class="input" rows="3" placeholder="选填审批意见"></textarea>
        </div>
        <div class="form-group" v-if="approveAction === 'approve' && isPurchaseApprove()">
          <label>购买金额(元) <span class="required">*</span></label>
          <input v-model.number="approvePurchaseAmount" type="number" min="0" step="0.01" class="input" placeholder="请输入车位购买金额" />
        </div>
        <div class="modal-actions">
          <button class="btn btn-ghost" @click="showApproveModal = false">取消</button>
          <button class="btn" :class="approveAction === 'approve' ? 'btn-success' : 'btn-danger'" @click="confirmApprove">
            {{ approveAction === 'approve' ? '确认批准' : '确认驳回' }}
          </button>
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
const buildings = ref([])
const owners = ref([])
const keyword = ref('')
const statusFilter = ref(null)
const showModal = ref(false)
const formError = ref('')

// ========== 待审批相关 ==========
const showPending = ref(false)
const pendingList = ref([])
const showApproveModal = ref(false)
const approveTarget = ref(null)
const approveAction = ref('approve') // 'approve' | 'reject'
const approveRemark = ref('')
const approvePurchaseAmount = ref(null)

const form = reactive({
  id: null,
  spaceNo: '',
  buildingId: null,
  type: 'FIXED',
  status: 1,
  ownerId: null,
  areaSqm: null,
  monthlyFee: null,
  purchasePrice: null
})

// ========== Computed ==========

// 筛选后的列表
const filteredList = computed(() => {
  let result = list.value
  if (keyword.value.trim()) {
    const kw = keyword.value.trim().toLowerCase()
    result = result.filter(item => item.spaceNo?.toLowerCase().includes(kw))
  }
  if (statusFilter.value !== null && statusFilter.value !== undefined) {
    result = result.filter(item => item.status === statusFilter.value)
  }
  return result
})

// 状态统计
const statusStats = computed(() => {
  const stats = { free: 0, occupied: 0, disabled: 0 }
  list.value.forEach(item => {
    if (item.status === 1) stats.free++
    else if (item.status === 2) stats.occupied++
    else if (item.status === 0) stats.disabled++
  })
  return stats
})

// 待审批数量
const pendingCount = computed(() => pendingList.value.length)

// ========== 工具函数 ==========

function typeLabel(s) {
  const map = { FIXED: '购买', MONTHLY: '月租', TEMP: '临时' }
  return map[s] || s
}

function typeClass(s) {
  const map = { FIXED: 'tag-purchase', MONTHLY: 'tag-monthly', TEMP: 'tag-temp' }
  return map[s] || ''
}

function statusLabel(s) {
  const map = { 1: '空闲', 2: '占用', 0: '停用' }
  return map[s] || s
}

function statusClass(s) {
  return s === 1 ? 'tag-free' : s === 2 ? 'tag-occupied' : 'tag-disabled'
}

function buildingLabel(id) {
  return buildings.value.find(b => b.id === id)?.buildingName || `#${id}`
}

function ownerLabel(id) {
  return id ? owners.value.find(o => o.id === id)?.name || `#${id}` : '-'
}

function getSpaceNo(id) {
  const space = list.value.find(s => s.id === id)
  return space?.spaceNo || `#${id}`
}

function getOwnerName(id) {
  return owners.value.find(o => o.id === id)?.name || `业主#${id}`
}

function getSpacePurchasePrice(spaceId) {
  const space = list.value.find(s => s.id === spaceId)
  return space?.purchasePrice ?? 0
}

function isPurchaseApprove() {
  if (!approveTarget.value) return false
  if (approveTarget.value.applicationType === 'PURCHASE') return true
  if (approveTarget.value.applicationType === 'ADD') {
    const space = list.value.find(s => s.id === approveTarget.value.parkingSpaceId)
    return space?.type === 'FIXED'
  }
  return false
}

function appTypeLabel(val) {
  const map = { RENT: '租赁', RENEW: '续租', CANCEL: '退租', TRANSFER: '转让', ADD: '新增', CHANGE: '更换', PURCHASE: '购买' }
  return map[val] || val
}

function appTypeClass(val) {
  const map = { RENT: 'tag-rent', RENEW: 'tag-renew', CANCEL: 'tag-cancel', TRANSFER: 'tag-transfer', ADD: 'tag-add', CHANGE: 'tag-change', PURCHASE: 'tag-purchase' }
  return map[val] || ''
}

function durationLabel(val) {
  const map = { MONTHLY: '月租', QUARTERLY: '季租', YEARLY: '年租', PURCHASE: '购买', DAILY: '日租', HOURLY: '时租' }
  return map[val] || val || '-'
}

function formatDate(dt) {
  if (!dt) return '-'
  return dt.replace('T', ' ').slice(0, 16)
}

// ========== API 请求 ==========

async function loadMeta() {
  buildings.value = await request.get('/buildings')
  owners.value = await request.get('/owners')
}

async function load() {
  list.value = await request.get('/parking')
}

async function loadPending() {
  pendingList.value = await request.get('/parking-application/pending')
}

// ========== 车位 CRUD ==========

function openForm(item) {
  formError.value = ''
  Object.assign(form, {
    id: null,
    spaceNo: '',
    buildingId: buildings.value[0]?.id ?? null,
    type: 'FIXED',
    status: 1,
    ownerId: null,
    areaSqm: null,
    monthlyFee: null,
    purchasePrice: null
  })
  if (item) Object.assign(form, item)
  showModal.value = true
}

async function save() {
  if (!form.spaceNo?.trim() || !form.buildingId) {
    formError.value = '车位编号和所属楼栋不能为空'
    return
  }
  if (form.type === 'MONTHLY' && (form.monthlyFee === null || form.monthlyFee === undefined || form.monthlyFee < 0)) {
    formError.value = '月租类型请填写月租费'
    return
  }
  if (form.type === 'FIXED' && (form.purchasePrice === null || form.purchasePrice === undefined || form.purchasePrice < 0)) {
    formError.value = '购买类型请填写购买金额'
    return
  }
  try {
    if (form.id) {
      await request.put(`/parking/${form.id}`, form)
    } else {
      await request.post('/parking', form)
    }
    showModal.value = false
    await load()
    await loadPending()
  } catch (e) {
    formError.value = e.message
  }
}

async function remove(item) {
  const ok = await showConfirm('删除确认', `确定删除车位「${item.spaceNo}」吗？`)
  if (!ok) return
  await request.delete(`/parking/${item.id}`)
  await load()
  await loadPending()
}

// ========== 审批功能 ==========

function approveApplication(app, approved) {
  approveTarget.value = app
  approveAction.value = approved ? 'approve' : 'reject'
  approveRemark.value = ''
  approvePurchaseAmount.value = approved && isPurchaseApprove() ? getSpacePurchasePrice(app.parkingSpaceId) : null
  showApproveModal.value = true
}

async function confirmApprove() {
  const isApprove = approveAction.value === 'approve'
  
  if (isApprove && isPurchaseApprove()) {
    if (approvePurchaseAmount.value === null || approvePurchaseAmount.value === undefined || approvePurchaseAmount.value <= 0) {
      alert('请输入正确的购买金额')
      return
    }
  }
  
  try {
    const params = {
      approved: isApprove,
      remark: approveRemark.value
    }
    if (isApprove && approvePurchaseAmount.value) {
      params.purchaseAmount = approvePurchaseAmount.value
    }
    await request.put(`/parking-application/${approveTarget.value.id}/approve`, null, { params })
    showApproveModal.value = false
    await loadPending()
    await load()
    alert(isApprove ? '✅ 已批准该申请' : '❌ 已驳回该申请')
  } catch (e) {
    alert('操作失败：' + e.message)
  }
}

// ========== 生命周期 ==========

onMounted(async () => {
  await loadMeta()
  await load()
  await loadPending()
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
}
.stats-bar strong { color: #064E3B; }

.table .btn { margin-right: 8px; padding: 4px 10px; font-size: 13px; }
.amount { font-weight: 600; color: var(--color-primary); }
.empty { text-align: center; color: var(--color-text-secondary); padding: 24px !important; }
.err { color: var(--color-danger); font-size: 13px; }
.required { color: #DC2626; }

.tag { padding: 2px 12px; border-radius: 12px; font-size: 12px; }

.tag-purchase { background: #DBEAFE; color: #1E40AF; }
.tag-monthly { background: #D1FAE5; color: #065F46; }
.tag-temp { background: #FEF3C7; color: #B45309; }

.tag-free { background: #D1FAE5; color: #065F46; }
.tag-occupied { background: #FEE2E2; color: #991B1B; }
.tag-disabled { background: #F3F4F6; color: #6B7280; }

.tag-paid { background: #D1FAE5; color: #065F46; }

/* 申请类型标签 */
.tag-rent { background: #DBEAFE; color: #1E40AF; }
.tag-renew { background: #D1FAE5; color: #065F46; }
.tag-cancel { background: #FEE2E2; color: #991B1B; }
.tag-transfer { background: #FEF3C7; color: #B45309; }
.tag-add { background: #DBEAFE; color: #1E40AF; }
.tag-change { background: #FEF3C7; color: #B45309; }

.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }

/* 待审批卡片 */
.pending-card {
  margin-bottom: 16px;
  border-left: 4px solid #F59E0B;
}
.pending-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.pending-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}
.pending-title .badge {
  background: #EF4444;
  color: #fff;
  font-size: 12px;
  padding: 1px 8px;
  border-radius: 12px;
  margin-left: 6px;
}

/* 徽章 */
.badge {
  position: absolute;
  top: -6px;
  right: -6px;
  background: #EF4444;
  color: #fff;
  font-size: 11px;
  min-width: 20px;
  height: 20px;
  border-radius: 10px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0 6px;
}
.btn-warning {
  position: relative;
}

/* 审批弹窗 */
.approve-info {
  background: #F8FAFC;
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 16px;
  font-size: 14px;
  line-height: 1.8;
}
.approve-info strong {
  color: #334155;
}

.btn-success {
  background: #059669;
  color: #fff;
  border: none;
}
.btn-success:hover {
  background: #047857;
}
.btn-sm {
  padding: 3px 10px;
  font-size: 12px;
  margin-right: 4px !important;
}

@media (max-width: 640px) {
  .form-row { grid-template-columns: 1fr; }
  .toolbar { flex-direction: column; align-items: stretch; }
  .toolbar .actions { flex-wrap: wrap; }
  .toolbar .search { width: 100%; }
  .stats-bar { flex-wrap: wrap; gap: 8px; }
  .table { font-size: 12px; }
  .table .btn { font-size: 11px; padding: 2px 6px; }
}
</style>