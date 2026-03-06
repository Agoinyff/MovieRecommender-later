import http from './http';

export const getSystemStatus = () => {
  return http.get('/health/status');
};

export const getRatingCount = () => {
  return http.get('/health/rating-count');
};
