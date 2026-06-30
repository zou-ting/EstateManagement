<template>
  <div>
    <h2 class="page-title">个人信息</h2>
    <div class="card profile-card" v-if="owner">
      <AvatarUpload v-model="form.avatar" />
      <div class="form-grid">
        <div class="form-group"><label>业主编号</label><input :value="owner.ownerNo" class="input" disabled /></div>
        <div class="form-group"><label>姓名</label><input v-model="form.name" class="input" maxlength="50" /></div>
        <div class="form-group"><label>类型</label><input :value="owner.gender" class="input" disabled /></div>
        <div class="form-group"><label>楼栋单元</label><input :value="owner.grade || '—'" class="input" disabled /></div>
        <div class="form-group">
          <label>手机</label>
          <input v-model="form.phone" class="input" maxlength="11" />
          <span v-if="errors.phone" class="err">{{ errors.phone }}</span>
        </div>
        <div class="form-group">
          <label>邮箱</label>
          <input v-model="form.email" class="input" maxlength="100" />
          <span v-if="errors.email" class="err">{{ errors.email }}</span>
        </div>
      </div>
      <p v-if="msg" :class="msgOk ? 'msg-ok' : 'err'">{{ msg }}</p>
      <div class="actions">
        <button class="btn btn-primary" @click="save" :disabled="saving">{{ saving ? '保存中...' : '保存修改' }}</button>
        <button type="button" class="btn btn-ghost logout-btn" @click="logout">退出登录</button>
      </div>
    </div>
    <div v-else class="card"><p>加载中...</p></div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import request from '../api/request'
import AvatarUpload from '../components/AvatarUpload.vue'
import { validatePhone, validateEmail } from '../utils/validators'

const userStore = useUserStore()
const router = useRouter()
const owner = ref(null)
const saving = ref(false)
const msg = ref('')
const msgOk = ref(false)
const errors = reactive({ phone: '', email: '' })
const form = reactive({ name: '', phone: '', email: '', avatar: '' })

onMounted(async () => {
  if (!userStore.user?.ownerId) return
  owner.value = await request.get(`/owners/${userStore.user.ownerId}`)
  form.name = owner.value.name || ''
  form.phone = owner.value.phone || ''
  form.email = owner.value.email || ''
  form.avatar = owner.value.avatar || ''
  userStore.patchUser({ name: owner.value.name, avatar: owner.value.avatar || null })
})

async function save() {
  errors.phone = validatePhone(form.phone)
  errors.email = validateEmail(form.email)
  if (errors.phone || errors.email) return
  saving.value = true
  msg.value = ''
  try {
    const payload = { name: form.name, phone: form.phone, email: form.email, avatar: form.avatar?.trim() || null }
    const updated = await request.put(`/profile/owner/${owner.value.id}`, payload)
    owner.value = { ...owner.value, ...updated }
    userStore.patchUser({ name: updated.name, avatar: updated.avatar || null })
    msg.value = '保存成功'
    msgOk.value = true
  } catch (e) { msg.value = e.message || '保存失败'; msgOk.value = false }
  finally { saving.value = false }
}

function logout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.profile-card { 
  max-width: 600px; 
  margin: 0 auto;
}
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 20px; }
.form-group label { display: block; font-size: 13px; color: var(--color-text-secondary); margin-bottom: 6px; }
.err { color: #DC2626; font-size: 12px; }
.msg-ok { color: #059669; font-size: 13px; margin-bottom: 12px; }
.actions { display: flex; flex-direction: column; gap: 12px; margin-top: 8px; }
.logout-btn { color: var(--color-text-secondary); border: 1px solid var(--color-border); }
@media (max-width: 520px) { .form-grid { grid-template-columns: 1fr; } }
</style>
