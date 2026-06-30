<template>
  <div class="page-container">
    <h2 class="page-title">我的购房申请</h2>
    <p class="subtitle">查看您的购房申请进度</p>

    <div class="stats-bar">
      <div class="stat-item">
        <span class="stat-num">{{ stats.pending }}</span>
        <span class="stat-label">待审核</span>
      </div>
      <div class="stat-item">
        <span class="stat-num">{{ stats.approved }}</span>
        <span class="stat-label">待付款</span>
      </div>
      <div class="stat-item">
        <span class="stat-num">{{ stats.paid }}</span>
        <span class="stat-label">已付款</span>
      </div>
      <div class="stat-item">
        <span class="stat-num">{{ stats.completed }}</span>
        <span class="stat-label">已完成</span>
      </div>
    </div>

    <div class="card-list">
      <div v-if="!list.length" class="empty-state">
        <div class="empty-icon">🏠</div>
        <p>暂无购房申请</p>
        <p class="empty-hint">您可以在「小区房源」中浏览房屋并提交购房申请</p>
      </div>

      <div v-for="item in list" :key="item.id" class="application-card">
        <div class="card-header">
          <div class="house-info">
            <span class="building-name">{{ buildingMap[item.buildingId] }}</span>
            <span class="unit-no">{{ item.unitNo }}</span>
          </div>
          <span :class="['status-badge', statusClass(item.status)]">
            {{ statusLabel(item.status) }}
          </span>
        </div>

        <div class="card-body">
          <div class="info-row">
            <span class="label">{{ item.status === 'PENDING' ? '参考售价' : '购房金额' }}</span>
            <span class="value money">¥{{ formatMoney(item.purchaseAmount) }}</span>
          </div>
          <div class="info-row">
            <span class="label">申请日期</span>
            <span class="value">{{ formatDate(item.createdAt) }}</span>
          </div>
          <div v-if="item.approvalDate" class="info-row">
            <span class="label">审核日期</span>
            <span class="value">{{ formatDate(item.approvalDate) }}</span>
          </div>
          <div v-if="item.approvalRemark" class="info-row">
            <span class="label">审核备注</span>
            <span class="value">{{ item.approvalRemark }}</span>
          </div>
        </div>

        <div class="card-footer">
          <div v-if="item.status === 'PENDING'" class="action-hint">
            <span>您的申请正在审核中，请耐心等待...</span>
            <button class="btn-cancel" @click="cancelApplication(item)">撤销申请</button>
          </div>
          <div v-else-if="item.status === 'APPROVED'" class="action-hint pending-pay">
            <span>请前往「在线缴费」完成付款</span>
            <div class="action-buttons">
              <button class="btn-pay-now" @click="goToPay">立即付款</button>
              <button class="btn-cancel" @click="cancelApplication(item)">撤销申请</button>
            </div>
          </div>
          <div v-else-if="item.status === 'PAID'" class="action-hint">
            付款成功，等待管理员确认完成
          </div>
          <div v-else-if="item.status === 'COMPLETED'" class="action-hint success">
            🎉 购房成功！房屋已关联到您的名下
          </div>
          <div v-else-if="item.status === 'REJECTED'" class="action-hint error">
            申请被拒绝：{{ item.approvalRemark }}
          </div>
          <div v-else-if="item.status === 'CANCELLED'" class="action-hint error">
            申请已撤销，可重新申请
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '../api/request'
import { useUserStore } from '../stores/user'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()
const list = ref([])
const buildings = ref([])

const buildingMap = computed(() => {
  const map = {}
  buildings.value.forEach(b => { map[b.id] = b.buildingName })
  return map
})

const stats = computed(() => ({
  pending: list.value.filter(i => i.status === 'PENDING').length,
  approved: list.value.filter(i => i.status === 'APPROVED').length,
  paid: list.value.filter(i => i.status === 'PAID').length,
  completed: list.value.filter(i => i.status === 'COMPLETED').length
}))

function statusClass(status) {
  const classes = {
    PENDING: 'status-warning',
    APPROVED: 'status-info',
    PAID: 'status-success',
    COMPLETED: 'status-primary',
    REJECTED: 'status-danger',
    CANCELLED: 'status-default'
  }
  return classes[status] || 'status-default'
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
    list.value = await request.get(`/house-purchases/owner/${userStore.user.ownerId}`)
  } catch (e) {
    console.error('加载数据失败', e)
  }
}

async function loadBuildings() {
  try {
    buildings.value = await request.get('/buildings')
  } catch (e) {
    console.error('加载楼栋数据失败', e)
  }
}

function goToPay() {
  router.push('/pay-online')
}

async function cancelApplication(item) {
  if (!confirm('确定要撤销此购房申请吗？撤销后需重新申请。')) {
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

onMounted(async () => {
  await loadBuildings()
  await load()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.subtitle {
  font-size: 13px;
  color: #6B7280;
  margin: 0 0 20px 0;
}

.stats-bar {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.stat-item {
  flex: 1;
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.stat-num {
  display: block;
  font-size: 24px;
  font-weight: 700;
  color: #047857;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: #6B7280;
}

.card-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.empty-state p {
  margin: 0 0 8px;
  color: #374151;
  font-size: 15px;
}

.empty-hint {
  font-size: 13px !important;
  color: #9CA3AF !important;
}

.application-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: linear-gradient(135deg, #047857, #059669);
  color: #fff;
}

.house-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.building-name {
  font-size: 15px;
  font-weight: 500;
}

.unit-no {
  font-size: 18px;
  font-weight: 700;
}

.status-badge {
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.status-warning {
  background: rgba(255, 255, 255, 0.2);
}

.status-info {
  background: rgba(255, 255, 255, 0.3);
}

.status-success {
  background: rgba(255, 255, 255, 0.3);
}

.status-primary {
  background: rgba(255, 255, 255, 0.3);
}

.status-danger {
  background: rgba(239, 68, 68, 0.3);
}

.status-default {
  background: rgba(255, 255, 255, 0.2);
}

.card-body {
  padding: 16px 20px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #F3F4F6;
}

.info-row:last-child {
  border-bottom: none;
}

.label {
  font-size: 13px;
  color: #6B7280;
}

.value {
  font-size: 13px;
  color: #374151;
}

.money {
  font-size: 16px !important;
  font-weight: 600;
  color: #DC2626 !important;
}

.card-footer {
  padding: 12px 20px;
  background: #F9FAFB;
  border-top: 1px solid #F3F4F6;
}

.action-hint {
  font-size: 13px;
  color: #6B7280;
  text-align: center;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
}

.action-hint.pending-pay {
  color: #D97706;
  font-weight: 500;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 12px;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.btn-pay-now {
  background: linear-gradient(135deg, #047857, #059669);
  color: #fff;
  border: none;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-pay-now:hover {
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(4, 120, 87, 0.3);
}

.btn-cancel {
  background: transparent;
  color: #DC2626;
  border: 1px solid #DC2626;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-cancel:hover {
  background: rgba(239, 68, 68, 0.1);
}

.action-hint.success {
  color: #059669;
  font-weight: 500;
}

.action-hint.error {
  color: #DC2626;
}
</style>
