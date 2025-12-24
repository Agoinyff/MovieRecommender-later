import { defineStore } from 'pinia';
import { searchMovies } from '../api/movies';

export const useMovieLibraryStore = defineStore('movieLibrary', {
  state: () => ({
    query: '',
    page: 0,
    size: 12,
    totalElements: 0,
    totalPages: 0,
    movies: [],
    loading: false,
    error: ''
  }),
  actions: {
    async fetchMovies({ query = this.query, page = this.page, size = this.size } = {}) {
      this.loading = true;
      this.error = '';
      try {
        const data = await searchMovies({ query, page, size });
        this.query = query || '';
        this.page = page;
        this.size = size;
        this.movies = data.content || [];
        this.totalElements = data.totalElements || 0;
        this.totalPages = data.totalPages || 0;
      } catch (error) {
        this.error = error?.response?.data?.message || error.message || '无法获取电影列表';
        this.movies = [];
      } finally {
        this.loading = false;
      }
    }
  }
});
