<template>
  <div class="movie-card" @click="$emit('click', movie)">
    <div class="movie-poster">
      <div class="poster-placeholder">
        <i class="pi pi-video"></i>
      </div>
      <div v-if="showScore && movie.score" class="score-badge">
        <i class="pi pi-star-fill"></i>
        {{ movie.score.toFixed(1) }}
      </div>
    </div>
    
    <div class="movie-info">
      <h3 class="movie-title" :title="movie.name">{{ movie.name }}</h3>
      <div class="movie-meta">
        <span v-if="movie.publishedYear" class="year">
          <i class="pi pi-calendar"></i>
          {{ movie.publishedYear }}
        </span>
        <span v-if="movie.genres" class="genres">{{ movie.genres }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  movie: {
    type: Object,
    required: true
  },
  showScore: {
    type: Boolean,
    default: false
  }
});

defineEmits(['click']);
</script>

<style scoped>
.movie-card {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(229, 231, 235, 0.6);
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
}

.movie-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 40px rgba(99, 102, 241, 0.15);
  border-color: rgba(99, 102, 241, 0.3);
}

.movie-poster {
  position: relative;
  width: 100%;
  aspect-ratio: 2/3;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  overflow: hidden;
}

.poster-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.9);
  font-size: 48px;
}

.score-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 6px 12px;
  border-radius: 20px;
  font-weight: 700;
  color: #f59e0b;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 4px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.score-badge i {
  font-size: 12px;
}

.movie-info {
  padding: 16px;
}

.movie-title {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 44px;
}

.movie-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 13px;
  color: #6b7280;
}

.year {
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 600;
  color: #4b5563;
}

.year i {
  font-size: 12px;
}

.genres {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  font-size: 12px;
}
</style>

