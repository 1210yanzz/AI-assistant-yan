import axios from 'axios';
import { ElMessage } from 'element-plus';

// 创建axios实例
const service = axios.create({
  baseURL: '/api', // api 的 base_url（前缀）
  timeout: 5000, // 请求超时时间
})

// request拦截器（请求拦截器）
service.interceptors.request.use(
  config => {
    // 发送请求之前做些什么
    let token = localStorage.getItem('token')  // 获取token
    if (token) { // 判断是否存在token，如果存在的话，则每个http header都加上token
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    // 请求错误做些什么
    console.log(error) // for debug
    return Promise.reject(error)
  }
)

// 添加响应拦截器
service.interceptors.response.use(
  response => {
    // 响应成功做些什么
    const { data , config } = response  // 解构
    if (data.code === '200') {   // 处理业务码状态
      return data.data
    } else { 
      if (data.code === '-1') { 
        // 跳转登录页面,重新登录
        if (!config.url.includes('/login')) { 
          ElMessage.error(data.message || '登录过期，请重新登录')
          // 清除登录信息
          localStorage.removeItem('token')
          localStorage.removeItem('userInfo')
          // 跳转登录页面
          window.location.href = '/auth/login'
        } else { 
          ElMessage.error(data.message || '登录失败')
          return Promise.reject('请求失败.....')
        }
      }
    }
    return response.data
  },
  error => {
    // 响应错误做些什么
    console.log(error) // for debug
    return Promise.reject(error)
  }
)

export default service