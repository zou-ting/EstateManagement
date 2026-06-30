<template>
  <div>
    <div class="toolbar">
      <h2 class="page-title">缴费记录</h2>
      <button class="btn btn-primary" @click="openForm()">新增记录</button>
    </div>
    <div class="card">
      <table class="table">
        <thead><tr><th>房屋</th><th>关联账单</th><th>实缴金额</th><th>支付方式</th><th>缴费时间</th><th>操作人</th><th>备注</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="item in list" :key="item.id">
            <td>{{ unitLabel(item.roomId) }}</td>
            <td>{{ item.feeId }}</td>
            <td class="amount">¥{{ item.amount ?? 0 }}</td>
            <td><span class="tag">{{ payMethodLabel(item.payMethod) }}</span></td>
            <td>{{ item.payDate?.replace('T', ' ') }}</td>
            <td>{{ item.operator || '-' }}</td>
            <td>{{ item.remark || '-' }}</td>
            <td>
              <button class="btn btn-ghost" @click="openForm(item)">编辑</button>
              <button class="btn btn-danger" @click="remove(item)">删除</button>
            </td>
          </tr>
          <tr v-if="!list.length"><td colspan="8" class="empty">暂无数据</td></tr>
        </tbody>
      </table>
    </div>
    <div v-if="showModal" class="modal-mask" @click.self="showModal = false">
      <div class="modal">
        <h3>{{ form.id ? '编辑缴费记录' : '新增缴费记录' }}</h3>
        <div class="form-group">
          <label>房屋 *</label>
          <select v-model.number="form.roomId" class="select">
            <option v-for="u in units" :key="u.id" :value="u.id">{{ unitOptionLabel(u) }}</option>
          </select>
        </div>
        <div class="form-group"><label>关联账单ID *</label><input v-model.number="form.feeId" type="number" min="1" class="input" placeholder="账单ID" /></div>
        <div class="form-group"><label>实缴金额(元) *</label><input v-model.number="form.amount" type="number" min="0" step="0.01" class="input" /></div>
        <div class="form-group">
          <label>支付方式</label>
          <select v-model="form.payMethod" class="select">
            <option value="CASH">现金</option>
            <option value="ALIPAY">支付宝</option>
            <option value="WECHAT">微信</option>
            <option value="BANK">银行转账</option>
            <option value="CARD">刷卡</option>
          </select>
        </div>
        <div class="form-group"><label>缴费时间</label><input v-model="form.payDate" type="datetime-local" class="input" /></div>
        <div class="form-group"><label>操作人</label><input v-model="form.operator" class="input" /></div>
        <div class="form-group"><label>备注</label><textarea v-model="form.remark" class="input" rows="3"></textarea></div>
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
import { onMounted, reactive, ref } from 'vue'
import request from '../api/request'
import { showConfirm } from '../composables/useConfirm'

const list = ref([])
const units = ref([])
const buildings = ref([])
const showModal = ref(false)
const formError = ref('')
const form = reactive({ id: null, feeId: null, roomId: null, amount: 0, payMethod: 'CASH', payDate: '', operator: '', remark: '' })

function payMethodLabel(s) {
  const map = { CASH: '现金', ALIPAY: '支付宝', WECHAT: '微信', BANK: '银行转账', CARD: '刷卡' }
  return map[s] || s
}

function unitOptionLabel(u) {
  const b = buildings.value.find(x => x.id === u.buildingId)
  return `${b?.buildingName || ''} ${u.unitNo}`.trim()
}

function unitLabel(roomId) {
  const u = units.value.find(x => x.id === roomId)
  return u ? unitOptionLabel(u) : `#${roomId}`
}

async function loadMeta() {
  buildings.value = await request.get('/buildings')
  units.value = await request.get('/units')
}

async function load() { list.value = await request.get('/payments') }

function openForm(item) {
  formError.value = ''
  Object.assign(form, { id: null, feeId: null, roomId: units.value[0]?.id ?? null, amount: 0, payMethod: 'CASH', payDate: '', operator: '', remark: '' })
  if (item) {
    Object.assign(form, item)
    if (item.payDate) {
      form.payDate = item.payDate.replace('T', ' ').substring(0, 16)
    }
  }
  showModal.value = true
}

async function save() {
  if (!form.roomId || !form.feeId || !form.amount) { formError.value = '房屋、账单ID和金额不能为空'; return }
  try {
    if (form.id) await request.put(`/payments/${form.id}`, form)
    else await request.post('/payments', form)
    showModal.value = false
    load()
  } catch (e) { formError.value = e.message }
}

async function remove(item) {
  const ok = await showConfirm('删除确认', '确定删除该缴费记录吗？')
  if (!ok) return
  await request.delete(`/payments/${item.id}`)
  load()
}

onMounted(async () => { await loadMeta(); load() })
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.table .btn { margin-right: 8px; padding: 4px 10px; font-size: 13px; }
.amount { font-weight: 600; color: var(--color-primary); }
.tag { background: #DBEAFE; color: #1D4ED8; padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.empty { text-align: center; color: var(--color-text-secondary); padding: 24px !important; }
.err { color: var(--color-danger); font-size: 13px; }
</style>