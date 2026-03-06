import http from './http';

export const getMemoryStats = () => http.get('/metrics/memory');
export const getCacheStats = () => http.get('/metrics/cache');
export const clearAllCaches = () => http.get('/metrics/cache/clear');
