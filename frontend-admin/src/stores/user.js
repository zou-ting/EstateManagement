import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '../api/request'

export const useUserStore = defineStore('user', () => {
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  const menus = ref(JSON.parse(localStorage.getItem('admin_menus') || '[]'))

  async function fetchMenus(role) {
    const data = await request.get('/menus', { params: { role } })
    menus.value = data
    localStorage.setItem('admin_menus', JSON.stringify(data))
    return data
  }

  async function login(username, password) {
    const data = await request.post('/auth/login', { username, password }, { params: { portal: 'admin' } })
    user.value = data
    localStorage.setItem('user', JSON.stringify(data))
    await fetchMenus(data.role)
    return data
  }

  function logout() {
    user.value = null
    menus.value = []
    localStorage.removeItem('user')
    localStorage.removeItem('admin_menus')
  }

  function getAdminMenuPaths() {
    return menus.value.map(m => '/admin' + m.path)
  }

  // 新增 patchUser 方法
  function patchUser(updates) {
    if (user.value) {
      user.value = { ...user.value, ...updates }
      localStorage.setItem('user', JSON.stringify(user.value))
    }
  }

  return { user, menus, login, logout, fetchMenus, getAdminMenuPaths, patchUser }
})