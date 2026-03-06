import http from './http';

export const login = (data) => {
  return http.post('/auth/login', data);
};

export const register = (data) => {
  return http.post('/auth/register', data);
};

export const getCurrentUser = () => {
  return http.get('/auth/me');
};
