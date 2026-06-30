<template>
  <div>
    <!-- 工具栏 -->
    <div class="toolbar">
      <h2 class="page-title">来访登记</h2>
      <div class="actions">
        <input v-model="keyword" class="input search" placeholder="搜索访客姓名..." @keyup.enter="load" />
        <select v-model="statusFilter" class="select" @change="load">
          <option value="">全部状态</option>
          <option value="PENDING">⏳ 待审核</option>
          <option value="APPROVED">☑️ 已通过</option>
          <option value="REJECTED">❌ 已拒绝</option>
          <option value="COMPLETED">✅ 已完成</option>
        </select>
        <button class="btn btn-primary" @click="openForm()">+ 新增来访</button>
      </div>
    </div>

    <!-- 统计栏 -->
    <div class="stats-bar">
      <span>共 <strong>{{ filteredList.length }}</strong> 条</span>
      <span>⏳ 待审核 <strong>{{ statusStats.pending }}</strong> 条</span>
      <span>☑️ 已通过 <strong>{{ statusStats.approved }}</strong> 条</span>
      <span>❌ 已拒绝 <strong>{{ statusStats.rejected }}</strong> 条</span>
      <span>✅ 已完成 <strong>{{ statusStats.completed }}</strong> 条</span>
    </div>

    <!-- 表格 -->
    <div class="card">
      <table class="table">
        <thead>
        <tr>
          <th>序号</th>
          <th>业主</th>
          <th>访客姓名</th>
          <th>电话</th>
          <th>来访日期</th>
          <th>事由</th>
          <th>状态</th>
          <th>操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="(item, index) in filteredList" :key="item.id">
          <td>{{ index + 1 }}</td>
          <td>{{ ownerMap[item.ownerId] || item.ownerId }}</td>
          <td><strong>{{ item.visitorName }}</strong></td>
          <td>{{ item.visitorPhone || '—' }}</td>
          <td>{{ item.visitDate || '—' }}</td>
          <td>{{ item.reason || '—' }}</td>
          <td><span :class="['tag', statusClass(item.status)]">{{ statusLabel(item.status) }}</span></td>
          <td>
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
        <h3>{{ form.id ? '✏️ 编辑来访' : '➕ 新增来访' }}</h3>
        <div class="form-group">
          <label>业主 <span class="required">*</span></label>
          <select v-model.number="form.ownerId" class="select">
            <option v-for="o in owners" :key="o.id" :value="o.id">{{ o.name }}（{{ o.ownerNo }}）</option>
          </select>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>访客姓名 <span class="required">*</span></label>
            <input v-model="form.visitorName" class="input" maxlength="50" placeholder="请输入访客姓名" />
          </div>
          <div class="form-group">
            <label>电话</label>
            <input v-model="form.visitorPhone" class="input" maxlength="11" pattern="[0-9]*" inputmode="numeric" placeholder="请输入11位手机号" />
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>来访日期</label>
            <input v-model="form.visitDate" type="date" class="input" />
          </div>
          <div class="form-group">
            <label>状态</label>
            <select v-model="form.status" class="select">
              <option value="PENDING">⏳ 待审核</option>
              <option value="APPROVED">☑️ 已通过</option>
              <option value="REJECTED">❌ 已拒绝</option>
              <option value="COMPLETED">✅ 已完成</option>
            </select>
          </div>
        </div>
        <div class="form-group">
          <label>来访事由</label>
          <input v-model="form.reason" class="input" maxlength="200" placeholder="请输入来访事由" />
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
const owners = ref([])
const keyword = ref('')
const statusFilter = ref('')
const showModal = ref(false)
const formError = ref('')

const form = reactive({
  id: null,
  ownerId: null,
  visitorName: '',
  visitorPhone: '',
  visitDate: '',
  reason: '',
  status: 'PENDING'
})

// 业主映射
const ownerMap = computed(() => {
  const map = {}
  owners.value.forEach(o => {
    map[o.id] = `${o.name}（${o.ownerNo}）`
  })
  return map
})

// 筛选列表
const filteredList = computed(() => {
  let result = list.value
  if (keyword.value.trim()) {
    const kw = keyword.value.trim().toLowerCase()
    result = result.filter(item =>
        item.visitorName?.toLowerCase().includes(kw)
    )
  }
  if (statusFilter.value) {
    result = result.filter(item => item.status === statusFilter.value)
  }
  return result
})

// 状态统计
const statusStats = computed(() => {
  const stats = { pending: 0, approved: 0, rejected: 0, completed: 0 }
  list.value.forEach(item => {
    if (item.status === 'PENDING') stats.pending++
    else if (item.status === 'APPROVED') stats.approved++
    else if (item.status === 'REJECTED') stats.rejected++
    else if (item.status === 'COMPLETED') stats.completed++
  })
  return stats
})

function statusLabel(s) {
  const map = { PENDING: '待审核', APPROVED: '已通过', REJECTED: '已拒绝',COMPLETED:'已完成' }
  return map[s] || s
}

function statusClass(s) {
  const map = { PENDING: 'tag-pending', APPROVED: 'tag-approved', REJECTED: 'tag-rejected',COMPLETED:'tag-completed' }
  return map[s] || ''
}

async function loadMeta() {
  owners.value = await request.get('/owners')
}

async function load() {
  list.value = await request.get('/visitors')
}

function openForm(item) {
  formError.value = ''
  Object.assign(form, {
    id: null,
    ownerId: owners.value[0]?.id || null,
    visitorName: '',
    visitorPhone: '',
    visitDate: '',
    reason: '',
    status: 'PENDING'
  })
  if (item) Object.assign(form, item)
  showModal.value = true
}

async function save() {
  if (!form.ownerId) { formError.value = '请选择业主'; return }
  if (!form.visitorName?.trim()) { formError.value = '请输入访客姓名'; return }
  if (form.visitorPhone && !/^1[3-9]\d{9}$/.test(form.visitorPhone)) { formError.value = '请输入正确的11位手机号'; return }
  formError.value = ''
  try {
    if (form.id) {
      await request.put(`/visitors/${form.id}`, form)
    } else {
      await request.post('/visitors', form)
    }
    showModal.value = false
    load()
  } catch (e) {
    formError.value = e.message
  }
}

async function remove(item) {
  const ok = await showConfirm('删除确认', `确定删除该来访记录吗？`)
  if (!ok) return
  await request.delete(`/visitors/${item.id}`)
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

.table .btn { margin-right: 8px; padding: 4px 10px; font-size: 13px; }
.empty { text-align: center; color: var(--color-text-secondary); padding: 24px !important; }
.err { color: var(--color-danger); font-size: 13px; }
.required { color: #DC2626; }

.tag-pending { background: #FEF3C7; color: #B45309; padding: 2px 12px; border-radius: 12px; font-size: 12px; }
.tag-completed { background: #D1FAE5; color: #065F46; padding: 2px 12px; border-radius: 12px; font-size: 12px; }
.tag-rejected { background: #FEE2E2; color: #991B1B; padding: 2px 12px; border-radius: 12px; font-size: 12px; }
.tag-approved { background: #F5F3FF; color: #4F46E5; padding: 2px 12px; border-radius: 12px; font-size: 12px; }

.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }

@media (max-width: 640px) {
  .form-row { grid-template-columns: 1fr; }
  .toolbar { flex-direction: column; align-items: stretch; }
  .toolbar .actions { flex-wrap: wrap; }
  .toolbar .search { width: 100%; }
}
</style>