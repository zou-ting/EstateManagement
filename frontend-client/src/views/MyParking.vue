<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">我的车位</h2>
       <div class="header-actions">
    <!-- 没有车位：显示「申请车位」 -->
    <button v-if="!hasActiveParking" class="btn btn-primary" @click="openApplyModal('RENT')">
      + 申请车位
    </button>
    <!-- 有车位：显示「新增车位」和「更换车位」两个按钮 -->
    <template v-else>
      <button class="btn btn-outline" @click="openApplyModal('ADD')">
        + 新增车位
      </button>
      <button class="btn btn-warning" @click="openApplyModal('CHANGE')">
        🔄 更换车位
      </button>
    </template>
  </div>
  </div>

    <!-- 当前车位信息 -->
    <div v-if="allParkings.length > 0" class="card parking-card">
      <!-- 车位切换标签 -->
      <div class="parking-tabs">
        <button 
          v-for="(parking, index) in allParkings" 
          :key="parking.id"
          :class="['tab-btn', { active: selectedParkingIndex === index }]"
          @click="selectedParkingIndex = index"
        >
          <span class="tab-no">{{ parking.spaceNo }}</span>
          <span :class="['tab-status', getParkingStatusClass(parking)]">
            {{ getParkingStatusText(parking) }}
          </span>
        </button>
      </div>
      
      <div class="parking-header">
        <div>
          <span class="parking-no">{{ currentParking.spaceNo }}</span>
          <span class="parking-status" :class="isCurrentExpired ? 'status-expired' : 'status-active'">
            {{ isCurrentExpired ? '已到期' : '使用中' }}
          </span>
        </div>
        <div class="actions">
          <button v-if="currentParking.type !== 'FIXED'" class="btn btn-outline" @click="openRenewModal">续租</button>
          <button v-if="currentParking.type !== 'FIXED'" class="btn btn-danger-outline" @click="openCancelModal">退租</button>
        </div>
      </div>
      <div class="parking-info">
        <div><span>所属楼栋</span><span>{{ buildingLabel(currentParking.buildingId) }}</span></div>
        <div><span>车牌号</span><span>{{ currentParking.plateNumber || '-' }}</span></div>
        <div><span>车辆类型</span><span>{{ currentParking.vehicleType || '-' }}</span></div>
        <div><span>车位类型</span><span>{{ spaceTypeLabel(currentParking.type) }}</span></div>
        <div v-if="currentParking.type !== 'FIXED'">
          <span>租赁期限</span><span>{{ durationLabel(currentParking.rentalDuration) }}</span>
        </div>
        <div v-if="currentParking.type !== 'FIXED' && currentParking.rentStartDate">
          <span>租赁起始</span><span>{{ currentParking.rentStartDate }}</span>
        </div>
        <div v-if="currentParking.type !== 'FIXED' && currentParking.rentEndDate">
          <span>租赁到期</span><span>{{ currentParking.rentEndDate }}</span>
        </div>
        <div>
          <span>{{ currentParking.type === 'FIXED' ? '购买金额' : '月租费' }}</span>
          <span>{{ currentParking.type === 'FIXED' ? '¥' + (currentParking.purchasePrice ?? 0) : '¥' + (currentParking.monthlyFee ?? 0) + '/月' }}</span>
        </div>
      </div>
    </div>
    <div v-else class="empty-card">
      <div class="empty-icon">🚗</div>
      <p>您暂未分配车位</p>
      <p class="hint">点击右上角「申请车位」按钮提交申请</p>
    </div>

    <!-- 申请记录 -->
    <div class="section">
      <h3 class="section-title">申请记录</h3>
      <div class="card">
        <table class="table">
          <thead>
            <tr>
              <th>车位编号</th>
              <th>类型</th>
              <th>金额</th>
              <th>状态</th>
              <th>申请时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in applications" :key="item.id">
              <td>{{ item.parkingSpace?.spaceNo || item.parkingSpaceId }}</td>
              <td>{{ appTypeLabel(item.applicationType) }}</td>
              <td>{{ item.purchaseAmount ? '¥' + item.purchaseAmount : '-' }}</td>
              <td><span :class="['tag', appStatusClass(item.status)]">{{ appStatusLabel(item.status) }}</span></td>
              <td>{{ formatDate(item.createdAt) }}</td>
              <td>
                <button v-if="item.status === 'PENDING'" class="btn btn-danger" @click="cancelApplication(item.id)">取消</button>
                <button v-else-if="item.status === 'APPROVED' && (item.applicationType === 'PURCHASE' || item.applicationType === 'ADD' || item.applicationType === 'RENT')" class="btn btn-success" @click="payApplication(item)">立即付款</button>
                <button v-else class="btn btn-ghost" @click="viewDetail(item)">查看</button>
              </td>
            </tr>
            <tr v-if="!applications.length"><td colspan="6" class="empty">暂无申请记录</td></tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- ========== 申请车位弹窗 ========== -->
