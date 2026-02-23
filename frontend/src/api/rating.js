/**
 * 评分相关 API
 */
import http from './http';

/**
 * 提交用户评分
 * @param {Object} data - { userId, movieId, rating }
 * @returns {Promise<string>} 返回成功消息
 */
export const submitRating = (data) => {
  return http.post('/ratings', data);
};

/**
 * 获取指定用户的所有评分记录
 * @param {Number} userId - 用户 ID
 * @returns {Promise<Array>} 评分列表，每项包含 userId, movieId, movieName, rating, timestamp
 */
export const getUserRatings = (userId) => {
  return http.get(`/ratings/user/${userId}`);
};
