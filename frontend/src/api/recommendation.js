import http from './http';

export const getRecommendations = (params = {}) => {
  return http.get('/recommendations', { params });
};

export const getPopularMovies = (size = 10) => {
  return http.get('/movies', { params: { page: 0, size } });
};
