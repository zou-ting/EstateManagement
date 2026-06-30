<template>
  <div>
    <h2 class="page-title">我的房屋</h2>

    <!-- 房屋切换 -->
    <div v-if="units.length > 0" class="unit-tabs">
      <div
        v-for="unit in units"
        :key="unit.id"
        :class="['unit-tab', { active: selectedUnit?.id === unit.id }]"
        @click="selectUnit(unit)"
      >
        <span class="unit-tab-no">{{ unit.unitNo }}</span>
        <span class="unit-tab-status" :class="getStatusClass(unit.status)">
          {{ getStatusText(unit.status) }}
        </span>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading && !selectedUnit" class="loading">加载中...</div>

    <!-- 无房屋提示 -->
    <div v-else-if="!units.length && !loading" class="empty-card">
      <div class="empty-icon">🏠</div>
      <p>暂无房屋信息</p>
      <p class="hint">请联系物业管理员绑定房屋</p>
    </div>

    <!-- 房屋详细信息 -->
    <div v-else-if="selectedUnit" class="property-detail">
      <div class="detail-header">
        <div class="detail-icon">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
            <polyline points="9 22 9 12 15 12 15 22"/>
          </svg>
        </div>
        <div class="detail-info">
          <div class="detail-title">{{ selectedUnit.unitNo }}</div>
          <div class="detail-sub">{{ buildingName || '加载楼栋...' }}</div>
        </div>
        <div class="detail-status" :class="getStatusClass(selectedUnit.status)">
          {{ getStatusText(selectedUnit.status) }}
        </div>
      </div>

      <div class="detail-grid">
        <div class="detail-item">
          <label>楼栋</label>
          <span>{{ buildingName || '—' }}</span>
        </div>
        <div class="detail-item">
          <label>房号</label>
          <span>{{ selectedUnit.unitNo || '—' }}</span>
        </div>
        <div class="detail-item">
          <label>楼层</label>
          <span>{{ selectedUnit.floor || '—' }}层</span>
        </div>
        <div class="detail-item">
          <label>面积</label>
          <span>{{ selectedUnit.areaSqm || '—' }}㎡</span>
        </div>
        <div class="detail-item">
          <label>户型</label>
          <span>{{ selectedUnit.unitType || '—' }}</span>
        </div>
        <div class="detail-item">
          <label>状态</label>
          <span :class="getStatusClass(selectedUnit.status)">{{ getStatusText(selectedUnit.status) }}</span>
        </div>
      </div>

      <div v-if="selectedUnit.description" class="detail-desc">
        <label>备注说明</label>
        <p>{{ selectedUnit.description }}</p>
      </div>

      <!-- 业主信息 -->
      <div v-if="owner" class="owner-info">
        <h3 class="section-title">业主信息</h3>
        <div class="owner-grid">
          <div class="owner-item">
            <label>业主编号</label>
            <span>{{ owner.ownerNo }}</span>
          </div>
          <div class="owner-item">
            <label>姓名</label>
            <span>{{ owner.name }}</span>
          </div>
          <div class="owner-item">
            <label>性别</label>
            <span>{{ owner.gender || '—' }}</span>
          </div>
          <div class="owner-item">
            <label>手机</label>
            <span>{{ owner.phone || '—' }}</span>
          </div>
          <div class="owner-item">
            <label>产权证号</label>
            <span>{{ owner.propertyCert || '—' }}</span>
          </div>
          <div class="owner-item">
            <label>登记日期</label>
            <span>{{ owner.registerDate || '—' }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../api/request'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()
const loading = ref(true)
const owner = ref(null)
const units = ref([])
const selectedUnit = ref(null)
const buildings = ref([])
const buildingName = ref('')

// 状态映射
const STATUS_MAP = {
  0: '装修中',
  1: '已入住',
  2: '空置'
}

function getStatusText(status) {
  return STATUS_MAP[status] || '未知'
}

function getStatusClass(status) {
  switch (status) {
    case 0: return 'status-decorating'
    case 1: return 'status-occupied'
    case 2: return 'status-vacant'
    default: return ''
  }
}

function getBuildingName(buildingId) {
  const b = buildings.value.find(x => x.id === buildingId)
  return b?.buildingName || `#${buildingId}`
}

async function loadBuildings() {
  try {
    buildings.value = await request.get('/buildings')
  } catch (e) {
    console.error('加载楼栋失败', e)
  }
}

async function loadUnits() {
  const ownerId = userStore.user?.ownerId
  if (!ownerId) return

  try {
    units.value = await request.get(`/units/owner/${ownerId}`)
    if (units.value.length > 0) {
      selectedUnit.value = units.value[0]
      buildingName.value = getBuildingName(selectedUnit.value.buildingId)
    }
  } catch (e) {
    console.error('加载房屋失败', e)
  }
}

async function loadOwner() {
  const ownerId = userStore.user?.ownerId
  if (!ownerId) return

  try {
    owner.value = await request.get(`/owners/${ownerId}`)
  } catch (e) {
    console.error('加载业主信息失败', e)
  }
}

function selectUnit(unit) {
  selectedUnit.value = unit
  buildingName.value = getBuildingName(unit.buildingId)
}

onMounted(async () => {
  loading.value = true
  await loadBuildings()
  await Promise.all([loadUnits(), loadOwner()])
  loading.value = false
})
</script>

<style scoped>
.page-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 20px;
  color: var(--color-text);
}

