<template>
  <div>
    <div class="toolbar">
      <h2 class="page-title">购房申请审核</h2>
      <div class="actions">
        <input v-model="keyword" class="input search" placeholder="搜索房号/业主..." @keyup.enter="load" />
        <select v-model="statusFilter" class="select" @change="load">
          <option value="">全部状态 ({{ list.length }})</option>
          <option value="PENDING">⏳ 待审核 ({{ statusStats.pending }})</option>
          <option value="APPROVED">✅ 已通过待付款 ({{ statusStats.approved }})</option>
          <option value="PAID">💰 已付款 ({{ statusStats.paid }})</option>
          <option value="COMPLETED">🏠 已完成 ({{ statusStats.completed }})</option>
          <option value="REJECTED">❌ 已拒绝 ({{ statusStats.rejected }})</option>
          <option value="CANCELLED">🔷 已取消 ({{ statusStats.cancelled }})</option>
        </select>
        <button class="btn btn-primary" @click="load">刷新</button>
      </div>
    </div>

    <div class="stats-bar">
      <span>共 <strong>{{ filteredList.length }}</strong> 条</span>
      <span>⏳ 待审核 <strong>{{ statusStats.pending }}</strong></span>
      <span>✅ 待付款 <strong>{{ statusStats.approved }}</strong></span>
      <span>💰 已付款 <strong>{{ statusStats.paid }}</strong></span>
      <span>🏠 已完成 <strong>{{ statusStats.completed }}</strong></span>
      <span>❌ 已拒绝 <strong>{{ statusStats.rejected }}</strong></span>
    </div>

    <div class="card">
      <table class="table">
        <thead>
        <tr>
          <th>序号</th>
          <th>楼栋</th>
          <th>房号</th>
          <th>申请人</th>
          <th>购房金额</th>
          <th>申请日期</th>
          <th>状态</th>
          <th>操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="(item, index) in filteredList" :key="item.id">
          <td>{{ index + 1 }}</td>
          <td>{{ buildingMap[item.buildingId] || item.buildingId }}</td>
          <td><strong>{{ item.unitNo }}</strong></td>
          <td>{{ ownerMap[item.ownerId]?.name || ownerMap[item.ownerId]?.ownerName || item.ownerId }}</td>
          <td><strong class="money">¥{{ formatMoney(item.purchaseAmount) }}</strong></td>
          <td>{{ formatDate(item.createdAt) }}</td>
          <td><span :class="['tag', statusClass(item.status)]">{{ statusLabel(item.status) }}</span></td>
          <td>
            <button class="btn btn-ghost" @click="viewDetail(item)">查看详情</button>
            <button v-if="item.status === 'PENDING'" class="btn btn-success btn-sm" @click="approve(item)">通过</button>
            <button v-if="item.status === 'PENDING'" class="btn btn-danger btn-sm" @click="reject(item)">拒绝</button>
            <button v-if="item.status === 'APPROVED'" class="btn btn-warning btn-sm" @click="cancel(item)">撤销</button>
            <button v-if="item.status === 'PAID'" class="btn btn-primary btn-sm" @click="complete(item)">确认完成</button>
          </td>
        </tr>
        <tr v-if="!filteredList.length"><td colspan="8" class="empty">暂无数据</td></tr>
        </tbody>
      </table>
    </div>

    <!-- 详情弹窗 -->
    <div v-if="showDetailModal" class="modal-mask" @click.self="showDetailModal = false">
      <div class="modal modal-wide">
        <h3>申请详情</h3>
        <div class="detail-grid">
          <div class="detail-section">
            <h4>房屋信息</h4>
            <div class="form-group">
              <label>楼栋</label>
              <span class="form-value">{{ buildingMap[detailItem.buildingId] || detailItem.buildingId }}</span>
            </div>
            <div class="form-group">
              <label>房号</label>
              <span class="form-value">{{ detailItem.unitNo }}</span>
            </div>
            <div class="form-group">
              <label>面积</label>
              <span class="form-value">{{ unitMap[detailItem.unitId]?.areaSqm || '—' }} ㎡</span>
            </div>
            <div class="form-group">
              <label>户型</label>
              <span class="form-value">{{ unitMap[detailItem.unitId]?.unitType || '—' }}</span>
            </div>
            <div class="form-group">
              <label>楼层</label>
              <span class="form-value">{{ unitMap[detailItem.unitId]?.floor || '—' }}层</span>
            </div>
          </div>
          <div class="detail-section">
            <h4>申请人信息</h4>
            <div class="form-group">
              <label>申请人</label>
              <span class="form-value">{{ ownerMap[detailItem.ownerId]?.name || ownerMap[detailItem.ownerId]?.ownerName || detailItem.ownerId }}</span>
            </div>
            <div class="form-group">
              <label>联系电话</label>
              <span class="form-value">{{ ownerMap[detailItem.ownerId]?.phone || ownerMap[detailItem.ownerId]?.contactPhone || '—' }}</span>
            </div>
          </div>
          <div class="detail-section">
            <h4>申请信息</h4>
            <div class="form-group">
              <label>购房金额</label>
              <span class="form-value money">¥{{ formatMoney(detailItem.purchaseAmount) }}</span>
            </div>
            <div class="form-group">
              <label>申请日期</label>
              <span class="form-value">{{ formatDate(detailItem.createdAt) }}</span>
            </div>
            <div class="form-group">
              <label>申请备注</label>
              <span class="form-value">{{ detailItem.remark || '无' }}</span>
            </div>
          </div>
          <div class="detail-section">
            <h4>审核信息</h4>
            <div class="form-group">
              <label>状态</label>
              <span :class="['tag', statusClass(detailItem.status)]">{{ statusLabel(detailItem.status) }}</span>
            </div>
            <div v-if="detailItem.approvalDate" class="form-group">
              <label>审核日期</label>
              <span class="form-value">{{ formatDate(detailItem.approvalDate) }}</span>
            </div>
            <div v-if="detailItem.approvalRemark" class="form-group">
              <label>审核备注</label>
              <span class="form-value">{{ detailItem.approvalRemark }}</span>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="showDetailModal = false">关闭</button>
        </div>
      </div>
    </div>

    <!-- 审核弹窗 -->
    <div v-if="showApproveModal" class="modal-mask" @click.self="showApproveModal = false">
      <div class="modal">
        <h3>{{ approveAction === 'approve' ? '✅ 通过申请' : '❌ 拒绝申请' }}</h3>

        <div v-if="approveAction === 'approve'" class="form-group">
          <label>购房金额（必填）</label>
          <div class="input-group">
            <span class="input-prefix">¥</span>
            <input v-model.number="purchaseAmount" type="number" class="input" placeholder="请输入购房金额" />
            <span class="input-suffix">元</span>
          </div>
          <div class="form-hint">请输入该房屋的成交价格</div>
        </div>

        <div class="form-group">
          <label>审核备注</label>
          <textarea v-model="approvalRemark" class="textarea" rows="3" placeholder="请输入审核备注..."></textarea>
        </div>

        <div class="modal-footer">
          <button class="btn btn-secondary" @click="showApproveModal = false">取消</button>
          <button
            :class="['btn', approveAction === 'approve' ? 'btn-success' : 'btn-danger']"
            :disabled="approveAction === 'approve' && !purchaseAmount"
            @click="submitApproval">
            确认{{ approveAction === 'approve' ? '通过' : '拒绝' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '../api/request'

const list = ref([])
const buildings = ref([])
const owners = ref([])
const units = ref([])
const keyword = ref('')
const statusFilter = ref('')
const showDetailModal = ref(false)
const showApproveModal = ref(false)
const detailItem = ref({})
const approveAction = ref('')
const pendingItem = ref(null)
const approvalRemark = ref('')
const purchaseAmount = ref(null)

const buildingMap = computed(() => {
  const map = {}
  buildings.value.forEach(b => { map[b.id] = b.buildingName })
  return map
})

const ownerMap = computed(() => {
  const map = {}
  owners.value.forEach(o => { map[o.id] = o })
  return map
})

const unitMap = computed(() => {
  const map = {}
  units.value.forEach(u => { map[u.id] = u })
  return map
})

const filteredList = computed(() => {
  let result = list.value
  if (keyword.value) {
    const kw = keyword.value.toLowerCase()
    result = result.filter(item =>
      (item.unitNo || '').toLowerCase().includes(kw) ||
      (ownerMap.value[item.ownerId]?.name || '').toLowerCase().includes(kw)
    )
  }
  if (statusFilter.value) {
    result = result.filter(item => item.status === statusFilter.value)
  }
  return result.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
})

const statusStats = computed(() => ({
  pending: list.value.filter(i => i.status === 'PENDING').length,
  approved: list.value.filter(i => i.status === 'APPROVED').length,
  paid: list.value.filter(i => i.status === 'PAID').length,
  completed: list.value.filter(i => i.status === 'COMPLETED').length,
  rejected: list.value.filter(i => i.status === 'REJECTED').length,
  cancelled: list.value.filter(i => i.status === 'CANCELLED').length
}))

function statusClass(status) {
  const classes = {
    PENDING: 'tag-warning',
    APPROVED: 'tag-info',
    PAID: 'tag-success',
    COMPLETED: 'tag-primary',
    REJECTED: 'tag-danger',
    CANCELLED: 'tag-default'
  }
  return classes[status] || 'tag-default'
}

function statusLabel(status) {
  const labels = {
    PENDING: '待审核',
    APPROVED: '待付款',
    PAID: '已付款',
    COMPLETED: '已完成',
    REJECTED: '已拒绝',
    CANCELLED: '已取消'
  }
  return labels[status] || status
}

function formatDate(date) {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function formatMoney(amount) {
  if (!amount) return '0.00'
  return Number(amount).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

async function load() {
  try {
    list.value = await request.get('/house-purchases')
  } catch (e) {
    console.error('加载数据失败', e)
  }
}

async function loadRefData() {
  try {
    buildings.value = await request.get('/buildings')
    owners.value = await request.get('/owners')
    units.value = await request.get('/units')
  } catch (e) {
    console.error('加载参考数据失败', e)
  }
}

function viewDetail(item) {
  detailItem.value = item
  showDetailModal.value = true
}

function approve(item) {
  pendingItem.value = item
  approveAction.value = 'approve'
  approvalRemark.value = ''
  purchaseAmount.value = null
  showApproveModal.value = true
}

function reject(item) {
  pendingItem.value = item
  approveAction.value = 'reject'
  approvalRemark.value = ''
  purchaseAmount.value = null
  showApproveModal.value = true
}

async function submitApproval() {
  if (approveAction.value === 'approve' && !purchaseAmount.value) {
    alert('请输入购房金额')
    return
  }

  try {
    await request.post(`/house-purchases/${pendingItem.value.id}/approve`, null, {
      params: {
        approved: approveAction.value === 'approve',
        remark: approvalRemark.value,
        purchaseAmount: purchaseAmount.value
      }
    })
    showApproveModal.value = false
    await load()
    alert(approveAction.value === 'approve' ? '审核通过成功，将生成购房款账单' : '审核拒绝成功')
  } catch (e) {
    console.error('审核失败', e)
    alert('审核失败，请重试')
  }
}

async function cancel(item) {
  if (!confirm('确定要撤销此购房申请吗？撤销后业主需重新申请。')) {
    return
  }
  try {
    await request.delete(`/house-purchases/${item.id}`)
    await load()
    alert('撤销成功')
  } catch (e) {
    console.error('撤销失败', e)
    alert('撤销失败，请重试')
  }
}

async function complete(item) {
  if (!confirm('确定要确认完成此购房申请吗？确认后将把房屋关联到业主。')) {
    return
  }
  try {
    await request.post(`/house-purchases/${item.id}/complete`)
    await load()
    alert('确认完成成功')
  } catch (e) {
    console.error('确认完成失败', e)
    alert('确认完成失败，请重试')
  }
}

onMounted(async () => {
  await loadRefData()
  await load()
})
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text);
}

.actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.input.search {
  width: 200px;
}

.btn {
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
  font-family: inherit;
  border: none;
  transition: all var(--transition);
}

.btn-sm {
  padding: 5px 10px;
  font-size: 12px;
  margin-left: 4px;
}

.btn-primary {
  background: var(--color-primary);
  color: #fff;
}

.btn-primary:hover {
  background: var(--color-primary-dark);
}

.btn-success {
  background: #10B981;
  color: #fff;
}

.btn-success:hover {
  background: #059669;
}

.btn-success:disabled {
  background: #9CA3AF;
  cursor: not-allowed;
}

.btn-danger {
  background: #EF4444;
  color: #fff;
}

.btn-danger:hover {
  background: #DC2626;
}

.btn-secondary {
  background: var(--color-border);
  color: var(--color-text);
}

.btn-secondary:hover {
  background: var(--color-border-dark);
}

.btn-ghost {
  background: transparent;
  color: var(--color-primary);
  border: 1px solid var(--color-primary);
}

.btn-ghost:hover {
  background: rgba(4, 120, 87, 0.1);
}

.select {
  padding: 8px 12px;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  font-size: 13px;
  background: #fff;
  color: var(--color-text);
}

.stats-bar {
  display: flex;
  gap: 20px;
  padding: 12px 16px;
  background: var(--color-surface);
  border-radius: 8px;
  margin-bottom: 16px;
  font-size: 13px;
  color: var(--color-text-secondary);
}

.card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.table {
  width: 100%;
  border-collapse: collapse;
}

.table th, .table td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid var(--color-border);
  font-size: 13px;
}

.table th {
  background: var(--color-surface);
  font-weight: 600;
  color: var(--color-text);
}

.table tbody tr:hover {
  background: var(--color-surface-hover);
}

.money {
  color: #DC2626;
  font-weight: 600;
}

.tag {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.tag-warning {
  background: #FEF3C7;
  color: #D97706;
}

.tag-info {
  background: #DBEAFE;
  color: #2563EB;
}

.tag-success {
  background: #D1FAE5;
  color: #059669;
}

.tag-primary {
  background: #E0E7FF;
  color: #4F46E5;
}

.tag-danger {
  background: #FEE2E2;
  color: #DC2626;
}

.tag-default {
  background: var(--color-surface);
  color: var(--color-text-secondary);
}

.empty {
  text-align: center;
  padding: 40px;
  color: var(--color-text-secondary);
}

.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.modal {
  background: #fff;
  border-radius: 12px;
  width: 480px;
  max-height: 85vh;
  overflow-y: auto;
  padding: 24px;
}

.modal-wide {
  width: 640px;
}

.modal h3 {
  margin: 0 0 20px;
  font-size: 18px;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.detail-section {
  padding: 16px;
  background: var(--color-surface);
  border-radius: 8px;
}

.detail-section h4 {
  margin: 0 0 12px;
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
  padding-bottom: 8px;
  border-bottom: 1px solid var(--color-border);
}

.form-group {
  margin-bottom: 12px;
}

.form-group:last-child {
  margin-bottom: 0;
}

.form-group label {
  display: block;
  font-size: 12px;
  font-weight: 500;
  margin-bottom: 4px;
  color: var(--color-text-secondary);
}

.form-value {
  font-size: 14px;
  color: var(--color-text);
}

.form-hint {
  font-size: 12px;
  color: var(--color-text-secondary);
  margin-top: 4px;
}

.input-group {
  display: flex;
  align-items: center;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  overflow: hidden;
}

.input-prefix, .input-suffix {
  padding: 8px 12px;
  background: var(--color-surface);
  color: var(--color-text-secondary);
  font-size: 13px;
}

.input-group .input {
  flex: 1;
  border: none;
  padding: 8px 12px;
  font-size: 14px;
  outline: none;
}

.input-group .input:focus {
  box-shadow: none;
}

.textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  font-size: 13px;
  font-family: inherit;
  resize: vertical;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 24px;
}
</style>
