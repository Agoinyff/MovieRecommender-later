<template>
  <section class="panel">
    <header class="panel-head">
      <div>
        <p class="hint">推荐结果</p>
        <h2>Top picks</h2>
      </div>
      <span v-if="loading" class="pill">正在计算...</span>
    </header>

    <p v-if="error" class="error">{{ error }}</p>
    <p v-else-if="!loading && recommendations.length === 0" class="muted">暂无数据，请先提交查询。</p>

    <transition-group v-if="recommendations.length" name="fade-stagger" tag="div" class="grid">
      <MovieCard
        v-for="(item, index) in recommendations"
        :key="item.movieId"
        :movie="item"
        :score="item.score"
        :style="{ transitionDelay: `${index * 60}ms` }"
        @select="emit('select', item)"
      />
    </transition-group>
  </section>
</template>

<script setup>
import MovieCard from './MovieCard.vue';

const props = defineProps({
  recommendations: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  error: { type: String, default: '' }
});

const emit = defineEmits(['select']);
</script>

<style scoped>
.panel {
  background: rgba(15, 23, 42, 0.55);
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 24px;
  padding: 22px;
  box-shadow: 0 20px 60px rgba(15, 23, 42, 0.4);
  backdrop-filter: blur(16px);
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.hint {
  margin: 0;
  color: rgba(226, 232, 240, 0.7);
  font-weight: 600;
  letter-spacing: 0.01em;
}

h2 {
  margin: 4px 0 0;
  color: #e2e8f0;
}

.grid {
  display: grid;
  gap: 18px;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
}

.pill {
  background: rgba(129, 140, 248, 0.2);
  color: #c7d2fe;
  padding: 6px 10px;
  border-radius: 999px;
  font-weight: 700;
}

.muted {
  color: rgba(226, 232, 240, 0.65);
}

.error {
  color: #fecaca;
  background: rgba(248, 113, 113, 0.15);
  padding: 12px;
  border-radius: 12px;
  border: 1px solid rgba(248, 113, 113, 0.4);
}

.fade-stagger-enter-active,
.fade-stagger-leave-active {
  transition: opacity 0.35s ease, transform 0.35s ease;
}

.fade-stagger-enter-from {
  opacity: 0;
  transform: translateY(12px);
}

.fade-stagger-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
