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
 * 获取热门电影
 * @param {Number} size - 返回条数
 */
export const getPopularMovies = (size = 10) => {
  return http.get('/recommendations/popular', { params: { size } });
};

