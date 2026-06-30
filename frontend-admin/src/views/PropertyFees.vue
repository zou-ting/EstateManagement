<template>
  <div>
    <!-- 工具栏 -->
    <div class="toolbar">
      <h2 class="page-title">物业费管理</h2>
      <div class="actions">
        <input v-model="keyword" class="input search" placeholder="搜索业主/房屋..." @keyup.enter="load" />
        <select v-model="statusFilter" class="select" @change="load">
          <option value="">全部状态</option>
          <option value="PAID">✅ 已缴</option>
          <option value="UNPAID">⏳ 未缴</option>
        </select>
        <select v-model="monthFilter" class="select" @change="load">
          <option value="">全部账期</option>
          <option v-for="m in monthOptions" :key="m" :value="m">{{ m }}</option>
        </select>
        <button class="btn btn-primary" @click="openForm()">+ 生成账单</button>
      </div>
    </div>

    <!-- 统计栏 -->
    <div class="stats-bar">
      <span>共 <strong>{{ filteredList.length }}</strong> 笔</span>
      <span>💰 总金额 <strong>¥{{ totalAmount }}</strong></span>
      <span>✅ 已缴 <strong>{{ statusStats.paid }}</strong> 笔</span>
      <span>⏳ 未缴 <strong>{{ statusStats.unpaid }}</strong> 笔</span>
    </div>

    <!-- 表格 -->
    <div class="card">
      <table class="table">
        <thead>
        <tr>
          <th>序号</th>
          <th>房屋</th>
          <th>账期</th>
          <th>物业费</th>
          <th>公摊费</th>
          <th>合计</th>
          <th>状态</th>
          <th>操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="(item, index) in filteredList" :key="item.id">
          <td>{{ index + 1 }}</td>
          <td>{{ unitLabel(item.roomId) }}</td>
          <td>{{ item.billMonth }}</td>
          <td>¥{{ item.managementFee ?? 0 }}</td>
          <td>¥{{ item.publicFee ?? 0 }}</td>
          <td class="amount">¥{{ item.totalFee ?? 0 }}</td>
          <td><span :class="['tag', statusClass(item.payStatus)]">{{ payLabel(item.payStatus) }}</span></td>
          <td>
            <button class="btn btn-ghost" @click="viewDetail(item)">详情</button>
            <button class="btn btn-ghost" @click="openForm(item)">编辑</button>
            <button class="btn btn-danger" @click="remove(item)">删除</button>
          </td>
        </tr>
        <tr v-if="!filteredList.length"><td colspan="8" class="empty">暂无数据</td></tr>
        </tbody>
      </table>
    </div>

    <!-- 弹窗表单 -->
    <div v-if="showModal" class="modal-mask" @click.self="showModal = false">
      <div class="modal">
        <h3>{{ form.id ? '✏️ 编辑账单' : '➕ 生成账单' }}</h3>
        <div class="form-group">
          <label>房屋 <span class="required">*</span></label>
          <select v-model.number="form.roomId" class="select">
            <option v-for="u in units" :key="u.id" :value="u.id">{{ unitOptionLabel(u) }}</option>
          </select>
        </div>
        <div class="form-group">
          <label>账期(YYYY-MM) <span class="required">*</span></label>
          <input v-model="form.billMonth" class="input" placeholder="2025-03" />
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>物业费(元)</label>
            <input v-model.number="form.managementFee" type="number" min="0" step="0.01" class="input" @input="calcTotal" placeholder="0.00" />
          </div>
          <div class="form-group">
            <label>公摊费(元)</label>
            <input v-model.number="form.publicFee" type="number" min="0" step="0.01" class="input" @input="calcTotal" placeholder="0.00" />
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>合计(元)</label>
            <input v-model.number="form.totalFee" type="number" class="input" readonly />
          </div>
          <div class="form-group">
            <label>缴费状态</label>
            <select v-model="form.payStatus" class="select">
              <option value="UNPAID">⏳ 未缴</option>
              <option value="PAID">✅ 已缴</option>
            </select>
          </div>
        </div>

        <!-- 当缴费状态为"已缴"时，显示支付方式 -->
        <div class="form-group" v-if="form.payStatus === 'PAID'">
          <label>支付方式 <span class="required">*</span></label>
          <select v-model="form.payMethod" class="select">
            <option value="CASH">💵 现金</option>
            <option value="ALIPAY">📱 支付宝</option>
            <option value="WECHAT">💚 微信</option>
            <option value="BANK">🏦 银行卡</option>
            <option value="CARD">💳 刷卡</option>
          </select>
        </div>

        <p v-if="formError" class="err">{{ formError }}</p>
        <div class="modal-actions">
          <button class="btn btn-ghost" @click="showModal = false">取消</button>
          <button class="btn btn-primary" @click="save">保存</button>
        </div>
      </div>
    </div>

    <!-- 缴费记录详情弹窗 -->
    <div v-if="showDetailModal" class="modal-mask" @click.self="showDetailModal = false">
      <div class="modal modal-lg">
        <h3>📋 缴费记录 - {{ detailRoomName }}</h3>
        <div class="detail-stats">
          <span>💰 累计缴费：<strong>¥{{ detailTotal }}</strong></span>
          <span>📄 缴费次数：<strong>{{ detailRecords.length }}</strong> 次</span>
        </div>
        <table class="table" v-if="detailRecords.length">
          <thead>
          <tr>
            <th>账期</th>
            <th>金额</th>
            <th>支付方式</th>
            <th>缴费时间</th>
            <th>操作人</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="record in detailRecords" :key="record.id">
            <td>{{ record.billMonth || '—' }}</td>
            <td class="amount">¥{{ record.amount }}</td>
            <td><span :class="['pay-tag', payMethodClass(record.payMethod)]">{{ payMethodLabel(record.payMethod) }}</span></td>
            <td>{{ formatDate(record.payDate) }}</td>
            <td>{{ record.operator || '—' }}</td>
          </tr>
          </tbody>
        </table>
        <p v-else class="empty">暂无缴费记录</p>
        <div class="modal-actions">
          <button class="btn btn-ghost" @click="showDetailModal = false">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, computed } from 'vue'
