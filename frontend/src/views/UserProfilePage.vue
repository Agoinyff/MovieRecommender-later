<template>
  <div class="user-profile-page">
    <div class="page-container">
      <header class="page-header">
        <div>
          <h1 class="page-title">我的评分</h1>
          <p class="page-description">围绕当前登录用户管理评分记录，并让推荐结果随评分变化实时联动。</p>
        </div>
        <div class="identity-card">
          <div class="identity-avatar">{{ authStore.user.displayName.slice(0, 1).toUpperCase() }}</div>
          <div class="identity-meta">
            <strong>{{ authStore.user.displayName }}</strong>
            <span>@{{ authStore.user.username }} · {{ authStore.isAdmin ? '管理员' : '普通用户' }}</span>
          </div>
        </div>
      </header>

      <div v-if="loading && !hasLoaded" class="welcome-state">
        <div class="welcome-icon"><i class="pi pi-spin pi-spinner"></i></div>
        <h2>正在载入我的评分中心</h2>
      </div>

      <template v-else>
        <div class="stats-grid">
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
              <span class="stat-label">高分电影 (>= 4)</span>
            </div>
          </div>

          <div class="stat-card action-card">
            <router-link to="/recommendations" class="action-link">
              <i class="pi pi-sparkles"></i>
              <span>查看个性化推荐</span>
            </router-link>
          </div>
        </div>

        <div v-if="error" class="error-message">
          <i class="pi pi-exclamation-triangle"></i>
          <p>{{ error }}</p>
        </div>

        <div v-if="hasLoaded && ratings.length === 0 && !error" class="welcome-state compact">
          <div class="welcome-icon"><i class="pi pi-star"></i></div>
          <h2>你还没有评分记录</h2>
          <p>去电影库为几部电影打分，推荐页就会开始围绕你的账号生成结果。</p>
          <router-link to="/movies" class="browse-btn">去电影库评分</router-link>
        </div>

        <div class="ratings-section" v-if="ratings.length > 0 || loading">
          <UserRatingsTable
            :ratings="ratings"
            :loading="loading"
            :error="error"
            @movie-clicked="handleMovieClick"
          />
        </div>
      </template>
    </div>

    <MovieDetailDialog
      v-model:visible="dialogVisible"
      :movie="selectedMovie"
      @rating-updated="loadMyRatings"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { getMyRatings } from '@/api';
import { useAuthStore } from '@/store/auth';
import UserRatingsTable from '@/components/UserRatingsTable.vue';
import MovieDetailDialog from '@/components/MovieDetailDialog.vue';

const authStore = useAuthStore();
const ratings = ref([]);
const loading = ref(false);
const error = ref('');
const hasLoaded = ref(false);
const dialogVisible = ref(false);
const selectedMovie = ref(null);

const averageRating = computed(() => {
  if (ratings.value.length === 0) return 0;
  const sum = ratings.value.reduce((acc, item) => acc + item.rating, 0);
  return sum / ratings.value.length;
});

const highRatingsCount = computed(() => ratings.value.filter((item) => item.rating >= 4).length);

const loadMyRatings = async () => {
  loading.value = true;
  error.value = '';
  try {
    const data = await getMyRatings();
    ratings.value = data || [];
  } catch (err) {
    error.value = err.response?.data?.message || err.message || '加载评分失败';
    ratings.value = [];
  } finally {
    loading.value = false;
    hasLoaded.value = true;
  }
};

const handleMovieClick = (rating) => {
  selectedMovie.value = {
    id: rating.movieId,
    name: rating.movieName,
    userRating: rating.rating
  };
  dialogVisible.value = true;
};

onMounted(() => {
  loadMyRatings();
});
</script>

<style scoped>
.user-profile-page {
  min-height: calc(100vh - 64px);
  background: linear-gradient(180deg, rgba(249, 250, 251, 0.5) 0%, rgba(255, 255, 255, 0.8) 100%);
}

.page-container {
  max-width: 1240px;
  margin: 0 auto;
  padding: 40px 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
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

.identity-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
  background: rgba(255, 255, 255, 0.92);
  border-radius: 20px;
  border: 1px solid rgba(229, 231, 235, 0.7);
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.04);
}

.identity-avatar {
  width: 52px;
  height: 52px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  font-weight: 800;
  font-size: 20px;
}

.identity-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.identity-meta strong {
  color: #1f2937;
}

.identity-meta span {
  color: #6b7280;
  font-size: 13px;
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
}

.stat-label {
  font-size: 13px;
  color: #6b7280;
  font-weight: 600;
}

.action-card {
  padding: 0;
  overflow: hidden;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border: none;
}

.action-link {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 24px;
  color: #fff;
  text-decoration: none;
  font-size: 18px;
  font-weight: 700;
}

.error-message {
  margin-bottom: 24px;
  padding: 18px 20px;
  border-radius: 16px;
  background: #fef2f2;
  color: #dc2626;
  display: flex;
  align-items: center;
  gap: 10px;
}

.ratings-section {
  animation: slideUp 0.35s ease;
}

.welcome-state {
  text-align: center;
  padding: 100px 24px;
}

.welcome-state.compact {
  padding: 48px 24px 64px;
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
  font-size: 52px;
  color: #fff;
  box-shadow: 0 20px 50px rgba(99, 102, 241, 0.3);
}

.welcome-state h2 {
  margin: 0 0 12px;
  font-size: 30px;
  color: #1f2937;
}

.welcome-state p {
  margin: 0 auto;
  max-width: 520px;
  color: #6b7280;
  line-height: 1.7;
}

.browse-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-top: 24px;
  padding: 14px 26px;
  border-radius: 14px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  font-weight: 700;
  text-decoration: none;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(16px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
  }

  .page-title {
    font-size: 32px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
