/**
 * 性能指标监控 API
 */
import http from './http';

/**
 * 获取内存统计信息
 * @returns {Promise<Object>} 包含 totalMemory, usedMemory, freeMemory, maxMemory (单位: bytes)
 */
export const getMemoryStats = () => {
    return http.get('/metrics/memory');
};

/**
 * 获取缓存统计信息
 * @returns {Promise<Object>} 各缓存区域的统计信息（命中率、命中次数、未命中次数、当前大小等）
 */
export const getCacheStats = () => {
    return http.get('/metrics/cache');
};

/**
 * 清除所有缓存
 * @returns {Promise<Object>} 清理结果消息
 */
export const clearAllCaches = () => {
    return http.get('/metrics/cache/clear');
};
