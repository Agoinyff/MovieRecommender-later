<template>
  <div class="panel">
    <header class="panel-head">
      <div>
        <p class="hint">推荐结果</p>
        <h2>Top picks</h2>
      </div>
      <span v-if="loading" class="pill">正在计算...</span>
    </header>

    <p v-if="error" class="error">{{ error }}</p>
    <p v-else-if="!loading && recommendations.length === 0" class="muted">暂无数据，请先提交查询。</p>

    <div v-if="recommendations.length" class="movies-grid">
      <MovieCard
        v-for="item in recommendations"
        :key="item.movieId"
        :movie="{ 
          id: item.movieId, 
          name: item.name, 
          publishedYear: item.publishedYear, 
          genres: item.genres,
          posterUrl: item.posterUrl,
          score: item.score 
        }"
        :show-score="true"
        @click="handleMovieClick(item)"
      />
    </div>
  </div>
</template>

<script setup>
import MovieCard from './MovieCard.vue';

const props = defineProps({
  recommendations: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  error: { type: String, default: '' }
});

const emit = defineEmits(['movie-click']);

const handleMovieClick = (movie) => {
  emit('movie-click', {
    id: movie.movieId,
    movieId: movie.movieId,
    name: movie.name,
    publishedYear: movie.publishedYear,
    genres: movie.genres,
    posterUrl: movie.posterUrl,
    score: movie.score
  });
};

</script>

<style scoped>
.panel {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  padding: 18px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.04);
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.hint {
  margin: 0;
  color: #6b7280;
  font-weight: 600;
  letter-spacing: 0.01em;
}

h2 {
  margin: 4px 0 0;
}

.movies-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
  margin-top: 16px;
}

.pill {
  background: #eef2ff;
  color: #4338ca;
  padding: 6px 10px;
  border-radius: 999px;
  font-weight: 700;
  font-size: 13px;
}

.muted {
  color: #9ca3af;
  margin-top: 12px;
}

.error {
  color: #dc2626;
  background: #fef2f2;
  padding: 10px;
  border-radius: 12px;
  border: 1px solid #fecaca;
  margin-top: 12px;
}

@media (max-width: 768px) {
  .movies-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 16px;
  }
}
</style>

