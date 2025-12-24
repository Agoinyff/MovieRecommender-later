<template>
  <div v-if="visible" class="overlay" @click.self="emit('close')">
    <div class="dialog">
      <header>
        <h2>电影详情</h2>
        <button class="close" @click="emit('close')">×</button>
      </header>
      <div v-if="loading" class="status">加载中...</div>
      <div v-else-if="error" class="error">{{ error }}</div>
      <div v-else-if="movie" class="body">
        <h3>{{ movie.name }}</h3>
        <p class="meta">{{ movie.publishedYear || '未知年份' }} · {{ movie.genres || '暂无类型' }}</p>
        <div class="info-grid">
          <div>
            <span class="label">Movie ID</span>
            <span class="value">{{ movie.id ?? movie.movieId ?? '-' }}</span>
          </div>
          <div>
            <span class="label">类型</span>
            <span class="value">{{ movie.genres || '暂无' }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  visible: { type: Boolean, default: false },
  movie: { type: Object, default: null },
  loading: { type: Boolean, default: false },
  error: { type: String, default: '' }
});

const emit = defineEmits(['close']);
</script>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.7);
  backdrop-filter: blur(8px);
  display: grid;
  place-items: center;
  z-index: 50;
}

.dialog {
  width: min(540px, 92vw);
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(148, 163, 184, 0.3);
  border-radius: 20px;
  padding: 20px;
  color: #e2e8f0;
  box-shadow: 0 30px 80px rgba(15, 23, 42, 0.45);
}

header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

h2 {
  margin: 0;
}

.close {
  background: transparent;
  border: none;
  color: #e2e8f0;
  font-size: 24px;
  cursor: pointer;
}

.body {
  margin-top: 16px;
}

.meta {
  margin: 4px 0 16px;
  color: rgba(226, 232, 240, 0.7);
}

.info-grid {
  display: grid;
  gap: 12px;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
}

.label {
  display: block;
  font-size: 12px;
  color: rgba(226, 232, 240, 0.6);
}

.value {
  font-weight: 600;
}

.status {
  padding: 12px;
}

.error {
  padding: 12px;
  background: rgba(248, 113, 113, 0.2);
  border-radius: 12px;
}
</style>