<div v-if="showApplyModal" class="modal-mask" @click.self="showApplyModal = false">
  <div class="modal">
    <!-- 动态标题 -->
    <h3>{{ applyModalTitle }}</h3>
    
    <!-- 类型提示 -->
    <div class="apply-type-hint" :class="applyForm.applicationType === 'CHANGE' ? 'hint-warning' : 'hint-info'">
      <span v-if="applyForm.applicationType === 'RENT'">📌 首次申请车位，审批通过后将为您分配车位</span>
      <span v-if="applyForm.applicationType === 'ADD'">📌 新增车位，您将拥有多个车位</span>
      <span v-if="applyForm.applicationType === 'CHANGE'">⚠️ 更换车位后，您当前的车位将自动释放</span>
    </div>
    
    <div class="form-group">
      <label>申请类型 <span class="required">*</span></label>
      <select v-model="applyForm.applicationType" class="select">
        <option value="RENT">租赁申请</option>
        <option value="PURCHASE">购买申请</option>
        <option v-if="hasActiveParking" value="ADD">新增车位</option>
        <option v-if="hasActiveParking" value="CHANGE">更换车位</option>
      </select>
    </div>
    
    <div class="form-group">
      <label>车位类型 <span class="required">*</span></label>
      <select v-model="applyForm.spaceType" class="select" @change="onSpaceTypeChange">
        <option value="ALL">全部</option>
        <option value="FIXED">购买</option>
        <option value="MONTHLY">月租</option>
        <option value="TEMP">临时</option>
      </select>
    </div>
    
    <div class="form-group">
      <label>选择车位 <span class="required">*</span></label>
      <select v-model="applyForm.parkingSpaceId" class="select">
        <option v-for="s in filteredSpaces" :key="s.id" :value="s.id">
          {{ s.spaceNo }} - {{ buildingLabel(s.buildingId) }} - {{ getSpacePrice(s) }}
        </option>
      </select>
    </div>
    
    <div class="form-group">
      <label>申请方式 <span class="required">*</span></label>
      <select v-model="applyForm.rentalDuration" class="select">
        <option v-if="selectedSpaceType === 'FIXED'" value="PURCHASE">购买</option>
        <option v-if="selectedSpaceType === 'MONTHLY'" value="MONTHLY">月租</option>
        <option v-if="selectedSpaceType === 'MONTHLY'" value="QUARTERLY">季租</option>
        <option v-if="selectedSpaceType === 'MONTHLY'" value="YEARLY">年租</option>
        <option v-if="selectedSpaceType === 'TEMP'" value="DAILY">日租</option>
        <option v-if="selectedSpaceType === 'TEMP'" value="HOURLY">时租</option>
      </select>
    </div>
    
    <div class="form-group">
      <label>车牌号 <span class="required">*</span></label>
      <input v-model="applyForm.plateNumber" class="input" placeholder="如 湘A·88888" />
    </div>
    
    <div class="form-group">
      <label>车辆类型</label>
      <select v-model="applyForm.vehicleType" class="select">
        <option value="小型车">小型车</option>
        <option value="SUV">SUV</option>
        <option value="货车">货车</option>
        <option value="其他">其他</option>
      </select>
    </div>
    
    <div class="form-group">
      <label>备注</label>
      <input v-model="applyForm.reason" class="input" placeholder="选填" />
    </div>
    
    <p v-if="applyError" class="err">{{ applyError }}</p>
    <div class="modal-actions">
      <button class="btn btn-ghost" @click="showApplyModal = false">取消</button>
      <button class="btn btn-primary" @click="submitApplication" :disabled="submitting">提交申请</button>
    </div>
  </div>
