<template>
  <div>
    <div class="toolbar">
      <h2 class="page-title">费用详情</h2>
      <div class="actions">
        <input v-model="keyword" class="input search" placeholder="搜索房屋..." @keyup.enter="load" />
        <select v-model="statusFilter" class="select" @change="load">
          <option value="">全部状态</option>
          <option value="PAID">✅ 已缴</option>
          <option value="UNPAID">⏳ 未缴</option>
        </select>
        <select v-model="monthFilter" class="select" @change="load">
          <option value="">全部账期</option>
          <option v-for="m in monthOptions" :key="m" :value="m">{{ m }}</option>
        </select>
      </div>
    </div>

    <div class="stats-bar">
      <span>共 <strong>{{ filteredList.length }}</strong> 笔</span>
      <span>💰 总金额 <strong>¥{{ totalAmount }}</strong></span>
      <span>✅ 已缴 <strong>{{ statusStats.paid }}</strong> 笔</span>
      <span>⏳ 未缴 <strong>{{ statusStats.unpaid }}</strong> 笔</span>
    </div>

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
            <button v-if="item.payStatus !== 'PAID'" class="btn btn-primary" @click="payFee(item)">缴费</button>
          </td>
        </tr>
        <tr v-if="!filteredList.length"><td colspan="8" class="empty">暂无数据</td></tr>
        </tbody>
      </table>
    </div>

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

    <div v-if="currentStep === 'select'" class="modal-mask" @click.self="closeAllModal">
      <div class="modal">
        <h3>选择支付方式</h3>
        <div class="pay-info">
          <div><span>账单号</span><span>#{{ payTarget?.id }}</span></div>
          <div><span>账期</span><span>{{ payTarget?.billMonth }}</span></div>
          <div><span>金额</span><span class="amount">¥{{ payTarget?.totalFee ?? 0 }}</span></div>
        </div>
        <div class="pay-method">
          <label>支付方式</label>
          <div class="method-options">
            <button :class="['method-btn', payMethod === 'WECHAT' ? 'active' : '']" @click="payMethod = 'WECHAT'">
              <span class="wechat-icon">💚</span>
              <span>微信支付</span>
            </button>
            <button :class="['method-btn', payMethod === 'ALIPAY' ? 'active' : '']" @click="payMethod = 'ALIPAY'">
              <span class="alipay-icon">💙</span>
              <span>支付宝</span>
            </button>
          </div>
        </div>
        <div class="modal-actions">
          <button class="btn btn-ghost" @click="closeAllModal">取消</button>
          <button class="btn btn-primary" @click="goToQrCode" :disabled="!payMethod">确认选择</button>
        </div>
      </div>
    </div>

    <div v-if="currentStep === 'qrcode'" class="modal-mask" @click.self="closeAllModal">
      <div class="modal">
        <h3>扫码支付</h3>
        <div :class="['qr-content', payMethod.toLowerCase()]">
          <div class="qr-brand">{{ payMethod === 'WECHAT' ? '💚 微信支付' : '💙 支付宝' }}</div>
          <div class="qr-amount">¥{{ payTarget?.totalFee ?? 0 }}</div>
          <div class="qr-image-lg">
            <img :src="qrCodeUrl" alt="支付二维码" />
          </div>
          <p class="qr-hint">请使用{{ payMethod === 'WECHAT' ? '微信' : '支付宝' }}扫码完成支付</p>
        </div>
        <div class="modal-actions">
          <button class="btn btn-ghost" @click="goBack">重新选择</button>
          <button class="btn btn-primary" @click="handleConfirmPay">确认支付</button>
        </div>
      </div>
    </div>

    <div v-if="currentStep === 'success'" class="modal-mask" @click.self="closeAllModal">
      <div class="success-modal">
        <div class="success-icon">✓</div>
        <h3>支付成功</h3>
        <p>账单 #{{ payTarget?.id }} 已完成缴费</p>
        <button class="btn btn-primary" @click="closeAllModal">确定</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, computed } from 'vue'
import request from '../api/request'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()

const list = ref([])
const units = ref([])
const buildings = ref([])
const keyword = ref('')
const statusFilter = ref('')
const monthFilter = ref('')
const showDetailModal = ref(false)
const detailRecords = ref([])
const detailRoomName = ref('')
const detailTotal = ref('0.00')

const currentStep = ref('')
const payTarget = ref(null)
const payMethod = ref('')

const monthOptions = computed(() => {
  const months = new Set()
  list.value.forEach(item => {
    if (item.billMonth) months.add(item.billMonth)
  })
  return Array.from(months).sort()
})

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

const statusStats = computed(() => {
  const stats = { paid: 0, unpaid: 0 }
  list.value.forEach(item => {
    if (item.payStatus === 'PAID') stats.paid++
    else if (item.payStatus === 'UNPAID') stats.unpaid++
  })
  return stats
})

const totalAmount = computed(() => {
  const total = filteredList.value.reduce((sum, item) => sum + (item.totalFee || 0), 0)
  return total.toFixed(2)
})

const qrCodeUrl = computed(() => {
  if (payMethod.value === 'WECHAT') {
    return new URL('../assets/wechat-qr.png', import.meta.url).href
  } else if (payMethod.value === 'ALIPAY') {
    return new URL('../assets/alipay-qr.png', import.meta.url).href
  }
  return ''
})

