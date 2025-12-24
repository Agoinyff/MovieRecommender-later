import axios from 'axios';

const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080/api';

const http = axios.create({
  baseURL: API_BASE,
  timeout: 10000
});

export default http;