</div>

    <!-- ========== 续租弹窗 ========== -->
    <div v-if="showRenewModal" class="modal-mask" @click.self="showRenewModal = false">
      <div class="modal">
        <h3>续租车位</h3>
        <div class="form-group">
          <label>车位</label>
          <input class="input" :value="myParking?.spaceNo" disabled />
        </div>
        <div class="form-group">
          <label>续租期限 <span class="required">*</span></label>
          <select v-model="renewForm.rentalDuration" class="select">
            <option value="MONTHLY">续租1个月</option>
            <option value="QUARTERLY">续租3个月</option>
            <option value="YEARLY">续租1年</option>
          </select>
        </div>
        <div class="form-group">
          <label>备注</label>
          <input v-model="renewForm.reason" class="input" placeholder="选填" />
        </div>
        <p v-if="renewError" class="err">{{ renewError }}</p>
        <div class="modal-actions">
          <button class="btn btn-ghost" @click="showRenewModal = false">取消</button>
          <button class="btn btn-primary" @click="submitRenew">提交续租</button>
        </div>
      </div>
    </div>

    <!-- ========== 退租弹窗 ========== -->
    <div v-if="showCancelModal" class="modal-mask" @click.self="showCancelModal = false">
      <div class="modal">
        <h3>退租车位</h3>
        <p class="warning">⚠️ 退租后车位将被释放，您将无法继续使用</p>
        <div class="form-group">
          <label>车位</label>
          <input class="input" :value="myParking?.spaceNo" disabled />
        </div>
        <div class="form-group">
          <label>退租原因</label>
          <input v-model="cancelForm.reason" class="input" placeholder="请说明退租原因" />
        </div>
        <p v-if="cancelError" class="err">{{ cancelError }}</p>
        <div class="modal-actions">
          <button class="btn btn-ghost" @click="showCancelModal = false">取消</button>
          <button class="btn btn-danger" @click="submitCancel">确认退租</button>
        </div>
      </div>
    </div>
  </div>

  <!-- ========== 申请详情弹窗 ========== -->
<div v-if="showDetailModal" class="modal-mask" @click.self="showDetailModal = false">
  <div class="modal detail-modal">
    <h3>📋 申请详情</h3>
    <div class="detail-content" v-if="detailItem">
      <div class="detail-row">
        <span class="detail-label">申请ID</span>
        <span class="detail-value">#{{ detailItem.id }}</span>
      </div>
      <div class="detail-row">
        <span class="detail-label">车位编号</span>
        <span class="detail-value">{{ detailItem.parkingSpace?.spaceNo || detailItem.parkingSpaceId }}</span>
      </div>
      <div class="detail-row">
        <span class="detail-label">申请类型</span>
        <span class="detail-value"><span :class="['tag', appTypeClass(detailItem.applicationType)]">{{ appTypeLabel(detailItem.applicationType) }}</span></span>
      </div>
      <div class="detail-row">
        <span class="detail-label">状态</span>
        <span class="detail-value"><span :class="['tag', appStatusClass(detailItem.status)]">{{ appStatusLabel(detailItem.status) }}</span></span>
      </div>
      <div class="detail-row" v-if="detailItem.plateNumber">
        <span class="detail-label">车牌号</span>
        <span class="detail-value">{{ detailItem.plateNumber }}</span>
      </div>
      <div class="detail-row" v-if="detailItem.vehicleType">
        <span class="detail-label">车辆类型</span>
        <span class="detail-value">{{ detailItem.vehicleType }}</span>
      </div>
      <div class="detail-row" v-if="detailItem.rentalDuration">
        <span class="detail-label">{{ detailItem.parkingSpace?.type === 'FIXED' ? '购买方式' : '租赁方式' }}</span>
        <span class="detail-value">{{ durationLabel(detailItem.rentalDuration) }}</span>
      </div>
      <div class="detail-row" v-if="detailItem.startDate">
        <span class="detail-label">租赁起始</span>
        <span class="detail-value">{{ detailItem.startDate }}</span>
      </div>
      <div class="detail-row" v-if="detailItem.endDate">
        <span class="detail-label">租赁到期</span>
        <span class="detail-value">{{ detailItem.endDate }}</span>
      </div>
      <div class="detail-row" v-if="detailItem.purchaseAmount">
        <span class="detail-label">购买金额</span>
        <span class="detail-value">¥{{ detailItem.purchaseAmount }}</span>
      </div>
      <div class="detail-row" v-if="detailItem.reason">
        <span class="detail-label">申请原因</span>
        <span class="detail-value">{{ detailItem.reason }}</span>
      </div>
      <div class="detail-row" v-if="detailItem.approvalRemark">
        <span class="detail-label">审批备注</span>
        <span class="detail-value">{{ detailItem.approvalRemark }}</span>
      </div>
      <div class="detail-row" v-if="detailItem.approvalDate">
        <span class="detail-label">审批时间</span>
        <span class="detail-value">{{ formatDate(detailItem.approvalDate) }}</span>
      </div>
      <div class="detail-row">
        <span class="detail-label">申请时间</span>
        <span class="detail-value">{{ formatDate(detailItem.createdAt) }}</span>
      </div>
    </div>
    <div class="modal-actions">
      <button class="btn btn-primary" @click="showDetailModal = false">关闭</button>
    </div>
  </div>
