<template>
  <div class="recommendations-page">
    <div class="page-container">
      <header class="page-header">
        <div>
          <h1 class="page-title">个性化推荐</h1>
          <p class="page-description">基于协同过滤算法，为你精准推荐电影</p>
        </div>
      </header>

      <div class="content-layout">
        <!-- 推荐表单 -->
        <aside class="sidebar">
          <div class="form-card">
            <h3 class="form-title">
              <i class="pi pi-sliders-h"></i>
              <span>推荐设置</span>
            </h3>

            <form @submit.prevent="handleSubmit" class="form">
              <div class="form-field">
                <label>用户 ID</label>
                <input
                  v-model.number="form.userId"
                  type="number"
                  min="1"
                  required
                  class="input"
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
                <input
                  v-model.number="form.size"
                  type="number"
                  min="1"
                  max="50"
                  class="input"
                />
              </div>

              <button type="submit" :disabled="loading" class="submit-btn">
                <i :class="loading ? 'pi pi-spin pi-spinner' : 'pi pi-sparkles'"></i>
                <span>{{ loading ? '计算中...' : '获取推荐' }}</span>
              </button>
            </form>

            <div v-if="algorithmInfo" class="algorithm-info">
              <p class="info-title">算法说明</p>
              <p class="info-text">{{ algorithmInfo }}</p>
            </div>
          </div>
        </aside>

        <!-- 推荐结果 -->
        <main class="main-content">
          <div v-if="!hasSearched" class="welcome-state">
            <div class="welcome-icon">
              <i class="pi pi-star-fill"></i>
            </div>
            <h2>开始你的电影探索之旅</h2>
            <p>输入用户 ID 并选择推荐算法，系统将为你生成个性化的电影推荐列表</p>
          </div>

          <LoadingSpinner v-else-if="loading" message="正在计算推荐结果..." />

          <div v-else-if="error" class="error-message">
            <i class="pi pi-exclamation-triangle"></i>
            <p>{{ error }}</p>
          </div>

          <div v-else-if="recommendations.length === 0" class="empty-state">
            <i class="pi pi-inbox"></i>
            <p>暂无推荐结果</p>
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
                  :key="movie.movieId"
                  :movie="movie"
                  :show-score="true"
                  @click="showMovieDetail"
                  :style="{ transitionDelay: `${index * 50}ms` }"
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
    />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { getRecommendations } from '@/api';
import MovieCard from '@/components/MovieCard.vue';
import MovieDetailDialog from '@/components/MovieDetailDialog.vue';
import LoadingSpinner from '@/components/LoadingSpinner.vue';

const form = ref({
  userId: 100,
  strategy: 'USER_BASED',
  size: 12
});

const loading = ref(false);
const hasSearched = ref(false);
const recommendations = ref([]);
const error = ref('');
const dialogVisible = ref(false);
const selectedMovie = ref(null);

const algorithmInfo = computed(() => {
  const infos = {
    USER_BASED: '基于用户协同过滤算法，通过分析相似用户的喜好来推荐电影。适合冷启动场景。',
    ITEM_BASED: '基于物品协同过滤算法，通过分析电影之间的相似度来推荐。推荐结果更稳定。',
    SLOPE_ONE: '基于评分差值的预测算法，计算速度快，适合大规模数据集。'
  };
  return infos[form.value.strategy];
});

const handleSubmit = async () => {
  loading.value = true;
  error.value = '';
  hasSearched.value = true;
  
  try {
    const data = await getRecommendations({
      userId: form.value.userId,
      strategy: form.value.strategy,
      size: form.value.size
    });
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

.welcome-state {
  text-align: center;
  padding: 100px 40px;
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
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

.welcome-state h2 {
  margin: 0 0 16px;
  font-size: 32px;
  font-weight: 800;
  color: #1f2937;
}

.welcome-state p {
  margin: 0;
  font-size: 16px;
  line-height: 1.6;
  color: #6b7280;
  max-width: 500px;
  margin: 0 auto;
}

.error-message {
  text-align: center;
  padding: 60px 20px;
  color: #dc2626;
}

.error-message i {
  font-size: 48px;
  margin-bottom: 16px;
}

.error-message p {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: #9ca3af;
}

.empty-state i {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-state p {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
}

.results-container {
  animation: slideUp 0.5s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.results-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.8);
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
  font-size: 24px;
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

/* 动画效果 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.5s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.movie-list-move,
.movie-list-enter-active {
  transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.movie-list-enter-from {
  opacity: 0;
  transform: translateY(30px) scale(0.9);
}

@media (max-width: 1024px) {
  .content-layout {
    grid-template-columns: 1fr;
    gap: 24px;
  }

  .sidebar {
    position: relative;
    top: 0;
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

  .welcome-state {
    padding: 60px 20px;
  }

  .welcome-icon {
    width: 100px;
    height: 100px;
    font-size: 48px;
  }

  .welcome-state h2 {
    font-size: 24px;
  }
}
</style>

