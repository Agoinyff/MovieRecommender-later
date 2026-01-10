/**
 * 推荐系统 API
 */
import http from './http';

/**
 * 获取个性化推荐
 * @param {Object} params - { userId, strategy, size }
 * strategy: USER_BASED | ITEM_BASED | SLOPE_ONE
 */
export const getRecommendations = (params) => {
  return http.get('/recommendations', { params });
};

/**
 * 获取热门电影（暂未实现，预留接口）
 * @param {Number} size - 返回条数
 */
export const getPopularMovies = (size = 10) => {
  // 如果后端实现了 /recommendations/popular 接口，取消下面的注释
  // return http.get('/recommendations/popular', { params: { size } });
  
  // 临时方案：返回前 N 部电影
  return http.get('/movies', { params: { page: 0, size } });
};

