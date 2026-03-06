import http from './http';

export const submitRating = (data) => http.post('/ratings', data);
export const getMyRatings = (params) => http.get('/ratings/me', { params });
export const getMyRatingStats = () => http.get('/ratings/me/stats');
export const getMyMovieRating = (movieId) => http.get(`/ratings/me/movies/${movieId}`);