import request from '../api/request'
import { showConfirm } from '../composables/useConfirm'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()

const list = ref([])
const units = ref([])
const buildings = ref([])
const keyword = ref('')
const statusFilter = ref('')
const monthFilter = ref('')
const showModal = ref(false)
const showDetailModal = ref(false)
const formError = ref('')
const detailRecords = ref([])
const detailRoomName = ref('')
const detailTotal = ref('0.00')

const form = reactive({
  id: null,
  roomId: null,
  billMonth: '',
  managementFee: 0,
  publicFee: 0,
  totalFee: 0,
  payStatus: 'UNPAID',
  payMethod: 'CASH'
})

// 账期选项
const monthOptions = computed(() => {
  const months = new Set()
  list.value.forEach(item => {
    if (item.billMonth) months.add(item.billMonth)
  })
  return Array.from(months).sort()
})

// 筛选列表
const filteredList = computed(() => {
  let result = list.value
  if (keyword.value.trim()) {
    const kw = keyword.value.trim().toLowerCase()
    result = result.filter(item => {
      const label = unitLabel(item.roomId)
      return label?.toLowerCase().includes(kw)
    })
  }
  if (statusFilter.value) {
    result = result.filter(item => item.payStatus === statusFilter.value)
  }
  if (monthFilter.value) {
    result = result.filter(item => item.billMonth === monthFilter.value)
  }
  return result
})

// 状态统计
const statusStats = computed(() => {
  const stats = { paid: 0, unpaid: 0 }
  list.value.forEach(item => {
    if (item.payStatus === 'PAID') stats.paid++
    else if (item.payStatus === 'UNPAID') stats.unpaid++
  })
  return stats
})

// 总金额
const totalAmount = computed(() => {
  const total = filteredList.value.reduce((sum, item) => sum + (item.totalFee || 0), 0)
  return total.toFixed(2)
})

function payLabel(s) {
  return s === 'PAID' ? '已缴' : '未缴'
}

function statusClass(s) {
  return s === 'PAID' ? 'tag-paid' : 'tag-unpaid'
}

function unitOptionLabel(u) {
  const b = buildings.value.find(x => x.id === u.buildingId)
  return `${b?.buildingName || ''} ${u.unitNo}`.trim()
}

function unitLabel(roomId) {
  const u = units.value.find(x => x.id === roomId)
  return u ? unitOptionLabel(u) : `#${roomId}`
}

function calcTotal() {
  form.totalFee = Number(((form.managementFee || 0) + (form.publicFee || 0)).toFixed(2))
}

// 支付方式映射
function payMethodLabel(method) {
  const map = {
    'CASH': '现金',
    'ALIPAY': '支付宝',
    'WECHAT': '微信',
    'BANK': '银行卡',
    'CARD': '刷卡'
  }
  return map[method] || method || '—'
}

// 支付方式样式类
function payMethodClass(method) {
  const map = {
    'CASH': 'tag-cash',
    'ALIPAY': 'tag-alipay',
    'WECHAT': 'tag-wechat',
    'BANK': 'tag-bank',
    'CARD': 'tag-card'
  }
  return map[method] || 'tag-default'
}

