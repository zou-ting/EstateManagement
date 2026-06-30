<template>
  <div>
    <!-- 工具栏 -->
    <div class="toolbar">
      <h2 class="page-title">楼栋管理</h2>
      <div class="actions">
        <input v-model="keyword" class="input search" placeholder="搜索名称/编码..." @keyup.enter="load" />
        <select v-model="typeFilter" class="select" @change="load">
          <option value="">全部类型</option>
          <option value="住宅">住宅</option>
          <option value="商业">商业</option>
          <option value="混合">混合</option>
        </select>
        <button class="btn btn-primary" @click="openForm()">+ 新增楼栋</button>
      </div>
    </div>

    <!-- 统计栏 -->
    <div class="stats-bar">
      <span>共 <strong>{{ list.length }}</strong> 栋</span>
      <span v-if="typeStats.residential">🏠 住宅 {{ typeStats.residential }} 栋</span>
      <span v-if="typeStats.commercial">🏪 商业 {{ typeStats.commercial }} 栋</span>
      <span v-if="typeStats.mixed">🏢 混合 {{ typeStats.mixed }} 栋</span>
    </div>

    <!-- 表格 -->
    <div class="card">
      <table class="table">
        <thead>
        <tr>
          <th></th>
          <th>楼栋名称</th>
          <th>编码</th>
          <th>楼层</th>
          <th>类型</th>
          <th>地址</th>
          <th>操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="item in filteredList" :key="item.id">
          <td></td>
          <td><strong>{{ item.buildingName }}</strong></td>
          <td><code>{{ item.buildingCode }}</code></td>
          <td>{{ item.floors ?? '—' }}</td>
          <td><span :class="['tag', typeTagClass(item.buildingType)]">{{ item.buildingType || '—' }}</span></td>
          <td>{{ item.address || '—' }}</td>
          <td>
            <button class="btn btn-ghost" @click="openForm(item)">编辑</button>
            <button class="btn btn-danger" @click="remove(item)">删除</button>
          </td>
        </tr>
        <tr v-if="!filteredList.length"><td colspan="7" class="empty">暂无数据</td></tr>
        </tbody>
      </table>
    </div>

    <!-- 弹窗表单 -->
    <div v-if="showModal" class="modal-mask" @click.self="showModal = false">
      <div class="modal">
        <h3>{{ form.id ? '✏️ 编辑楼栋' : '➕ 新增楼栋' }}</h3>
        <div class="form-group">
          <label>楼栋名称 <span class="required">*</span></label>
          <input v-model="form.buildingName" class="input" maxlength="50" placeholder="如：翠湖苑1号楼" />
        </div>
        <div class="form-group">
          <label>编码 <span class="required">*</span></label>
          <input v-model="form.buildingCode" class="input" maxlength="20" placeholder="如：CH1" :readonly="!!form.id" />
          <small class="hint" v-if="!form.id">编码不可修改，请谨慎填写</small>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>楼层数</label>
            <input v-model.number="form.floors" type="number" min="1" max="60" class="input" />
          </div>
          <div class="form-group">
            <label>类型</label>
            <select v-model="form.buildingType" class="select">
              <option value="住宅">🏠 住宅</option>
              <option value="商业">🏪 商业</option>
              <option value="混合">🏢 混合</option>
            </select>
          </div>
        </div>
        <div class="form-group">
          <label>地址</label>
          <input v-model="form.address" class="input" maxlength="200" placeholder="如：翠湖苑小区东区" />
        </div>
        <div class="form-group">
          <label>描述</label>
          <textarea v-model="form.description" class="input textarea" maxlength="200" rows="2" placeholder="备注信息"></textarea>
        </div>
        <div class="form-group">
          <label>排序</label>
          <input v-model.number="form.sortOrder" type="number" class="input" placeholder="数字越大越靠前" />
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
const showModal = ref(false)
const formError = ref('')
const keyword = ref('')
const typeFilter = ref('')
const selectedIds = ref([])

const form = reactive({
  id: null,
  buildingName: '',
  buildingCode: '',
  floors: 18,
  buildingType: '住宅',
  address: '',
  description: '',
  sortOrder: 0
})

// 计算属性：根据关键词和类型筛选
const filteredList = computed(() => {
  let result = list.value
  if (keyword.value.trim()) {
    const kw = keyword.value.trim().toLowerCase()
    result = result.filter(item =>
        item.buildingName?.toLowerCase().includes(kw) ||
        item.buildingCode?.toLowerCase().includes(kw)
    )
  }
  if (typeFilter.value) {
    result = result.filter(item => item.buildingType === typeFilter.value)
  }
  return result
})

// 类型统计
const typeStats = computed(() => {
  const stats = { residential: 0, commercial: 0, mixed: 0 }
  list.value.forEach(item => {
    if (item.buildingType === '住宅') stats.residential++
    else if (item.buildingType === '商业') stats.commercial++
    else if (item.buildingType === '混合') stats.mixed++
  })
  return stats
})

// 类型标签样式
function typeTagClass(type) {
  const map = {
    '住宅': 'tag-residential',
    '商业': 'tag-commercial',
    '混合': 'tag-mixed'
  }
  return map[type] || ''
}

async function load() {
  try {
    list.value = await request.get('/buildings')
  } catch (e) {
    console.error('加载楼栋失败:', e)
  }
}

function openForm(item) {
  formError.value = ''
  Object.assign(form, {
    id: null,
    buildingName: '',
    buildingCode: '',
    floors: 18,
    buildingType: '住宅',
    address: '',
    description: '',
    sortOrder: 0
  })
  if (item) Object.assign(form, item)
  showModal.value = true
}

async function save() {
  if (!form.buildingName?.trim()) {
    formError.value = '请输入楼栋名称'
    return
  }
  if (!form.buildingCode?.trim()) {
    formError.value = '请输入编码'
    return
  }
  formError.value = ''
  try {
    if (form.id) {
      await request.put(`/buildings/${form.id}`, form)
    } else {
      await request.post('/buildings', form)
    }
    showModal.value = false
    load()
  } catch (e) {
    formError.value = e.message
  }
}

async function remove(item) {
  const ok = await showConfirm('删除楼栋', `确定删除「${item.buildingName}」吗？`)
  if (!ok) return
  try {
    await request.delete(`/buildings/${item.id}`)
    load()
  } catch (e) {
    formError.value = e.message
  }
}

onMounted(load)
</script>

<style scoped>
/* 完全复制 Owners.vue 的样式 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.actions {
  display: flex;
  gap: 8px;
}
.search {
  width: 200px;
}

/* 其他样式保持不变 */
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
.hint { font-size: 12px; color: #94A3B8; }

.tag-residential { background: #E0F2FE; color: #0369A1; padding: 2px 12px; border-radius: 12px; font-size: 12px; }
.tag-commercial { background: #FCE7F3; color: #BE185D; padding: 2px 12px; border-radius: 12px; font-size: 12px; }
.tag-mixed { background: #FEF3C7; color: #B45309; padding: 2px 12px; border-radius: 12px; font-size: 12px; }

.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.textarea { resize: vertical; min-height: 60px; }

@media (max-width: 640px) {
  .form-row { grid-template-columns: 1fr; }
  .toolbar { flex-direction: column; align-items: stretch; }
  .toolbar .actions { flex-wrap: wrap; }
  .toolbar .search { width: 100%; }
}
</style>