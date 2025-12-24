import http from './http';

export const searchMovies = async ({ query, page = 0, size = 12 }) => {
  const response = await http.get('/movies', {
    params: {
      query: query || undefined,
      page,
      size
    }
  });
  return response.data;
};

export const getMovieById = async (id) => {
  const response = await http.get(`/movies/${id}`);
  return response.data;
};