/* 房屋切换标签 */
.unit-tabs {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding-bottom: 4px;
  margin-bottom: 16px;
}

.unit-tab {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: #fff;
  border: 2px solid var(--color-border);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
  min-width: 120px;
}

.unit-tab:hover {
  border-color: rgba(5, 150, 105, 0.3);
}

.unit-tab.active {
  border-color: var(--color-primary);
  background: rgba(5, 150, 105, 0.05);
}

.unit-tab-no {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
}

.unit-tab-status {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  background: rgba(0, 0, 0, 0.05);
}

.unit-tab-status.status-occupied {
  background: rgba(5, 150, 105, 0.15);
  color: #059669;
}

.unit-tab-status.status-vacant {
  background: rgba(251, 191, 36, 0.15);
  color: #d97706;
}

.unit-tab-status.status-decorating {
  background: rgba(139, 92, 246, 0.15);
  color: #7c3aed;
}

/* 加载和空状态 */
.loading {
  text-align: center;
  padding: 40px;
  color: var(--color-text-secondary);
}

.empty-card {
  text-align: center;
  padding: 48px 24px;
  background: #fff;
  border-radius: var(--radius);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-card p {
  margin: 0 0 8px 0;
  color: var(--color-text-secondary);
}

.empty-card p.hint {
  font-size: 13px;
}

/* 房屋详细信息 */
.property-detail {
  background: #fff;
  border-radius: var(--radius);
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--color-border);
  margin-bottom: 20px;
}

.detail-icon {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #ECFDF5, #D1FAE5);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-primary);
}

.detail-info {
  flex: 1;
}

.detail-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: 4px;
}

.detail-sub {
  font-size: 13px;
  color: var(--color-text-secondary);
}

.detail-status {
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.detail-status.status-occupied {
  background: rgba(5, 150, 105, 0.15);
  color: #059669;
}

.detail-status.status-vacant {
  background: rgba(251, 191, 36, 0.15);
  color: #d97706;
}

.detail-status.status-decorating {
  background: rgba(139, 92, 246, 0.15);
  color: #7c3aed;
}

/* 详细信息网格 */
.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-item label {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.detail-item span {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text);
}

.detail-item span.status-occupied {
  color: #059669;
}

.detail-item span.status-vacant {
  color: #d97706;
}

.detail-item span.status-decorating {
  color: #7c3aed;
}

/* 备注说明 */
.detail-desc {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--color-border);
}

.detail-desc label {
  font-size: 12px;
  color: var(--color-text-secondary);
  margin-bottom: 8px;
  display: block;
}

.detail-desc p {
  font-size: 14px;
  color: var(--color-text);
  line-height: 1.6;
  margin: 0;
}

/* 业主信息 */
.owner-info {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid var(--color-border);
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 16px;
}

.owner-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.owner-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.owner-item label {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.owner-item span {
  font-size: 14px;
  color: var(--color-text);
}

@media (max-width: 640px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }

  .owner-grid {
    grid-template-columns: 1fr;
  }

  .unit-tabs {
    flex-direction: column;
  }

  .unit-tab {
    min-width: auto;
  }
}
</style>