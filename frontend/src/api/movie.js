/**
 * 电影相关 API
 */
import http from './http';

/**
 * 获取电影列表（支持搜索和分页）
 * @param {Object} params - { query, page, size }
 * query - 电影名称关键字（可选）
 * page - 页码，从 0 开始（默认 0）
 * size - 每页条数（默认 20）
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
 * 搜索电影（实际调用 /movies 接口，通过 query 参数实现）
 * @param {Object} params - { keyword, page, size }
 */
export const searchMovies = (params) => {
  // 将 keyword 参数映射为后端的 query 参数
  const { keyword, ...rest } = params;
  return http.get('/movies', { 
    params: { 
      query: keyword,
      ...rest 
    } 
  });
};

