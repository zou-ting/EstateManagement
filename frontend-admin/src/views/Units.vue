<template>
  <div>
    <!-- 工具栏 -->
    <div class="toolbar">
      <h2 class="page-title">房屋管理</h2>
      <div class="actions">
        <input v-model="keyword" class="input search" placeholder="搜索房号..." @keyup.enter="load" />
        <select v-model="buildingFilter" class="select" @change="load">
          <option :value="null">全部楼栋</option>
          <option v-for="b in buildings" :key="b.id" :value="b.id">{{ b.buildingName }}</option>
        </select>
        <select v-model="statusFilter" class="select" @change="load">
          <option :value="null">全部状态</option>
          <option :value="1">已入住</option>
          <option :value="2">空置</option>
          <option :value="0">装修中</option>
        </select>
        <button class="btn btn-primary" @click="openForm()">+ 新增房屋</button>
      </div>
    </div>

    <!-- 统计栏 -->
    <div class="stats-bar">
      <span>共 <strong>{{ filteredList.length }}</strong> 套</span>
      <span v-if="statusStats.occupied">🏠 已入住 {{ statusStats.occupied }} 套</span>
      <span v-if="statusStats.vacant">📭 空置 {{ statusStats.vacant }} 套</span>
      <span v-if="statusStats.decorating">🔧 装修中 {{ statusStats.decorating }} 套</span>
    </div>

    <!-- 表格 -->
    <div class="card">
      <table class="table">
        <thead>
        <tr>
          <th>序号</th>
          <th>房号</th>
          <th>楼栋</th>
          <th>楼层</th>
          <th>户型</th>
          <th>面积</th>
          <th>业主数</th>
          <th>状态</th>
          <th>操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="(item, index) in filteredList" :key="item.id">
          <td>{{ index + 1 }}</td>
          <td><strong>{{ item.unitNo }}</strong></td>
          <td>{{ buildingMap[item.buildingId] || '—' }}</td>
          <td>{{ item.floor ?? '—' }}</td>
          <td>{{ item.unitType || '—' }}</td>
          <td>{{ item.areaSqm ? item.areaSqm + '㎡' : '—' }}</td>
          <td>{{ item.ownerCount ?? 0 }}</td>
          <td><span :class="['tag', statusTagClass(item.status)]">{{ statusLabel(item.status) }}</span></td>
          <td>
            <button class="btn btn-ghost" @click="openForm(item)">编辑</button>
            <button class="btn btn-danger" @click="remove(item)">删除</button>
          </td>
        </tr>
        <tr v-if="!filteredList.length"><td colspan="9" class="empty">暂无数据</td></tr>
        </tbody>
      </table>
    </div>

    <!-- 弹窗表单 -->
    <div v-if="showModal" class="modal-mask" @click.self="showModal = false">
      <div class="modal">
        <h3>{{ form.id ? '✏️ 编辑房屋' : '➕ 新增房屋' }}</h3>
        <div class="form-group">
          <label>楼栋 <span class="required">*</span></label>
          <select v-model="form.buildingId" class="select">
            <option v-for="b in buildings" :key="b.id" :value="b.id">{{ b.buildingName }}</option>
          </select>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>房号 <span class="required">*</span></label>
            <input v-model="form.unitNo" class="input" maxlength="20" placeholder="如 1-101" />
          </div>
          <div class="form-group">
            <label>楼层</label>
            <input v-model.number="form.floor" type="number" min="1" class="input" />
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>户型</label>
            <select v-model="form.unitType" class="select">
              <option value="">请选择</option>
              <option>一室一厅</option>
              <option>两室一厅</option>
              <option>三室两厅</option>
              <option>复式</option>
              <option>商铺</option>
            </select>
          </div>
          <div class="form-group">
            <label>面积(㎡) <span class="required">*</span></label>
            <input v-model.number="form.areaSqm" type="number" min="1" step="0.01" class="input" />
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>登记业主数</label>
            <input v-model.number="form.ownerCount" type="number" min="0" class="input" />
          </div>
          <div class="form-group">
            <label>状态</label>
            <select v-model.number="form.status" class="select">
              <option :value="1">🏠 已入住</option>
              <option :value="2">📭 空置</option>
              <option :value="0">🔧 装修中</option>
            </select>
          </div>
        </div>
        <div class="form-group">
          <label>备注</label>
          <input v-model="form.description" class="input" maxlength="200" placeholder="备注信息" />
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
import { computed, onMounted, reactive, ref } from 'vue'
import request from '../api/request'
import { showConfirm } from '../composables/useConfirm'

