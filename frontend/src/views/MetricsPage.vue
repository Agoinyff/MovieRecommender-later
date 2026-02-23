<template>
  <div class="metrics-page">
    <div class="page-container">
      <header class="page-header">
        <div>
          <h1 class="page-title">系统监控</h1>
          <p class="page-description">实时性能指标与缓存统计</p>
        </div>
        <div class="header-actions">
          <button @click="toggleAutoRefresh" class="toggle-btn" :class="{ active: autoRefresh }">
            <i :class="autoRefresh ? 'pi pi-pause' : 'pi pi-play'"></i>
            <span>{{ autoRefresh ? '停止刷新' : '自动刷新' }}</span>
          </button>
          <button @click="refreshAll" class="refresh-btn" :disabled="loading">
            <i class="pi pi-refresh" :class="{ 'pi-spin': loading }"></i>
          </button>
        </div>
      </header>

      <!-- Health Status Section -->
      <div class="health-section">
        <div class="section-card">
          <div class="section-header">
            <h2 class="section-title">
              <i class="pi pi-heart-fill"></i>
              <span>系统健康状态</span>
            </h2>
          </div>
          <div class="health-content">
            <div class="health-item">
              <HealthStatusBadge :status="healthStatus" :details="healthDetails" />
              <span class="health-label">服务状态</span>
            </div>
            <div class="health-item">
              <div class="health-value">
                {{ formatNumber(totalRatings) }}
              </div>
              <span class="health-label">评分总数</span>
            </div>
            <div class="health-item">
              <div class="health-value">
                {{ uptime }}
              </div>
              <span class="health-label">运行时间</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Memory Metrics Section -->
      <div class="metrics-section">
        <h2 class="section-title">
          <i class="pi pi-server"></i>
          <span>内存使用情况</span>
        </h2>
        <div class="metrics-grid">
          <MetricsCard
            title="总内存"
            :value="formatBytes(memoryStats.totalMemory)"
            icon="pi pi-database"
            color="#6366f1"
          />
          <MetricsCard
            title="已用内存"
            :value="formatBytes(memoryStats.usedMemory)"
            icon="pi pi-chart-bar"
            color="#f59e0b"
            :percentage="memoryUsagePercent"
            :subtitle="`使用率: ${memoryUsagePercent.toFixed(1)}%`"
          />
          <MetricsCard
            title="空闲内存"
            :value="formatBytes(memoryStats.freeMemory)"
            icon="pi pi-inbox"
            color="#10b981"
          />
          <MetricsCard
            title="最大内存"
            :value="formatBytes(memoryStats.maxMemory)"
            icon="pi pi-arrow-up"
            color="#8b5cf6"
          />
        </div>
      </div>

      <!-- Cache Statistics Section -->
      <div class="cache-section">
        <h2 class="section-title">
          <i class="pi pi-bolt"></i>
          <span>缓存性能</span>
        </h2>
        <CacheStatsPanel
          :cache-stats="cacheStats"
          :loading="cacheLoading"
          :error="cacheError"
          :refreshing="loading"
          @refresh="loadCacheStats"
          @clear-cache="handleClearCache"
        />
      </div>

      <!-- Last Updated Info -->
      <div v-if="lastUpdated" class="update-info">
        <i class="pi pi-clock"></i>
        <span>最后更新: {{ lastUpdatedText }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { getMemoryStats, getCacheStats, clearAllCaches, getSystemStatus, getRatingCount } from '@/api';
import MetricsCard from '@/components/MetricsCard.vue';
import CacheStatsPanel from '@/components/CacheStatsPanel.vue';
import HealthStatusBadge from '@/components/HealthStatusBadge.vue';

const loading = ref(false);
const cacheLoading = ref(false);
const cacheError = ref('');
const autoRefresh = ref(false);
const lastUpdated = ref(null);
const refreshInterval = ref(null);

// Health data
const healthStatus = ref('unknown');
const healthDetails = ref('');
const totalRatings = ref(0);
const startTime = ref(Date.now());

// Memory data
const memoryStats = ref({
  totalMemory: 0,
  usedMemory: 0,
  freeMemory: 0,
  maxMemory: 0
});

// Cache data
const cacheStats = ref({});

const memoryUsagePercent = computed(() => {
  if (memoryStats.value.totalMemory === 0) return 0;
  return (memoryStats.value.usedMemory / memoryStats.value.totalMemory) * 100;
});

const uptime = computed(() => {
  const seconds = Math.floor((Date.now() - startTime.value) / 1000);
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  return `${hours}h ${minutes}m`;
});

const lastUpdatedText = computed(() => {
  if (!lastUpdated.value) return '';
  const now = new Date();
  const diff = Math.floor((now - lastUpdated.value) / 1000);
  if (diff < 60) return `${diff}秒前`;
  if (diff < 3600) return `${Math.floor(diff / 60)}分钟前`;
  return lastUpdated.value.toLocaleTimeString('zh-CN');
});

const formatBytes = (bytes) => {
  if (!bytes) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return `${(bytes / Math.pow(k, i)).toFixed(2)} ${sizes[i]}`;
};

const formatNumber = (num) => {
  if (num === undefined || num === null) return '0';
  return num.toLocaleString();
};

const loadHealthStatus = async () => {
  try {
    const status = await getSystemStatus();
    healthStatus.value = status.status === 'ok' ? 'online' : 'error';
    healthDetails.value = `系统服务运行正常`;
  } catch (err) {
    healthStatus.value = 'offline';
    healthDetails.value = '无法连接到后端服务';
    console.error('Failed to load health status:', err);
  }
};

const loadRatingCount = async () => {
  try {
    // Add timeout to prevent hanging
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 10000); // 10 second timeout
    
    const response = await fetch('http://localhost:8080/api/health/rating-count', {
      signal: controller.signal
    });
    clearTimeout(timeoutId);
    
    const data = await response.json();
    totalRatings.value = data.totalRatings || 0;
  } catch (err) {
    if (err.name === 'AbortError') {
      console.warn('Rating count request timed out');
      totalRatings.value = 0; // Keep default value
    } else {
      console.error('Failed to load rating count:', err);
    }
  }
};