</div>

<!-- ========== 支付弹窗 ========== -->
<div v-if="payItem" class="modal-mask" @click.self="payItem = null">
  <div class="modal">
    <h3>💳 确认支付</h3>
    <div class="detail-content">
      <div class="detail-row">
        <span class="detail-label">车位编号</span>
        <span class="detail-value">{{ payItem.parkingSpace?.spaceNo || payItem.parkingSpaceId }}</span>
      </div>
      <div class="detail-row">
        <span class="detail-label">申请类型</span>
        <span class="detail-value"><span :class="['tag', appTypeClass(payItem.applicationType)]">{{ appTypeLabel(payItem.applicationType) }}</span></span>
      </div>
      <div class="detail-row">
        <span class="detail-label">待支付金额</span>
        <span class="detail-value" style="color: #DC2626; font-size: 20px; font-weight: 600;">¥{{ payItem.purchaseAmount || 0 }}</span>
      </div>
    </div>
    <p class="warning">请确认支付以上金额，点击确认支付后将跳转到缴费页面完成支付</p>
    <div class="modal-actions">
      <button class="btn btn-ghost" @click="payItem = null">取消</button>
      <button class="btn btn-success" @click="confirmPay" :disabled="paying">
        {{ paying ? '支付中...' : '确认支付' }}
      </button>
    </div>
  </div>
</div>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import request from '../api/request'
import { useUserStore } from '../stores/user'
import { showConfirm } from '../composables/useConfirm'

const router = useRouter()

const userStore = useUserStore()
const allParkings = ref([])
const buildings = ref([])
const applications = ref([])
const availableSpaces = ref([])
const selectedParkingIndex = ref(0)

// 弹窗控制
const showApplyModal = ref(false)
const showRenewModal = ref(false)
const showCancelModal = ref(false)
const submitting = ref(false)
const applyError = ref('')
const renewError = ref('')
const cancelError = ref('')
const showDetailModal = ref(false)
const detailItem = ref(null)
const payItem = ref(null)
const paying = ref(false)

// 表单数据
const applyForm = ref({
  parkingSpaceId: null,
  applicationType: 'RENT', // RENT / ADD / CHANGE
  spaceType: 'ALL', // ALL / FIXED / MONTHLY / TEMP
  rentalDuration: 'MONTHLY',
  plateNumber: '',
  vehicleType: '小型车',
  reason: ''
})

const renewForm = ref({
  rentalDuration: 'MONTHLY',
  reason: ''
})

const cancelForm = ref({
  reason: ''
})

// 计算属性
const hasActiveParking = computed(() => {
  return allParkings.value.length > 0
})

const currentParking = computed(() => {
  return allParkings.value[selectedParkingIndex.value] || null
})

const isCurrentExpired = computed(() => {
  if (!currentParking.value) return false
  if (currentParking.value.type === 'FIXED') return false
  if (!currentParking.value.rentEndDate) return false
  return new Date(currentParking.value.rentEndDate) <= new Date()
})

function getParkingStatusClass(parking) {
  if (parking.type === 'FIXED') return 'status-active'
  if (!parking.rentEndDate) return 'status-active'
  return new Date(parking.rentEndDate) <= new Date() ? 'status-expired' : 'status-active'
}

function getParkingStatusText(parking) {
  if (parking.type === 'FIXED') return '已购买'
  if (!parking.rentEndDate) return '使用中'
  return new Date(parking.rentEndDate) <= new Date() ? '已到期' : '使用中'
}

