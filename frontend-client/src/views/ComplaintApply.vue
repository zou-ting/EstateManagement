<template>
  <div class="page">
    <h2 class="page-title">投诉建议</h2>
    <div class="card">
      <div class="form-group">
        <label>类型 *</label>
        <div class="radio-group">
          <label :class="['radio', complaintType === 'COMPLAINT' ? 'active' : '']">
            <input type="radio" v-model="complaintType" value="COMPLAINT" />
            <span>投诉</span>
          </label>
          <label :class="['radio', complaintType === 'SUGGESTION' ? 'active' : '']">
            <input type="radio" v-model="complaintType" value="SUGGESTION" />
            <span>建议</span>
          </label>
        </div>
      </div>
      <div class="form-group">
        <label>标题 *</label>
        <input v-model="form.title" class="input" placeholder="请输入标题" />
      </div>
      <div class="form-group">
        <label>内容 *</label>
        <textarea v-model="form.content" class="textarea" rows="5" placeholder="请详细描述您的投诉或建议"></textarea>
      </div>
      <p v-if="formError" class="err">{{ formError }}</p>
      <button class="btn btn-primary btn-submit" @click="submit">提交</button>
    </div>
    <div class="section">
      <h3>我的投诉建议</h3>
      <div v-for="item in myComplaints" :key="item.id" class="complaint-item">
        <div class="complaint-header">
          <span :class="['tag', item.type === 'COMPLAINT' ? 'complaint' : 'suggestion']">
            {{ item.type === 'COMPLAINT' ? '投诉' : '建议' }}
          </span>
          <span :class="['tag', statusClass(item.status)]">{{ statusLabel(item.status) }}</span>
        </div>
        <h4>{{ item.title }}</h4>
        <p class="complaint-content">{{ item.content }}</p>
        <div class="complaint-time">{{ formatDate(item.createdAt) }}</div>
        
        <div class="conversation-section" v-if="conversationMap[item.id]?.length">
          <div class="conversation-header">💬 对话记录</div>
          <div class="conversation-list">
            <div v-for="reply in conversationMap[item.id]" :key="reply.id" 
                 :class="['conversation-item', reply.ownerId ? 'owner' : 'admin']">
              <div class="avatar">
                {{ reply.ownerId ? '👤' : '👨‍💼' }}
              </div>
              <div class="bubble">
                <div class="bubble-header">
                  <span class="sender">{{ reply.ownerId ? '我' : '管理员' }}</span>
                  <span class="time">{{ formatDate(reply.createdAt) }}</span>
                </div>
                <div class="bubble-content">{{ reply.replyContent }}</div>
              </div>
            </div>
          </div>
        </div>

        <div class="reply-input-area">
          <textarea v-model="replyContentMap[item.id]" 
                    class="reply-textarea" 
                    rows="2" 
                    placeholder="输入您的回复..."></textarea>
          <button class="btn btn-primary btn-reply" 
                  @click="submitOwnerReply(item)"
                  :disabled="!replyContentMap[item.id]?.trim()">
            发送回复
          </button>
        </div>
      </div>
      <div v-if="!myComplaints.length" class="empty">暂无投诉建议记录</div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, reactive, ref } from 'vue'
import request from '../api/request'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()
const complaintType = ref('COMPLAINT')
const formError = ref('')
const form = reactive({ title: '', content: '' })
const myComplaints = ref([])
const conversationMap = reactive({})
const replyContentMap = reactive({})
let refreshTimer = null

function statusLabel(s) {
  const labels = { PENDING: '待处理', PROCESSING: '处理中', DONE: '已完成', APPROVED: '已采纳', REJECTED: '已驳回' }
  return labels[s] || s
}

function statusClass(s) {
  return { PENDING: 'pending', PROCESSING: 'processing', DONE: 'done', APPROVED: 'approved', REJECTED: 'rejected' }[s] || ''
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return dateStr.replace('T', ' ').slice(0, 16)
}

async function submit() {
  if (!form.title?.trim()) { formError.value = '请输入标题'; return }
  if (!form.content?.trim()) { formError.value = '请输入内容'; return }
  if (!userStore.user?.ownerId) { formError.value = '请先登录'; return }

  try {
    const owner = await request.get(`/owners/${userStore.user.ownerId}`)
    await request.post('/complaints', {
      ownerId: userStore.user.ownerId,
      roomId: owner.roomId,
      title: form.title,
      content: form.content,
      type: complaintType.value
    })
    form.title = ''
    form.content = ''
    formError.value = ''
    await loadMyComplaints()
  } catch (e) {
    formError.value = e.message
  }
}

async function loadMyComplaints() {
  if (!userStore.user?.ownerId) return
  myComplaints.value = await request.get(`/complaints/owner/${userStore.user.ownerId}`)
  for (const item of myComplaints.value) {
    await loadConversation(item.id)
  }
}

async function loadConversation(complaintId) {
  try {
    conversationMap[complaintId] = await request.get(`/complaints/${complaintId}/replies`)
  } catch (e) {
    conversationMap[complaintId] = []
  }
}

