<template>
  <div class="user-ratings-table">
    <div class="table-header">
      <div class="search-box">
        <i class="pi pi-search"></i>
        <input
          v-model="searchQuery"
          type="text"
          placeholder="搜索电影名称..."
          class="search-input"
        />
      </div>
      <div class="table-info">
        共 {{ filteredRatings.length }} 条评分记录
      </div>
    </div>

    <div v-if="loading" class="loading-state">
      <i class="pi pi-spin pi-spinner"></i>
      <p>加载中...</p>
    </div>

    <div v-else-if="error" class="error-state">
      <i class="pi pi-exclamation-triangle"></i>
      <p>{{ error }}</p>
    </div>

    <div v-else-if="ratings.length === 0" class="empty-state">
      <i class="pi pi-inbox"></i>
      <p>暂无评分记录</p>
    </div>

    <div v-else class="table-container">
      <table class="ratings-table">
        <thead>
          <tr>
            <th @click="sortBy('movieName')" class="sortable">
              <span>电影名称</span>
              <i :class="getSortIcon('movieName')"></i>
            </th>
            <th @click="sortBy('rating')" class="sortable">
              <span>评分</span>
              <i :class="getSortIcon('rating')"></i>
            </th>
            <th @click="sortBy('timestamp')" class="sortable">
              <span>评分时间</span>
              <i :class="getSortIcon('timestamp')"></i>
            </th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="rating in paginatedRatings"
            :key="rating.movieId"
            @click="$emit('movie-clicked', rating)"
            class="rating-row"
          >
            <td class="movie-name-cell">
              <span class="movie-name">{{ rating.movieName }}</span>
            </td>
            <td class="rating-cell">
              <RatingStars :modelValue="rating.rating" :readonly="true" :size="20" />
              <span class="rating-value">{{ rating.rating.toFixed(1) }}</span>
            </td>
            <td class="timestamp-cell">
              {{ formatDate(rating.timestamp) }}
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="totalPages > 1" class="pagination">
        <button 
          @click="currentPage = Math.max(1, currentPage - 1)" 
          :disabled="currentPage === 1"
          class="page-btn"
        >
          <i class="pi pi-chevron-left"></i>
        </button>
        <span class="page-info">
          第 {{ currentPage }} / {{ totalPages }} 页
        </span>
        <button 
          @click="currentPage = Math.min(totalPages, currentPage + 1)" 
          :disabled="currentPage === totalPages"
          class="page-btn"
        >
          <i class="pi pi-chevron-right"></i>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import RatingStars from './RatingStars.vue';

const props = defineProps({
  ratings: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  },
  error: {
    type: String,
    default: ''
  }
});

const emit = defineEmits(['movie-clicked']);

const searchQuery = ref('');
const sortField = ref('timestamp');
const sortOrder = ref('desc');
const currentPage = ref(1);
const pageSize = 10;

const filteredRatings = computed(() => {
  let result = [...props.ratings];

  // Search filter
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase();
    result = result.filter(r => 
      r.movieName.toLowerCase().includes(query)
    );
  }

  // Sort
  result.sort((a, b) => {
    const aVal = a[sortField.value];
    const bVal = b[sortField.value];
    
    let comparison = 0;
    if (aVal < bVal) comparison = -1;
    if (aVal > bVal) comparison = 1;
    
    return sortOrder.value === 'asc' ? comparison : -comparison;
  });

  return result;
});

const totalPages = computed(() => {
  return Math.ceil(filteredRatings.value.length / pageSize);
});

const paginatedRatings = computed(() => {
  const start = (currentPage.value - 1) * pageSize;
  const end = start + pageSize;
  return filteredRatings.value.slice(start, end);
});

const sortBy = (field) => {
  if (sortField.value === field) {
    sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc';
  } else {
    sortField.value = field;
    sortOrder.value = 'desc';
  }
  currentPage.value = 1;
};

const getSortIcon = (field) => {
  if (sortField.value !== field) {
    return 'pi pi-sort-alt';
  }
  return sortOrder.value === 'asc' ? 'pi pi-sort-amount-up' : 'pi pi-sort-amount-down';
};

