<template>
  <Dialog
    v-model:visible="isVisible"
    :modal="true"
    :closable="true"
    :dismissableMask="true"
    :style="{ width: '600px' }"
    class="movie-detail-dialog"
  >
    <template #header>
      <div class="dialog-header">
        <i class="pi pi-film"></i>
        <span>电影详情</span>
      </div>
    </template>

    <div v-if="movie" class="dialog-content">
      <div class="detail-poster">
        <div class="poster-wrapper">
          <img 
            v-if="movie.posterUrl && !posterError" 
            :src="movie.posterUrl" 
            :alt="movie.name || movie.movieName"
            class="poster-image"
            @error="handlePosterError"
          />
          <i v-else class="pi pi-video"></i>
        </div>
      </div>

      <div class="detail-info">
        <h2 class="detail-title">{{ movie.name || movie.movieName }}</h2>
        
        <div class="detail-row" v-if="movie.publishedYear">
          <span class="label">上映年份</span>
          <span class="value">{{ movie.publishedYear }}</span>
        </div>

        <div class="detail-row" v-if="movie.genres">
          <span class="label">类型</span>
          <span class="value">{{ movie.genres }}</span>
        </div>

        <div class="detail-row" v-if="movie.movieId || movie.id">
          <span class="label">电影 ID</span>
          <span class="value">{{ movie.movieId || movie.id }}</span>
        </div>

        <div class="detail-row" v-if="movie.score">
          <span class="label">推荐指数</span>
          <div class="score-display">
            <Rating :modelValue="Math.round(movie.score)" :readonly="true" :cancel="false" :stars="5" />
            <span class="score-value">{{ movie.score.toFixed(2) }}</span>
          </div>
        </div>

        <div class="detail-row" v-if="movie.userRating">
          <span class="label">我的评分</span>
          <div class="score-display">
            <RatingStars :modelValue="movie.userRating" :readonly="true" :size="20" />
            <span class="score-value">{{ movie.userRating.toFixed(1) }}</span>
          </div>
        </div>
      </div>

      <div class="rating-action">
        <button @click="openRatingDialog" class="rate-btn">
          <i class="pi pi-star"></i>
          <span>{{ movie.userRating ? '修改评分' : '为这部电影评分' }}</span>
        </button>
      </div>
    </div>

    <template #footer>
      <Button label="关闭" icon="pi pi-times" @click="closeDialog" class="p-button-text" />
    </template>
  </Dialog>

  <RatingDialog
    v-model:visible="ratingDialogVisible"
    :movie="movie"
    :userId="100"
    @rating-submitted="handleRatingSubmitted"
  />
</template>

<script setup>
import { ref, watch } from 'vue';
import Dialog from 'primevue/dialog';
import Button from 'primevue/button';
import Rating from 'primevue/rating';
import RatingStars from './RatingStars.vue';
import RatingDialog from './RatingDialog.vue';

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  movie: {
    type: Object,
    default: null
  }
});

const emit = defineEmits(['update:visible', 'rating-updated']);

const isVisible = ref(props.visible);
const ratingDialogVisible = ref(false);
const posterError = ref(false);

watch(() => props.visible, (newVal) => {
  isVisible.value = newVal;
  // Reset poster error when dialog opens
  if (newVal) {
    posterError.value = false;
  }
});

watch(isVisible, (newVal) => {
  emit('update:visible', newVal);
});

const closeDialog = () => {
  isVisible.value = false;
};

const openRatingDialog = () => {
  ratingDialogVisible.value = true;
};

const handlePosterError = () => {
  posterError.value = true;
};

const handleRatingSubmitted = (ratingData) => {
  emit('rating-updated', ratingData);
  // Optionally update local movie data
  if (props.movie) {
    props.movie.userRating = ratingData.rating;
  }
};
</script>

<style scoped>
.dialog-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
}

.dialog-header i {
  font-size: 24px;
  color: #6366f1;
}

.dialog-content {
  display: grid;
  gap: 24px;
}

.detail-poster {
  display: flex;
  justify-content: center;
}

.poster-wrapper {
  width: 200px;
  aspect-ratio: 2/3;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.9);
  font-size: 64px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  overflow: hidden;
}

.poster-wrapper .poster-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-info {
  display: grid;
  gap: 16px;
}

.detail-title {
  margin: 0;
  font-size: 24px;
  font-weight: 800;
  color: #1f2937;
  text-align: center;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: rgba(99, 102, 241, 0.05);
  border-radius: 12px;
  border: 1px solid rgba(99, 102, 241, 0.1);
}

.label {
  font-weight: 600;
  color: #4b5563;
}

.value {
  font-weight: 500;
  color: #1f2937;
}

.score-display {
  display: flex;
  align-items: center;
  gap: 12px;
}

.score-value {
  font-weight: 700;
  font-size: 18px;
  color: #f59e0b;
}

.rating-action {
  margin-top: 8px;
}

.rate-btn {
  width: 100%;
  height: 52px;
  background: linear-gradient(135deg, #fbbf24, #f59e0b);
  color: #ffffff;
  border: none;
  border-radius: 14px;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 16px rgba(251, 191, 36, 0.3);
}

.rate-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(251, 191, 36, 0.4);
}

.rate-btn i {
  font-size: 20px;
}
</style>