// 格式化日期
function formatDate(date) {
  if (!date) return '—'
  const d = new Date(date)
  return d.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

async function loadMeta() {
  buildings.value = await request.get('/buildings')
  units.value = await request.get('/units')
}

async function load() {
  list.value = await request.get('/property-fees')
}
// 查看详情
async function viewDetail(item) {
  try {
    // 获取缴费记录
    let records = await request.get(`/payments/fee/${item.id}`)

    // 如果该账单是已缴但没有缴费记录，自动补录
    if (item.payStatus === 'PAID' && (!records || records.length === 0)) {
      try {
        await request.post(`/payments/repair/${item.id}`)
        records = await request.get(`/payments/fee/${item.id}`)
      } catch (e) {
        console.log('补录失败:', e)
      }
    }

    // ✅ 如果有缴费记录，用账单金额覆盖（保证一致）
    if (records && records.length > 0) {
      // 用账单的合计金额覆盖缴费记录的金额
      records = records.map(r => ({
        ...r,
        amount: item.totalFee,  // ✅ 以账单为准
        billMonth: item.billMonth  // ✅ 以账单为准
      }))
    } else if (item.payStatus === 'PAID') {
      // 没有缴费记录但账单已缴 → 用账单数据构造
      records = [{
        id: item.id,
        feeId: item.id,
        roomId: item.roomId,
        billMonth: item.billMonth,
        amount: item.totalFee,
        payMethod: 'CASH',
        payDate: new Date().toISOString(),
        operator: '系统自动'
      }]
    }

    detailRecords.value = records || []
    detailRoomName.value = unitLabel(item.roomId)
    const total = (records || []).reduce((sum, r) => sum + (r.amount || 0), 0)
    detailTotal.value = total.toFixed(2)
    showDetailModal.value = true
  } catch (e) {
    console.error('加载缴费记录失败:', e)
  }
}

function openForm(item) {
  formError.value = ''
  Object.assign(form, {
    id: null,
    roomId: units.value[0]?.id ?? null,
    billMonth: new Date().toISOString().slice(0, 7),
    managementFee: 0,
    publicFee: 0,
    totalFee: 0,
    payStatus: 'UNPAID',
    payMethod: 'CASH'
  })
  if (item) Object.assign(form, item)
  showModal.value = true
}

async function save() {
  if (!form.roomId) { formError.value = '请选择房屋'; return }
  if (!form.billMonth?.trim()) { formError.value = '请输入账期'; return }
  if (!/^\d{4}-\d{2}$/.test(form.billMonth)) { formError.value = '账期格式错误，请使用 YYYY-MM 格式'; return }
  calcTotal()
  formError.value = ''

  // 如果状态是"已缴"，必须选择支付方式
  if (form.payStatus === 'PAID' && !form.payMethod) {
    formError.value = '请选择支付方式'
    return
  }

  try {
    let updatedFee
    if (form.id) {
      updatedFee = await request.put(`/property-fees/${form.id}`, form)
    } else {
      updatedFee = await request.post('/property-fees', form)
    }

    // ✅ 处理缴费记录
    if (form.id) {
      // 查询是否已有缴费记录
      const existingPayments = await request.get(`/payments/fee/${form.id}`)

      if (form.payStatus === 'PAID') {
        // ✅ 账单已缴 → 创建或更新缴费记录
        const paymentData = {
          feeId: form.id,
          roomId: form.roomId,
          amount: form.totalFee,
          payMethod: form.payMethod,
          operator: userStore.user?.name || '系统自动',
          remark: '同步账单更新'
        }

        if (existingPayments && existingPayments.length > 0) {
          // 已有记录 → 更新
          await request.put(`/payments/fee/${form.id}`, paymentData)
        } else {
          // 没有记录 → 新增
          await request.post('/payments', paymentData)
        }
      } else {
        // ✅ 账单未缴 → 删除缴费记录
        if (existingPayments && existingPayments.length > 0) {
          await request.delete(`/payments/fee/${form.id}`)
        }
      }
    }

    showModal.value = false
    load()
  } catch (e) {
    formError.value = e.message
  }
}

async function remove(item) {
  const ok = await showConfirm('删除确认', '确定删除该账单吗？')
  if (!ok) return
  await request.delete(`/property-fees/${item.id}`)
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
}
.stats-bar strong { color: #064E3B; }

.detail-stats {
  display: flex;
  gap: 30px;
  padding: 12px 16px;
  background: #F8FAFC;
  border-radius: 8px;
  margin-bottom: 16px;
  font-size: 14px;
}
.detail-stats strong {
  color: #064E3B;
}

.table .btn { margin-right: 8px; padding: 4px 10px; font-size: 13px; }
.amount { font-weight: 600; color: var(--color-primary); }
.empty { text-align: center; color: var(--color-text-secondary); padding: 24px !important; }
.err { color: var(--color-danger); font-size: 13px; }
.required { color: #DC2626; }

.tag-paid { background: #D1FAE5; color: #065F46; padding: 2px 12px; border-radius: 12px; font-size: 12px; }
.tag-unpaid { background: #FEF3C7; color: #B45309; padding: 2px 12px; border-radius: 12px; font-size: 12px; }

/* 支付方式标签 */
.pay-tag {
  padding: 2px 12px;
  border-radius: 12px;
  font-size: 12px;
  display: inline-block;
  font-weight: 500;
}
.tag-cash { background: #E0F2FE; color: #0369A1; }
.tag-alipay { background: #1677FF; color: #fff; }
.tag-wechat { background: #07C160; color: #fff; }
.tag-bank { background: #FEF3C7; color: #B45309; }
.tag-card { background: #F3E8FF; color: #7C3AED; }
.tag-default { background: #F1F5F9; color: #64748B; }

.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.modal-lg { width: min(700px, 94vw); }

@media (max-width: 640px) {
  .form-row { grid-template-columns: 1fr; }
  .toolbar { flex-direction: column; align-items: stretch; }
  .toolbar .actions { flex-wrap: wrap; }
  .toolbar .search { width: 100%; }
}
</style>