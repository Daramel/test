import axios, { AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { getToken } from '@/utils/auth'
import router from '@/router'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000
})

request.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  (response) => {
    const { code, message } = response.data
    if (code === 200 || code === 0) {
      return response.data
    }
    ElMessage.error(message || '请求失败')
    return Promise.reject(new Error(message))
  },
  (error) => {
    if (error.response) {
      const { status, data } = error.response
      if (status === 401) {
        ElMessage.error('登录已过期')
        router.push({ name: 'Login' })
      } else if (status === 403) {
        ElMessage.error('没有权限')
      } else if (status === 500) {
        ElMessage.error('服务器错误')
      }
    }
    return Promise.reject(error)
  }
)

export const createRequest = () => {
  const requestFn = <T = any>(config: AxiosRequestConfig): Promise<T> => {
    return request(config)
  }

  return {
    get: <T = any>(url: string, config?: AxiosRequestConfig) => requestFn<T>({ ...config, url, method: 'GET' }),
    post: <T = any>(url: string, data?: any, config?: AxiosRequestConfig) =>
      requestFn<T>({ ...config, url, method: 'POST', data }),
    put: <T = any>(url: string, data?: any, config?: AxiosRequestConfig) =>
      requestFn<T>({ ...config, url, method: 'PUT', data }),
    delete: <T = any>(url: string, config?: AxiosRequestConfig) =>
      requestFn<T>({ ...config, url, method: 'DELETE' })
  }
}

export default request
