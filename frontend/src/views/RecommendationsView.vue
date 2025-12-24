<template>
  <section class="page">
    <header class="hero">
      <div>
        <p class="eyebrow">Personalized picks</p>
        <h1>个性化推荐</h1>
        <p class="lede">
          输入用户 ID，选择算法（用户协同、物品协同或 Slope One），即可查看 Spring Boot 后端生成的推荐结果。
        </p>
      </div>
      <div class="status" v-if="connectionInfo">
        <span class="dot" :class="connectionInfo.status"></span>
        <span>{{ connectionInfo.message }}</span>
      </div>
    </header>

    <section class="content">
      <RecommendationForm
        :initial-user="demoUserId"
        :loading="loading"
        @submit="onSubmit"
      />

      <RecommendationsList
        :recommendations="recommendations"
        :loading="loading"
        :error="error"
        @select="openDetail"
      />
    </section>

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
import RecommendationForm from '../components/RecommendationForm.vue';
import RecommendationsList from '../components/RecommendationsList.vue';
import MovieDetailDialog from '../components/MovieDetailDialog.vue';
import { fetchRecommendations } from '../api/recommendations';
import { getMovieById, searchMovies } from '../api/movies';

const loading = ref(false);
const recommendations = ref([]);
const error = ref('');
const demoUserId = ref(100);
const connectionInfo = ref(null);

const detailVisible = ref(false);
const detailLoading = ref(false);
const selectedMovie = ref(null);
const detailError = ref('');

const onSubmit = async ({ userId, strategy, size }) => {
  loading.value = true;
  error.value = '';
  try {
    recommendations.value = await fetchRecommendations({ userId, strategy, size });
  } catch (err) {
    error.value = err?.response?.data?.message || err.message || '无法获取推荐结果';
    recommendations.value = [];
  } finally {
    loading.value = false;
  }
};

const openDetail = async (movie) => {
  detailVisible.value = true;
  detailLoading.value = true;
  detailError.value = '';
  try {
    selectedMovie.value = await getMovieById(movie.movieId ?? movie.id);
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

onMounted(async () => {
  try {
    const health = await searchMovies({ size: 1 });
    connectionInfo.value = {
      status: 'ok',
      message: `后端已连接（${health.totalElements || '无数据'} 条电影）`
    };
  } catch (err) {
    connectionInfo.value = {
      status: 'warn',
      message: '无法连接后端，请检查 8080 端口和数据库'
    };
  }
});
</script>

<style scoped>
.page {
  max-width: 1200px;
  margin: 0 auto;
}

.hero {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.eyebrow {
  color: rgba(129, 140, 248, 0.95);
  font-weight: 700;
  letter-spacing: 0.02em;
  margin: 0 0 4px;
}

h1 {
  margin: 0 0 8px;
  font-size: 32px;
  color: #e2e8f0;
}

.lede {
  margin: 0;
  color: rgba(226, 232, 240, 0.7);
  max-width: 720px;
}

.content {
  display: grid;
  grid-template-columns: 1fr;
  gap: 18px;
}

.status {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  background: rgba(129, 140, 248, 0.2);
  border-radius: 12px;
  color: #e2e8f0;
  font-weight: 600;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  background: #22c55e;
}

.dot.warn {
  background: #f59e0b;
}

@media (min-width: 900px) {
  .content {
    grid-template-columns: 360px 1fr;
    align-items: start;
  }
}
</style>
