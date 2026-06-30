import { defineStore } from 'pinia'

import { ref } from 'vue'

import request from '../api/request'



export const useUserStore = defineStore('user', () => {
  const user = ref(null)
  const unpaidCount = ref(0)

  function _initUser() {
    try {
      const saved = localStorage.getItem('client_user')
      if (saved) {
        user.value = JSON.parse(saved)
      }
    } catch (e) {
      console.error('Failed to load user from localStorage:', e)
    }
  }

  _initUser()

  async function login(username, password) {
    const data = await request.post('/auth/login', { username, password }, { params: { portal: 'client' } })
    user.value = data
    try {
      localStorage.setItem('client_user', JSON.stringify(data))
    } catch (e) {
      console.error('Failed to save user to localStorage:', e)
    }
    return data
  }




  async function register(payload) {

    return request.post('/auth/register', payload)

  }




  function logout() {
    user.value = null
    try {
      localStorage.removeItem('client_user')
    } catch (e) {
      console.error('Failed to remove user from localStorage:', e)
    }
  }

  function patchUser(partial) {
    if (!user.value) return
    user.value = { ...user.value, ...partial }
  }

  function setUnpaidCount(count) {
    unpaidCount.value = count
  }

  async function fetchUnpaidCount() {
    const ownerId = user.value?.ownerId
    if (!ownerId) {
      unpaidCount.value = 0
      return
    }
    try {
      const owner = await request.get(`/owners/${ownerId}`)
      if (owner?.roomId) {
        const fees = await request.get('/property-fees', { params: { roomId: owner.roomId } })
        const unpaid = fees.filter(f => f.payStatus !== 'PAID').length
        unpaidCount.value = unpaid
      } else {
        unpaidCount.value = 0
      }
    } catch (e) {
      console.error('获取待缴账单失败', e)
      unpaidCount.value = 0
    }
  }

  return { user, login, register, logout, patchUser, unpaidCount, setUnpaidCount, fetchUnpaidCount }
})

