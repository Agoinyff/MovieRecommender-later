<template>
  <div class="profile-page">
    <div class="page-container">
      <header class="page-header">
        <div>
          <h1>个人中心</h1>
          <p>查看你的评分画像、搜索评分记录，并继续回流推荐系统。</p>
        </div>
      </header>

      <section class="profile-summary">
        <div class="summary-card">
          <span>用户名</span>
          <strong>{{ authStore.user?.username }}</strong>
        </div>
        <div class="summary-card">
          <span>角色</span>
          <strong>{{ authStore.user?.role === 'ADMIN' ? '管理员' : '普通用户' }}</strong>
        </div>
        <div class="summary-card">
          <span>累计评分</span>
          <strong>{{ stats.totalRatings || 0 }}</strong>
        </div>
        <div class="summary-card">
          <span>平均评分</span>
          <strong>{{ Number(stats.averageRating || 0).toFixed(1) }}</strong>
        </div>
      </section>

      <section class="toolbar-card">
        <div class="filters">
          <input v-model.trim="filters.query" type="text" placeholder="按电影名搜索" @keyup.enter="loadRatings(0)" />
          <select v-model="filters.minRating" @change="loadRatings(0)">
            <option value="">最低评分</option>
            <option v-for="n in 5" :key="n" :value="n">{{ n }} 星</option>
          </select>
          <select v-model="filters.maxRating" @change="loadRatings(0)">
            <option value="">最高评分</option>
            <option v-for="n in 5" :key="n" :value="n">{{ n }} 星</option>
          </select>
          <button @click="loadRatings(0)">查询</button>
        </div>
      </section>

      <div class="table-card">
        <LoadingSpinner v-if="loading" message="正在加载评分..." />
        <div v-else-if="error" class="state error">{{ error }}</div>
        <template v-else>
          <table v-if="ratings.length" class="ratings-table">
            <thead>
              <tr>
                <th>电影名</th>
                <th>评分</th>
                <th>评分时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in ratings" :key="item.movieId">
                <td>{{ item.movieName }}</td>
                <td>{{ item.rating.toFixed(1) }}</td>
                <td>{{ formatDate(item.timestamp) }}</td>
                <td>
                  <button class="link-btn" @click="openMovie(item)">预览</button>
                  <router-link class="link-btn" :to="`/movies/${item.movieId}`">详情</router-link>
                </td>
              </tr>
            </tbody>
          </table>
          <div v-else class="state">暂无评分记录</div>
        </template>
      </div>

      <div v-if="pagination.totalPages > 1" class="pagination">
        <button :disabled="pagination.page === 0" @click="loadRatings(pagination.page - 1)">上一页</button>
        <span>第 {{ pagination.page + 1 }} / {{ pagination.totalPages }} 页</span>
        <button :disabled="pagination.page + 1 >= pagination.totalPages" @click="loadRatings(pagination.page + 1)">下一页</button>
      </div>
    </div>

    <MovieDetailDialog v-model:visible="dialogVisible" :movie="selectedMovie" @rating-updated="handleRatingUpdated" />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { getMyRatings, getMyRatingStats } from '@/api';
import MovieDetailDialog from '@/components/MovieDetailDialog.vue';
import LoadingSpinner from '@/components/LoadingSpinner.vue';
import { useAuthStore } from '@/store/auth';

const authStore = useAuthStore();
const stats = ref({ totalRatings: 0, averageRating: 0 });
const ratings = ref([]);
const loading = ref(false);
const error = ref('');
const dialogVisible = ref(false);
const selectedMovie = ref(null);
const pagination = reactive({ page: 0, totalPages: 0, totalElements: 0 });
const filters = reactive({ query: '', minRating: '', maxRating: '' });

const loadStats = async () => {
  stats.value = await getMyRatingStats();
};

const loadRatings = async (page = 0) => {
  loading.value = true;
  error.value = '';
  try {
    const data = await getMyRatings({
      page,
      size: 10,
      query: filters.query || undefined,
      minRating: filters.minRating || undefined,
      maxRating: filters.maxRating || undefined
    });
    ratings.value = data.content || [];
    pagination.page = data.page || 0;
    pagination.totalPages = data.totalPages || 0;
    pagination.totalElements = data.totalElements || 0;
  } catch (err) {
    error.value = err.response?.data?.message || err.message || '加载评分失败';
    ratings.value = [];
  } finally {
    loading.value = false;
  }
};

const openMovie = (rating) => {
  selectedMovie.value = {
    id: rating.movieId,
    movieId: rating.movieId,
    name: rating.movieName,
    movieName: rating.movieName,
    userRating: rating.rating
  };
  dialogVisible.value = true;
};

const handleRatingUpdated = async () => {
  await Promise.all([loadStats(), loadRatings(pagination.page)]);
};

const formatDate = (timestamp) => {
  if (!timestamp) {
    return '-';
  }
  return new Date(timestamp * 1000).toLocaleString('zh-CN');
};

onMounted(async () => {
  await Promise.all([loadStats(), loadRatings(0)]);
});
</script>

<style scoped>
.profile-page {
  min-height: calc(100vh - 68px);
  background: linear-gradient(180deg, #fff7ed, #ffffff);
}

.page-container {
  max-width: 1240px;
  margin: 0 auto;
  padding: 40px 24px;
}

.page-header h1 {
  margin: 0 0 8px;
  font-size: 42px;
}

.page-header p {
  margin: 0 0 24px;
  color: #64748b;
}

.profile-summary {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 18px;
  margin-bottom: 24px;
}

.summary-card,
.toolbar-card,
.table-card {
  background: #fff;
  border-radius: 24px;
  padding: 22px;
  box-shadow: 0 18px 60px rgba(15, 23, 42, 0.06);
}

.summary-card span {
  display: block;
  color: #64748b;
  margin-bottom: 10px;
}

.summary-card strong {
  font-size: 28px;
  color: #111827;
}

.filters {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.filters input,
.filters select,
.filters button {
  height: 46px;
  border-radius: 14px;
}

.filters input,
.filters select {
  border: 1px solid #cbd5e1;
  padding: 0 14px;
}

.filters button,
.pagination button,
.link-btn {
  border: none;
  background: #ea580c;
  color: #fff;
  padding: 0 16px;
  font-weight: 700;
}

.ratings-table {
  width: 100%;
  border-collapse: collapse;
}

.ratings-table th,
.ratings-table td {
  padding: 14px 8px;
  border-bottom: 1px solid #e2e8f0;
  text-align: left;
}

.link-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 36px;
  margin-right: 8px;
  border-radius: 10px;
  text-decoration: none;
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
  gap: 14px;
  align-items: center;
}

.state {
  padding: 24px 0;
  text-align: center;
  color: #64748b;
}

.state.error {
  color: #dc2626;
}
</style>
