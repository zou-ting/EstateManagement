<template>
  <div class="page">
    <h2 class="page-title">在线缴费</h2>
    <div class="card-list">
      <div v-for="fee in unpaidFees" :key="fee.id" class="card">
        <div class="fee-header">
          <span class="fee-billno">账单 #{{ fee.id }}</span>
          <span v-if="fee.feeType === 'HOUSE_PURCHASE'" class="fee-type-tag">🏠 购房款</span>
          <span v-else-if="fee.feeType === 'PARKING_PURCHASE'" class="fee-type-tag">🚗 车位购买款</span>
          <span v-else class="fee-date">{{ fee.billMonth }}</span>
        </div>
        <div class="fee-room">{{ unitLabel(fee.roomId) }}</div>
        <div class="fee-details">
          <div v-if="fee.feeType === 'HOUSE_PURCHASE'" class="fee-item house-purchase">
            <span>购房款</span>
            <span class="amount-lg">¥{{ fee.totalFee ?? 0 }}</span>
          </div>
          <div v-else-if="fee.feeType === 'PARKING_PURCHASE'" class="fee-item house-purchase">
            <span>车位购买款</span>
            <span class="amount-lg">¥{{ fee.totalFee ?? 0 }}</span>
          </div>
          <template v-else>
            <div class="fee-item">
              <span>物业费</span>
              <span>¥{{ fee.managementFee ?? 0 }}</span>
            </div>
            <div class="fee-item">
              <span>公摊费</span>
              <span>¥{{ fee.publicFee ?? 0 }}</span>
            </div>
          </template>
        </div>
        <div v-if="fee.feeType !== 'HOUSE_PURCHASE'" class="fee-total">
          <span>合计</span>
          <span class="amount">¥{{ fee.totalFee ?? 0 }}</span>
        </div>
        <button class="btn btn-primary btn-pay" @click="payFee(fee)">
          {{ fee.feeType === 'HOUSE_PURCHASE' ? '立即支付购房款' : fee.feeType === 'PARKING_PURCHASE' ? '立即支付车位款' : '立即支付' }}
        </button>
      </div>
      <div v-if="!unpaidFees.length" class="empty-state">
        <div class="empty-icon">✓</div>
        <p>暂无待缴费用</p>
      </div>
    </div>

    <!-- 支付方式选择弹窗 -->
    <div v-if="currentStep === 'select'" class="modal-mask" @click.self="closeAllModal">
      <div class="modal">
        <h3>选择支付方式</h3>
        <div class="pay-info">
          <div><span>账单号</span><span>#{{ payTarget?.id }}</span></div>
          <div><span>账期/类型</span><span>{{ payTarget?.feeType === 'HOUSE_PURCHASE' ? '购房款' : payTarget?.feeType === 'PARKING_PURCHASE' ? '车位购买款' : payTarget?.billMonth }}</span></div>
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

    <!-- 二维码弹窗 -->
    <div v-if="currentStep === 'qrcode'" class="modal-mask" @click.self="closeAllModal">
      <div class="qr-modal">
        <div class="qr-header">
          <h3>扫码支付</h3>
          <button class="close-btn" @click="closeAllModal">×</button>
        </div>
        <div :class="['qr-content', payMethod.toLowerCase()]">
          <div class="qr-brand">
            <span v-if="payMethod === 'WECHAT'" class="brand-icon wechat">✓</span>
            <span v-else class="brand-icon alipay">支</span>
            <span class="brand-name">{{ payMethod === 'WECHAT' ? '微信支付' : '支付宝' }}</span>
          </div>
          <div class="qr-code-box">
            <img :src="qrCodeUrl" :alt="payMethod === 'WECHAT' ? '微信支付二维码' : '支付宝支付二维码'" />
            <div class="qr-amount">¥{{ payTarget?.totalFee ?? 0 }}</div>
          </div>
          <p class="qr-tip">打开{{ payMethod === 'WECHAT' ? '微信' : '支付宝' }}扫一扫</p>
        </div>
        <div class="qr-actions">
          <button class="btn btn-ghost" @click="goBack">重新选择</button>
          <button class="btn btn-primary" @click="confirmPay">确认支付</button>
        </div>
      </div>
    </div>

    <!-- 支付成功弹窗 -->
    <div v-if="currentStep === 'success'" class="modal-mask" @click.self="closeAllModal">
      <div class="success-modal">
        <div class="success-icon">✓</div>
        <h3>支付成功</h3>
        <p>您已成功支付 ¥{{ payTarget?.totalFee ?? 0 }}</p>
        <button class="btn btn-primary" @click="closeAllModal">确定</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import request from '../api/request'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()
