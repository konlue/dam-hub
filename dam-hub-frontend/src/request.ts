import axios from "axios";
import { ElMessage } from "element-plus";

// 区分开发和生产环境
const DEV_BASE_URL = "http://localhost:8123";
// const PROD_BASE_URL = "http://119.29.249.72";
// 创建 Axios 实例
const myAxios = axios.create({
    baseURL: DEV_BASE_URL,
    timeout: 10000,
    withCredentials: true,
});

// 全局请求拦截器
myAxios.interceptors.request.use(
  function (config) {
    return config
  },
  function (error) {
    return Promise.reject(error)
  },
)

// 全局响应拦截器
myAxios.interceptors.response.use(
  function (response) {
    const { data } = response
    // 未登录
    if (data.code === 40100) {
      if (
        !response.request.responseURL.includes('user/get/login') &&
        !window.location.pathname.includes('/user/login')
      ) {
        ElMessage.warning('请先登录')
        window.location.href = `/user/login?redirect=${window.location.href}`
      }
    }
    return response
  },
  function (error) {
    return Promise.reject(error)
  },
)

export default myAxios;
