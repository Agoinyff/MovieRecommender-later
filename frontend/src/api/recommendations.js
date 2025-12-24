import http from './http';

export const fetchRecommendations = async ({ userId, strategy, size }) => {
  const response = await http.get('/recommendations', {
    params: { userId, strategy, size }
  });
  return response.data;
};
