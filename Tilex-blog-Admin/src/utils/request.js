import axios from "axios";

// 创建 axios 实例
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL, // API 根地址
  timeout: 5000, // 请求超时时间
});

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 可以在这里加 token 或其他公共请求配置
    // const token = localStorage.getItem('token');
    // if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
  },
  (error) => Promise.reject(error)
);

// 响应拦截器
request.interceptors.response.use(
  (response) => response.data, // 直接返回 data
  (error) => {
    console.error("请求出错:", error);
    return Promise.reject(error);
  }
);

export default request;
