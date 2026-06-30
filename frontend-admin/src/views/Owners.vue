<template>
  <div>
    <!-- 工具栏 -->
    <div class="toolbar">
      <h2 class="page-title">业主管理</h2>
      <div class="actions">
        <input v-model="keyword" class="input search" placeholder="搜索姓名..." maxlength="50" @keyup.enter="load" />
        <select v-model="genderFilter" class="select" @change="load">
          <option :value="null">全部性别</option>
          <option value="男">男</option>
          <option value="女">女</option>
        </select>
        <select v-model="statusFilter" class="select" @change="load">
          <option :value="null">全部状态</option>
          <option :value="1">在册</option>
          <option :value="0">已迁出</option>
        </select>
        <button class="btn btn-primary" @click="openForm()">+ 新增业主</button>
      </div>
    </div>

    <!-- 统计栏 -->
    <div class="stats-bar">
      <span>共 <strong>{{ filteredList.length }}</strong> 位业主</span>
      <span>✅ 在册 {{ statusStats.active }} 位</span>
      <span>📤 已迁出 {{ statusStats.inactive }} 位</span>
    </div>

    <!-- 表格 -->
    <div class="card">
      <table class="table">
        <thead>
        <tr>
          <th>序号</th>
          <th>业主编号</th>
          <th>姓名</th>
          <th>性别</th>
          <th>手机</th>
          <th>绑定房屋</th>
          <th>产权证号</th>
          <th>状态</th>
          <th>操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="(item, index) in filteredList" :key="item.id">
          <td>{{ index + 1 }}</td>
          <td>{{ item.ownerNo }}</td>
          <td><strong>{{ item.name }}</strong></td>
          <td>{{ item.gender || '—' }}</td>
          <td>{{ item.phone || '—' }}</td>
          <td>{{ unitLabel(item.roomId) }}</td>
          <td>{{ item.propertyCert || '—' }}</td>
          <td>
            <!-- ⭐ 状态标签可点击切换：在册 ↔ 已迁出 -->
            <span 
              :class="['tag', statusTagClass(item.status), 'clickable']" 
              @click="toggleStatus(item)"
              :title="item.status === 1 ? '点击迁出' : '点击迁入'"
            >
              {{ item.status === 1 ? '✅ 在册' : '📤 已迁出' }}
            </span>
          </td>
          <td>
            <!-- ⭐ 方案三：在册显示 编辑 + 迁出；已迁出显示 编辑 + 删除 -->
            <button class="btn btn-ghost btn-sm" @click="openForm(item)">编辑</button>
            <button 
              v-if="item.status === 1" 
              class="btn btn-warning btn-sm" 
              @click="moveOut(item)"
            >迁出</button>
            <button 
              v-else 
              class="btn btn-danger btn-sm" 
              @click="remove(item)"
            >删除</button>
          </td>
        </tr>
        <tr v-if="!filteredList.length"><td colspan="9" class="empty">暂无数据</td></tr>
        </tbody>
      </table>
    </div>

    <!-- 弹窗表单 -->
    <div v-if="showModal" class="modal-mask" @click.self="showModal = false">
      <div class="modal modal-lg">
        <h3>{{ form.id ? '✏️ 编辑业主' : '➕ 新增业主' }}</h3>
        <div class="form-row">
          <div class="form-group"><label>业主编号 <span class="required">*</span></label><input v-model="form.ownerNo" class="input" maxlength="20" :readonly="!!form.id" /></div>
          <div class="form-group"><label>姓名 <span class="required">*</span></label><input v-model="form.name" class="input" maxlength="50" /></div>
          <div class="form-group">
            <label>性别</label>
            <select v-model="form.gender" class="select"><option>男</option><option>女</option></select>
          </div>
          <div class="form-group"><label>手机</label><input v-model="form.phone" class="input" maxlength="11" pattern="[0-9]*" inputmode="numeric" placeholder="请输入11位手机号" /></div>
          <div class="form-group"><label>所属单元</label><input v-model="form.unitSection" class="input" maxlength="30" placeholder="如 1单元" /></div>
          <div class="form-group"><label>产权证号</label><input v-model="form.propertyCert" class="input" maxlength="30" /></div>
          <div class="form-group">
            <label>绑定房屋</label>
            <select v-model="form.roomId" class="select">
              <option :value="null">未绑定</option>
              <option v-for="u in units" :key="u.id" :value="u.id">{{ unitOptionLabel(u) }}</option>
            </select>
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
const units = ref([])
const buildings = ref([])
const keyword = ref('')
const genderFilter = ref(null)
const statusFilter = ref(null)
const showModal = ref(false)
const formError = ref('')

const form = reactive({
  id: null,
  ownerNo: '',
  name: '',
  gender: '男',
  phone: '',
  unitSection: '',
  propertyCert: '',
  roomId: null,
  buildingId: null,
  status: 1
})

// 筛选后的列表
const filteredList = computed(() => {
  let result = list.value
  if (keyword.value.trim()) {
    const kw = keyword.value.trim().toLowerCase()
    result = result.filter(item => item.name?.toLowerCase().includes(kw))
  }
  if (genderFilter.value !== null && genderFilter.value !== undefined) {
    result = result.filter(item => item.gender === genderFilter.value)
  }
  if (statusFilter.value !== null && statusFilter.value !== undefined) {
    result = result.filter(item => item.status === statusFilter.value)
  }
  return result
})

