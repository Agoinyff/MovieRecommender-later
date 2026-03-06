<template>
  <div class="metrics-page">
    <div class="page-container">
      <header class="page-header">
        <div>
          <h1>系统监控</h1>
          <p>管理员查看缓存和内存指标。</p>
        </div>
        <button class="refresh-btn" @click="loadAll">刷新</button>
      </header>

      <div class="metrics-grid">
        <div class="metric-card">
          <span>评分总数</span>
          <strong>{{ ratingCount.totalRatings || 0 }}</strong>
        </div>
        <div class="metric-card">
          <span>总内存(MB)</span>
          <strong>{{ memory.totalMemoryMB || 0 }}</strong>
        </div>
        <div class="metric-card">
          <span>已用内存(MB)</span>
          <strong>{{ memory.usedMemoryMB || 0 }}</strong>
        </div>
        <div class="metric-card">
          <span>系统状态</span>
          <strong>{{ health.status || 'unknown' }}</strong>
        </div>
      </div>

      <section class="table-card">
        <div class="table-header">
          <h2>缓存统计</h2>
          <button @click="clearCache">清空缓存</button>
        </div>
        <table class="cache-table" v-if="cacheEntries.length">
          <thead>
            <tr>
              <th>缓存名</th>
              <th>命中率</th>
              <th>命中</th>
              <th>未命中</th>
              <th>大小</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="entry in cacheEntries" :key="entry.name">
              <td>{{ entry.name }}</td>
              <td>{{ (entry.stats.hitRate * 100).toFixed(1) }}%</td>
              <td>{{ entry.stats.hitCount }}</td>
              <td>{{ entry.stats.missCount }}</td>
              <td>{{ entry.stats.size }}</td>
            </tr>
          </tbody>
        </table>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { clearAllCaches, getCacheStats, getMemoryStats, getRatingCount, getSystemStatus } from '@/api';

const memory = ref({});
const cache = ref({});
const ratingCount = ref({});
const health = ref({});

const cacheEntries = computed(() => Object.entries(cache.value).map(([name, stats]) => ({ name, stats })));

const loadAll = async () => {
  const [memoryData, cacheData, ratingData, healthData] = await Promise.all([
    getMemoryStats(),
    getCacheStats(),
    getRatingCount(),
    getSystemStatus()
  ]);
  memory.value = memoryData;
  cache.value = cacheData;
  ratingCount.value = ratingData;
  health.value = healthData;
};

const clearCache = async () => {
  await clearAllCaches();
  await loadAll();
};

onMounted(loadAll);
</script>

<style scoped>
.metrics-page {
  min-height: calc(100vh - 68px);
  background: linear-gradient(180deg, #fff7ed, #ffffff);
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
  gap: 16px;
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

.refresh-btn,
.table-header button {
  height: 42px;
  border: none;
  border-radius: 12px;
  background: #ea580c;
  color: #fff;
  padding: 0 14px;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 18px;
  margin-bottom: 24px;
}

.metric-card,
.table-card {
  background: #fff;
  border-radius: 24px;
  padding: 22px;
  box-shadow: 0 18px 60px rgba(15, 23, 42, 0.06);
}

.metric-card span {
  color: #64748b;
}

.metric-card strong {
  display: block;
  margin-top: 10px;
  font-size: 28px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
}

.cache-table {
  width: 100%;
  border-collapse: collapse;
}

.cache-table th,
.cache-table td {
  padding: 12px 8px;
  border-bottom: 1px solid #e2e8f0;
  text-align: left;
}
</style>
