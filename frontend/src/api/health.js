/**
 * 系统健康检查 API
 */
import http from './http';

/**
 * 检查系统运行状态
 * @returns {Promise<Object>} { status: "ok" }
 */
export const getSystemStatus = () => {
    return http.get('/health/status');
};

/**
 * 查询数据库评分总数
 * @returns {Promise<Object>} { totalRatings: number, status: "success" }
 */
export const getRatingCount = () => {
    return http.get('/health/rating-count');
};