// 弹窗标题
const applyModalTitle = computed(() => {
  const map = {
    RENT: '➕ 申请车位',
    PURCHASE: '🏠 购买车位',
    ADD: '➕ 新增车位（第二辆车）',
    CHANGE: '🔄 更换车位'
  }
  return map[applyForm.value.applicationType] || '申请车位'
})

// 工具函数
function buildingLabel(buildingId) {
  const b = buildings.value.find(x => x.id === buildingId)
  return b?.buildingName || `#${buildingId}`
}

function durationLabel(val) {
  const map = { MONTHLY: '月租', QUARTERLY: '季租', YEARLY: '年租', PURCHASE: '购买', DAILY: '日租', HOURLY: '时租' }
  return map[val] || val
}

function spaceTypeLabel(val) {
  const map = { FIXED: '购买', MONTHLY: '月租', TEMP: '临时' }
  return map[val] || val
}

function appTypeLabel(val) {
  const map = { RENT: '租赁', RENEW: '续租', CANCEL: '退租', TRANSFER: '转让', ADD: '新增', CHANGE: '更换', PURCHASE: '购买' }
  return map[val] || val
}

function appStatusLabel(val) {
  const map = { PENDING: '待审批', APPROVED: '待付款', REJECTED: '已驳回', CANCELLED: '已取消', PAID: '已付款', DONE: '已完成' }
  return map[val] || val
}

function appStatusClass(val) {
  const map = { 
    PENDING: 'tag-pending', 
    APPROVED: 'tag-approved', 
    REJECTED: 'tag-rejected', 
    CANCELLED: 'tag-cancelled', 
    PAID: 'tag-paid',
    DONE: 'tag-done' 
  }
  return map[val] || ''
}

function appTypeClass(val) {
  const map = { 
    RENT: 'tag-rent', 
    RENEW: 'tag-renew', 
    CANCEL: 'tag-cancel', 
    TRANSFER: 'tag-transfer',
    ADD: 'tag-add',
    CHANGE: 'tag-change',
    PURCHASE: 'tag-purchase'
  }
  return map[val] || ''
}

function formatDate(dt) {
  if (!dt) return '-'
  return dt.replace('T', ' ').slice(0, 16)
}

// 车位类型相关
const selectedSpaceType = computed(() => {
  if (applyForm.value.spaceType === 'ALL' && applyForm.value.parkingSpaceId) {
    const space = availableSpaces.value.find(s => s.id === applyForm.value.parkingSpaceId)
    return space?.type || 'MONTHLY'
  }
  return applyForm.value.spaceType === 'ALL' ? 'MONTHLY' : applyForm.value.spaceType
})

const filteredSpaces = computed(() => {
  if (applyForm.value.spaceType === 'ALL') {
    return availableSpaces.value
  }
  return availableSpaces.value.filter(s => s.type === applyForm.value.spaceType)
})

function getSpacePrice(space) {
  if (space.type === 'FIXED') {
    return `¥${space.purchasePrice || 0}`
  }
  if (space.type === 'MONTHLY') {
    return `¥${space.monthlyFee || 0}/月`
  }
  return '—'
}

function onSpaceTypeChange() {
  const spaces = filteredSpaces.value
  if (spaces.length > 0) {
    applyForm.value.parkingSpaceId = spaces[0].id
  } else {
    applyForm.value.parkingSpaceId = null
  }
  // 根据类型重置申请方式
  if (applyForm.value.spaceType === 'FIXED') {
    applyForm.value.rentalDuration = 'PURCHASE'
  } else if (applyForm.value.spaceType === 'MONTHLY') {
    applyForm.value.rentalDuration = 'MONTHLY'
  } else if (applyForm.value.spaceType === 'TEMP') {
    applyForm.value.rentalDuration = 'DAILY'
  }
}

// 加载数据
async function loadMyParking() {
  if (!userStore.user?.ownerId) return
  allParkings.value = await request.get(`/parking/owner/${userStore.user.ownerId}`)
  selectedParkingIndex.value = 0
}

async function loadApplications() {
  if (!userStore.user?.ownerId) return
  applications.value = await request.get(`/parking-application/owner/${userStore.user.ownerId}`)
}

async function loadAvailableSpaces() {
  availableSpaces.value = await request.get('/parking-application/available')
}

async function loadMeta() {
  buildings.value = await request.get('/buildings')
}

