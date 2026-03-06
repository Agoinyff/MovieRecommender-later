import http from './http';

export const getAdminStats = () => http.get('/admin/stats');
export const getAdminUsers = (params) => http.get('/admin/users', { params });
