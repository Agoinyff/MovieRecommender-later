<template>
  <div class="recommendations-page">
    <div class="page-container">
      <header class="page-header">
        <div>
          <h1 class="page-title">个性化推荐</h1>
          <p class="page-description">默认围绕当前登录用户生成推荐，管理员可额外按用户 ID 代查。</p>
        </div>
        <div class="identity-banner">
          <span class="identity-pill">当前登录：{{ authStore.user.displayName }} (#{{ authStore.user.id }})</span>
          <span class="identity-pill role">{{ authStore.isAdmin ? '管理员视角' : '普通用户视角' }}</span>
          <span v-if="effectiveUserId" class="identity-pill target">推荐目标：{{ effectiveUserId }}</span>
        </div>
      </header>

      <div class="content-layout">
        <aside class="sidebar">
          <div class="form-card">
            <h3 class="form-title">
              <i class="pi pi-sliders-h"></i>
              <span>推荐设置</span>
            </h3>

            <form @submit.prevent="handleSubmit" class="form">
              <div v-if="authStore.isAdmin" class="form-field">
                <label>查询用户 ID（管理员）</label>
                <input
                  v-model.number="form.userId"
                  type="number"
                  min="1"
                  class="input"
                  placeholder="留空则使用当前登录用户"
                />
              </div>

              <div class="form-field">
                <label>推荐算法</label>
                <select v-model="form.strategy" class="input">
                  <option value="USER_BASED">用户协同过滤</option>
                  <option value="ITEM_BASED">物品协同过滤</option>
                  <option value="SLOPE_ONE">Slope One</option>
                </select>
              </div>

              <div class="form-field">
                <label>返回条数</label>
                <input v-model.number="form.size" type="number" min="1" max="50" class="input" />
              </div>

              <button type="submit" :disabled="loading" class="submit-btn">
                <i :class="loading ? 'pi pi-spin pi-spinner' : 'pi pi-sparkles'"></i>
                <span>{{ loading ? '计算中...' : '获取推荐' }}</span>
              </button>
            </form>

            <div class="algorithm-info">
              <p class="info-title">算法说明</p>
              <p class="info-text">{{ algorithmInfo }}</p>
            </div>
          </div>
        </aside>

        <main class="main-content">
          <div v-if="!hasSearched" class="welcome-state">
            <div class="welcome-icon">
              <i class="pi pi-star-fill"></i>
            </div>
            <h2>你的推荐会围绕当前登录账号持续更新</h2>
            <p>评分越多，结果越稳定。管理员也可以在左侧直接输入用户 ID 观察不同用户画像。</p>
          </div>

          <LoadingSpinner v-else-if="loading" message="正在计算推荐结果..." />

          <div v-else-if="error" class="error-message">
            <i class="pi pi-exclamation-triangle"></i>
            <p>{{ error }}</p>
          </div>

          <div v-else-if="recommendations.length === 0" class="empty-state">
            <i class="pi pi-inbox"></i>
            <p>当前暂无推荐结果，建议先去电影详情页完成几次评分。</p>
          </div>

          <Transition name="fade">
            <div v-if="recommendations.length > 0" class="results-container">
              <div class="results-header">
                <h2 class="results-title">
                  <i class="pi pi-heart-fill"></i>
                  <span>为你推荐</span>
                </h2>
                <span class="results-count">{{ recommendations.length }} 部电影</span>
              </div>

              <TransitionGroup name="movie-list" tag="div" class="recommendations-grid">
                <MovieCard
                  v-for="(movie, index) in recommendations"
                  :key="movie.movieId || movie.id"
                  :movie="movie"
                  :show-score="true"
                  @click="showMovieDetail"
                  :style="{ transitionDelay: `${index * 40}ms` }"
                />
              </TransitionGroup>
            </div>
          </Transition>
        </main>
      </div>
    </div>

    <MovieDetailDialog
      v-model:visible="dialogVisible"
      :movie="selectedMovie"
      :recommendation-user-id="effectiveUserId"
      @rating-updated="handleRatingUpdated"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { getRecommendations } from '@/api';
import { useAuthStore } from '@/store/auth';
import MovieCard from '@/components/MovieCard.vue';
import MovieDetailDialog from '@/components/MovieDetailDialog.vue';
import LoadingSpinner from '@/components/LoadingSpinner.vue';

const authStore = useAuthStore();
const form = ref({
  userId: null,
  strategy: 'USER_BASED',
  size: 12
});

const loading = ref(false);
const hasSearched = ref(false);
const recommendations = ref([]);
const error = ref('');
const dialogVisible = ref(false);
const selectedMovie = ref(null);

const effectiveUserId = computed(() => form.value.userId || authStore.user.id);

const algorithmInfo = computed(() => {
  const infos = {
    USER_BASED: '依据与你兴趣相似的用户行为生成推荐，适合已经有一定评分积累的用户。',
    ITEM_BASED: '优先寻找与你高分电影相似的作品，结果更稳定，也更适合详情页联动展示。',
    SLOPE_ONE: '基于评分差值快速预测，适合大数据量场景下快速给出结果。'
  };
  return infos[form.value.strategy];
});

const handleSubmit = async () => {
  loading.value = true;
  error.value = '';
  hasSearched.value = true;

  try {
    const params = {
      strategy: form.value.strategy,
      size: form.value.size
    };
    if (authStore.isAdmin && form.value.userId) {
      params.userId = form.value.userId;
    }
    const data = await getRecommendations(params);
    recommendations.value = data || [];
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
  if (!hasSearched.value || (authStore.isAdmin && form.value.userId)) {
    return;
  }
  await handleSubmit();
};

onMounted(() => {
  handleSubmit();
});
</script>

<style scoped>
.recommendations-page {
  min-height: calc(100vh - 64px);
  background: linear-gradient(180deg, rgba(249, 250, 251, 0.5) 0%, rgba(255, 255, 255, 0.8) 100%);
}

.page-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 40px 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 32px;
}

.page-title {
  margin: 0 0 8px;
  font-size: 42px;
  font-weight: 800;
  color: #1f2937;
}

.page-description {
  margin: 0;
  font-size: 16px;
  color: #6b7280;
}

.identity-banner {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-content: flex-start;
  justify-content: flex-end;
}

.identity-pill {
  display: inline-flex;
  align-items: center;
  padding: 10px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(229, 231, 235, 0.7);
  color: #374151;
  font-size: 13px;
  font-weight: 700;
}

.identity-pill.role {
  color: #4f46e5;
}

.identity-pill.target {
  background: rgba(79, 70, 229, 0.08);
}

.content-layout {
  display: grid;
  grid-template-columns: 360px 1fr;
  gap: 32px;
  align-items: start;
}

.sidebar {
  position: sticky;
  top: 80px;
}

.form-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(229, 231, 235, 0.6);
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.04);
}