// 申请车位 - 支持三种类型
function openApplyModal(type) {
  applyError.value = ''
  applyForm.value = { 
    parkingSpaceId: null, 
    applicationType: type || 'RENT',
    spaceType: 'ALL',
    rentalDuration: 'MONTHLY', 
    plateNumber: '', 
    vehicleType: '小型车', 
    reason: '' 
  }
  loadAvailableSpaces().then(() => {
    if (availableSpaces.value.length) {
      applyForm.value.parkingSpaceId = availableSpaces.value[0].id
      applyForm.value.spaceType = availableSpaces.value[0].type
      onSpaceTypeChange()
    }
    showApplyModal.value = true
  })
}

async function submitApplication() {
  if (!applyForm.value.parkingSpaceId) {
    applyError.value = '请选择车位'
    return
  }
  if (!applyForm.value.plateNumber.trim()) {
    applyError.value = '请输入车牌号'
    return
  }
  submitting.value = true
  try {
    await request.post('/parking-application', {
      parkingSpaceId: applyForm.value.parkingSpaceId,
      ownerId: userStore.user.ownerId,
      applicationType: applyForm.value.applicationType,
      plateNumber: applyForm.value.plateNumber,
      vehicleType: applyForm.value.vehicleType,
      rentalDuration: applyForm.value.rentalDuration,
      startDate: new Date().toISOString().slice(0, 10),
      reason: applyForm.value.reason
    })
    showApplyModal.value = false
    await loadApplications()
    await loadMyParking()
  } catch (e) {
    applyError.value = e.message
  } finally {
    submitting.value = false
  }
}

// 续租
function openRenewModal() {
  renewError.value = ''
  renewForm.value = { rentalDuration: 'MONTHLY', reason: '' }
  showRenewModal.value = true
}

async function submitRenew() {
  try {
    await request.post('/parking-application/renew', {
      parkingSpaceId: currentParking.value.id,
      ownerId: userStore.user.ownerId,
      rentalDuration: renewForm.value.rentalDuration,
      reason: renewForm.value.reason
    })
    showRenewModal.value = false
    await loadApplications()
    await loadMyParking()
  } catch (e) {
    renewError.value = e.message
  }
}

// 退租
function openCancelModal() {
  cancelError.value = ''
  cancelForm.value = { reason: '' }
  showCancelModal.value = true
}

async function submitCancel() {
  const ok = await showConfirm('退租确认', '确定要退租该车位吗？')
  if (!ok) return
  try {
    await request.post('/parking-application/cancel-rent', {
      parkingSpaceId: currentParking.value.id,
      ownerId: userStore.user.ownerId,
      reason: cancelForm.value.reason
    })
    showCancelModal.value = false
    await loadApplications()
    await loadMyParking()
  } catch (e) {
    cancelError.value = e.message
  }
}

// 取消申请
async function cancelApplication(id) {
  const ok = await showConfirm('取消申请', '确定要取消该申请吗？')
  if (!ok) return
  await request.put(`/parking-application/${id}/cancel`)
  await loadApplications()
}

function viewDetail(item) {
  detailItem.value = item
  showDetailModal.value = true
}

// 支付申请
function payApplication(item) {
  payItem.value = item
}

function confirmPay() {
  payItem.value = null
  router.push('/pay-online')
}

onMounted(async () => {
  await loadMeta()
  await loadMyParking()
  await loadApplications()
})
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-title { font-size: 18px; font-weight: 600; margin: 0; }

.card { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); margin-bottom: 16px; }

