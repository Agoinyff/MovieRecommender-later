<template>
  <div class="recommendations-page">
    <div class="page-container">
      <header class="page-header">
        <div>
          <h1>个性化推荐</h1>
          <p>围绕当前登录用户生成推荐；管理员可额外按用户 ID 代查。</p>
        </div>
        <button class="refresh-btn" :disabled="loading" @click="loadRecommendations">{{ loading ? '刷新中...' : '刷新推荐' }}</button>
      </header>

      <div class="toolbar-card">
        <div v-if="authStore.isAdmin" class="mode-switch">
          <button :class="{ active: mode === 'self' }" @click="mode = 'self'">看我的推荐</button>
          <button :class="{ active: mode === 'admin' }" @click="mode = 'admin'">按用户 ID 查看</button>
        </div>

        <div class="filters">
          <label>
            推荐策略
            <select v-model="form.strategy" @change="handleStrategyChange">
              <option value="USER_BASED">用户协同</option>
              <option value="ITEM_BASED">物品协同</option>
              <option value="SLOPE_ONE">Slope One</option>
            </select>
          </label>
          <label>
            返回数量
            <input v-model.number="form.size" type="number" min="4" max="20" />
          </label>
          <label v-if="authStore.isAdmin && mode === 'admin'">
            用户 ID
            <input v-model.number="form.userId" type="number" min="1" />
          </label>
        </div>

        <div v-if="ratingStats.totalRatings < 5" class="notice-card">
          <strong>冷启动提醒</strong>
          <p>当前账号评分不足 5 条，系统会优先返回热门电影。建议先为几部电影评分，推荐会更稳定。</p>
        </div>
      </div>

      <LoadingSpinner v-if="loading" message="正在计算推荐..." />
      <div v-else-if="error" class="state error">{{ error }}</div>
      <div v-else-if="recommendations.length === 0" class="state">暂无推荐结果</div>
      <div v-else class="recommendations-grid">
        <MovieCard
          v-for="movie in recommendations"
          :key="movie.movieId"
          :movie="movie"
          :show-score="true"
          @click="showMovieDetail"
        />
      </div>
    </div>

    <MovieDetailDialog v-model:visible="dialogVisible" :movie="selectedMovie" @rating-updated="handleRatingUpdated" />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { getMyRatingStats, getRecommendationsAdmin, getRecommendationsMe } from '@/api';
import LoadingSpinner from '@/components/LoadingSpinner.vue';
import MovieCard from '@/components/MovieCard.vue';
import MovieDetailDialog from '@/components/MovieDetailDialog.vue';
import { useAuthStore } from '@/store/auth';
import { useRecommendationStore } from '@/store/recommendation';

const authStore = useAuthStore();
const recommendationStore = useRecommendationStore();

const loading = ref(false);
const error = ref('');
const recommendations = ref([]);
const dialogVisible = ref(false);
const selectedMovie = ref(null);
const mode = ref('self');
const ratingStats = ref({ totalRatings: 0 });
const form = reactive({
  strategy: recommendationStore.currentStrategy,
  size: 12,
  userId: ''
});

const handleStrategyChange = () => {
  recommendationStore.setStrategy(form.strategy);
  loadRecommendations();
};

const loadStats = async () => {
  try {
    ratingStats.value = await getMyRatingStats();
  } catch (err) {
    ratingStats.value = { totalRatings: 0 };
  }
};

const loadRecommendations = async () => {
  loading.value = true;
  error.value = '';
  try {
    const params = { strategy: form.strategy, size: form.size };
    if (authStore.isAdmin && mode.value === 'admin') {
      params.userId = form.userId;
      recommendations.value = await getRecommendationsAdmin(params);
    } else {
      recommendations.value = await getRecommendationsMe(params);
    }
  } catch (err) {
    error.value = err.response?.data?.message || err.message || '获取推荐失败';
    recommendations.value = [];
  } finally {
    loading.value = false;
  }
};

const showMovieDetail = (movie) => {
  selectedMovie.value = movie;
  dialogVisible.value = true;
};

const handleRatingUpdated = async () => {
  if (mode.value === 'self') {
    await loadStats();
    await loadRecommendations();
  }
};

onMounted(async () => {
  await loadStats();
  await loadRecommendations();
});
</script>

<style scoped>
.recommendations-page {
  min-height: calc(100vh - 68px);
  background: linear-gradient(180deg, #fff7ed, #ffffff);
}

.page-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 40px 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
  margin-bottom: 24px;
}

.page-header h1 {
  margin: 0 0 8px;
  font-size: 42px;
}

.page-header p {
  margin: 0;
  color: #64748b;
}

.refresh-btn {
  height: 46px;
  border: none;
  border-radius: 14px;
  padding: 0 18px;
  background: linear-gradient(135deg, #ea580c, #f59e0b);
  color: #fff;
  font-weight: 800;
}

.toolbar-card {
  padding: 24px;
  border-radius: 24px;
  background: #fff;
  box-shadow: 0 18px 60px rgba(15, 23, 42, 0.06);
  margin-bottom: 24px;
}

.mode-switch,
.filters {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.mode-switch {
  margin-bottom: 18px;
}

.mode-switch button {
  height: 42px;
  border-radius: 999px;
  border: 1px solid #fdba74;
  background: #fff7ed;
  color: #9a3412;
  padding: 0 16px;
  font-weight: 700;
}

.mode-switch button.active {
  background: #ea580c;
  color: #fff;
}

label {
  display: grid;
  gap: 8px;
  font-weight: 600;
  color: #334155;
}

select,
input {
  height: 46px;
  min-width: 180px;
  border-radius: 14px;
  border: 1px solid #cbd5e1;
  padding: 0 14px;
}

.notice-card {
  margin-top: 18px;
  padding: 16px 18px;
  border-radius: 18px;
  background: #fff7ed;
  color: #9a3412;
}

.notice-card p {
  margin: 8px 0 0;
}

.recommendations-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(210px, 1fr));
  gap: 22px;
}

.state {
  padding: 40px 0;
  text-align: center;
  color: #64748b;
}

.state.error {
  color: #dc2626;
}
</style>
