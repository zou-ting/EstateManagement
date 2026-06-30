<template>
  <div>
    <!-- 工具栏 -->
    <div class="toolbar">
      <h2 class="page-title">巡检记录</h2>
      <div class="actions">
        <input v-model="keyword" class="input search" placeholder="搜索检查员..." @keyup.enter="load" />
        <select v-model="resultFilter" class="select" @change="load">
          <option value="">全部结果</option>
          <option value="PASS">✅ 合格</option>
          <option value="RECTIFY">⚠️ 待整改</option>
          <option value="FAIL">❌ 不合格</option>
        </select>
        <button class="btn btn-primary" @click="openForm()">+ 新增巡检</button>
      </div>
    </div>

    <!-- 统计栏 -->
    <div class="stats-bar">
      <span>共 <strong>{{ filteredList.length }}</strong> 条</span>
      <span>✅ 合格 <strong>{{ resultStats.pass }}</strong> 条</span>
      <span>⚠️ 待整改 <strong>{{ resultStats.rectify }}</strong> 条</span>
      <span>❌ 不合格 <strong>{{ resultStats.fail }}</strong> 条</span>
    </div>

    <!-- 表格 -->
    <div class="card">
      <table class="table">
        <thead>
        <tr>
          <th>序号</th>
          <th>房间</th>
          <th>楼栋</th>
          <th>检查员</th>
          <th>得分</th>
          <th>检查日期</th>
          <th>问题描述</th>
          <th>结果</th>
          <th>操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="(item, index) in filteredList" :key="item.id">
          <td>{{ index + 1 }}</td>
          <td>{{ unitMap[item.roomId] || item.roomId }}</td>
          <td>{{ buildingMap[item.buildingId] || '—' }}</td>
          <td>{{ item.inspectorName || '—' }}</td>
          <td>
            <span :class="['score', scoreClass(item.score)]">{{ item.score }}</span>
          </td>
          <td>{{ item.checkDate || '—' }}</td>
          <td>{{ item.issues || '—' }}</td>
          <td><span :class="['tag', resultClass(item.result)]">{{ resultLabel(item.result) }}</span></td>
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
        <h3>{{ form.id ? '✏️ 编辑巡检' : '➕ 新增巡检' }}</h3>
         <div class="form-group">
          <label>房间 <span class="required">*</span></label>
          <select v-model.number="form.roomId" class="select">
            <option v-for="u in units" :key="u.id" :value="u.id">
              {{ u.unitNo }} <!-- 只显示房间号 -->
            </option>
          </select>
        </div>
        <div class="form-group">
          <label>楼栋 <span class="required">*</span></label>
          <select v-model.number="form.buildingId" class="select">
            <option v-for="b in buildings" :key="b.id" :value="b.id">
              {{ b.buildingName }}
            </option>
          </select>
        </div>
        <div class="form-group">
          <label>检查员</label>
          <input v-model="form.inspectorName" class="input" maxlength="50" placeholder="请输入检查员姓名" />
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>得分 <span class="required">*</span></label>
            <input v-model.number="form.score" type="number" min="0" max="100" class="input" placeholder="0-100" />
          </div>
          <div class="form-group">
            <label>检查日期</label>
            <input v-model="form.checkDate" type="date" class="input" />
          </div>
        </div>
        <div class="form-group">
          <label>问题描述</label>
          <textarea v-model="form.issues" class="input textarea" maxlength="500" rows="2" placeholder="如有问题请描述"></textarea>
        </div>
        <div class="form-group">
          <label>结果</label>
          <select v-model="form.result" class="select">
            <option value="PASS">✅ 合格</option>
            <option value="RECTIFY">⚠️ 待整改</option>
            <option value="FAIL">❌ 不合格</option>
          </select>
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
const resultFilter = ref('')
const showModal = ref(false)
const formError = ref('')

const form = reactive({
  id: null,
  roomId: null,
  buildingId: null,  // 楼栋ID字段
  inspectorName: '',
  score: 90,
  checkDate: '',
  issues: '',
  result: 'PASS'
})

