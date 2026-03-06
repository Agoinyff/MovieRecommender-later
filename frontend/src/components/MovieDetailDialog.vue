<template>
  <Dialog
    v-model:visible="isVisible"
    :modal="true"
    :closable="true"
    :dismissableMask="true"
    :style="{ width: 'min(900px, 94vw)' }"
    class="movie-detail-dialog"
  >
    <template #header>
      <div class="dialog-header">
        <i class="pi pi-film"></i>
        <span>电影详情</span>
      </div>
    </template>

    <div class="dialog-shell">
      <div v-if="loading" class="loading-state">
        <i class="pi pi-spin pi-spinner"></i>
        <span>正在加载详情...</span>
      </div>

      <div v-else-if="detailMovie" class="dialog-content">
        <div class="hero-layout">
          <div class="detail-poster">
            <div class="poster-wrapper">
              <img
                v-if="detailMovie.posterUrl && !posterError"
                :src="detailMovie.posterUrl"
                :alt="detailMovie.name"
                class="poster-image"
                @error="handlePosterError"
              />
              <i v-else class="pi pi-video"></i>
            </div>
          </div>

          <div class="detail-main">
            <div class="detail-info-card">
              <h2 class="detail-title">{{ detailMovie.name }}</h2>
              <div class="detail-grid">
                <div class="detail-row" v-if="detailMovie.publishedYear">
                  <span class="label">上映年份</span>
                  <span class="value">{{ detailMovie.publishedYear }}</span>
                </div>
                <div class="detail-row" v-if="detailMovie.genres">
                  <span class="label">类型</span>
                  <span class="value">{{ detailMovie.genres }}</span>
                </div>
                <div class="detail-row">
                  <span class="label">电影 ID</span>
                  <span class="value">{{ detailMovie.id || detailMovie.movieId }}</span>
                </div>
                <div class="detail-row" v-if="detailMovie.score">
                  <span class="label">推荐分</span>
                  <span class="value score-value">{{ Number(detailMovie.score).toFixed(2) }}</span>
                </div>
              </div>
            </div>

            <div class="rating-card">
              <div class="rating-card-header">
                <div>
                  <p class="rating-card-kicker">评分卡片</p>
                  <h3>让推荐围绕你的当前账号变化</h3>
                </div>
                <span class="rating-status">{{ authStore.isAuthenticated ? '已登录' : '未登录' }}</span>
              </div>

              <div class="rating-preview" v-if="authStore.isAuthenticated && currentRating">
                <span class="rating-preview-label">我的评分</span>
                <div class="rating-preview-body">
                  <RatingStars :modelValue="currentRating" :readonly="true" :size="22" />
                  <strong>{{ Number(currentRating).toFixed(1) }}</strong>
                </div>
              </div>

              <p class="rating-hint">
                {{ authStore.isAuthenticated ? '提交评分后，会刷新你的推荐联动和评分中心。' : '登录后即可记录评分，并让个性化推荐真正围绕你的账号工作。' }}
              </p>

              <div class="linkage-card">
                <div class="linkage-item">
                  <span class="linkage-label">推荐联动</span>
                  <strong>{{ linkageText }}</strong>
                </div>
                <div class="linkage-item">
                  <span class="linkage-label">推荐来源</span>
                  <strong>{{ recommendationTitle }}</strong>
                </div>
              </div>

              <button v-if="authStore.isAuthenticated" @click="openRatingDialog" class="rate-btn">
                <i class="pi pi-star"></i>
                <span>{{ currentRating ? '修改评分' : '为这部电影评分' }}</span>
              </button>
              <button v-else @click="goToAuth" class="login-btn">
                <i class="pi pi-sign-in"></i>
                <span>登录后评分</span>
              </button>
            </div>
          </div>
        </div>

        <div class="recommend-section" v-if="bottomRecommendations.length > 0">
          <div class="section-header">
            <div>
              <p class="section-kicker">推荐联动</p>
              <h3>{{ recommendationTitle }}</h3>
            </div>
            <span class="section-tip">点击底部电影继续查看详情</span>
          </div>
          <div class="horizontal-list">
            <MovieCard
              v-for="movie in bottomRecommendations"
              :key="movie.id || movie.movieId"
              :movie="movie"
              :show-score="Boolean(movie.score)"
              class="horizontal-card"
              @click="selectRecommendedMovie"
            />
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <Button label="关闭" icon="pi pi-times" @click="closeDialog" class="p-button-text" />
    </template>
  </Dialog>

  <RatingDialog
    v-if="authStore.isAuthenticated"
    v-model:visible="ratingDialogVisible"
    :movie="detailMovie"
    :initial-rating="currentRating || 0"
    @rating-submitted="handleRatingSubmitted"
  />
</template>