.form-title {
  margin: 0 0 24px;
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
  display: flex;
  align-items: center;
  gap: 10px;
}

.form-title i {
  color: #6366f1;
}

.form {
  display: grid;
  gap: 20px;
}

.form-field {
  display: grid;
  gap: 8px;
}

.form-field label {
  font-weight: 600;
  color: #4b5563;
  font-size: 14px;
}

.input {
  height: 48px;
  padding: 0 16px;
  border: 2px solid rgba(229, 231, 235, 0.6);
  border-radius: 12px;
  font-size: 15px;
  background: rgba(255, 255, 255, 0.9);
  transition: all 0.3s ease;
}

.input:focus {
  outline: none;
  border-color: #6366f1;
  box-shadow: 0 0 0 4px rgba(99, 102, 241, 0.1);
}

.submit-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  height: 48px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 8px;
  box-shadow: 0 4px 16px rgba(99, 102, 241, 0.3);
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(99, 102, 241, 0.4);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.algorithm-info {
  margin-top: 24px;
  padding: 16px;
  background: rgba(99, 102, 241, 0.05);
  border-radius: 12px;
  border: 1px solid rgba(99, 102, 241, 0.1);
}

.info-title {
  margin: 0 0 8px;
  font-size: 13px;
  font-weight: 700;
  color: #6366f1;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-text {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
  color: #4b5563;
}

.main-content {
  min-height: 500px;
}

.welcome-state,
.empty-state,
.error-message {
  text-align: center;
  padding: 80px 24px;
}

.welcome-icon {
  width: 120px;
  height: 120px;
  margin: 0 auto 24px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 56px;
  color: #fff;
  box-shadow: 0 20px 50px rgba(99, 102, 241, 0.3);
}

.welcome-state h2 {
  margin: 0 0 16px;
  font-size: 32px;
  font-weight: 800;
  color: #1f2937;
}

.welcome-state p,
.empty-state p,
.error-message p {
  margin: 0 auto;
  max-width: 520px;
  font-size: 16px;
  line-height: 1.6;
  color: #6b7280;
}

.error-message {
  color: #dc2626;
}

.results-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  border: 1px solid rgba(229, 231, 235, 0.6);
}

.results-title {
  margin: 0;
  font-size: 28px;
  font-weight: 800;
  color: #1f2937;
  display: flex;
  align-items: center;
  gap: 12px;
}

.results-title i {
  color: #ec4899;
}

.results-count {
  padding: 8px 16px;
  background: rgba(99, 102, 241, 0.1);
  border-radius: 12px;
  color: #4f46e5;
  font-weight: 700;
  font-size: 14px;
}

.recommendations-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 24px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.4s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.movie-list-move,
.movie-list-enter-active {
  transition: all 0.5s ease;
}

.movie-list-enter-from {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}

@media (max-width: 1024px) {
  .page-header,
  .content-layout {
    grid-template-columns: 1fr;
    display: grid;
  }

  .page-header {
    gap: 16px;
  }

  .sidebar {
    position: static;
  }
}

@media (max-width: 768px) {
  .page-title {
    font-size: 32px;
  }

  .recommendations-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 16px;
  }

  .results-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>
