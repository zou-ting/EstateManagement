import axios from 'axios'

// 使用环境变量配置 baseURL
const baseURL = import.meta.env.VITE_API_BASE_URL 
  ? `${import.meta.env.VITE_API_BASE_URL}/api` 
  : '/api'

const request = axios.create({ baseURL: '/api', timeout: 10000 })

request.interceptors.response.use(
  (res) => {
    const body = res.data
    if (body.code !== 200) return Promise.reject(new Error(body.message || '请求失败'))
    return body.data
  },
  (err) => Promise.reject(err)
)

export default request
