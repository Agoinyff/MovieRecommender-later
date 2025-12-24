<template>
  <div class="panel">
    <header class="panel-head">
      <div>
        <p class="hint">推荐结果</p>
        <h2>Top picks</h2>
      </div>
      <span v-if="loading" class="pill">正在计算...</span>
    </header>

    <p v-if="error" class="error">{{ error }}</p>
    <p v-else-if="!loading && recommendations.length === 0" class="muted">暂无数据，请先提交查询。</p>

    <ul v-if="recommendations.length" class="list">
      <li v-for="item in recommendations" :key="item.movieId" class="row">
        <div>
          <div class="title">{{ item.name }}</div>
          <div class="meta">
            <span>{{ item.publishedYear || '未知年份' }}</span>
            <span v-if="item.genres">· {{ item.genres }}</span>
          </div>
        </div>
        <span class="pill score">{{ item.score.toFixed(2) }}</span>
      </li>
    </ul>
  </div>
</template>

<script setup>
const props = defineProps({
  recommendations: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  error: { type: String, default: '' }
});
</script>

<style scoped>
.panel {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  padding: 18px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.04);
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.hint {
  margin: 0;
  color: #6b7280;
  font-weight: 600;
  letter-spacing: 0.01em;
}

h2 {
  margin: 4px 0 0;
}

.list {
  list-style: none;
  padding: 0;
  margin: 12px 0 0;
  display: grid;
  gap: 10px;
}

.row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
}

.title {
  font-weight: 700;
}

.meta {
  color: #6b7280;
  font-size: 14px;
}

.pill {
  background: #eef2ff;
  color: #4338ca;
  padding: 6px 10px;
  border-radius: 999px;
  font-weight: 700;
}

.pill.score {
  background: #ecfeff;
  color: #0ea5e9;
}

.muted {
  color: #9ca3af;
}

.error {
  color: #dc2626;
  background: #fef2f2;
  padding: 10px;
  border-radius: 12px;
  border: 1px solid #fecaca;
}
</style>
