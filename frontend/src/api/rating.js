import http from './http';

export const submitRating = (data) => {
  return http.post('/ratings', data);
};

export const getMyRatings = () => {
  return http.get('/ratings/me');
};

export const getMyMovieRating = (movieId) => {
  return http.get(`/ratings/me/movie/${movieId}`);
};

export const getUserRatings = (userId) => {
  return http.get(`/ratings/user/${userId}`);
};
