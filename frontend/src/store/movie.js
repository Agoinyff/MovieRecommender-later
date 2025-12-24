/**
 * 电影状态管理
 */
import { defineStore } from 'pinia';
import { ref } from 'vue';
import { getMovies, searchMovies } from '@/api';

export const useMovieStore = defineStore('movie', () => {
  const movies = ref([]);
  const loading = ref(false);
  const error = ref('');
  const totalElements = ref(0);
  const totalPages = ref(0);
  const currentPage = ref(0);

  // 加载电影列表
  const fetchMovies = async (params = {}) => {
    loading.value = true;
    error.value = '';
    try {
      const data = await getMovies(params);
      movies.value = data.content || [];
      totalElements.value = data.totalElements || 0;
      totalPages.value = data.totalPages || 0;
      currentPage.value = data.number || 0;
    } catch (err) {
      error.value = err.response?.data?.message || err.message || '加载失败';
      movies.value = [];
    } finally {
      loading.value = false;
    }
  };

  // 搜索电影
  const search = async (keyword, page = 0, size = 20) => {
    loading.value = true;
    error.value = '';
    try {
      const data = await searchMovies({ keyword, page, size });
      movies.value = data.content || [];
      totalElements.value = data.totalElements || 0;
      totalPages.value = data.totalPages || 0;
      currentPage.value = data.number || 0;
    } catch (err) {
      error.value = err.response?.data?.message || err.message || '搜索失败';
      movies.value = [];
    } finally {
      loading.value = false;
    }
  };

  return {
    movies,
    loading,
    error,
    totalElements,
    totalPages,
    currentPage,
    fetchMovies,
    search
  };
});

