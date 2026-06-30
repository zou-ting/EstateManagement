<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">小区房源</h2>
      <p class="page-desc">浏览小区所有楼栋和房屋信息，选择心仪的房源</p>
    </div>

    <div class="filter-bar">
      <div class="legend">
        <span class="legend-label">状态说明</span>
        <div class="legend-item">
          <span class="legend-dot available"></span>
          <span>空置（可购买）</span>
        </div>
      </div>
      <div class="filter-divider"></div>
      <div class="filter-item">
        <label>面积范围</label>
        <div class="range-input">
          <input v-model.number="areaMin" type="number" placeholder="最小㎡" class="input" />
          <span>-</span>
          <input v-model.number="areaMax" type="number" placeholder="最大㎡" class="input" />
        </div>
      </div>
      <div class="filter-item">
        <label>户型</label>
        <select v-model="unitTypeFilter" class="select" @change="filterUnits">
          <option value="">全部户型</option>
          <option value="一室">一室</option>
          <option value="两室">两室</option>
          <option value="三室">三室</option>
          <option value="四室">四室</option>
          <option value="四室以上">四室以上</option>
        </select>
      </div>
      
      <div class="filter-actions">
        <button class="btn btn-ghost" @click="resetFilter">重置</button>
        <button class="btn btn-primary" @click="filterUnits">筛选</button>
      </div>
    </div>

    <div class="main-content">
      <div class="sidebar-left">
        <h3 class="sidebar-title">楼栋列表</h3>
        <div class="building-list">
          <div
            v-for="building in buildings"
            :key="building.id"
            :class="['building-item', { active: selectedBuilding?.id === building.id }]"
            @click="selectBuilding(building)"
          >
            <div class="building-icon">🏢</div>
            <div class="building-info">
              <div class="building-name">{{ building.buildingName }}</div>
              <div class="building-detail">{{ building.floors }}层 · {{ building.buildingType }}</div>
            </div>
            <div class="building-stats">
              <span class="stat-badge available">{{ getBuildingStats(building.id).available }}套可售</span>
            </div>
          </div>
        </div>
      </div>

      <div class="center-panel">
        <div v-if="selectedBuilding" class="floor-container">
          <h3 class="panel-title">{{ selectedBuilding.buildingName }} - 楼层分布</h3>
          <div class="floor-list">
            <div
              v-for="floor in floorList"
              :key="floor"
              :class="['floor-item', { active: selectedFloor === floor }]"
              @click="selectFloor(floor)"
            >
              <span class="floor-number">{{ floor }}层</span>
              <div class="floor-units">
                <div
                  v-for="unit in getFloorUnits(floor)"
                  :key="unit.id"
                  :class="['unit-thumb', statusClass(unit.status)]"
                  :title="unit.unitNo"
                  @click.stop="selectUnit(unit)"
                >
                  {{ unit.unitNo.slice(-2) }}
                </div>
                <div v-if="!getFloorUnits(floor).length" class="floor-empty">暂无房屋</div>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="empty-center">
          <div class="empty-icon">🏠</div>
          <p>请选择左侧楼栋查看楼层分布</p>
        </div>
      </div>

      <div class="detail-panel">
        <div v-if="selectedUnit" class="unit-detail">
          <div class="detail-header">
            <h3 class="detail-title">{{ selectedUnit.unitNo }}</h3>
            <span class="status-tag available">可购买</span>
          </div>
          
          <div class="detail-info">
            <div class="info-row">
              <span class="info-label">所属楼栋</span>
              <span class="info-value">{{ getBuildingName(selectedUnit.buildingId) }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">楼层</span>
              <span class="info-value">{{ selectedUnit.floor }}层（共{{ selectedBuilding?.floors || 6 }}层）</span>
            </div>
            <div class="info-row">
              <span class="info-label">面积</span>
              <span class="info-value">{{ selectedUnit.areaSqm }} ㎡</span>
            </div>
            <div class="info-row">
              <span class="info-label">户型</span>
              <span class="info-value">{{ selectedUnit.unitType || '普通住宅' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">朝向</span>
              <span class="info-value">{{ getOrientation(selectedUnit.unitNo) }}</span>
            </div>
            <div class="info-row" v-if="selectedUnit.description">
              <span class="info-label">描述</span>
              <span class="info-value">{{ selectedUnit.description }}</span>
            </div>
          </div>

          <div class="price-section">
            <div class="price-info">
              <span class="price-label">参考售价</span>
              <span class="price-value">¥{{ formatPrice(selectedUnit.areaSqm) }}</span>
            </div>
            <div class="price-info">
              <span class="price-label">单价</span>
              <span class="price-sub">¥{{ getUnitPrice() }} / ㎡</span>
            </div>
          </div>

          <button 
            class="btn btn-primary btn-buy" 
            @click="showBuyModal = true"
          >
            立即购买
          </button>
        </div>
        <div v-else class="empty-detail">
          <div class="empty-icon">🔍</div>
          <p>点击中间楼层中的房屋查看详情</p>
        </div>
      </div>
    </div>

    <div v-if="showBuyModal" class="modal-mask" @click.self="showBuyModal = false">
      <div class="modal">
        <h3>确认购买</h3>
        <div class="buy-info">
          <div>
            <span>楼栋</span>
            <span>{{ getBuildingName(selectedUnit?.buildingId) }}</span>
          </div>
          <div>
            <span>房号</span>
            <span>{{ selectedUnit?.unitNo }}</span>
          </div>
          <div>
            <span>面积</span>
            <span>{{ selectedUnit?.areaSqm }} ㎡</span>
          </div>
          <div>
            <span>售价</span>
            <span class="price-highlight">¥{{ formatPrice(selectedUnit?.areaSqm) }}</span>
          </div>
        </div>
        <p class="buy-tip">点击确认后将提交购房申请，等待管理员审核</p>
        <div class="modal-actions">
          <button class="btn btn-ghost" @click="showBuyModal = false">取消</button>
          <button class="btn btn-primary" @click="submitBuy">确认购买</button>
        </div>
      </div>
    </div>

    <div v-if="showSuccessModal" class="modal-mask" @click.self="showSuccessModal = false">
      <div class="success-modal">
        <div class="success-icon">✓</div>
        <h3>申请已提交</h3>
        <p>您的购房申请已提交，等待管理员审核</p>
        <button class="btn btn-primary" @click="showSuccessModal = false">确定</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '../api/request'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()

const buildings = ref([])
const units = ref([])
const selectedBuilding = ref(null)
const selectedFloor = ref(null)
const selectedUnit = ref(null)

const areaMin = ref('')
const areaMax = ref('')
const unitTypeFilter = ref('')
const statusFilter = ref('2')

const showBuyModal = ref(false)
const showSuccessModal = ref(false)

const floorList = computed(() => {
  if (!selectedBuilding.value) return []
  const floors = []
  for (let i = selectedBuilding.value.floors; i >= 1; i--) {
    floors.push(i)
  }
  return floors
})

const filteredUnits = computed(() => {
  let result = units.value
  if (selectedBuilding.value) {
    result = result.filter(u => u.buildingId === selectedBuilding.value.id)
  }
  if (areaMin.value) {
    result = result.filter(u => u.areaSqm >= areaMin.value)
  }
  if (areaMax.value) {
    result = result.filter(u => u.areaSqm <= areaMax.value)
  }
  if (unitTypeFilter.value) {
    result = result.filter(u => u.unitType?.includes(unitTypeFilter.value))
  }
  if (statusFilter.value !== '') {
    result = result.filter(u => u.status === parseInt(statusFilter.value))
  }
  return result
})

function getBuildingStats(buildingId) {
  const buildingUnits = units.value.filter(u => u.buildingId === buildingId && u.status === 2)
  return { available: buildingUnits.length }
}

function getFloorUnits(floor) {
  return filteredUnits.value.filter(u => u.floor === floor).sort((a, b) => a.unitNo.localeCompare(b.unitNo))
}

function getBuildingName(buildingId) {
  const building = buildings.value.find(b => b.id === buildingId)
  return building?.buildingName || '未知楼栋'
}

function getOrientation(unitNo) {
  const lastDigit = parseInt(unitNo.slice(-1))
  if (lastDigit === 1 || lastDigit === 2) return '朝南'
  if (lastDigit === 3 || lastDigit === 4) return '朝北'
  if (lastDigit === 5 || lastDigit === 6) return '朝东'
  if (lastDigit === 7 || lastDigit === 8) return '朝西'
  return '朝南'
}

function statusClass(status) {
  return {
    0: 'decorating',
    1: 'occupied',
    2: 'available'
  }[status] || 'unknown'
}

function statusLabel(status) {
  return {
    0: '装修中',
    1: '已入住',
    2: '空置'
  }[status] || '未知'
}

function formatPrice(areaSqm) {
  if (!areaSqm) return '0'
  const pricePerSqm = 15000
  const total = Math.round(Number(areaSqm) * pricePerSqm)
  return total.toLocaleString()
}

function getUnitPrice() {
  return '15,000'
}

function selectBuilding(building) {
  selectedBuilding.value = building
  selectedFloor.value = null
  selectedUnit.value = null
}

function selectFloor(floor) {
  selectedFloor.value = floor
}

function selectUnit(unit) {
  selectedUnit.value = unit
}

function filterUnits() {
  selectedUnit.value = null
}

function resetFilter() {
  areaMin.value = ''
  areaMax.value = ''
  unitTypeFilter.value = ''
  statusFilter.value = ''
  filterUnits()
}

async function submitBuy() {
  try {
    const purchaseAmount = selectedUnit.value.areaSqm * 15000
    await request.post('/house-purchases', {
      unitId: selectedUnit.value.id,
      buildingId: selectedUnit.value.buildingId,
      unitNo: selectedUnit.value.unitNo,
      ownerId: userStore.user.ownerId,
      purchaseAmount: purchaseAmount
    })
    showBuyModal.value = false
    showSuccessModal.value = true
    await loadData()
  } catch (e) {
    console.error('提交购房申请失败', e)
    alert('提交失败，请重试')
  }
}

async function loadData() {
  try {
    const [buildingsData, unitsData] = await Promise.all([
      request.get('/buildings'),
      request.get('/units')
    ])
    buildings.value = buildingsData.sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
    units.value = unitsData.filter(u => u.status === 2)
    if (buildings.value.length > 0) {
      selectedBuilding.value = buildings.value[0]
    }
  } catch (e) {
    console.error('加载数据失败', e)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 20px;
  font-weight: 700;
  margin: 0 0 8px 0;
  color: var(--color-text);
}

.page-desc {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin: 0;
}

.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  padding: 16px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  margin-bottom: 20px;
  align-items: center;
}

.legend {
  display: flex;
  align-items: center;
  gap: 16px;
}

.legend-label {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-secondary);
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--color-text-secondary);
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 4px;
}

.legend-dot.available {
  background: #D1FAE5;
}

.legend-dot.occupied {
  background: #DBEAFE;
}

.legend-dot.decorating {
  background: #FEF3C7;
}

.filter-divider {
  width: 1px;
  height: 32px;
  background: var(--color-border);
}

.filter-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.filter-item label {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-secondary);
}

