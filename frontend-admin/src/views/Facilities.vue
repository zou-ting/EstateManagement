<template>
  <div>
    <!-- 工具栏 -->
    <div class="toolbar">
      <h2 class="page-title">设备管理</h2>
      <div class="actions">
        <input v-model="keyword" class="input search" placeholder="搜索名称/编号..." @keyup.enter="load" />
        <select v-model="typeFilter" class="select" @change="load">
          <option :value="null">全部类型</option>
          <option value="电梯">电梯</option>
          <option value="消防">消防</option>
          <option value="供电">供电</option>
          <option value="供水">供水</option>
          <option value="门禁">门禁</option>
          <option value="监控">监控</option>
        </select>
        <select v-model="statusFilter" class="select" @change="load">
          <option :value="null">全部状态</option>
          <option :value="1">正常</option>
          <option :value="2">维修中</option>
          <option :value="0">停用</option>
        </select>
        <button class="btn btn-primary" @click="openForm()">+ 新增设备</button>
      </div>
    </div>

    <!-- 统计栏 -->
    <div class="stats-bar">
      <span>共 <strong>{{ filteredList.length }}</strong> 台设备</span>
      <span>🟢 正常 {{ statusStats.normal }} 台</span>
      <span>🟡 维修中 {{ statusStats.repairing }} 台</span>
      <span>⚪ 停用 {{ statusStats.disabled }} 台</span>
      <span v-if="expiringCount > 0" class="warning">⚠️ {{ expiringCount }} 台即将过保</span>
    </div>

    <div class="card">
      <table class="table">
        <thead>
        <tr>
          <th>序号</th>
          <th>设备编号</th>
          <th>设备名称</th>
          <th>类型</th>
          <th>所在楼栋</th>
          <th>位置</th>
          <th>状态</th>
          <th>安装日期</th>
          <th>保修截止</th>
          <th>操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="(item, index) in filteredList" :key="item.id">
          <td>{{ index + 1 }}</td>
          <td><code>{{ item.facilityNo }}</code></td>
          <td><strong>{{ item.facilityName }}</strong></td>
          <td><span class="tag tag-type">{{ item.facilityType || '-' }}</span></td>
          <td>{{ buildingLabel(item.buildingId) }}</td>
          <td>{{ item.location || '-' }}</td>
          <td><span :class="['tag', statusClass(item.status)]">{{ statusLabel(item.status) }}</span></td>
          <td>{{ item.installDate || '-' }}</td>
          <td :class="{ 'warning-text': isExpiring(item.warrantyEnd) }">{{ item.warrantyEnd || '-' }}</td>
          <td>
            <button class="btn btn-ghost" @click="openForm(item)">编辑</button>
            <button class="btn btn-danger btn-sm" @click="remove(item)">删除</button>
          </td>
        </tr>
        <tr v-if="!filteredList.length"><td colspan="10" class="empty">暂无数据</td></tr>
        </tbody>
      </table>
    </div>

    <!-- 弹窗表单 -->
    <div v-if="showModal" class="modal-mask" @click.self="showModal = false">
      <div class="modal">
        <h3>{{ form.id ? '✏️ 编辑设备' : '➕ 新增设备' }}</h3>
        <div class="form-row">
          <div class="form-group">
            <label>设备编号 <span class="required">*</span></label>
            <input v-model="form.facilityNo" class="input" placeholder="如 ELEV-CH1-01" :readonly="!!form.id" />
            <small class="hint" v-if="!form.id">编码不可修改，请谨慎填写</small>
          </div>
          <div class="form-group">
            <label>设备名称 <span class="required">*</span></label>
            <input v-model="form.facilityName" class="input" placeholder="如 1号楼1号电梯" />
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>设备类型</label>
            <select v-model="form.facilityType" class="select">
              <option value="">请选择</option>
              <option value="电梯">电梯</option>
              <option value="消防">消防</option>
              <option value="供电">供电</option>
              <option value="供水">供水</option>
              <option value="门禁">门禁</option>
              <option value="监控">监控</option>
            </select>
          </div>
          <div class="form-group">
            <label>所在楼栋 <span class="required">*</span></label>
            <select v-model.number="form.buildingId" class="select">
              <option v-for="b in buildings" :key="b.id" :value="b.id">{{ b.buildingName }}</option>
            </select>
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>具体位置</label>
            <input v-model="form.location" class="input" placeholder="如 1号楼东侧" />
          </div>
          <div class="form-group">
            <label>状态</label>
            <select v-model.number="form.status" class="select">
              <option :value="1">正常</option>
              <option :value="2">维修中</option>
              <option :value="0">停用</option>
            </select>
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>安装日期</label>
            <input v-model="form.installDate" type="date" class="input" />
          </div>
          <div class="form-group">
            <label>保修截止日期</label>
            <input v-model="form.warrantyEnd" type="date" class="input" />
          </div>
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
import { onMounted, reactive, ref, computed } from 'vue'
import request from '../api/request'
import { showConfirm } from '../composables/useConfirm'