<script setup>
import { computed, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import Dialog from 'primevue/dialog';
import Button from 'primevue/button';
import RatingStars from './RatingStars.vue';
import RatingDialog from './RatingDialog.vue';
import MovieCard from './MovieCard.vue';
import { getMovieById, getMyMovieRating, getRecommendations, getRelatedMovies } from '@/api';
import { useAuthStore } from '@/store/auth';

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  movie: {
    type: Object,
    default: null
  },
  recommendationUserId: {
    type: Number,
    default: null
  }
});

const emit = defineEmits(['update:visible', 'rating-updated']);

const authStore = useAuthStore();
const router = useRouter();
const isVisible = ref(props.visible);
const ratingDialogVisible = ref(false);
const posterError = ref(false);
const loading = ref(false);
const detailMovie = ref(null);
const currentRating = ref(null);
const personalizedRecommendations = ref([]);
const relatedRecommendations = ref([]);
const activeMovie = ref(null);
let requestToken = 0;

const bottomRecommendations = computed(() => {
  const personalized = personalizedRecommendations.value.filter(
    (movie) => (movie.id || movie.movieId) !== (detailMovie.value?.id || detailMovie.value?.movieId)
  );
  if (personalized.length > 0) {
    return personalized.slice(0, 6);
  }
  return relatedRecommendations.value
    .filter((movie) => (movie.id || movie.movieId) !== (detailMovie.value?.id || detailMovie.value?.movieId))
    .slice(0, 6);
});

const recommendationTitle = computed(() => {
  return personalizedRecommendations.value.length > 0 ? '围绕当前用户的推荐联动' : '底部横向推荐';
});

const linkageText = computed(() => {
  if (!authStore.isAuthenticated) {
    return '登录后可用';
  }
  if (currentRating.value) {
    return '你的评分已接入推荐';
  }
  return '评分后立即刷新';
});

watch(
  () => props.visible,
  (newVal) => {
    isVisible.value = newVal;
    if (newVal && props.movie) {
      activeMovie.value = props.movie;
      loadMovieContext(props.movie);
    }
  }
);

watch(
  () => props.movie,
  (movie) => {
    if (isVisible.value && movie) {
      activeMovie.value = movie;
      loadMovieContext(movie);
    }
  }
);

watch(isVisible, (newVal) => {
  emit('update:visible', newVal);
});

const closeDialog = () => {
  isVisible.value = false;
};

const handlePosterError = () => {
  posterError.value = true;
};

const resolveMovieId = (movie) => movie?.id || movie?.movieId;

const buildRecommendationParams = () => {
  const params = {
    strategy: 'ITEM_BASED',
    size: 10
  };
  if (authStore.isAdmin && props.recommendationUserId) {
    params.userId = props.recommendationUserId;
  }
  return params;
};

const loadMovieContext = async (movie) => {
  const movieId = resolveMovieId(movie);
  if (!movieId) {
    return;
  }

  const currentRequest = ++requestToken;
  loading.value = true;
  posterError.value = false;

  try {
    const [movieDetail, related] = await Promise.all([
      getMovieById(movieId),
      getRelatedMovies(movieId, 10)
    ]);

    if (currentRequest !== requestToken) {
      return;
    }

    detailMovie.value = {
      ...movie,
      ...movieDetail
    };
    relatedRecommendations.value = related || [];

    if (authStore.isAuthenticated) {
      const [ratingResult, recommendationResult] = await Promise.allSettled([
        getMyMovieRating(movieId),
        getRecommendations(buildRecommendationParams())
      ]);

      if (currentRequest !== requestToken) {
        return;
      }

      currentRating.value = ratingResult.status === 'fulfilled' ? ratingResult.value?.rating ?? null : null;
      if (currentRating.value) {
        detailMovie.value.userRating = currentRating.value;
      }

      personalizedRecommendations.value = recommendationResult.status === 'fulfilled'
        ? (recommendationResult.value || [])
        : [];
    } else {
      currentRating.value = null;
      personalizedRecommendations.value = [];
    }
  } finally {
    if (currentRequest === requestToken) {
      loading.value = false;
    }
  }
};

const openRatingDialog = () => {
  ratingDialogVisible.value = true;
};

const goToAuth = () => {
  router.push({ path: '/auth', query: { redirect: router.currentRoute.value.fullPath } });
};

const handleRatingSubmitted = async (ratingData) => {
  currentRating.value = ratingData.rating;
  if (detailMovie.value) {
    detailMovie.value.userRating = ratingData.rating;
  }
  emit('rating-updated', ratingData);
  await loadMovieContext(detailMovie.value || activeMovie.value);
};

const selectRecommendedMovie = (movie) => {
  activeMovie.value = movie;
  loadMovieContext(movie);
};
</script>

<style scoped>
:deep(.movie-detail-dialog .p-dialog) {
  border-radius: 28px;
  overflow: hidden;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
}

