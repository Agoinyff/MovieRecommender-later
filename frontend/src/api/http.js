/**
 * Axios instance and interceptors
 */
import axios from 'axios';

const TOKEN_KEY = 'movie-recommender-token';
const USER_KEY = 'movie-recommender-user';

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || 'http://localhost:8080/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
});

http.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem(TOKEN_KEY);
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

http.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const status = error.response?.status;
    if (status === 401) {
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
      if (!window.location.pathname.startsWith('/auth')) {
        const redirect = encodeURIComponent(window.location.pathname + window.location.search);
        window.location.href = `/auth?redirect=${redirect}`;
      }
    }
    const message = error.response?.data?.message || error.message || '请求失败';
    console.error('API Error:', message);
    return Promise.reject(error);
  }
);

export default http;
