<template>
  <div class="movies-page">
    <div class="page-container">
      <header class="page-header">
        <div>
          <h1>电影库</h1>
          <p>按片名搜索、浏览热门电影，进入详情页继续评分和查看推荐。</p>
        </div>
      </header>

      <div class="search-bar">
        <input v-model="searchKeyword" @keyup.enter="handleSearch" type="text" placeholder="搜索电影名" />
        <button @click="handleSearch">搜索</button>
        <button v-if="searchKeyword" class="ghost" @click="clearSearch">清空</button>
      </div>

      <LoadingSpinner v-if="loading" message="正在加载电影..." />
      <div v-else-if="error" class="state error">{{ error }}</div>
      <div v-else class="movies-grid">
        <MovieCard v-for="movie in movies" :key="movie.id || movie.movieId" :movie="movie" @click="showMovieDetail" />
      </div>

      <div v-if="totalPages > 1" class="pagination">
        <button :disabled="currentPage === 0" @click="onPageChange(currentPage - 1)">上一页</button>
        <span>第 {{ currentPage + 1 }} / {{ totalPages }} 页</span>
        <button :disabled="currentPage + 1 >= totalPages" @click="onPageChange(currentPage + 1)">下一页</button>
      </div>
    </div>

    <MovieDetailDialog v-model:visible="dialogVisible" :movie="selectedMovie" />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useMovieStore } from '@/store/movie';
import LoadingSpinner from '@/components/LoadingSpinner.vue';
import MovieCard from '@/components/MovieCard.vue';
import MovieDetailDialog from '@/components/MovieDetailDialog.vue';

const movieStore = useMovieStore();
const searchKeyword = ref('');
const dialogVisible = ref(false);
const selectedMovie = ref(null);
const pageSize = 20;

const movies = computed(() => movieStore.movies);
const loading = computed(() => movieStore.loading);
const error = computed(() => movieStore.error);
const totalPages = computed(() => movieStore.totalPages);
const currentPage = computed(() => movieStore.currentPage);

const loadMovies = (page = 0) => movieStore.fetchMovies({ page, size: pageSize });

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    movieStore.search(searchKeyword.value.trim(), 0, pageSize);
  } else {
    loadMovies(0);
  }
};

const clearSearch = () => {
  searchKeyword.value = '';
  loadMovies(0);
};

const onPageChange = (page) => {
  if (searchKeyword.value.trim()) {
    movieStore.search(searchKeyword.value.trim(), page, pageSize);
  } else {
    loadMovies(page);
  }
};

const showMovieDetail = (movie) => {
  selectedMovie.value = movie;
  dialogVisible.value = true;
};

onMounted(() => loadMovies());
</script>

<style scoped>
.movies-page {
  min-height: calc(100vh - 68px);
  background: linear-gradient(180deg, #fff7ed, #ffffff);
}

.page-container {
  max-width: 1240px;
  margin: 0 auto;
  padding: 40px 24px;
}

.page-header h1 {
  margin: 0 0 8px;
  font-size: 44px;
}

.page-header p {
  margin: 0 0 24px;
  color: #64748b;
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.search-bar input {
  flex: 1;
  height: 48px;
  border-radius: 16px;
  border: 1px solid #cbd5e1;
  padding: 0 16px;
}

.search-bar button {
  height: 48px;
  padding: 0 18px;
  border-radius: 16px;
  border: none;
  background: #ea580c;
  color: #fff;
  font-weight: 700;
}

.search-bar .ghost {
  background: #fff;
  color: #475569;
  border: 1px solid #cbd5e1;
}

.movies-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 14px;
  margin-top: 28px;
}

.pagination button {
  height: 42px;
  padding: 0 14px;
  border-radius: 12px;
  border: 1px solid #cbd5e1;
  background: #fff;
}

.state.error {
  color: #dc2626;
}
</style>