const list = ref([])
const buildings = ref([])
const keyword = ref('')
const typeFilter = ref(null)
const statusFilter = ref(null)
const showModal = ref(false)
const formError = ref('')

const form = reactive({
  id: null,
  facilityNo: '',
  facilityName: '',
  facilityType: '',
  buildingId: null,
  location: '',
  status: 1,
  installDate: '',
  warrantyEnd: ''
})

// 筛选后的列表
const filteredList = computed(() => {
  let result = list.value
  if (keyword.value.trim()) {
    const kw = keyword.value.trim().toLowerCase()
    result = result.filter(item =>
        item.facilityName?.toLowerCase().includes(kw) ||
        item.facilityNo?.toLowerCase().includes(kw)
    )
  }
  if (typeFilter.value) {
    result = result.filter(item => item.facilityType === typeFilter.value)
  }
  if (statusFilter.value !== null && statusFilter.value !== undefined) {
    result = result.filter(item => item.status === statusFilter.value)
  }
  return result
})

// 状态统计
const statusStats = computed(() => {
  const stats = { normal: 0, repairing: 0, disabled: 0 }
  list.value.forEach(item => {
    if (item.status === 1) stats.normal++
    else if (item.status === 2) stats.repairing++
    else if (item.status === 0) stats.disabled++
  })
  return stats
})

// 即将过保（30天内）
const expiringCount = computed(() => {
  const now = new Date()
  const thirtyDaysLater = new Date()
  thirtyDaysLater.setDate(now.getDate() + 30)
  return list.value.filter(item => {
    if (!item.warrantyEnd) return false
    const end = new Date(item.warrantyEnd)
    return end >= now && end <= thirtyDaysLater
  }).length
})

function isExpiring(date) {
  if (!date) return false
  const end = new Date(date)
  const now = new Date()
  const thirtyDaysLater = new Date()
  thirtyDaysLater.setDate(now.getDate() + 30)
  return end >= now && end <= thirtyDaysLater
}

function statusLabel(s) {
  const map = { 1: '正常', 2: '维修中', 0: '停用' }
  return map[s] || s
}

function statusClass(s) {
  return s === 1 ? 'tag-normal' : s === 2 ? 'tag-repairing' : 'tag-disabled'
}

function buildingLabel(id) {
  return buildings.value.find(b => b.id === id)?.buildingName || `#${id}`
}

async function loadMeta() {
  buildings.value = await request.get('/buildings')
}

async function load() {
  list.value = await request.get('/facilities')
}

function openForm(item) {
  formError.value = ''
  Object.assign(form, {
    id: null,
    facilityNo: '',
    facilityName: '',
    facilityType: '',
    buildingId: buildings.value[0]?.id ?? null,
    location: '',
    status: 1,
    installDate: '',
    warrantyEnd: ''
  })
  if (item) Object.assign(form, item)
  showModal.value = true
}

async function save() {
  if (!form.facilityNo?.trim() || !form.facilityName?.trim() || !form.buildingId) {
    formError.value = '设备编号、名称和所在楼栋不能为空'
    return
  }
  try {
    if (form.id) {
      await request.put(`/facilities/${form.id}`, form)
    } else {
      await request.post('/facilities', form)
    }
    showModal.value = false
    load()
  } catch (e) {
    formError.value = e.message
  }
}

async function remove(item) {
  const ok = await showConfirm('删除确认', `确定删除设备「${item.facilityName}」吗？`)
  if (!ok) return
  await request.delete(`/facilities/${item.id}`)
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
  flex-wrap: wrap;
}
.stats-bar strong { color: #064E3B; }
.stats-bar .warning { color: #DC2626; font-weight: 500; }

.table .btn { margin-right: 4px; padding: 4px 10px; font-size: 13px; }
.btn-sm { padding: 4px 10px; font-size: 12px; }

.tag { padding: 2px 12px; border-radius: 12px; font-size: 12px; }
.tag-type { background: #DBEAFE; color: #1D4ED8; }
.tag-normal { background: #D1FAE5; color: #065F46; }
.tag-repairing { background: #FEF3C7; color: #B45309; }
.tag-disabled { background: #F3F4F6; color: #6B7280; }

.warning-text { color: #DC2626; font-weight: 500; }

.empty { text-align: center; color: var(--color-text-secondary); padding: 24px !important; }
.err { color: var(--color-danger); font-size: 13px; }
.required { color: #DC2626; }
.hint { font-size: 12px; color: #94A3B8; }

.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }

@media (max-width: 640px) {
  .form-row { grid-template-columns: 1fr; }
  .toolbar { flex-direction: column; align-items: stretch; }
  .toolbar .actions { flex-wrap: wrap; }
  .toolbar .search { width: 100%; }
}
</style>