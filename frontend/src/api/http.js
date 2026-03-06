import axios from 'axios';

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || 'http://localhost:8080/api',
  timeout: 30000,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json'
  }
});

http.interceptors.response.use(
  (response) => response.data,
  (error) => Promise.reject(error)
);

export default http;
