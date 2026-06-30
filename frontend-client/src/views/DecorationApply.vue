<template>
  <div>
    <div class="toolbar">
      <h2 class="page-title">装修申请</h2>
      <button class="btn btn-primary" @click="openForm()">+ 申请装修</button>
    </div>

    <div class="card">
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="!list.length" class="empty">暂无装修申请记录</div>
      <div v-else class="table-container">
        <table class="table">
          <thead>
            <tr>
              <th>类型</th>
              <th>预计时间</th>
              <th>状态</th>
              <th>申请时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in list" :key="item.id">
              <td>{{ decorationTypeLabel(item.decorationType) }}</td>
              <td>{{ formatDate(item.startDate) }} ~ {{ formatDate(item.endDate) }}</td>
              <td><span :class="['status-tag', `status-${item.status.toLowerCase()}`]">{{ statusLabel(item.status) }}</span></td>
              <td>{{ formatDate(item.createdAt) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="showModal" class="modal-mask" @click.self="showModal = false">
      <div class="modal">
        <h3>申请装修</h3>
        <div class="form-group">
          <label>装修类型 <span class="required">*</span></label>
          <select v-model="form.decorationType" class="select">
            <option value="NEW">新房装修</option>
            <option value="RENOVATION">旧房翻新</option>
            <option value="PARTIAL">局部改造</option>
            <option value="OTHER">其他</option>
          </select>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>预计开始日期 <span class="required">*</span></label>
            <input v-model="form.startDate" type="date" class="input" />
          </div>
          <div class="form-group">
            <label>预计结束日期 <span class="required">*</span></label>
            <input v-model="form.endDate" type="date" class="input" />
          </div>
        </div>
        <div class="form-group">
          <label>施工单位</label>
          <input v-model="form.contractor" class="input" placeholder="请输入施工单位名称" />
        </div>
        <div class="form-group">
          <label>负责人</label>
          <input v-model="form.supervisor" class="input" placeholder="请输入负责人姓名" />
        </div>
        <div class="form-group">
          <label>备注说明</label>
          <textarea v-model="form.remark" class="input textarea" rows="3" placeholder="请描述装修内容..."></textarea>
        </div>
        <p v-if="formError" class="err">{{ formError }}</p>
        <div class="modal-actions">
          <button class="btn btn-ghost" @click="showModal = false">取消</button>
          <button class="btn btn-primary" @click="submit">提交申请</button>
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
const list = ref([])
const loading = ref(false)
const showModal = ref(false)
const formError = ref('')

const form = ref({
  decorationType: 'NEW',
  startDate: '',
  endDate: '',
  contractor: '',
  supervisor: '',
  remark: ''
})

function decorationTypeLabel(type) {
  const map = { NEW: '新房装修', RENOVATION: '旧房翻新', PARTIAL: '局部改造', OTHER: '其他' }
  return map[type] || type
}

function statusLabel(status) {
  const map = { PENDING: '待审批', APPROVED: '已批准', REJECTED: '已驳回' }
  return map[status] || status
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return dateStr.slice(0, 10)
}

async function load() {
  loading.value = true
  const ownerId = userStore.user?.ownerId
  if (!ownerId) {
    loading.value = false
    return
  }
  try {
    list.value = await request.get(`/decoration/owner/${ownerId}`)
  } catch (e) {
    console.error('加载失败', e)
  } finally {
    loading.value = false
  }
}

function openForm() {
  formError.value = ''
  const today = new Date().toISOString().slice(0, 10)
  const nextMonth = new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString().slice(0, 10)
  form.value = {
    decorationType: 'NEW',
    startDate: today,
    endDate: nextMonth,
    contractor: '',
    supervisor: '',
    remark: ''
  }
  showModal.value = true
}

async function submit() {
  if (!form.value.startDate) {
    formError.value = '请选择开始日期'
    return
  }
  if (!form.value.endDate) {
    formError.value = '请选择结束日期'
    return
  }
  if (form.value.startDate > form.value.endDate) {
    formError.value = '结束日期不能早于开始日期'
    return
  }
  try {
    await request.post('/decoration', {
      decorationType: form.value.decorationType,
      startDate: form.value.startDate,
      endDate: form.value.endDate,
      contractor: form.value.contractor,
      supervisor: form.value.supervisor,
      remark: form.value.remark,
      ownerId: userStore.user?.ownerId,
      roomId: userStore.user?.roomId,
      status: 'PENDING'
    })
    showModal.value = false
    load()
  } catch (e) {
    formError.value = e.message
  }
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
}

.card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  padding: 20px;
}

.loading, .empty {
  text-align: center;
  color: var(--color-text-secondary);
  padding: 40px;
}

.table-container {
  overflow-x: auto;
}

.table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.table th, .table td {
  padding: 12px 8px;
  text-align: left;
  border-bottom: 1px solid var(--color-border);
}

.table th {
  background: #F8FAFC;
  font-weight: 600;
  color: #374151;
}

.status-tag {
  padding: 3px 10px;
  border-radius: 10px;
  font-size: 12px;
}

.status-pending {
  background: #FEF3C7;
  color: #B45309;
}

.status-approved {
  background: #D1FAE5;
  color: #065F46;
}

.status-rejected {
  background: #FEE2E2;
  color: #991B1B;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 6px;
}

.required {
  color: #DC2626;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.err {
  color: var(--color-danger);
  font-size: 13px;
  margin-bottom: 12px;
}

.textarea {
  resize: vertical;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 16px;
}

@media (max-width: 640px) {
  .form-row { grid-template-columns: 1fr; }
}
</style>
