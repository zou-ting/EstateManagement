<template>
  <div>
    <h2 class="page-title">个人资料</h2>
    <div class="profile-wrapper">
      <div class="card profile-card" v-if="profile">
        <AvatarUpload v-model="form.avatar" />
        <div class="form-grid">
          <div class="form-group">
            <label>用户名</label>
            <input :value="profile.username" class="input" disabled />
          </div>
          <div class="form-group">
            <label>角色</label>
            <input :value="roleLabel" class="input" disabled />
          </div>
          <div class="form-group">
            <label>姓名</label>
            <input v-model="form.name" class="input" maxlength="50" />
          </div>
          <div class="form-group">
            <label>手机</label>
            <input v-model="form.phone" class="input" maxlength="11" @input="form.phone = form.phone.replace(/\D/g,'').slice(0,11)" />
            <span v-if="errors.phone" class="err">{{ errors.phone }}</span>
          </div>
          <div class="form-group">
            <label>邮箱</label>
            <input v-model="form.email" class="input" maxlength="100" />
            <span v-if="errors.email" class="err">{{ errors.email }}</span>
          </div>
        </div>
        <p v-if="msg" :class="msgOk ? 'msg-ok' : 'err'">{{ msg }}</p>
        <button class="btn btn-primary" @click="save" :disabled="saving">{{ saving ? '保存中...' : '保存修改' }}</button>
      </div>
      <div v-else class="card"><p>加载中...</p></div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useUserStore } from '../stores/user'
import request from '../api/request'
import AvatarUpload from '../components/AvatarUpload.vue'
import { validatePhone, validateEmail } from '../utils/validators'

const userStore = useUserStore()
const profile = ref(null)
const saving = ref(false)
const msg = ref('')
const msgOk = ref(false)
const errors = reactive({ phone: '', email: '' })
const form = reactive({ name: '', phone: '', email: '', avatar: '' })

const roleLabel = computed(() => ({
  ADMIN: '管理员',
  PROPERTY_MANAGER: '物业管家'
}[profile.value?.role] || profile.value?.role))

onMounted(async () => {
  const uid = userStore.user?.userId
  if (!uid) return
  profile.value = await request.get(`/profile/user/${uid}`)
  form.name = profile.value.name || ''
  form.phone = profile.value.phone || ''
  form.email = profile.value.email || ''
  form.avatar = profile.value.avatar || ''
  userStore.patchUser({ name: profile.value.name, avatar: profile.value.avatar || null })
})

async function save() {
  errors.phone = validatePhone(form.phone)
  errors.email = validateEmail(form.email)
  if (errors.phone || errors.email) return
  saving.value = true
  msg.value = ''
  try {
    const payload = {
      name: form.name,
      phone: form.phone,
      email: form.email,
      avatar: form.avatar?.trim() || null
    }
    const updated = await request.put(`/profile/user/${userStore.user.userId}`, payload)
    profile.value = updated
    form.avatar = updated.avatar || ''
    userStore.patchUser({ name: updated.name, avatar: updated.avatar || null })
    msg.value = '保存成功'
    msgOk.value = true
  } catch (e) {
    msg.value = e.message || '保存失败'
    msgOk.value = false
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.profile-wrapper {
  display: flex;
  justify-content: center;
}

.profile-card {
  max-width: 640px;
  width: 100%;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 20px;
}

.err {
  color: var(--color-danger);
  font-size: 12px;
}
.msg-ok {
  color: var(--color-success);
  font-size: 13px;
  margin-bottom: 12px;
}

.btn-primary {
  display: block;
  margin: 0 auto;
  min-width: 140px;
}

@media (max-width: 520px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>