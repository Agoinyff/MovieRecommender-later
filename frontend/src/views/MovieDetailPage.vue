<template>
  <div class="movie-detail-page">
    <div class="page-container" v-if="movie">
      <section class="hero">
        <div class="poster-box">
          <img v-if="movie.posterUrl" :src="movie.posterUrl" :alt="movie.name" />
          <div v-else class="poster-placeholder"><i class="pi pi-video"></i></div>
        </div>
        <div class="hero-copy">
          <p class="meta">{{ movie.publishedYear || '年份未知' }} · {{ movie.genres || '类型未知' }}</p>
          <h1>{{ movie.name }}</h1>
          <p class="description">在这里可以直接给电影评分，并立即刷新同策略推荐结果。</p>
          <div class="actions">
            <router-link to="/recommendations" class="ghost-btn">返回推荐页</router-link>
          </div>
        </div>
      </section>

      <section class="content-grid">
        <div class="rating-card">
          <p class="card-title">评分卡片</p>
          <div class="rating-current">
            <RatingStars :model-value="currentRating" :readonly="true" :size="28" />
            <strong>{{ currentRating ? currentRating.toFixed(1) : '未评分' }}</strong>
          </div>
          <div class="rating-input">
            <RatingStars v-model="draftRating" :size="34" />
          </div>
          <div class="button-row">
            <button class="primary-btn" :disabled="ratingLoading || draftRating === 0" @click="submitCurrentRating">
              {{ ratingLoading ? '提交中...' : currentRating ? '修改评分' : '提交评分' }}
            </button>
            <button class="secondary-btn" :disabled="recommendationLoading" @click="loadRecommendations">刷新推荐</button>
          </div>
          <p v-if="message" class="message">{{ message }}</p>
        </div>

        <div class="recommend-card">
          <div class="card-head">
            <div>
              <p class="card-title">为你推荐（同策略）</p>
              <small>当前策略：{{ strategyLabel }}</small>
            </div>
          </div>
          <div v-if="recommendationLoading" class="state">正在加载推荐...</div>
          <div v-else-if="recommendations.length === 0" class="state">暂无推荐结果</div>
          <div v-else class="recommend-strip">
            <MovieCard
              v-for="item in recommendations"
              :key="item.movieId"
              :movie="item"
              :show-score="true"
              @click="goToMovie(item.movieId)"
            />
          </div>
        </div>
      </section>
    </div>
    <LoadingSpinner v-else message="正在加载电影详情..." />
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getMovieById, getMovieRecommendations, getMyMovieRating, submitRating } from '@/api';
import LoadingSpinner from '@/components/LoadingSpinner.vue';
import MovieCard from '@/components/MovieCard.vue';
import RatingStars from '@/components/RatingStars.vue';
import { useRecommendationStore } from '@/store/recommendation';

const route = useRoute();
const router = useRouter();
const recommendationStore = useRecommendationStore();

const movie = ref(null);
const recommendations = ref([]);
const currentRating = ref(0);
const draftRating = ref(0);
const message = ref('');
const ratingLoading = ref(false);
const recommendationLoading = ref(false);

const strategy = computed(() => recommendationStore.currentStrategy || 'ITEM_BASED');
const strategyLabel = computed(() => ({ USER_BASED: '用户协同', ITEM_BASED: '物品协同', SLOPE_ONE: 'Slope One' }[strategy.value] || strategy.value));

const loadMovie = async () => {
  movie.value = await getMovieById(route.params.id);
};

const loadMyRating = async () => {
  try {
    const data = await getMyMovieRating(route.params.id);
    currentRating.value = data.rating;
    draftRating.value = data.rating;
  } catch (err) {
    currentRating.value = 0;
    draftRating.value = 0;
  }
};

const loadRecommendations = async () => {
  recommendationLoading.value = true;
  try {
    recommendations.value = await getMovieRecommendations(route.params.id, {
      strategy: strategy.value,
      size: 10
    });
  } finally {
    recommendationLoading.value = false;
  }
};

const submitCurrentRating = async () => {
  ratingLoading.value = true;
  message.value = '';
  try {
    await submitRating({ movieId: Number(route.params.id), rating: draftRating.value });
    currentRating.value = draftRating.value;
    message.value = '评分提交成功，推荐已刷新';
    await loadRecommendations();
  } catch (err) {
    message.value = err.response?.data?.message || err.message || '提交评分失败';
  } finally {
    ratingLoading.value = false;
  }
};

const goToMovie = (movieId) => router.push(`/movies/${movieId}`);

watch(() => route.params.id, async () => {
  await Promise.all([loadMovie(), loadMyRating(), loadRecommendations()]);
});

onMounted(async () => {
  await Promise.all([loadMovie(), loadMyRating(), loadRecommendations()]);
});
</script>

<style scoped>
.movie-detail-page {
  min-height: calc(100vh - 68px);
  background: linear-gradient(180deg, #fff7ed, #ffffff);
}

.page-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 40px 24px 60px;
}

.hero {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 32px;
  margin-bottom: 30px;
}

.poster-box {
  aspect-ratio: 2 / 3;
  border-radius: 24px;
  overflow: hidden;
  background: linear-gradient(135deg, #ea580c, #f59e0b);
}

.poster-box img,
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
  font-size: 60px;
}

.meta {
  color: #9a3412;
  font-weight: 700;
}

.hero-copy h1 {
  margin: 10px 0;
  font-size: 50px;
  line-height: 1.04;
}

.description {
  max-width: 680px;
  color: #475569;
  line-height: 1.7;
}

.actions {
  margin-top: 20px;
}

.ghost-btn,
.primary-btn,
.secondary-btn {
  height: 46px;
  border-radius: 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0 18px;
  font-weight: 800;
  text-decoration: none;
}

.ghost-btn,
.secondary-btn {
  background: #fff;
  border: 1px solid #cbd5e1;
  color: #475569;
}

.primary-btn {
  border: none;
  background: linear-gradient(135deg, #ea580c, #f59e0b);
  color: #fff;
}

.content-grid {
  display: grid;
  gap: 24px;
}

.rating-card,
.recommend-card {
  background: #fff;
  border-radius: 24px;
  padding: 24px;
  box-shadow: 0 18px 60px rgba(15, 23, 42, 0.06);
}

.card-title {
  margin: 0 0 14px;
  color: #9a3412;
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  font-size: 12px;
}

.rating-current {
  display: flex;
  gap: 12px;
  align-items: center;
}

.rating-current strong {
  font-size: 24px;
}

.rating-input {
  margin: 18px 0;
}

.button-row {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.message {
  margin-top: 14px;
  color: #0f766e;
}

.recommend-strip {
  display: grid;
  grid-auto-flow: column;
  grid-auto-columns: minmax(200px, 240px);
  gap: 18px;
  overflow-x: auto;
  padding-bottom: 8px;
}

.state {
  color: #64748b;
}

@media (max-width: 900px) {
  .hero {
    grid-template-columns: 1fr;
  }

  .hero-copy h1 {
    font-size: 38px;
  }
}
</style>