const formatDate = (timestamp) => {
  if (!timestamp) return '-';
  const date = new Date(timestamp * 1000); // Convert from seconds to milliseconds
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};
</script>

<style scoped>
.user-ratings-table {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(229, 231, 235, 0.6);
  border-radius: 20px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 28px;
  border-bottom: 1px solid rgba(229, 231, 235, 0.6);
  gap: 20px;
}

.search-box {
  flex: 1;
  max-width: 400px;
  position: relative;
  display: flex;
  align-items: center;
}

.search-box i {
  position: absolute;
  left: 16px;
  color: #9ca3af;
}

.search-input {
  width: 100%;
  height: 44px;
  padding: 0 16px 0 44px;
  border: 2px solid rgba(229, 231, 235, 0.6);
  border-radius: 12px;
  font-size: 14px;
  background: rgba(255, 255, 255, 0.9);
  transition: all 0.3s ease;
}

.search-input:focus {
  outline: none;
  border-color: #6366f1;
  box-shadow: 0 0 0 4px rgba(99, 102, 241, 0.1);
}

.table-info {
  font-size: 14px;
  color: #6b7280;
  font-weight: 500;
}

.loading-state,
.error-state,
.empty-state {
  padding: 80px 20px;
  text-align: center;
}

.loading-state {
  color: #9ca3af;
}

.loading-state i,
.error-state i,
.empty-state i {
  font-size: 56px;
  margin-bottom: 16px;
}

.error-state {
  color: #dc2626;
}

.empty-state {
  color: #9ca3af;
}

.table-container {
  overflow-x: auto;
}

.ratings-table {
  width: 100%;
  border-collapse: collapse;
}

.ratings-table thead {
  background: rgba(249, 250, 251, 0.8);
}

.ratings-table th {
  padding: 16px 20px;
  text-align: left;
  font-size: 13px;
  font-weight: 700;
  color: #4b5563;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border-bottom: 2px solid rgba(229, 231, 235, 0.6);
}

.sortable {
  cursor: pointer;
  user-select: none;
  transition: all 0.2s ease;
}

.sortable:hover {
  background: rgba(99, 102, 241, 0.05);
}

.sortable span {
  margin-right: 8px;
}

.sortable i {
  font-size: 12px;
  color: #9ca3af;
}

.rating-row {
  cursor: pointer;
  transition: all 0.2s ease;
  border-bottom: 1px solid rgba(229, 231, 235, 0.4);
}

.rating-row:hover {
  background: rgba(99, 102, 241, 0.03);
}

.ratings-table td {
  padding: 18px 20px;
  font-size: 15px;
  color: #1f2937;
}

.movie-name-cell {
  font-weight: 600;
  color: #1f2937;
}

.movie-name {
  display: block;
  max-width: 400px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.rating-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.rating-value {
  font-weight: 700;
  color: #fbbf24;
  font-size: 16px;
}

.timestamp-cell {
  color: #6b7280;
  font-size: 14px;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  padding: 24px;
  border-top: 1px solid rgba(229, 231, 235, 0.6);
}

.page-btn {
  width: 36px;
  height: 36px;
  border: 2px solid rgba(229, 231, 235, 0.6);
  background: #ffffff;
  border-radius: 10px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #4b5563;
  transition: all 0.2s ease;
}

.page-btn:hover:not(:disabled) {
  border-color: #6366f1;
  color: #6366f1;
  background: rgba(99, 102, 241, 0.05);
}

.page-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.page-info {
  font-size: 14px;
  font-weight: 600;
  color: #4b5563;
}

@media (max-width: 768px) {
  .table-header {
    flex-direction: column;
    align-items: stretch;
  }

  .search-box {
    max-width: 100%;
  }

  .ratings-table {
    font-size: 13px;
  }

  .ratings-table th,
  .ratings-table td {
    padding: 12px;
  }

  .movie-name {
    max-width: 200px;
  }
}
</style>