:deep(.movie-detail-dialog .p-dialog-header) {
  padding: 16px 22px 10px;
}

:deep(.movie-detail-dialog .p-dialog-content) {
  padding: 0 22px 14px;
  overflow: hidden;
  flex: 1;
}

:deep(.movie-detail-dialog .p-dialog-footer) {
  padding: 0 22px 20px;
}

.dialog-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
}

.dialog-header i {
  color: #6366f1;
}

.dialog-shell,
.dialog-content,
.detail-main,
.detail-grid {
  min-width: 0;
}

.loading-state {
  min-height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #6b7280;
}

.dialog-content {
  display: grid;
  gap: 20px;
}

.hero-layout {
  display: grid;
  grid-template-columns: minmax(0, 140px) minmax(0, 1fr);
  gap: 16px;
  align-items: start;
}

.poster-wrapper {
  width: 100%;
  aspect-ratio: 2 / 3;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.9);
  font-size: 52px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.14);
  overflow: hidden;
}

.poster-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-main {
  display: grid;
  gap: 14px;
}

.detail-info-card,
.rating-card {
  padding: 18px;
  border-radius: 20px;
  background: rgba(248, 250, 252, 0.92);
  border: 1px solid rgba(229, 231, 235, 0.8);
}

.detail-title {
  margin: 0 0 12px;
  font-size: 24px;
  font-weight: 800;
  color: #1f2937;
  line-height: 1.25;
}

.detail-grid {
  display: grid;
  gap: 8px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 14px;
  padding: 10px 12px;
  border-radius: 12px;
  background: #fff;
}

.label {
  color: #6b7280;
  font-weight: 600;
  flex-shrink: 0;
}

.value {
  color: #111827;
  font-weight: 700;
  text-align: right;
  word-break: break-word;
}

.score-value {
  color: #f59e0b;
}

.rating-card-header {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  align-items: flex-start;
}

.rating-card-kicker,
.section-kicker {
  margin: 0 0 4px;
  color: #6366f1;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.rating-card-header h3,
.section-header h3 {
  margin: 0;
  font-size: 19px;
  color: #1f2937;
  line-height: 1.35;
}

.rating-status {
  padding: 7px 11px;
  border-radius: 999px;
  background: rgba(99, 102, 241, 0.08);
  color: #4f46e5;
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
}

.rating-preview {
  margin-top: 14px;
  padding: 14px;
  border-radius: 14px;
  background: #fff;
}

.rating-preview-label {
  display: block;
  margin-bottom: 8px;
  color: #6b7280;
  font-size: 13px;
  font-weight: 700;
}

.rating-preview-body {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.rating-preview-body strong {
  color: #f59e0b;
  font-size: 17px;
}

.rating-hint {
  margin: 14px 0;
  color: #6b7280;
  line-height: 1.65;
  font-size: 14px;
}

.linkage-card {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 14px;
}

.linkage-item {
  padding: 12px;
  border-radius: 14px;
  background: #fff;
}

.linkage-label {
  display: block;
  margin-bottom: 4px;
  color: #6b7280;
  font-size: 12px;
  font-weight: 700;
}

.linkage-item strong {
  color: #111827;
  line-height: 1.4;
  font-size: 14px;
}

.rate-btn,
.login-btn {
  width: 100%;
  height: 46px;
  border: none;
  border-radius: 14px;
  color: #fff;
  font-size: 15px;
  font-weight: 800;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.rate-btn {
  background: linear-gradient(135deg, #fbbf24, #f59e0b);
  box-shadow: 0 10px 24px rgba(251, 191, 36, 0.22);
}

.login-btn {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
}

.recommend-section {
  padding-top: 2px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 18px;
  margin-bottom: 14px;
}

.section-tip {
  color: #6b7280;
  font-size: 12px;
}

.horizontal-list {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  padding-bottom: 4px;
  -ms-overflow-style: none; /* IE and Edge */
  scrollbar-width: none; /* Firefox */
}

.horizontal-list::-webkit-scrollbar {
  display: none;
}

.horizontal-card {
  min-width: 140px;
  max-width: 140px;
}

@media (max-width: 900px) {
  .hero-layout {
    grid-template-columns: 1fr;
  }

  .poster-wrapper {
    max-width: 130px;
    margin: 0 auto;
  }
}

@media (max-width: 640px) {
  :deep(.movie-detail-dialog .p-dialog-header),
  :deep(.movie-detail-dialog .p-dialog-content),
  :deep(.movie-detail-dialog .p-dialog-footer) {
    padding-left: 16px;
    padding-right: 16px;
  }

  .detail-title {
    font-size: 22px;
  }

  .linkage-card {
    grid-template-columns: 1fr;
  }

  .section-header,
  .rating-card-header,
  .detail-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .value {
    text-align: left;
  }

  .horizontal-card {
    min-width: 164px;
    max-width: 164px;
  }
}
</style>