.parking-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; flex-wrap: wrap; gap: 12px; }
.parking-no { font-size: 20px; font-weight: 600; color: var(--color-primary); }
.parking-status { padding: 4px 12px; border-radius: 20px; font-size: 13px; margin-left: 12px; }
.status-active { background: rgba(5,150,105,0.12); color: #059669; }
.status-expired { background: #FEE2E2; color: #DC2626; }

.parking-info { display: grid; grid-template-columns: 1fr 1fr; gap: 8px 24px; }
.parking-info div { display: flex; justify-content: space-between; padding: 6px 0; border-bottom: 1px dashed #F1F5F9; }
.parking-info div:last-child { border-bottom: none; }
.parking-info span:first-child { color: #64748B; }

.empty-card { text-align: center; padding: 40px; background: #fff; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.empty-icon { font-size: 48px; margin-bottom: 12px; }
.empty-card p { margin: 0 0 8px 0; color: #64748B; }
.empty-card p.hint { font-size: 13px; }

.section { margin-top: 24px; }
.section-title { font-size: 16px; font-weight: 600; margin-bottom: 12px; }

.table { width: 100%; border-collapse: collapse; font-size: 14px; }
.table th { text-align: left; padding: 10px 12px; background: #F8FAFC; font-weight: 500; color: #475569; border-bottom: 1px solid #E2E8F0; }
.table td { padding: 10px 12px; border-bottom: 1px solid #F1F5F9; }
.table .empty { text-align: center; color: #94A3B8; }

.tag { padding: 2px 12px; border-radius: 12px; font-size: 12px; }
.tag-pending { background: #FEF3C7; color: #B45309; }
.tag-approved { background: #FEF3C7; color: #B45309; }
.tag-rejected { background: #FEE2E2; color: #991B1B; }
.tag-cancelled { background: #F3F4F6; color: #6B7280; }
.tag-paid { background: #D1FAE5; color: #065F46; }
.tag-done { background: #DBEAFE; color: #1E40AF; }

.warning { padding: 12px 16px; background: #FEF3C7; border-radius: 8px; color: #B45309; margin-bottom: 16px; }

.btn-outline { background: transparent; border: 1px solid var(--color-primary); color: var(--color-primary); }
.btn-danger-outline { background: transparent; border: 1px solid #DC2626; color: #DC2626; }
.btn-danger-outline:hover { background: #FEE2E2; }

.modal { max-width: 480px; }
/* 详情弹窗 */
.detail-modal {
  max-width: 520px;
}
.detail-content {
  margin: 16px 0;
}
.detail-row {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px dashed #F1F5F9;
}
.detail-row:last-child {
  border-bottom: none;
}
.detail-label {
  color: #64748B;
  font-size: 14px;
}
.detail-value {
  font-weight: 500;
  color: #1E293B;
  text-align: right;
  max-width: 60%;
  word-break: break-all;
}

/* 申请类型标签 */
.tag-rent { background: #DBEAFE; color: #1E40AF; }
.tag-renew { background: #D1FAE5; color: #065F46; }
.tag-cancel { background: #FEE2E2; color: #991B1B; }
.tag-transfer { background: #FEF3C7; color: #B45309; }
/* 头部按钮布局 */
.header-actions {
  display: flex;
  gap: 8px;
}
.header-actions .btn {
  white-space: nowrap;
}

/* 申请类型提示 */
.apply-type-hint {
  padding: 10px 14px;
  border-radius: 8px;
  margin-bottom: 16px;
  font-size: 13px;
}
.apply-type-hint.hint-info {
  background: #EFF6FF;
  color: #1D4ED8;
}
.apply-type-hint.hint-warning {
  background: #FEF3C7;
  color: #B45309;
}

/* 新增标签样式 */
.tag-add { background: #DBEAFE; color: #1E40AF; }
.tag-change { background: #FEF3C7; color: #B45309; }

/* 车位数量角标 */
.parking-count {
  font-size: 12px;
  background: #E2E8F0;
  color: #475569;
  padding: 2px 10px;
  border-radius: 12px;
  margin-left: 8px;
}

/* 车位切换标签 */
.parking-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #E2E8F0;
  overflow-x: auto;
}
.parking-tabs::-webkit-scrollbar {
  display: none;
}
.parking-tabs {
  scrollbar-width: none;
  -ms-overflow-style: none;
}
.tab-btn {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border: 1px solid #E2E8F0;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  transition: all 0.2s;
}
.tab-btn:hover {
  border-color: var(--color-primary);
}
.tab-btn.active {
  border-color: var(--color-primary);
  background: rgba(2, 132, 199, 0.08);
}
.tab-no {
  font-weight: 600;
  color: #1E293B;
}
.tab-status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
}
.tab-status.status-active {
  background: rgba(5, 150, 105, 0.12);
  color: #059669;
}
.tab-status.status-expired {
  background: #FEE2E2;
  color: #DC2626;
}

.btn-warning {
  background: #F59E0B;
  color: #fff;
  border: none;
}
.btn-warning:hover {
  background: #D97706;
}

.btn-success {
  background: #059669;
  color: #fff;
  border: none;
}
.btn-success:hover {
  background: #047857;
}
</style>