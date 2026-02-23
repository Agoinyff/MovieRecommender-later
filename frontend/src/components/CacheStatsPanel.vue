<template>
  <div class="cache-stats-panel">
    <div class="panel-header">
      <h3 class="panel-title">
        <i class="pi pi-database"></i>
        <span>缓存统计</span>
      </h3>
      <button 
        @click="$emit('refresh')" 
        class="refresh-btn"
        :class="{ spinning: refreshing }"
      >
        <i class="pi pi-refresh"></i>
      </button>
    </div>

    <div v-if="loading" class="loading-state">
      <i class="pi pi-spin pi-spinner"></i>
      <p>加载中...</p>
    </div>

    <div v-else-if="error" class="error-state">
      <i class="pi pi-exclamation-triangle"></i>
      <p>{{ error }}</p>
    </div>

    <div v-else class="cache-list">
      <div 
        v-for="(stats, cacheName) in cacheStats" 
        :key="cacheName"
        class="cache-item"
      >
        <div class="cache-header">
          <h4 class="cache-name">{{ cacheName }}</h4>
          <span class="hit-rate" :class="getHitRateClass(stats.hitRate)">
            {{ formatHitRate(stats.hitRate) }}
          </span>
        </div>

        <div class="cache-metrics">
          <div class="metric">
            <span class="metric-label">命中次数</span>
            <span class="metric-value">{{ formatNumber(stats.hitCount) }}</span>
          </div>
          <div class="metric">
            <span class="metric-label">未命中</span>
            <span class="metric-value">{{ formatNumber(stats.missCount) }}</span>
          </div>
          <div class="metric">
            <span class="metric-label">当前大小</span>
            <span class="metric-value">{{ formatNumber(stats.size) }}</span>
          </div>
        </div>

        <div class="hit-rate-bar">
          <div 
            class="hit-rate-fill" 
            :style="{ width: `${(stats.hitRate || 0) * 100}%` }"
            :class="getHitRateClass(stats.hitRate)"
          ></div>
        </div>
      </div>

      <div v-if="Object.keys(cacheStats).length === 0" class="empty-state">
        <i class="pi pi-inbox"></i>
        <p>暂无缓存数据</p>
      </div>
    </div>

    <div v-if="!loading && !error && Object.keys(cacheStats).length > 0" class="panel-footer">
      <button @click="handleClearCache" :disabled="clearing" class="clear-btn">
        <i :class="clearing ? 'pi pi-spin pi-spinner' : 'pi pi-trash'"></i>
        <span>{{ clearing ? '清理中...' : '清除所有缓存' }}</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const props = defineProps({
  cacheStats: {
    type: Object,
    default: () => ({})
  },
  loading: {
    type: Boolean,
    default: false
  },
  error: {
    type: String,
    default: ''
  },
  refreshing: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['refresh', 'clear-cache']);

const clearing = ref(false);

const formatNumber = (num) => {
  if (num === undefined || num === null) return '0';
  return num.toLocaleString();
};

const formatHitRate = (rate) => {
  if (rate === undefined || rate === null || isNaN(rate)) return 'NaN%';
  return `${(rate * 100).toFixed(1)}%`;
};

const getHitRateClass = (rate) => {
  if (!rate) return 'rate-low';
  if (rate >= 0.8) return 'rate-high';
  if (rate >= 0.5) return 'rate-medium';
  return 'rate-low';
};

const handleClearCache = () => {
  if (confirm('确定要清除所有缓存吗？此操作无法撤销。')) {
    clearing.value = true;
    emit('clear-cache');
    setTimeout(() => {
      clearing.value = false;
    }, 2000);
  }
};
</script>

<style scoped>
.cache-stats-panel {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(229, 231, 235, 0.6);
  border-radius: 20px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 28px;
  border-bottom: 1px solid rgba(229, 231, 235, 0.6);
}

.panel-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
  display: flex;
  align-items: center;
  gap: 12px;
}

.panel-title i {
  color: #6366f1;
}

.refresh-btn {
  width: 40px;
  height: 40px;
  border: none;
  background: rgba(99, 102, 241, 0.1);
  color: #6366f1;
  border-radius: 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.refresh-btn:hover {
  background: rgba(99, 102, 241, 0.2);
  transform: rotate(90deg);
}

.refresh-btn.spinning i {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.loading-state,
.error-state {
  padding: 60px 20px;
  text-align: center;
  color: #9ca3af;
}

.loading-state i,
.error-state i {
  font-size: 48px;
  margin-bottom: 16px;
}

.error-state {
  color: #dc2626;
}

.cache-list {
  padding: 24px;
  display: grid;
  gap: 20px;
}

.cache-item {
  background: rgba(249, 250, 251, 0.8);
  border: 1px solid rgba(229, 231, 235, 0.6);
  border-radius: 16px;
  padding: 20px;
  transition: all 0.3s ease;
}

.cache-item:hover {
  border-color: rgba(99, 102, 241, 0.3);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
}

.cache-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.cache-name {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #1f2937;
}

.hit-rate {
  padding: 6px 12px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 700;
}

.rate-high {
  background: rgba(16, 185, 129, 0.1);
  color: #059669;
}

.rate-medium {
  background: rgba(245, 158, 11, 0.1);
  color: #d97706;
}

.rate-low {
  background: rgba(239, 68, 68, 0.1);
  color: #dc2626;
}

.cache-metrics {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 12px;
}

.metric {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.metric-label {
  font-size: 12px;
  color: #6b7280;
  font-weight: 500;
}

.metric-value {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
}

.hit-rate-bar {
  height: 6px;
  background: #e5e7eb;
  border-radius: 10px;
  overflow: hidden;
}

.hit-rate-fill {
  height: 100%;
  transition: width 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 10px;
}

.hit-rate-fill.rate-high {
  background: linear-gradient(90deg, #10b981, #059669);
}

.hit-rate-fill.rate-medium {
  background: linear-gradient(90deg, #f59e0b, #d97706);
}

.hit-rate-fill.rate-low {
  background: linear-gradient(90deg, #ef4444, #dc2626);
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #9ca3af;
}

.empty-state i {
  font-size: 56px;
  margin-bottom: 16px;
}

.panel-footer {
  padding: 20px 28px;
  border-top: 1px solid rgba(229, 231, 235, 0.6);
  background: rgba(249, 250, 251, 0.5);
}

.clear-btn {
  width: 100%;
  height: 48px;
  border: 2px solid #ef4444;
  background: #ffffff;
  color: #ef4444;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.2s ease;
}

.clear-btn:hover:not(:disabled) {
  background: #ef4444;
  color: #ffffff;
}

.clear-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .cache-metrics {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .metric {
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
  }
}
</style>