.range-input {
  display: flex;
  align-items: center;
  gap: 8px;
}

.range-input .input {
  width: 80px;
  padding: 8px 12px;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  font-size: 13px;
}

.filter-item .select {
  padding: 8px 12px;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  font-size: 13px;
  min-width: 120px;
}

.filter-actions {
  display: flex;
  gap: 8px;
  align-items: flex-end;
}

.main-content {
  display: grid;
  grid-template-columns: 260px 320px 1fr;
  gap: 16px;
}

.sidebar-left {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  padding: 16px;
  max-height: calc(100vh - 200px);
  overflow-y: auto;
  scrollbar-width: none;
}

.sidebar-left::-webkit-scrollbar {
  display: none;
}

.sidebar-title {
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 12px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--color-border);
}

.building-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.building-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: all var(--transition);
  border: 2px solid transparent;
}

.building-item:hover {
  background: rgba(5, 150, 105, 0.05);
}

.building-item.active {
  background: rgba(5, 150, 105, 0.08);
  border-color: var(--color-primary);
}

.building-icon {
  font-size: 24px;
}

.building-info {
  flex: 1;
  min-width: 0;
}

.building-name {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 2px;
}

.building-detail {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.building-stats {
  display: flex;
  gap: 6px;
}

.stat-badge {
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 4px;
}

.stat-badge.available {
  background: rgba(16, 185, 129, 0.15);
  color: #065F46;
}

.stat-badge.occupied {
  background: rgba(59, 130, 246, 0.15);
  color: #1D4ED8;
}

.center-panel {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  padding: 16px;
  max-height: calc(100vh - 200px);
  overflow-y: auto;
  scrollbar-width: none;
}

.center-panel::-webkit-scrollbar {
  display: none;
}

.panel-title {
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 12px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--color-border);
}

