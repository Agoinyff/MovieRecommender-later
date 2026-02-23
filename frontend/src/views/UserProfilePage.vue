<template>
  <div class="user-profile-page">
    <div class="page-container">
      <header class="page-header">
        <h1 class="page-title">我的评分</h1>
        <p class="page-description">查看和管理你的电影评分记录</p>
      </header>

      <div class="user-selector">
        <div class="selector-card">
          <label class="selector-label">用户 ID</label>
          <div class="selector-input-group">
            <input
              v-model.number="userId"
              type="number"
              min="1"
              placeholder="请输入用户 ID"
              class="user-input"
              @keyup.enter="loadUserRatings"
            />
            <button @click="loadUserRatings" :disabled="!userId || loading" class="load-btn">
              <i :class="loading ? 'pi pi-spin pi-spinner' : 'pi pi-search'"></i>
              <span>{{ loading ? '加载中...' : '查询' }}</span>
            </button>
          </div>
        </div>
      </div>

      <div v-if="hasLoaded && !loading" class="content-section">
        <div v-if="ratings.length > 0" class="stats-grid">
          <div class="stat-card">
            <div class="stat-icon" style="background: rgba(99, 102, 241, 0.1);">
              <i class="pi pi-star-fill" style="color: #6366f1;"></i>
            </div>
            <div class="stat-content">
              <span class="stat-value">{{ ratings.length }}</span>
              <span class="stat-label">评分总数</span>
            </div>
          </div>

          <div class="stat-card">
            <div class="stat-icon" style="background: rgba(245, 158, 11, 0.1);">
              <i class="pi pi-chart-line" style="color: #f59e0b;"></i>
            </div>
            <div class="stat-content">
              <span class="stat-value">{{ averageRating.toFixed(1) }}</span>
              <span class="stat-label">平均评分</span>
            </div>
          </div>

          <div class="stat-card">
            <div class="stat-icon" style="background: rgba(236, 72, 153, 0.1);">
              <i class="pi pi-heart-fill" style="color: #ec4899;"></i>
            </div>
            <div class="stat-content">
              <span class="stat-value">{{ highRatingsCount }}</span>
              <span class="stat-label">高分电影 (≥4星)</span>
            </div>
          </div>

          <div class="stat-card action-card">
            <router-link to="/recommendations" class="action-link">
              <i class="pi pi-sparkles"></i>
              <span>获取推荐</span>
            </router-link>
          </div>
        </div>

        <div class="ratings-section">
          <UserRatingsTable
            :ratings="ratings"
            :loading="loading"
            :error="error"
            @movie-clicked="handleMovieClick"
          />
        </div>
      </div>

      <div v-else-if="!hasLoaded" class="welcome-state">
        <div class="welcome-icon">
          <i class="pi pi-user"></i>
        </div>
        <h2>查看用户评分记录</h2>
        <p>输入用户 ID 来查看该用户的所有电影评分</p>
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
import { getUserRatings } from '@/api';
import UserRatingsTable from '@/components/UserRatingsTable.vue';
import MovieDetailDialog from '@/components/MovieDetailDialog.vue';

const userId = ref(100);
const ratings = ref([]);
const loading = ref(false);
const error = ref('');
const hasLoaded = ref(false);
const dialogVisible = ref(false);
const selectedMovie = ref(null);

const averageRating = computed(() => {
  if (ratings.value.length === 0) return 0;
  const sum = ratings.value.reduce((acc, r) => acc + r.rating, 0);
  return sum / ratings.value.length;
});

const highRatingsCount = computed(() => {
  return ratings.value.filter(r => r.rating >= 4).length;
});

const loadUserRatings = async () => {
  if (!userId.value) return;

  loading.value = true;
  error.value = '';
  hasLoaded.value = true;

  try {
    const data = await getUserRatings(userId.value);
    ratings.value = data || [];
  } catch (err) {
    error.value = err.response?.data?.message || err.message || '加载评分失败';
    ratings.value = [];
  } finally {
    loading.value = false;
  }
};

const handleMovieClick = (rating) => {
  selectedMovie.value = {
    id: rating.movieId,
    name: rating.movieName,
    movieName: rating.movieName,
    userRating: rating.rating
  };
  dialogVisible.value = true;
};
</script>

<style scoped>
.user-profile-page {
  min-height: calc(100vh - 64px);
  background: linear-gradient(180deg, rgba(249, 250, 251, 0.5) 0%, rgba(255, 255, 255, 0.8) 100%);
}

.page-container {
  max-width: 1200px;
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

.user-selector {
  margin-bottom: 32px;
}

.selector-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(229, 231, 235, 0.6);
  border-radius: 20px;
  padding: 28px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.04);
}

.selector-label {
  display: block;
  margin-bottom: 12px;
  font-size: 15px;
  font-weight: 600;
  color: #4b5563;
}

.selector-input-group {
  display: flex;
  gap: 12px;
}

.user-input {
  flex: 1;
  height: 52px;
  padding: 0 20px;
  border: 2px solid rgba(229, 231, 235, 0.6);
  border-radius: 14px;
  font-size: 16px;
  background: rgba(255, 255, 255, 0.9);
  transition: all 0.3s ease;
}

.user-input:focus {
  outline: none;
  border-color: #6366f1;
  box-shadow: 0 0 0 4px rgba(99, 102, 241, 0.1);
}

.load-btn {
  height: 52px;
  padding: 0 32px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #ffffff;
  border: none;
  border-radius: 14px;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 16px rgba(99, 102, 241, 0.3);
}

.load-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(99, 102, 241, 0.4);
}

.load-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(229, 231, 235, 0.6);
  border-radius: 20px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 15px 50px rgba(0, 0, 0, 0.08);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
}

.stat-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-value {
  font-size: 32px;
  font-weight: 800;
  color: #1f2937;
  line-height: 1;
}

.stat-label {
  font-size: 13px;
  color: #6b7280;
  font-weight: 600;
}

.action-card {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border: none;
  cursor: pointer;
  padding: 0;
  overflow: hidden;
}

.action-card:hover {
  transform: translateY(-4px) scale(1.02);
}

.action-link {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 24px;
  color: #ffffff;
  text-decoration: none;
  font-size: 18px;
  font-weight: 700;
}

.action-link i {
  font-size: 24px;
}

.ratings-section {
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

@media (max-width: 768px) {
  .page-title {
    font-size: 32px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .selector-input-group {
    flex-direction: column;
  }

  .load-btn {
    width: 100%;
    justify-content: center;
  }
}
</style>