// 楼栋映射 - 通过楼栋ID获取楼栋名称
const buildingMap = computed(() => {
  const map = {}
  buildings.value.forEach(b => {
    map[b.id] = b.buildingName
  })
  return map
})

// 房间映射 - 只显示房间号
const unitMap = computed(() => {
  const map = {}
  units.value.forEach(u => {
    map[u.id] = u.unitNo
  })
  return map
})

// 筛选列表
const filteredList = computed(() => {
  let result = list.value
  if (keyword.value.trim()) {
    const kw = keyword.value.trim().toLowerCase()
    result = result.filter(item =>
        item.inspectorName?.toLowerCase().includes(kw)
    )
  }
  if (resultFilter.value) {
    result = result.filter(item => item.result === resultFilter.value)
  }
  return result
})

// 结果统计
const resultStats = computed(() => {
  const stats = { pass: 0, rectify: 0 }
  list.value.forEach(item => {
    if (item.result === 'PASS') stats.pass++
    else if (item.result === 'RECTIFY') stats.rectify++
  })
  return stats
})

function resultLabel(r) {
  return { PASS: '合格', RECTIFY: '待整改' ,FAIL:'不合格'}[r] || r
}

function resultClass(r) {
  return { PASS: 'tag-pass', RECTIFY: 'tag-rectify',FAIL:'tag-fail' }[r] || ''
}

function scoreClass(score) {
  if (score >= 80) return 'score-good'
  if (score >= 60) return 'score-medium'
  return 'score-bad'
}

async function loadMeta() {
  [units.value, buildings.value] = await Promise.all([
    request.get('/units'),
    request.get('/buildings')
  ])
}

async function load() {
  list.value = await request.get('/inspection-records')
}

function openForm(item) {
  formError.value = ''
  Object.assign(form, {
    id: null,
    roomId: units.value[0]?.id || null,
    buildingId: buildings.value[0]?.id || null,  // 设置默认楼栋
    inspectorName: '',
    score: 90,
    checkDate: '',
    issues: '',
    result: 'PASS'
  })
  if (item) Object.assign(form, item)
  showModal.value = true
}

async function save() {
  if (!form.roomId) { formError.value = '请选择房间'; return }
  if (!form.buildingId) { formError.value = '请选择楼栋'; return }  // 验证楼栋
  if (form.score === undefined || form.score === null) { formError.value = '请输入得分'; return }
  if (form.score < 0 || form.score > 100) { formError.value = '得分必须在0-100之间'; return }
  formError.value = ''
  try {
    if (form.id) {
      await request.put(`/inspection-records/${form.id}`, form)
    } else {
      await request.post('/inspection-records', form)
    }
    showModal.value = false
    load()
  } catch (e) {
    formError.value = e.message
  }
}

async function remove(item) {
  const ok = await showConfirm('删除确认', `确定删除该巡检记录吗？`)
  if (!ok) return
  await request.delete(`/inspection-records/${item.id}`)
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

.tag-pass { background: #D1FAE5; color: #065F46; padding: 2px 12px; border-radius: 12px; font-size: 12px; }
.tag-rectify { background: #FEF3C7; color: #B45309; padding: 2px 12px; border-radius: 12px; font-size: 12px; }
.tag-fail { background: #FEE2E2; color: #991B1B; padding: 2px 12px; border-radius: 12px; font-size: 12px; }

.score-good { color: #065F46; font-weight: 600; }
.score-medium { color: #B45309; font-weight: 600; }
.score-bad { color: #991B1B; font-weight: 600; }

.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.textarea { resize: vertical; min-height: 60px; }

@media (max-width: 640px) {
  .form-row { grid-template-columns: 1fr; }
  .toolbar { flex-direction: column; align-items: stretch; }
  .toolbar .actions { flex-wrap: wrap; }
  .toolbar .search { width: 100%; }
}
</style>