const list = ref([])
const buildings = ref([])
const buildingFilter = ref(null)
const statusFilter = ref(null)
const keyword = ref('')
const showModal = ref(false)
const formError = ref('')

const form = reactive({
  id: null,
  buildingId: null,
  unitNo: '',
  floor: 1,
  unitType: '',
  areaSqm: 80,
  ownerCount: 0,
  status: 1,
  description: ''
})

const buildingMap = computed(() => Object.fromEntries(buildings.value.map(b => [b.id, b.buildingName])))

// 筛选后的列表
const filteredList = computed(() => {
  let result = list.value
  if (keyword.value.trim()) {
    const kw = keyword.value.trim().toLowerCase()
    result = result.filter(item => item.unitNo?.toLowerCase().includes(kw))
  }
  if (buildingFilter.value !== null && buildingFilter.value !== undefined) {
    result = result.filter(item => item.buildingId === buildingFilter.value)
  }
  if (statusFilter.value !== null && statusFilter.value !== undefined) {
    result = result.filter(item => item.status === statusFilter.value)
  }
  return result
})

// 状态统计
const statusStats = computed(() => {
  const stats = { occupied: 0, vacant: 0, decorating: 0 }
  list.value.forEach(item => {
    if (item.status === 1) stats.occupied++
    else if (item.status === 2) stats.vacant++
    else if (item.status === 0) stats.decorating++
  })
  return stats
})

function statusLabel(s) {
  return { 1: '已入住', 2: '空置', 0: '装修中' }[s] ?? '—'
}

function statusTagClass(s) {
  const map = { 1: 'tag-occupied', 2: 'tag-vacant', 0: 'tag-decorating' }
  return map[s] || ''
}

async function loadBuildings() {
  buildings.value = await request.get('/buildings')
}

async function load() {
  const params = {}
  if (buildingFilter.value !== null && buildingFilter.value !== undefined) {
    params.buildingId = buildingFilter.value
  }
  list.value = await request.get('/units', { params })
}

function openForm(item) {
  formError.value = ''
  Object.assign(form, {
    id: null,
    buildingId: buildings.value[0]?.id ?? null,
    unitNo: '',
    floor: 1,
    unitType: '',
    areaSqm: 80,
    ownerCount: 0,
    status: 1,
    description: ''
  })
  if (item) Object.assign(form, item)
  showModal.value = true
}

async function save() {
  if (!form.buildingId || !form.unitNo?.trim()) {
    formError.value = '楼栋和房号不能为空'
    return
  }
  if (!form.areaSqm || form.areaSqm <= 0) {
    formError.value = '面积必须大于0'
    return
  }
  formError.value = ''
  try {
    if (form.id) {
      await request.put(`/units/${form.id}`, form)
    } else {
      await request.post('/units', form)
    }
    showModal.value = false
    load()
  } catch (e) {
    formError.value = e.message
  }
}

async function remove(item) {
  const ok = await showConfirm('删除房屋', `确定删除「${item.unitNo}」吗？`)
  if (!ok) return
  try {
    await request.delete(`/units/${item.id}`)
    load()
  } catch (e) {
    formError.value = e.message
  }
}

onMounted(async () => {
  await loadBuildings()
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

.table .btn { margin-right: 8px; padding: 4px 10px; font-size: 13px; }
.empty { text-align: center; color: var(--color-text-secondary); padding: 24px !important; }
.err { color: var(--color-danger); font-size: 13px; }
.required { color: #DC2626; }

.tag-occupied { background: #D1FAE5; color: #065F46; padding: 2px 12px; border-radius: 12px; font-size: 12px; }
.tag-vacant { background: #FEF3C7; color: #B45309; padding: 2px 12px; border-radius: 12px; font-size: 12px; }
.tag-decorating { background: #FEE2E2; color: #991B1B; padding: 2px 12px; border-radius: 12px; font-size: 12px; }

.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }

@media (max-width: 640px) {
  .form-row { grid-template-columns: 1fr; }
  .toolbar { flex-direction: column; align-items: stretch; }
  .toolbar .actions { flex-wrap: wrap; }
  .toolbar .search { width: 100%; }
}
</style>