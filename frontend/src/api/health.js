import http from './http';

export const getSystemStatus = () => http.get('/health/status');
export const getRatingCount = () => http.get('/health/rating-count');