async function submitOwnerReply(item) {
  if (!replyContentMap[item.id]?.trim()) return
  if (!userStore.user?.ownerId) return

  try {
    await request.post(`/complaints/${item.id}/owner-reply`, {
      reply: replyContentMap[item.id],
      ownerId: userStore.user.ownerId
    })
    replyContentMap[item.id] = ''
    await loadConversation(item.id)
  } catch (e) {
    console.error('回复失败:', e)
    alert('回复失败，请重试')
  }
}

// 定时刷新对话记录（每15秒）
function startAutoRefresh() {
  refreshTimer = setInterval(async () => {
    if (!userStore.user?.ownerId) return
    for (const item of myComplaints.value) {
      await loadConversation(item.id)
    }
  }, 15000)
}

function stopAutoRefresh() {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

onMounted(async () => {
  await loadMyComplaints()
  startAutoRefresh()
})

onUnmounted(() => {
  stopAutoRefresh()
})
</script>

<style scoped>
.page-title { margin-bottom: 20px; font-size: 18px; font-weight: 600; }
.card { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); margin-bottom: 24px; }
.form-group { margin-bottom: 16px; }
.form-group label { display: block; margin-bottom: 8px; font-size: 14px; font-weight: 500; }
.radio-group { display: flex; gap: 16px; }
.radio { display: flex; align-items: center; gap: 6px; padding: 8px 16px; border: 2px solid #E2E8F0; border-radius: 8px; cursor: pointer; transition: all var(--transition); }
.radio.active { border-color: var(--color-primary); background: rgba(4, 120, 87, 0.05); }
.radio input { margin: 0; }
.input { width: 100%; padding: 12px; border: 1px solid #E2E8F0; border-radius: 8px; font-size: 14px; }
.textarea { width: 100%; padding: 12px; border: 1px solid #E2E8F0; border-radius: 8px; font-size: 14px; resize: vertical; }
.err { color: var(--color-danger); font-size: 13px; margin-bottom: 16px; }
.btn-submit { width: 100%; padding: 12px; }
.section { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.section h3 { margin: 0 0 16px 0; font-size: 16px; }
.complaint-item { padding: 16px; border: 1px solid #E2E8F0; border-radius: 8px; margin-bottom: 12px; }
.complaint-item:last-child { margin-bottom: 0; }
.complaint-header { display: flex; gap: 8px; margin-bottom: 12px; }
.tag { padding: 4px 10px; border-radius: 4px; font-size: 12px; }
.tag.complaint { background: rgba(239,68,68,0.12); color: #EF4444; }
.tag.suggestion { background: rgba(59,130,246,0.12); color: #3B82F6; }
.tag.pending { background: rgba(239,68,68,0.12); color: #EF4444; }
.tag.processing { background: rgba(217,119,6,0.12); color: #D97706; }
.tag.done { background: rgba(5,150,105,0.12); color: #059669; }
.tag.approved { background: rgba(129,140,248,0.12); color: #6366F1; }
.tag.rejected { background: rgba(156,163,175,0.12); color: #6B7280; }
.complaint-item h4 { margin: 0 0 8px 0; font-size: 15px; }
.complaint-content { margin: 0 0 12px 0; font-size: 13px; color: var(--color-text-secondary); line-height: 1.6; }
.complaint-time { font-size: 12px; color: var(--color-text-secondary); margin-bottom: 12px; }
.empty { text-align: center; padding: 24px; color: var(--color-text-secondary); }

.conversation-section {
  margin-top: 16px;
  margin-bottom: 16px;
  padding: 12px;
  background: #F8FAFC;
  border-radius: 8px;
}

.conversation-header {
  font-size: 13px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #E5E7EB;
}

.conversation-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.conversation-item {
  display: flex;
  gap: 8px;
}

.conversation-item.owner {
  flex-direction: row-reverse;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.conversation-item.admin .avatar {
  background: #DBEAFE;
}

.conversation-item.owner .avatar {
  background: #D1FAE5;
}

.bubble {
  max-width: 75%;
}

.conversation-item.owner .bubble {
  text-align: right;
}

.bubble-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
  font-size: 12px;
}

.conversation-item.owner .bubble-header {
  justify-content: flex-end;
}

.sender {
  font-weight: 600;
}

.conversation-item.admin .sender {
  color: #1E40AF;
}

.conversation-item.owner .sender {
  color: #065F46;
}

.time {
  color: #9CA3AF;
}

.bubble-content {
  padding: 8px 12px;
  border-radius: 12px;
  font-size: 13px;
  line-height: 1.5;
}

.conversation-item.admin .bubble-content {
  background: #fff;
  border: 1px solid #DBEAFE;
  color: #374151;
  border-top-left-radius: 4px;
}

.conversation-item.owner .bubble-content {
  background: var(--color-primary);
  color: #fff;
  border-top-right-radius: 4px;
}

.reply-input-area {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #E5E7EB;
}

.reply-textarea {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #E2E8F0;
  border-radius: 8px;
  font-size: 13px;
  resize: none;
}

.btn-reply {
  padding: 8px 16px;
  font-size: 13px;
  white-space: nowrap;
}

.btn-reply:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>