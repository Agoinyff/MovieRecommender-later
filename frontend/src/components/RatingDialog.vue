<template>
  <div v-if="visible" class="dialog-overlay" @click.self="closeDialog">
    <div class="dialog-container">
      <div class="dialog-header">
        <h3 class="dialog-title">为电影评分</h3>
        <button @click="closeDialog" class="close-btn">
          <i class="pi pi-times"></i>
        </button>
      </div>

      <div class="dialog-body">
        <div v-if="movie" class="movie-info">
          <h4 class="movie-name">{{ movie.name || movie.movieName }}</h4>
          <p v-if="movie.publishedYear" class="movie-meta">{{ movie.publishedYear }}</p>
          <p v-if="movie.genres" class="movie-genres">{{ movie.genres }}</p>
        </div>

        <div class="rating-section">
          <p class="rating-label">你的评分</p>
          <RatingStars v-model="currentRating" :size="40" />
          <p v-if="currentRating > 0" class="rating-text">{{ getRatingText(currentRating) }}</p>
        </div>
      </div>

      <div class="dialog-footer">
        <button @click="closeDialog" class="btn btn-secondary">取消</button>
        <button @click="handleSubmit" :disabled="currentRating === 0 || loading" class="btn btn-primary">
          {{ loading ? '提交中...' : '提交评分' }}
        </button>
      </div>

      <p v-if="error" class="message error">{{ error }}</p>
      <p v-if="success" class="message success">评分已更新</p>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue';
import { submitRating } from '@/api';
import RatingStars from './RatingStars.vue';

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  movie: {
    type: Object,
    default: null
  },
  initialRating: {
    type: Number,
    default: 0
  }
});

const emit = defineEmits(['update:visible', 'rating-submitted']);

const currentRating = ref(0);
const loading = ref(false);
const error = ref('');
const success = ref(false);

watch(
  () => props.visible,
  (visible) => {
    if (visible) {
      currentRating.value = props.initialRating || props.movie?.userRating || 0;
      error.value = '';
      success.value = false;
    }
  }
);

const getRatingText = (rating) => {
  const texts = {
    1: '不喜欢',
    2: '一般',
    3: '还不错',
    4: '很喜欢',
    5: '强烈推荐'
  };
  return texts[rating] || '';
};

const closeDialog = () => emit('update:visible', false);

const handleSubmit = async () => {
  if (!props.movie) {
    return;
  }
  loading.value = true;
  error.value = '';
  try {
    await submitRating({
      movieId: props.movie.id || props.movie.movieId,
      rating: currentRating.value
    });
    success.value = true;
    emit('rating-submitted', {
      movieId: props.movie.id || props.movie.movieId,
      rating: currentRating.value
    });
    setTimeout(closeDialog, 800);
  } catch (err) {
    error.value = err.response?.data?.message || err.message || '提交评分失败';
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.dialog-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.55);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1200;
}

.dialog-container {
  width: min(92vw, 460px);
  background: #fff;
  border-radius: 24px;
  padding: 24px;
}

.dialog-header,
.dialog-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.dialog-body {
  padding: 20px 0;
}

.close-btn {
  border: none;
  background: #f1f5f9;
  width: 36px;
  height: 36px;
  border-radius: 10px;
}

.movie-info {
  text-align: center;
  margin-bottom: 20px;
}

.movie-name {
  margin: 0 0 6px;
  font-size: 22px;
}

.movie-meta,
.movie-genres,
.rating-label,
.rating-text {
  margin: 0;
  color: #64748b;
}

.rating-section {
  display: grid;
  justify-items: center;
  gap: 12px;
  padding: 24px;
  border-radius: 18px;
  background: #fff7ed;
}

.btn {
  flex: 1;
  height: 46px;
  border-radius: 14px;
  border: none;
  font-weight: 700;
  cursor: pointer;
}

.btn-secondary {
  background: #e2e8f0;
  color: #334155;
}

.btn-primary {
  background: linear-gradient(135deg, #ea580c, #f59e0b);
  color: #fff;
}

.message {
  margin: 16px 0 0;
  text-align: center;
  font-size: 14px;
}

.error {
  color: #dc2626;
}

.success {
  color: #16a34a;
}
</style>