// 状态统计
const statusStats = computed(() => {
  const stats = { active: 0, inactive: 0 }
  list.value.forEach(item => {
    if (item.status === 1) stats.active++
    else if (item.status === 0) stats.inactive++
  })
  return stats
})

function statusTagClass(status) {
  return status === 1 ? 'tag-active' : 'tag-inactive'
}

function unitOptionLabel(u) {
  const b = buildings.value.find(x => x.id === u.buildingId)
  return `${b?.buildingName || ''} ${u.unitNo}`.trim()
}

function unitLabel(roomId) {
  if (!roomId) return '—'
  const u = units.value.find(x => x.id === roomId)
  return u ? unitOptionLabel(u) : `#${roomId}`
}

async function loadMeta() {
  buildings.value = await request.get('/buildings')
  units.value = await request.get('/units')
}

async function load() {
  list.value = await request.get('/owners', { params: { keyword: keyword.value || undefined } })
}

function openForm(item) {
  formError.value = ''
  Object.assign(form, {
    id: null,
    ownerNo: '',
    name: '',
    gender: '男',
    phone: '',
    unitSection: '',
    propertyCert: '',
    roomId: null,
    buildingId: null,
    status: 1
  })
  if (item) Object.assign(form, item)
  showModal.value = true
}

async function save() {
  if (!form.ownerNo?.trim() || !form.name?.trim()) {
    formError.value = '业主编号和姓名不能为空'
    return
  }
  if (form.phone && !/^1[3-9]\d{9}$/.test(form.phone)) {
    formError.value = '请输入正确的11位手机号'
    return
  }
  const unit = units.value.find(u => u.id === form.roomId)
  if (unit) form.buildingId = unit.buildingId
  try {
    if (form.id) {
      await request.put(`/owners/${form.id}`, form)
    } else {
      await request.post('/owners', form)
    }
    showModal.value = false
    load()
  } catch (e) {
    formError.value = e.message
  }
}

// ⭐ 切换业主状态（在册 ↔ 已迁出）—— 点击状态标签触发
async function toggleStatus(item) {
  const newStatus = item.status === 1 ? 0 : 1
  const action = newStatus === 0 ? '迁出' : '迁入'
  const confirmText = newStatus === 0 ? '确认迁出' : '确认迁入'
  const msg = newStatus === 0 
    ? `确定将「${item.name}」迁出吗？\n迁出后该业主将变为"已迁出"状态，历史记录保留。`
    : `确定将「${item.name}」迁入吗？\n迁入后该业主将恢复为"在册"状态。`
  
  const ok = await showConfirm(`${action}业主`, msg,confirmText)
  if (!ok) return
  
  try {
    // 只传 status 字段，不改接口
    await request.put(`/owners/${item.id}`, { status: newStatus })
    await load()
    formError.value = ''
  } catch (e) {
    formError.value = e.message || `${action}失败`
  }
}

// ⭐ 迁出方法（按钮触发，与 toggleStatus 逻辑一致，保留作为备用）
async function moveOut(item) {
  if (item.status === 0) {
    formError.value = '该业主已迁出'
    return
  }
  const ok = await showConfirm('业主迁出', `确定将「${item.name}」迁出吗？\n迁出后该业主将变为"已迁出"状态，历史记录保留。`, '确认迁出' )
  if (!ok) return
  try {
    await request.put(`/owners/${item.id}`, { status: 0 })
    await load()
    formError.value = ''
  } catch (e) {
    formError.value = e.message || '迁出失败'
  }
}

// 删除（物理删除，仅用于已迁出的垃圾数据清理）
async function remove(item) {
  const ok = await showConfirm('删除业主', `确定删除「${item.name}」吗？\n此操作不可恢复！`,'确认删除')
  if (!ok) return
  try {
    await request.delete(`/owners/${item.id}`)
    await load()
  } catch (e) {
    formError.value = e.message
  }
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

.table .btn { margin-right: 4px; padding: 4px 10px; font-size: 13px; }
.btn-sm { padding: 3px 10px; font-size: 12px; }

.empty { text-align: center; color: var(--color-text-secondary); padding: 24px !important; }
.err { color: var(--color-danger); font-size: 13px; }
.required { color: #DC2626; }

/* 状态标签基础样式 */
.tag {
  padding: 2px 12px;
  border-radius: 12px;
  font-size: 12px;
  display: inline-block;
  transition: all 0.2s ease;
}
.tag-active {
  background: #D1FAE5;
  color: #065F46;
}
.tag-inactive {
  background: #FEE2E2;
  color: #991B1B;
}

/* ⭐ 可点击状态标签样式 */
.tag.clickable {
  cursor: pointer;
  user-select: none;
}
.tag.clickable:hover {
  transform: scale(1.04);
  opacity: 0.85;
}
.tag-active.clickable:hover {
  box-shadow: 0 0 0 2px rgba(6, 95, 70, 0.3);
}
.tag-inactive.clickable:hover {
  box-shadow: 0 0 0 2px rgba(153, 27, 27, 0.3);
}

/* ⭐ 迁出按钮样式 */
.btn-warning {
  background: #F59E0B;
  color: #FFFFFF;
}
.btn-warning:hover {
  background: #D97706;
}

@media (max-width: 640px) {
  .toolbar { flex-direction: column; align-items: stretch; }
  .toolbar .actions { flex-wrap: wrap; }
  .toolbar .search { width: 100%; }
}
</style>