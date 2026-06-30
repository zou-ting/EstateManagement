<template>
  <div>
    <!-- 工具栏 -->
    <div class="toolbar">
      <h2 class="page-title">公告通知</h2>
      <div class="actions">
        <input v-model="keyword" class="input search" placeholder="搜索标题..." @keyup.enter="load" />
        <select v-model="statusFilter" class="select" @change="load">
          <option value="">全部状态</option>
          <option value="PUBLISHED">📢 已发布</option>
          <option value="DRAFT">📝 草稿</option>
        </select>
        <button class="btn btn-primary" @click="openForm()">+ 新增公告</button>
      </div>
    </div>

    <!-- 统计栏 -->
    <div class="stats-bar">
      <span>共 <strong>{{ filteredList.length }}</strong> 条</span>
      <span>📢 已发布 <strong>{{ statusStats.published }}</strong> 条</span>
      <span>📝 草稿 <strong>{{ statusStats.draft }}</strong> 条</span>
    </div>

    <!-- 表格 -->
    <div class="card">
      <table class="table">
        <thead>
        <tr>
          <th>序号</th>
          <th>标题</th>
          <th>内容预览</th>
          <th>发布日期</th>
          <th>发布人</th>
          <th>状态</th>
          <th>操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="(item, index) in filteredList" :key="item.id">
          <td>{{ index + 1 }}</td>
          <td><strong>{{ item.title }}</strong></td>
          <td>{{ item.content ? item.content.slice(0, 30) + (item.content.length > 30 ? '...' : '') : '—' }}</td>
          <td>{{ item.publishDate || '—' }}</td>
          <td>{{ item.publisher || '—' }}</td>
          <td><span :class="['tag', statusClass(item.status)]">{{ statusLabel(item.status) }}</span></td>
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
        <h3>{{ form.id ? '✏️ 编辑公告' : '➕ 新增公告' }}</h3>
        <div class="form-group">
          <label>标题 <span class="required">*</span></label>
          <input v-model="form.title" class="input" maxlength="100" placeholder="请输入公告标题" />
        </div>
        <div class="form-group">
          <label>内容</label>
          <textarea v-model="form.content" class="input textarea" maxlength="1000" rows="4" placeholder="请输入公告内容"></textarea>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>发布日期</label>
            <input v-model="form.publishDate" type="date" class="input" />
          </div>
          <div class="form-group">
            <label>发布人</label>
            <div v-if="!form.id" class="readonly-value">{{ userStore.user?.name || '系统管理员' }}</div>
            <input v-else v-model="form.publisher" class="input" maxlength="50" placeholder="请输入发布人" />
          </div>
        </div>
        <div class="form-group">
          <label>状态</label>
          <select v-model="form.status" class="select">
            <option value="PUBLISHED">📢 已发布</option>
            <option value="DRAFT">📝 草稿</option>
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
import { useUserStore } from '../stores/user'

const userStore = useUserStore()
const list = ref([])
const keyword = ref('')
const statusFilter = ref('')
const showModal = ref(false)
const formError = ref('')

const form = reactive({
  id: null,
  title: '',
  content: '',
  publishDate: '',
  publisher: '',
  status: 'PUBLISHED'
})

// 筛选列表
const filteredList = computed(() => {
  let result = list.value
  if (keyword.value.trim()) {
    const kw = keyword.value.trim().toLowerCase()
    result = result.filter(item =>
        item.title?.toLowerCase().includes(kw)
    )
  }
  if (statusFilter.value) {
    result = result.filter(item => item.status === statusFilter.value)
  }
  return result
})

// 状态统计
const statusStats = computed(() => {
  const stats = { published: 0, draft: 0 }
  list.value.forEach(item => {
    if (item.status === 'PUBLISHED') stats.published++
    else if (item.status === 'DRAFT') stats.draft++
  })
  return stats
})

function statusLabel(s) {
  const map = { PUBLISHED: '已发布', DRAFT: '草稿' }
  return map[s] || s
}

function statusClass(s) {
  const map = { PUBLISHED: 'tag-published', DRAFT: 'tag-draft' }
  return map[s] || ''
}

async function load() {
  list.value = await request.get('/announcements')
}

function openForm(item) {
  formError.value = ''
  Object.assign(form, {
    id: null,
    title: '',
    content: '',
    publishDate: '',
    publisher: userStore.user?.name || '系统管理员',
    status: 'PUBLISHED'
  })
  if (item) Object.assign(form, item)
  showModal.value = true
}

async function save() {
  if (!form.title?.trim()) {
    formError.value = '请输入标题'
    return
  }
  formError.value = ''
  try {
    if (form.id) {
      await request.put(`/announcements/${form.id}`, form)
    } else {
      await request.post('/announcements', form)
    }
    showModal.value = false
    load()
  } catch (e) {
    formError.value = e.message
  }
}

async function remove(item) {
  const ok = await showConfirm('删除确认', `确定删除「${item.title}」吗？`)
  if (!ok) return
  await request.delete(`/announcements/${item.id}`)
  load()
}

onMounted(load)
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

.tag-published { background: #D1FAE5; color: #065F46; padding: 2px 12px; border-radius: 12px; font-size: 12px; }
.tag-draft { background: #FEF3C7; color: #B45309; padding: 2px 12px; border-radius: 12px; font-size: 12px; }

.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.textarea { resize: vertical; min-height: 80px; }
.readonly-value {
  padding: 10px 14px;
  background: #F3F4F6;
  border: 1px solid #E5E7EB;
  border-radius: var(--radius);
  font-size: 14px;
  color: #374151;
}

@media (max-width: 640px) {
  .form-row { grid-template-columns: 1fr; }
  .toolbar { flex-direction: column; align-items: stretch; }
  .toolbar .actions { flex-wrap: wrap; }
  .toolbar .search { width: 100%; }
}
</style>