.floor-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.floor-item {
  padding: 10px;
  border-radius: 10px;
  background: #F8FAFC;
  cursor: pointer;
  transition: all var(--transition);
}

.floor-item:hover {
  background: #F1F5F9;
}

.floor-item.active {
  background: rgba(5, 150, 105, 0.1);
  border: 1px solid rgba(5, 150, 105, 0.2);
}

.floor-number {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 8px;
  display: block;
}

.floor-units {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.unit-thumb {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition);
  border: 2px solid transparent;
}

.unit-thumb.available {
  background: #D1FAE5;
  color: #065F46;
}

.unit-thumb.available:hover {
  background: #A7F3D0;
  border-color: #10B981;
}

.unit-thumb.occupied {
  background: #DBEAFE;
  color: #1D4ED8;
}

.unit-thumb.decorating {
  background: #FEF3C7;
  color: #B45309;
}

.floor-empty {
  font-size: 12px;
  color: var(--color-text-secondary);
  width: 100%;
  text-align: center;
  padding: 8px;
}

.empty-center, .empty-detail {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
  color: var(--color-text-secondary);
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.empty-center p, .empty-detail p {
  font-size: 14px;
  margin: 0;
}

.detail-panel {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  padding: 20px;
}

.unit-detail {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--color-border);
}

