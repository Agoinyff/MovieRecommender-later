<template>
  <div v-if="visible" class="dialog-overlay" @click.self="closeDialog">
    <div class="dialog-container">
      <div class="dialog-header">
        <h3 class="dialog-title">
          <i class="pi pi-star"></i>
          <span>为电影评分</span>
        </h3>
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
          <RatingStars v-model="currentRating" :size="48" />
          <p v-if="currentRating > 0" class="rating-text">
            {{ getRatingText(currentRating) }}
          </p>
        </div>
      </div>

      <div class="dialog-footer">
        <button @click="closeDialog" class="btn btn-secondary">
          取消
        </button>
        <button 
          @click="handleSubmit" 
          :disabled="currentRating === 0 || loading"
          class="btn btn-primary"
        >
          <i :class="loading ? 'pi pi-spin pi-spinner' : 'pi pi-check'"></i>
          <span>{{ loading ? '提交中...' : '提交评分' }}</span>
        </button>
      </div>

      <div v-if="error" class="error-message">
        <i class="pi pi-exclamation-circle"></i>
        <span>{{ error }}</span>
      </div>

      <div v-if="success" class="success-message">
        <i class="pi pi-check-circle"></i>
        <span>评分提交成功！</span>
      </div>
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
  userId: {
    type: Number,
    required: true
  }
});

const emit = defineEmits(['update:visible', 'rating-submitted']);

const currentRating = ref(0);
const loading = ref(false);
const error = ref('');
const success = ref(false);

watch(() => props.visible, (newVal) => {
  if (newVal) {
    currentRating.value = 0;
    error.value = '';
    success.value = false;
  }
});

const getRatingText = (rating) => {
  const texts = {
    1: '很差',
    2: '较差',
    3: '一般',
    4: '不错',
    5: '非常好'
  };
  return texts[rating] || '';
};

const closeDialog = () => {
  emit('update:visible', false);
};

const handleSubmit = async () => {
  if (!props.movie || currentRating.value === 0) return;

  loading.value = true;
  error.value = '';
  success.value = false;

  try {
    await submitRating({
      userId: props.userId,
      movieId: props.movie.id || props.movie.movieId,
      rating: currentRating.value
    });

    success.value = true;
    emit('rating-submitted', {
      movieId: props.movie.id || props.movie.movieId,
      rating: currentRating.value
    });

    setTimeout(() => {
      closeDialog();
    }, 1500);
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
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  animation: fadeIn 0.2s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.dialog-container {
  background: #ffffff;
  border-radius: 24px;
  width: 90%;
  max-width: 500px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: slideUp 0.3s ease-out;
  overflow: hidden;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 28px;
  border-bottom: 1px solid #e5e7eb;
}

.dialog-title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
  display: flex;
  align-items: center;
  gap: 12px;
}

.dialog-title i {
  color: #fbbf24;
  font-size: 24px;
}

.close-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: #f3f4f6;
  border-radius: 10px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6b7280;
  transition: all 0.2s ease;
}

.close-btn:hover {
  background: #e5e7eb;
  color: #1f2937;
}

.dialog-body {
  padding: 32px 28px;
}

.movie-info {
  margin-bottom: 32px;
  text-align: center;
}

.movie-name {
  margin: 0 0 8px;
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
}

.movie-meta {
  margin: 4px 0;
  font-size: 14px;
  color: #6b7280;
}

.movie-genres {
  margin: 8px 0 0;
  font-size: 13px;
  color: #9ca3af;
}

.rating-section {
  text-align: center;
  padding: 24px;
  background: linear-gradient(135deg, rgba(251, 191, 36, 0.05), rgba(245, 158, 11, 0.05));
  border-radius: 16px;
  border: 2px dashed rgba(251, 191, 36, 0.2);
}

.rating-label {
  margin: 0 0 16px;
  font-size: 15px;
  font-weight: 600;
  color: #4b5563;
}

.rating-text {
  margin: 12px 0 0;
  font-size: 18px;
  font-weight: 700;
  color: #fbbf24;
}

.dialog-footer {
  display: flex;
  gap: 12px;
  padding: 20px 28px 28px;
}

.btn {
  flex: 1;
  height: 48px;
  border: none;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.btn-secondary {
  background: #f3f4f6;
  color: #4b5563;
}

.btn-secondary:hover {
  background: #e5e7eb;
}

.btn-primary {
  background: linear-gradient(135deg, #fbbf24, #f59e0b);
  color: #ffffff;
  box-shadow: 0 4px 16px rgba(251, 191, 36, 0.3);
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(251, 191, 36, 0.4);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.error-message,
.success-message {
  margin: 0 28px 20px;
  padding: 12px 16px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  font-weight: 500;
  animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.error-message {
  background: #fef2f2;
  color: #dc2626;
  border: 1px solid #fecaca;
}

.success-message {
  background: #f0fdf4;
  color: #16a34a;
  border: 1px solid #bbf7d0;
}

@media (max-width: 576px) {
  .dialog-container {
    width: 95%;
    margin: 0 16px;
  }

  .dialog-header,
  .dialog-body,
  .dialog-footer {
    padding-left: 20px;
    padding-right: 20px;
  }
}
</style>