const loadMemoryStats = async () => {
  try {
    const data = await getMemoryStats();
    // Support both formats: with and without MB suffix
    memoryStats.value = {
      totalMemory: data.totalMemory || data.totalMemoryMB * 1024 * 1024 || 0,
      usedMemory: data.usedMemory || data.usedMemoryMB * 1024 * 1024 || 0,
      freeMemory: data.freeMemory || data.freeMemoryMB * 1024 * 1024 || 0,
      maxMemory: data.maxMemory || data.maxMemoryMB * 1024 * 1024 || 0
    };
  } catch (err) {
    console.error('Failed to load memory stats:', err);
  }
};

const loadCacheStats = async () => {
  cacheLoading.value = true;
  cacheError.value = '';
  try {
    const data = await getCacheStats();
    cacheStats.value = data || {};
  } catch (err) {
    cacheError.value = err.response?.data?.message || err.message || '加载缓存统计失败';
    cacheStats.value = {};
  } finally {
    cacheLoading.value = false;
  }
};

const refreshAll = async () => {
  loading.value = true;
  
  // Load all metrics independently, don't wait for all to complete
  // This way one timeout won't block the others
  const promises = [
    loadHealthStatus(),
    loadRatingCount(),
    loadMemoryStats(),
    loadCacheStats()
  ];
  
  // Wait for all to complete (whether success or failure)
  await Promise.allSettled(promises);
  
  lastUpdated.value = new Date();
  loading.value = false;
};

const toggleAutoRefresh = () => {
  autoRefresh.value = !autoRefresh.value;
  
  if (autoRefresh.value) {
    refreshInterval.value = setInterval(() => {
      refreshAll();
    }, 5000); // Refresh every 5 seconds
  } else {
    if (refreshInterval.value) {
      clearInterval(refreshInterval.value);
      refreshInterval.value = null;
    }
  }
};

const handleClearCache = async () => {
  try {
    await clearAllCaches();
    await loadCacheStats();
  } catch (err) {
    alert('清除缓存失败: ' + (err.response?.data?.message || err.message));
  }
};

onMounted(() => {
  refreshAll();
});

onUnmounted(() => {
  if (refreshInterval.value) {
    clearInterval(refreshInterval.value);
  }
});
</script>

<style scoped>
.metrics-page {
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
  align-items: flex-start;
  margin-bottom: 32px;
  gap: 20px;
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

.header-actions {
  display: flex;
  gap: 12px;
}

.toggle-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 44px;
  padding: 0 20px;
  border: 2px solid rgba(229, 231, 235, 0.6);
  background: #ffffff;
  color: #4b5563;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.toggle-btn:hover {
  border-color: #6366f1;
  color: #6366f1;
  background: rgba(99, 102, 241, 0.05);
}

.toggle-btn.active {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #ffffff;
  border-color: transparent;
}

.refresh-btn {
  width: 44px;
  height: 44px;
  border: 2px solid rgba(229, 231, 235, 0.6);
  background: #ffffff;
  color: #4b5563;
  border-radius: 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.refresh-btn:hover:not(:disabled) {
  border-color: #6366f1;
  color: #6366f1;
  background: rgba(99, 102, 241, 0.05);
  transform: rotate(90deg);
}

.refresh-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.health-section {
  margin-bottom: 32px;
}

.section-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(229, 231, 235, 0.6);
  border-radius: 20px;
  padding: 28px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.04);
}

.section-header {
  margin-bottom: 24px;
}

.section-title {
  margin: 0 0 24px;
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
  display: flex;
  align-items: center;
  gap: 12px;
}

.section-title i {
  color: #6366f1;
  font-size: 24px;
}

.health-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 24px;
}

.health-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 20px;
  background: rgba(249, 250, 251, 0.8);
  border-radius: 16px;
  border: 1px solid rgba(229, 231, 235, 0.4);
}

.health-value {
  font-size: 32px;
  font-weight: 800;
  color: #1f2937;
}

.health-label {
  font-size: 13px;
  color: #6b7280;
  font-weight: 600;
}

.metrics-section,
.cache-section {
  margin-bottom: 32px;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 20px;
}

.update-info {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 32px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 12px;
  font-size: 13px;
  color: #6b7280;
}

.update-info i {
  font-size: 14px;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: stretch;
  }

  .page-title {
    font-size: 32px;
  }

  .header-actions {
    width: 100%;
  }

  .toggle-btn {
    flex: 1;
    justify-content: center;
  }

  .metrics-grid {
    grid-template-columns: 1fr;
  }

  .health-content {
    grid-template-columns: 1fr;
  }
}
</style>