.detail-title {
  font-size: 20px;
  font-weight: 700;
  margin: 0;
}

.status-tag {
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 20px;
  font-weight: 500;
}

.status-tag.available {
  background: #D1FAE5;
  color: #065F46;
}

.status-tag.occupied {
  background: #DBEAFE;
  color: #1D4ED8;
}

.status-tag.decorating {
  background: #FEF3C7;
  color: #B45309;
}

.detail-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  background: #F8FAFC;
  border-radius: 8px;
}

.info-label {
  font-size: 13px;
  color: var(--color-text-secondary);
}

.info-value {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text);
}

.price-section {
  background: linear-gradient(135deg, #ECFDF5 0%, #D1FAE5 100%);
  border-radius: 12px;
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price-info {
  display: flex;
  flex-direction: column;
}

.price-label {
  font-size: 12px;
  color: var(--color-text-secondary);
  margin-bottom: 4px;
}

.price-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-danger);
}

.price-sub {
  font-size: 14px;
  color: var(--color-primary);
  font-weight: 500;
}

.btn-buy {
  width: 100%;
  padding: 14px;
  font-size: 16px;
  font-weight: 600;
}

.not-available {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px;
  background: #F3F4F6;
  border-radius: 8px;
  color: var(--color-text-secondary);
  font-size: 14px;
}

.not-available-icon {
  font-size: 18px;
}

.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.modal {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  width: 360px;
}

.modal h3 {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 16px 0;
}

.buy-info {
  margin-bottom: 16px;
}

.buy-info div {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px dashed #E2E8F0;
  font-size: 14px;
}

.buy-info div span:first-child {
  color: var(--color-text-secondary);
}

.price-highlight {
  color: var(--color-danger);
  font-weight: 600;
  font-size: 18px;
}

.buy-tip {
  font-size: 13px;
  color: var(--color-text-secondary);
  margin: 0 0 16px 0;
  padding: 12px;
  background: #FEF3C7;
  border-radius: 8px;
}

.modal-actions {
  display: flex;
  gap: 12px;
}

.modal-actions .btn {
  flex: 1;
}

.success-modal {
  background: #fff;
  border-radius: 16px;
  padding: 32px 24px;
  width: 280px;
  text-align: center;
}

.success-icon {
  width: 80px;
  height: 80px;
  background: #10B981;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  color: #fff;
  margin: 0 auto 16px;
}

.success-modal h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
}

.success-modal p {
  margin: 0 0 24px 0;
  color: var(--color-text-secondary);
}

@media (max-width: 1024px) {
  .main-content {
    grid-template-columns: 220px 1fr;
  }
  .center-panel {
    grid-column: 2;
  }
  .detail-panel {
    grid-column: 1 / -1;
  }
}

@media (max-width: 640px) {
  .main-content {
    grid-template-columns: 1fr;
  }
  .center-panel {
    grid-column: 1;
  }
  .filter-bar {
    flex-direction: column;
  }
  .filter-item {
    width: 100%;
  }
  .filter-item .select,
  .range-input .input {
    width: 100%;
  }
}
</style>