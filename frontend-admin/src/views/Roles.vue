<template>

  <div>

    <div class="toolbar">

      <h2 class="page-title">角色管理</h2>

      <button class="btn btn-primary" @click="openForm()">新增角色</button>

    </div>

    <div class="card">

      <table class="table">

        <thead>

          <tr><th>编码</th><th>名称</th><th>描述</th><th>状态</th><th>操作</th></tr>

        </thead>

        <tbody>

          <tr v-for="item in list" :key="item.id">

            <td>{{ item.roleCode }}</td>

            <td>{{ item.roleName }}</td>

            <td>{{ item.description || '—' }}</td>

            <td>{{ item.status === 1 ? '启用' : '禁用' }}</td>

            <td>

              <button class="btn btn-ghost" @click="openForm(item)">编辑</button>

              <button class="btn btn-ghost" @click="openMenus(item)">分配菜单</button>

              <button class="btn btn-danger" @click="remove(item)">删除</button>

            </td>

          </tr>

          <tr v-if="!list.length"><td colspan="5" class="empty">暂无数据</td></tr>

        </tbody>

      </table>

    </div>



    <div v-if="showModal" class="modal-mask" @click.self="showModal = false">

      <div class="modal">

        <h3>{{ form.id ? '编辑角色' : '新增角色' }}</h3>

        <div class="form-group">

          <label>角色编码 <span class="req">*</span></label>

          <input v-model="form.roleCode" class="input" maxlength="20" :readonly="!!form.id" />

        </div>

        <div class="form-group">

          <label>角色名称 <span class="req">*</span></label>

          <input v-model="form.roleName" class="input" maxlength="50" />

        </div>

        <div class="form-group">

          <label>描述</label>

          <input v-model="form.description" class="input" maxlength="200" />

        </div>

        <div class="form-group">

          <label>状态</label>

          <select v-model="form.status" class="select">

            <option :value="1">启用</option>

            <option :value="0">禁用</option>

          </select>

        </div>

        <p v-if="formError" class="err">{{ formError }}</p>

        <div class="modal-actions">

          <button class="btn btn-ghost" @click="showModal = false">取消</button>

          <button class="btn btn-primary" @click="save">保存</button>

        </div>

      </div>

    </div>



    <div v-if="showMenuModal" class="modal-mask" @click.self="showMenuModal = false">

      <div class="modal modal-lg">

        <h3>分配菜单 · {{ currentRole?.roleName }}</h3>

        <div class="menu-checks">

          <label v-for="m in allMenus" :key="m.id" class="check-item">

            <input type="checkbox" :value="m.id" v-model="selectedMenuIds" />

            {{ m.title }} ({{ m.path }})

          </label>

        </div>

        <div class="modal-actions">

          <button class="btn btn-ghost" @click="showMenuModal = false">取消</button>

          <button class="btn btn-primary" @click="saveMenus">保存</button>

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

const allMenus = ref([])

const showModal = ref(false)

const showMenuModal = ref(false)

const formError = ref('')

const currentRole = ref(null)

const selectedMenuIds = ref([])

const form = reactive({ id: null, roleCode: '', roleName: '', description: '', status: 1 })



async function load() {

  list.value = await request.get('/roles')

}



async function loadMenus() {

  allMenus.value = await request.get('/menus')

}



function openForm(item) {

  formError.value = ''

  Object.assign(form, { id: null, roleCode: '', roleName: '', description: '', status: 1 })

  if (item) Object.assign(form, item)

  showModal.value = true

}



async function openMenus(item) {

  currentRole.value = item

  selectedMenuIds.value = await request.get(`/roles/${item.id}/menus`)

  showMenuModal.value = true

}



async function save() {

  formError.value = ''

  if (!form.roleCode?.trim() || !form.roleName?.trim()) {

    formError.value = '编码和名称不能为空'

    return

  }

  if (form.id) await request.put(`/roles/${form.id}`, form)

  else await request.post('/roles', form)

  showModal.value = false

  await load()

}



async function saveMenus() {

  await request.put(`/roles/${currentRole.value.id}/menus`, { menuIds: selectedMenuIds.value })

  showMenuModal.value = false

}



async function remove(item) {

  if (!await showConfirm(`确定删除角色「${item.roleName}」？`)) return

  await request.delete(`/roles/${item.id}`)

  await load()

}



onMounted(async () => {

  await load()

  await loadMenus()

})

</script>



<style scoped>

.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }

.page-title { font-size: 20px; font-weight: 600; }

.menu-checks { display: flex; flex-direction: column; gap: 10px; max-height: 320px; overflow-y: auto; margin: 16px 0; }

.check-item { display: flex; align-items: center; gap: 8px; font-size: 14px; cursor: pointer; }

.req { color: #DC2626; }

.err { color: #DC2626; font-size: 13px; }

.empty { text-align: center; color: var(--color-text-secondary); padding: 24px; }

</style>

