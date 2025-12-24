<template>
  <div class="movies-page">
    <div class="page-container">
      <header class="page-header">
        <div>
          <h1 class="page-title">电影库</h1>
          <p class="page-description">浏览和搜索海量电影资源</p>
        </div>
        <div v-if="totalElements > 0" class="stats-badge">
          <i class="pi pi-database"></i>
          <span>共 {{ totalElements }} 部电影</span>
        </div>
      </header>

      <div class="search-bar">
        <div class="search-input-wrapper">
          <i class="pi pi-search"></i>
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索电影名称、类型..."
            @keyup.enter="handleSearch"
            class="search-input"
          />
          <button v-if="searchKeyword" @click="clearSearch" class="clear-btn">
            <i class="pi pi-times"></i>
          </button>
        </div>
        <button @click="handleSearch" class="search-btn" :disabled="loading">
          <i class="pi pi-search"></i>
          <span>搜索</span>
        </button>
      </div>

      <LoadingSpinner v-if="loading" message="加载中..." />

      <div v-else-if="error" class="error-message">
        <i class="pi pi-exclamation-triangle"></i>
        <p>{{ error }}</p>
      </div>

      <div v-else-if="movies.length === 0" class="empty-state">
        <i class="pi pi-inbox"></i>
        <p>暂无电影数据</p>
      </div>

      <Transition name="fade">
        <div v-if="movies.length > 0" class="movies-grid">
          <TransitionGroup name="movie-list">
            <MovieCard
              v-for="movie in movies"
              :key="movie.movieId"
              :movie="movie"
              @click="showMovieDetail"
            />
          </TransitionGroup>
        </div>
      </Transition>

      <div v-if="totalPages > 1" class="pagination-wrapper">
        <Paginator
          :rows="pageSize"
          :totalRecords="totalElements"
          :first="currentPage * pageSize"
          @page="onPageChange"
          :pageLinkSize="5"
          template="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink"
        />
      </div>
    </div>

    <MovieDetailDialog
      v-model:visible="dialogVisible"
      :movie="selectedMovie"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useMovieStore } from '@/store/movie';
import MovieCard from '@/components/MovieCard.vue';
import MovieDetailDialog from '@/components/MovieDetailDialog.vue';
import LoadingSpinner from '@/components/LoadingSpinner.vue';
import Paginator from 'primevue/paginator';

const movieStore = useMovieStore();
const searchKeyword = ref('');
const pageSize = ref(20);
const dialogVisible = ref(false);
const selectedMovie = ref(null);

const movies = computed(() => movieStore.movies);
const loading = computed(() => movieStore.loading);
const error = computed(() => movieStore.error);
const totalElements = computed(() => movieStore.totalElements);
const totalPages = computed(() => movieStore.totalPages);
const currentPage = computed(() => movieStore.currentPage);

const loadMovies = (page = 0) => {
  movieStore.fetchMovies({ page, size: pageSize.value });
};

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    movieStore.search(searchKeyword.value.trim(), 0, pageSize.value);
  } else {
    loadMovies(0);
  }
};

const clearSearch = () => {
  searchKeyword.value = '';
  loadMovies(0);
};

const onPageChange = (event) => {
  const page = event.page;
  if (searchKeyword.value.trim()) {
    movieStore.search(searchKeyword.value.trim(), page, pageSize.value);
  } else {
    loadMovies(page);
  }
  window.scrollTo({ top: 0, behavior: 'smooth' });
};

const showMovieDetail = (movie) => {
  selectedMovie.value = movie;
  dialogVisible.value = true;
};

onMounted(() => {
  loadMovies();
});
</script>

<style scoped>
.movies-page {
  min-height: calc(100vh - 64px);
  background: linear-gradient(180deg, rgba(249, 250, 251, 0.5) 0%, rgba(255, 255, 255, 0.8) 100%);
}

.page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 32px;
  gap: 20px;
}

.page-title {
  margin: 0 0 8px;
  font-size: 42px;
  font-weight: 800;
  color: #1f2937;
}

.page-description {
  margin: 0;
  font-size: 16px;
  color: #6b7280;
}

.stats-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: rgba(99, 102, 241, 0.1);
  border: 1px solid rgba(99, 102, 241, 0.2);
  border-radius: 20px;
  color: #4f46e5;
  font-weight: 700;
  white-space: nowrap;
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 32px;
}

.search-input-wrapper {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
}

.search-input-wrapper > i {
  position: absolute;
  left: 18px;
  color: #9ca3af;
  font-size: 18px;
  pointer-events: none;
}

.search-input {
  width: 100%;
  height: 52px;
  padding: 0 48px 0 52px;
  border: 2px solid rgba(229, 231, 235, 0.6);
  border-radius: 16px;
  font-size: 16px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.search-input:focus {
  outline: none;
  border-color: #6366f1;
  box-shadow: 0 0 0 4px rgba(99, 102, 241, 0.1);
}

.clear-btn {
  position: absolute;
  right: 12px;
  width: 32px;
  height: 32px;
  border: none;
  background: rgba(156, 163, 175, 0.1);
  border-radius: 8px;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.clear-btn:hover {
  background: rgba(156, 163, 175, 0.2);
  color: #374151;
}

.search-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 28px;
  height: 52px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  border: none;
  border-radius: 16px;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 16px rgba(99, 102, 241, 0.3);
}

.search-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(99, 102, 241, 0.4);
}

.search-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.movies-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 24px;
  margin-bottom: 40px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}

.error-message {
  text-align: center;
  padding: 60px 20px;
  color: #dc2626;
}

.error-message i {
  font-size: 48px;
  margin-bottom: 16px;
}

.error-message p {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: #9ca3af;
}

.empty-state i {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-state p {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
}

/* 动画效果 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.movie-list-move,
.movie-list-enter-active {
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.movie-list-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.movie-list-leave-active {
  position: absolute;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
  }

  .page-title {
    font-size: 32px;
  }

  .search-bar {
    flex-direction: column;
  }

  .search-btn {
    width: 100%;
    justify-content: center;
  }

  .movies-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 16px;
  }
}
</style>

