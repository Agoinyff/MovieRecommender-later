/**
 * 电影相关 API
 */
import http from './http';

/**
 * 获取电影列表（支持搜索和分页）
 * @param {Object} params - { keyword, page, size }
 */
export const getMovies = (params) => {
  return http.get('/movies', { params });
};

/**
 * 根据 ID 获取电影详情
 * @param {Number} movieId 
 */
export const getMovieById = (movieId) => {
  return http.get(`/movies/${movieId}`);
};

/**
 * 搜索电影
 * @param {Object} params - { keyword, page, size }
 */
export const searchMovies = (params) => {
  return http.get('/movies/search', { params });
};

