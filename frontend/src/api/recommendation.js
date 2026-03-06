import http from './http';

export const getRecommendationsMe = (params) => http.get('/recommendations/me', { params });
export const getRecommendationsAdmin = (params) => http.get('/recommendations/admin', { params });
export const getPopularRecommendations = (size = 10) => http.get('/recommendations/popular', { params: { size } });
