<template>
  <Dialog v-model:visible="isVisible" :modal="true" :style="{ width: '680px' }" class="movie-detail-dialog">
    <template #header>
      <div class="dialog-header">
        <span>{{ movie?.name || movie?.movieName || '电影详情' }}</span>
      </div>
    </template>

    <div v-if="movie" class="dialog-content">
      <div class="poster-area">
        <img v-if="movie.posterUrl && !posterError" :src="movie.posterUrl" :alt="movie.name || movie.movieName" class="poster-image" @error="posterError = true" />
        <div v-else class="poster-placeholder"><i class="pi pi-video"></i></div>
      </div>

      <div class="info-area">
        <div class="info-card">
          <p><strong>年份：</strong>{{ movie.publishedYear || '未知' }}</p>
          <p><strong>类型：</strong>{{ movie.genres || '未知' }}</p>
          <p v-if="myRating"><strong>我的评分：</strong>{{ myRating.rating.toFixed(1) }}</p>
          <p v-else><strong>我的评分：</strong>暂未评分</p>
          <p v-if="movie.score"><strong>推荐分：</strong>{{ Number(movie.score).toFixed(2) }}</p>
        </div>

        <div class="actions">
          <button v-if="authStore.isAuthenticated" class="primary-btn" @click="ratingDialogVisible = true">{{ myRating ? '修改评分' : '我要评分' }}</button>
          <router-link class="secondary-btn" :to="`/movies/${movie.id || movie.movieId}`" @click="closeDialog">查看详情页</router-link>
        </div>
      </div>
    </div>
  </Dialog>

  <RatingDialog
    v-model:visible="ratingDialogVisible"
    :movie="movie"
    :initial-rating="myRating?.rating || movie?.userRating || 0"
    @rating-submitted="handleRatingSubmitted"
  />
</template>

<script setup>
import { ref, watch } from 'vue';
import Dialog from 'primevue/dialog';
import { getMyMovieRating } from '@/api';
import { useAuthStore } from '@/store/auth';
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

const authStore = useAuthStore();
const isVisible = ref(props.visible);
const ratingDialogVisible = ref(false);
const posterError = ref(false);
const myRating = ref(null);

watch(
  () => props.visible,
  async (value) => {
    isVisible.value = value;
    posterError.value = false;
    if (value && props.movie && authStore.isAuthenticated) {
      await loadMyRating();
    }
  }
);

watch(isVisible, (value) => emit('update:visible', value));

const closeDialog = () => {
  isVisible.value = false;
};

const loadMyRating = async () => {
  try {
    myRating.value = await getMyMovieRating(props.movie.id || props.movie.movieId);
  } catch (error) {
    myRating.value = null;
  }
};

const handleRatingSubmitted = async (ratingData) => {
  myRating.value = { ...myRating.value, rating: ratingData.rating };
  emit('rating-updated', ratingData);
};
</script>

<style scoped>
.dialog-header {
  font-size: 20px;
  font-weight: 800;
}

.dialog-content {
  display: grid;
  grid-template-columns: 220px 1fr;
  gap: 24px;
}

.poster-area {
  border-radius: 18px;
  overflow: hidden;
  background: linear-gradient(135deg, #f97316, #fbbf24);
  min-height: 320px;
}

.poster-image,
.poster-placeholder {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.poster-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 56px;
}

.info-area {
  display: grid;
  gap: 16px;
}

.info-card {
  padding: 20px;
  border-radius: 18px;
  background: #f8fafc;
}

.info-card p {
  margin: 0 0 12px;
}

.actions {
  display: grid;
  gap: 12px;
}

.primary-btn,
.secondary-btn {
  height: 46px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  text-decoration: none;
  font-weight: 700;
}

.primary-btn {
  border: none;
  background: linear-gradient(135deg, #ea580c, #f59e0b);
  color: #fff;
}

.secondary-btn {
  background: #fff;
  color: #334155;
  border: 1px solid #cbd5e1;
}

@media (max-width: 768px) {
  .dialog-content {
    grid-template-columns: 1fr;
  }
}
</style>
