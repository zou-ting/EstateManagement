import axios from 'axios'

const baseURL = import.meta.env.VITE_API_BASE_URL 
  ? `${import.meta.env.VITE_API_BASE_URL}/api` 
  : '/api'

export async function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  const res = await axios.post('/api/files/upload', formData, {
    timeout: 30000
  })
  const body = res.data
  if (body.code !== 200) {
    throw new Error(body.message || '上传失败')
  }
  return body.data.url
}
