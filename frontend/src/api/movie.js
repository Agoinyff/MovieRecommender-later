import http from './http';

export const getMovies = (params) => {
  return http.get('/movies', { params });
};

export const getMovieById = (movieId) => {
  return http.get(`/movies/${movieId}`);
};

export const getRelatedMovies = (movieId, size = 10) => {
  return http.get(`/movies/${movieId}/related`, { params: { size } });
};

export const searchMovies = (params) => {
  const { keyword, ...rest } = params;
  return http.get('/movies', {
    params: {
      query: keyword,
      ...rest
    }
  });
};
