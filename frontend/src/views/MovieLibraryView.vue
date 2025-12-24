<template>
  <section class="page">
    <header class="hero">
      <div>
        <p class="eyebrow">Movie library</p>
        <h1>电影库检索 / 浏览</h1>
        <p class="lede">输入关键词快速定位电影，分页浏览完整片库。</p>
      </div>
    </header>

    <div class="toolbar">
      <div class="search">
        <input v-model="searchInput" placeholder="搜索电影名称" @keyup.enter="handleSearch" />
        <button @click="handleSearch" :disabled="store.loading">搜索</button>
      </div>
      <div class="meta">
        <span>共 {{ store.totalElements }} 部电影</span>
        <span>每页 {{ store.size }} 部</span>
      </div>
    </div>

    <p v-if="store.error" class="error">{{ store.error }}</p>
    <p v-else-if="!store.loading && store.movies.length === 0" class="muted">暂无结果，请调整关键词。</p>

    <div class="grid">
      <MovieCard
        v-for="movie in store.movies"
        :key="movie.id"
        :movie="movie"
        @select="openDetail"
      />
    </div>

    <Pagination :page="store.page" :total-pages="store.totalPages" @change="changePage" />

    <MovieDetailDialog
      :visible="detailVisible"
      :movie="selectedMovie"
      :loading="detailLoading"
      :error="detailError"
      @close="closeDetail"
    />
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { useMovieLibraryStore } from '../store/movieLibrary';
import MovieCard from '../components/MovieCard.vue';
import Pagination from '../components/Pagination.vue';
import MovieDetailDialog from '../components/MovieDetailDialog.vue';
import { getMovieById } from '../api/movies';

const store = useMovieLibraryStore();
const searchInput = ref(store.query);

const detailVisible = ref(false);
const detailLoading = ref(false);
const selectedMovie = ref(null);
const detailError = ref('');

const handleSearch = () => {
  store.fetchMovies({ query: searchInput.value, page: 0 });
};

const changePage = (page) => {
  store.fetchMovies({ query: store.query, page });
};

const openDetail = async (movie) => {
  detailVisible.value = true;
  detailLoading.value = true;
  detailError.value = '';
  try {
    selectedMovie.value = await getMovieById(movie.id);
  } catch (err) {
    detailError.value = err?.response?.data?.message || err.message || '无法获取电影详情';
    selectedMovie.value = movie;
  } finally {
    detailLoading.value = false;
  }
};

const closeDetail = () => {
  detailVisible.value = false;
  detailError.value = '';
};

onMounted(() => {
  store.fetchMovies();
});
</script>

<style scoped>
.page {
  max-width: 1200px;
  margin: 0 auto;
}

.hero {
  margin-bottom: 16px;
}

.eyebrow {
  color: rgba(129, 140, 248, 0.95);
  font-weight: 700;
  letter-spacing: 0.02em;
  margin: 0 0 4px;
}

h1 {
  margin: 0 0 8px;
  color: #e2e8f0;
}

.lede {
  margin: 0;
  color: rgba(226, 232, 240, 0.7);
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  padding: 16px;
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 18px;
  margin-bottom: 18px;
  backdrop-filter: blur(16px);
}

.search {
  display: flex;
  gap: 10px;
}

input {
  height: 42px;
  border-radius: 12px;
  border: 1px solid rgba(148, 163, 184, 0.4);
  padding: 0 12px;
  font-size: 15px;
  background: rgba(15, 23, 42, 0.25);
  color: #e2e8f0;
}

input:focus {
  outline: none;
  border-color: #a5b4fc;
  box-shadow: 0 0 0 3px rgba(129, 140, 248, 0.25);
}

button {
  height: 42px;
  border-radius: 999px;
  padding: 0 18px;
  background: linear-gradient(135deg, #6366f1, #38bdf8);
  border: none;
  color: #fff;
  font-weight: 600;
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.meta {
  display: flex;
  gap: 12px;
  color: rgba(226, 232, 240, 0.7);
  font-size: 14px;
}

.grid {
  display: grid;
  gap: 18px;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
}

.muted {
  color: rgba(226, 232, 240, 0.6);
}

.error {
  color: #fecaca;
  background: rgba(248, 113, 113, 0.15);
  padding: 12px;
  border-radius: 12px;
  border: 1px solid rgba(248, 113, 113, 0.4);
}
</style>