function payLabel(s) {
  return s === 'PAID' ? '已缴' : '未缴'
}

function statusClass(s) {
  return s === 'PAID' ? 'tag-paid' : 'tag-unpaid'
}

function unitLabel(roomId) {
  const u = units.value.find(x => x.id === roomId)
  if (!u) return `#${roomId}`
  const b = buildings.value.find(x => x.id === u.buildingId)
  return `${b?.buildingName || ''} ${u.unitNo}`.trim()
}

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
  if (!userStore.user?.ownerId) return
  const owner = await request.get(`/owners/${userStore.user.ownerId}`)
  if (owner?.roomId) {
    list.value = await request.get('/property-fees', { params: { roomId: owner.roomId } })
    const unpaid = list.value.filter(f => f.payStatus !== 'PAID').length
    userStore.setUnpaidCount(unpaid)
  }
}

async function viewDetail(item) {
  try {
    let records = await request.get(`/payments/fee/${item.id}`)

    if (item.payStatus === 'PAID' && (!records || records.length === 0)) {
      try {
        await request.post(`/payments/repair/${item.id}`)
        records = await request.get(`/payments/fee/${item.id}`)
      } catch (e) {
        console.log('补录失败:', e)
      }
    }

    if (records && records.length > 0) {
      records = records.map(r => ({
        ...r,
        amount: item.totalFee,
        billMonth: item.billMonth
      }))
    } else if (item.payStatus === 'PAID') {
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

function payFee(fee) {
  payTarget.value = fee
  payMethod.value = ''
  currentStep.value = 'select'
}

function goToQrCode() {
  if (!payMethod.value) return
  currentStep.value = 'qrcode'
}

function goBack() {
  currentStep.value = 'select'
}

async function handleConfirmPay() {
  if (!payTarget.value || !payMethod.value) return
  
  try {
    await request.put(`/property-fees/${payTarget.value.id}`, {
      ...payTarget.value,
      payStatus: 'PAID',
      payMethod: payMethod.value
    })
    
    await request.post('/payments', {
      feeId: payTarget.value.id,
      roomId: payTarget.value.roomId,
      billMonth: payTarget.value.billMonth,
      amount: payTarget.value.totalFee,
      payMethod: payMethod.value,
      payDate: new Date().toISOString(),
      operator: userStore.user?.name || '业主'
    })
    
    currentStep.value = 'success'
    await load()
  } catch (e) {
    console.error('支付失败:', e)
    alert('支付失败，请重试')
  }
}

function closeAllModal() {
  currentStep.value = ''
  payTarget.value = null
  payMethod.value = ''
  showDetailModal.value = false
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
}
.toolbar .search {
  width: 180px;
}
.toolbar .select {
  min-width: 120px;
  width: auto;
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

.tag-paid { background: #D1FAE5; color: #065F46; padding: 2px 12px; border-radius: 12px; font-size: 12px; }
.tag-unpaid { background: #FEF3C7; color: #B45309; padding: 2px 12px; border-radius: 12px; font-size: 12px; }

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

.modal-lg { width: min(700px, 94vw); }

.pay-info {
  background: #F8FAFC;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}
.pay-info div {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #E5E7EB;
}
.pay-info div:last-child { border-bottom: none; }
.pay-info span:first-child { color: #64748B; }
.pay-info .amount { color: var(--color-primary); font-weight: 600; }

.pay-method label { display: block; margin-bottom: 12px; font-weight: 500; }
.method-options { display: flex; gap: 12px; }
.method-btn {
  flex: 1;
  padding: 16px;
  border: 2px solid #E5E7EB;
  border-radius: 12px;
  background: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.2s;
}
.method-btn:hover { border-color: var(--color-primary); }
.method-btn.active { border-color: var(--color-primary); background: rgba(5, 150, 105, 0.06); }
.method-btn span:last-child { font-weight: 500; }

.qr-content {
  padding: 32px;
  border-radius: 16px;
  text-align: center;
}
.qr-content.wechat { background: linear-gradient(135deg, #ECFDF5 0%, #D1FAE5 100%); }
.qr-content.alipay { background: linear-gradient(135deg, #EFF6FF 0%, #DBEAFE 100%); }
.qr-brand { font-size: 20px; font-weight: 600; margin-bottom: 12px; }
.qr-amount { font-size: 28px; font-weight: 700; color: var(--color-primary); margin-bottom: 20px; }
.qr-image-lg { width: 260px; height: 260px; margin: 0 auto 16px; background: #fff; border-radius: 16px; padding: 16px; box-shadow: 0 4px 16px rgba(0,0,0,0.12); }
.qr-image-lg img { width: 100%; height: 100%; object-fit: contain; }
.qr-hint { color: #64748B; font-size: 14px; }

.success-modal {
  text-align: center;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
}
.success-icon {
  width: 80px;
  height: 80px;
  background: #D1FAE5;
  color: #059669;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  margin: 0 auto 20px;
}
.success-modal h3 { margin-bottom: 8px; }
.success-modal p { color: #64748B; }

@media (max-width: 640px) {
  .toolbar { flex-direction: column; align-items: stretch; }
  .toolbar .actions { flex-wrap: wrap; }
  .toolbar .search { width: 100%; }
}
</style>