const fees = ref([])
const units = ref([])
const buildings = ref([])
const currentStep = ref('') // 'select', 'qrcode', 'success', ''
const payTarget = ref(null)
const payMethod = ref('')

const unpaidFees = computed(() => 
  fees.value.filter(f => f.payStatus !== 'PAID')
)

const qrCodeUrl = computed(() => {
  if (payMethod.value === 'WECHAT') {
    return new URL('../assets/wechat-qr.png', import.meta.url).href
  } else if (payMethod.value === 'ALIPAY') {
    return new URL('../assets/alipay-qr.png', import.meta.url).href
  }
  return ''
})

function unitLabel(roomId) {
  const u = units.value.find(x => x.id === roomId)
  if (!u) return `#${roomId}`
  const b = buildings.value.find(x => x.id === u.buildingId)
  return `${b?.buildingName || ''} ${u.unitNo}`.trim()
}

async function loadFees() {
  if (!userStore.user?.ownerId) return
  const owner = await request.get(`/owners/${userStore.user.ownerId}`)
  fees.value = await request.get(`/property-fees/owner/${userStore.user.ownerId}`, { 
    params: { roomId: owner?.roomId } 
  })
}

async function loadMeta() {
  buildings.value = await request.get('/buildings')
  units.value = await request.get('/units')
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

function closeAllModal() {
  currentStep.value = ''
  payTarget.value = null
  payMethod.value = ''
}

async function confirmPay() {
  try {
    await request.post(`/payments/pay/${payTarget.value.id}`, { 
      payMethod: payMethod.value,
      operator: userStore.user?.name || '业主'
    })

    // 如果是购房款，调用购房申请接口标记为已付款并完成购房
    if (payTarget.value.feeType === 'HOUSE_PURCHASE' && payTarget.value.housePurchaseId) {
      try {
        await request.post(`/house-purchases/${payTarget.value.housePurchaseId}/mark-paid`)
        await request.post(`/house-purchases/${payTarget.value.housePurchaseId}/complete`)
      } catch (e) {
        console.warn('更新购房申请状态失败', e)
      }
    }

    // 如果是车位购买款，调用车位申请接口标记为已付款
    if (payTarget.value.feeType === 'PARKING_PURCHASE' && payTarget.value.parkingPurchaseId) {
      try {
        await request.post(`/parking-application/${payTarget.value.parkingPurchaseId}/mark-paid`)
      } catch (e) {
        console.warn('更新车位购买申请状态失败', e)
      }
    }

    currentStep.value = 'success'
    await loadFees()
    await userStore.fetchUnpaidCount()
  } catch (e) {
    console.error('支付失败', e)
    alert('支付失败，请重试')
  }
}

onMounted(async () => {
  await loadMeta()
  await loadFees()
  await userStore.fetchUnpaidCount()
})
</script>

<style scoped>
.page-title { margin-bottom: 20px; font-size: 18px; font-weight: 600; }
.card-list { display: flex; flex-direction: column; gap: 16px; }
.card { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.fee-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.fee-billno { font-size: 13px; color: var(--color-text-secondary); }
.fee-type-tag { 
  background: linear-gradient(135deg, #047857, #059669);
  color: #fff; 
  font-size: 12px; 
  padding: 4px 12px; 
  border-radius: 20px;
  font-weight: 500;
}
.fee-date { font-size: 13px; font-weight: 500; color: var(--color-primary); }
.fee-room { font-size: 14px; margin-bottom: 16px; padding-bottom: 12px; border-bottom: 1px dashed #E2E8F0; }
.fee-details { display: flex; gap: 24px; margin-bottom: 12px; }
.fee-item { display: flex; justify-content: space-between; width: 100%; font-size: 13px; color: var(--color-text-secondary); }
.fee-item.house-purchase { padding: 12px; background: #F0FDF4; border-radius: 8px; }
.fee-item.house-purchase span:first-child { font-size: 14px; font-weight: 500; color: #059669; }
.amount-lg { font-size: 22px !important; font-weight: 700; color: #DC2626 !important; }
.fee-total { display: flex; justify-content: space-between; align-items: center; padding-top: 12px; border-top: 1px dashed #E2E8F0; margin-bottom: 16px; }
.fee-total span:first-child { font-size: 14px; }
.amount { font-size: 20px; font-weight: 600; color: var(--color-danger); }
.btn-pay { width: 100%; padding: 12px; }
.empty-state { text-align: center; padding: 40px; background: #F0FDF4; border-radius: 12px; }
.empty-icon { font-size: 48px; color: #10B981; margin-bottom: 12px; }
.empty-state p { color: var(--color-text-secondary); }
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal { background: #fff; border-radius: 16px; padding: 24px; width: 320px; }
.modal h3 { margin: 0 0 20px 0; font-size: 18px; }
.pay-info { margin-bottom: 20px; }
.pay-info div { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px dashed #E2E8F0; }
.pay-info .amount { color: var(--color-danger); }
.pay-method label { display: block; margin-bottom: 12px; font-size: 14px; }
.method-options { display: flex; gap: 12px; margin-bottom: 20px; }
.method-btn { flex: 1; display: flex; align-items: center; justify-content: center; gap: 8px; padding: 12px; border: 2px solid #E2E8F0; border-radius: 10px; background: transparent; cursor: pointer; transition: all var(--transition); }
.method-btn.active { border-color: var(--color-primary); background: rgba(4, 120, 87, 0.05); }
.modal-actions { display: flex; gap: 12px; }
.modal-actions .btn { flex: 1; }

.qr-modal { background: #fff; border-radius: 16px; padding: 0; width: 320px; overflow: hidden; }
.qr-header { display: flex; justify-content: space-between; align-items: center; padding: 16px 24px; border-bottom: 1px solid #E2E8F0; }
.qr-header h3 { margin: 0; font-size: 16px; }
.close-btn { width: 28px; height: 28px; border: none; background: #F1F5F9; border-radius: 50%; font-size: 20px; line-height: 24px; cursor: pointer; }
.qr-content { padding: 24px; }
.qr-content.wechat { background: linear-gradient(180deg, #07C160 0%, #10B981 100%); color: #fff; }
.qr-content.alipay { background: linear-gradient(180deg, #1677FF 0%, #3B82F6 100%); color: #fff; }
.qr-brand { display: flex; align-items: center; justify-content: center; gap: 8px; margin-bottom: 20px; }
.brand-icon { width: 32px; height: 32px; border-radius: 8px; display: flex; align-items: center; justify-content: center; font-size: 18px; font-weight: bold; }
.brand-icon.wechat { background: #fff; color: #07C160; }
.brand-icon.alipay { background: #fff; color: #1677FF; }
.brand-name { font-size: 18px; font-weight: 600; }
.qr-code-box { background: #fff; border-radius: 16px; padding: 20px; margin-bottom: 16px; position: relative; }
.qr-code-box img { width: 100%; max-width: 200px; display: block; margin: 0 auto; }
.qr-amount { position: absolute; bottom: 8px; left: 50%; transform: translateX(-50%); font-size: 16px; font-weight: 600; color: #333; }
.qr-tip { text-align: center; font-size: 14px; opacity: 0.9; margin: 0; }
.qr-actions { padding: 16px 24px; display: flex; gap: 12px; border-top: 1px solid #E2E8F0; }
.qr-actions .btn { flex: 1; }

.success-modal { background: #fff; border-radius: 16px; padding: 32px 24px; width: 280px; text-align: center; }
.success-icon { width: 80px; height: 80px; background: #10B981; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 48px; color: #fff; margin: 0 auto 16px; }
.success-modal h3 { margin: 0 0 8px 0; font-size: 18px; }
.success-modal p { margin: 0 0 24px 0; color: var(--color-text-secondary); }
</style>
