import axios from 'axios'

const request = axios.create({
    baseURL: '/api',
    timeout: 10000
})

// ✅ 请求拦截器：自动携带 token
request.interceptors.request.use(
    (config) => {
        const userData = localStorage.getItem('user')
        if (userData) {
            try {
                const user = JSON.parse(userData)
                if (user && user.token) {
                    config.headers.Authorization = 'Bearer ' + user.token
                }
            } catch (e) {
                console.log('解析用户数据失败:', e)
            }
        }
        return config
    },
    (error) => Promise.reject(error)
)

request.interceptors.response.use(
    (res) => {
        const body = res.data
        if (body.code !== 200) {
            return Promise.reject(new Error(body.message || '请求失败'))
        }
        return body.data
    },
    (err) => {
        // 如果是 401 未认证，跳转登录
        if (err.response && err.response.status === 401) {
            localStorage.removeItem('user')
            localStorage.removeItem('admin_menus')
            window.location.href = '/login'
        }
        return Promise.reject(err)
    }
)

export default request