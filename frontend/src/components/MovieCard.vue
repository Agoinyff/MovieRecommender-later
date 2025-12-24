<template>
  <article class="card" @click="emit('select', movie)">
    <div class="poster">
      <div class="shine"></div>
      <span class="badge" v-if="score !== null">推荐指数 {{ displayScore }}</span>
    </div>
    <div class="content">
      <h3>{{ movie.name }}</h3>
      <p class="meta">
        <span>{{ movie.publishedYear || '未知年份' }}</span>
        <span v-if="movie.genres">· {{ movie.genres }}</span>
      </p>
      <div v-if="score !== null" class="score">
        <div class="stars">
          <span v-for="n in 5" :key="n" :class="['star', { active: n <= starCount }]">★</span>
        </div>
        <div class="bar">
          <div class="fill" :style="{ width: `${percent}%` }"></div>
        </div>
      </div>
    </div>
  </article>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  movie: { type: Object, required: true },
  score: { type: Number, default: null }
});

const emit = defineEmits(['select']);

const percent = computed(() => {
  const raw = Number(props.score ?? 0);
  if (raw <= 1) return Math.round(raw * 100);
  if (raw <= 5) return Math.round((raw / 5) * 100);
  if (raw <= 10) return Math.round((raw / 10) * 100);
  return 100;
});

const starCount = computed(() => Math.max(0, Math.round(percent.value / 20)));

const displayScore = computed(() => {
  if (props.score === null || Number.isNaN(props.score)) return '-';
  return props.score.toFixed(2);
});
</script>

<style scoped>
.card {
  display: grid;
  grid-template-rows: 160px 1fr;
  border-radius: 20px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(148, 163, 184, 0.25);
  backdrop-filter: blur(18px);
  box-shadow: 0 18px 45px rgba(15, 23, 42, 0.2);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  cursor: pointer;
}

.card:hover {
  transform: translateY(-4px);
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.28);
}

.poster {
  position: relative;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.5), rgba(14, 165, 233, 0.55));
}

.shine {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at top left, rgba(255, 255, 255, 0.35), transparent 55%);
}

.badge {
  position: absolute;
  bottom: 12px;
  left: 12px;
  background: rgba(15, 23, 42, 0.7);
  color: #e2e8f0;
  padding: 6px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.content {
  padding: 16px;
  color: #e2e8f0;
}

h3 {
  margin: 0 0 6px;
  font-size: 18px;
}

.meta {
  margin: 0 0 12px;
  color: rgba(226, 232, 240, 0.75);
  font-size: 13px;
}

.score {
  display: grid;
  gap: 8px;
}

.stars {
  display: flex;
  gap: 4px;
}

.star {
  color: rgba(226, 232, 240, 0.35);
}

.star.active {
  color: #fbbf24;
}

.bar {
  height: 6px;
  border-radius: 999px;
  background: rgba(226, 232, 240, 0.2);
  overflow: hidden;
}

.fill {
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, #38bdf8, #818cf8);
}
</style>
