import http from './http';

export const getMovies = (params) => http.get('/movies', { params });
export const getMovieById = (movieId) => http.get(`/movies/${movieId}`);
export const getPopularMovies = (size = 10) => http.get('/movies/popular', { params: { size } });
export const getMovieRecommendations = (movieId, params) => http.get(`/movies/${movieId}/recommendations`, { params });
export const searchMovies = (params) => {
  const { keyword, ...rest } = params;
  return http.get('/movies', { params: { query: keyword, ...rest } });
};
