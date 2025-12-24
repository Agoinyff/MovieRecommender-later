<template>
  <main class="page">
    <header class="hero">
      <div>
        <p class="eyebrow">Movie recommender</p>
        <h1>Get personalized movie suggestions</h1>
        <p class="lede">
          输入用户 ID，选择算法（用户协同、物品协同或 Slope One），即可查看 Spring Boot 后端生成的推荐结果。
        </p>
      </div>
      <div class="status" v-if="connectionInfo">
        <span class="dot" :class="connectionInfo.status"></span>
        <span>{{ connectionInfo.message }}</span>
      </div>
    </header>

    <section class="content">
      <RecommendationForm
        :initial-user="demoUserId"
        :loading="loading"
        @submit="onSubmit"
      />

      <RecommendationsList
        :recommendations="recommendations"
        :loading="loading"
        :error="error"
      />
    </section>
  </main>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import axios from 'axios';
import RecommendationForm from './components/RecommendationForm.vue';
import RecommendationsList from './components/RecommendationsList.vue';

const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080/api';
const loading = ref(false);
const recommendations = ref([]);
const error = ref('');
const demoUserId = ref(100);
const connectionInfo = ref(null);

const onSubmit = async ({ userId, strategy, size }) => {
  loading.value = true;
  error.value = '';
  try {
    const response = await axios.get(`${API_BASE}/recommendations`, {
      params: { userId, strategy, size }
    });
    recommendations.value = response.data;
  } catch (err) {
    error.value = err?.response?.data?.message || err.message || '无法获取推荐结果';
    recommendations.value = [];
  } finally {
    loading.value = false;
  }
};

onMounted(async () => {
  try {
    const health = await axios.get(`${API_BASE}/movies`, { params: { size: 1 } });
    connectionInfo.value = { status: 'ok', message: `后端已连接（${health.data.totalElements || '无数据'} 条电影）` };
  } catch (err) {
    connectionInfo.value = { status: 'warn', message: '无法连接后端，请检查 8080 端口和数据库' };
  }
});
</script>

<style scoped>
.page {
  max-width: 1100px;
  margin: 0 auto;
  padding: 32px 24px 48px;
}

.hero {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 24px;
}

.eyebrow {
  color: #6366f1;
  font-weight: 700;
  letter-spacing: 0.02em;
  margin: 0 0 4px;
}

h1 {
  margin: 0 0 8px;
  font-size: 32px;
}

.lede {
  margin: 0;
  color: #4b5563;
  max-width: 720px;
}

.content {
  display: grid;
  grid-template-columns: 1fr;
  gap: 16px;
}

.status {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  background: #eef2ff;
  border-radius: 12px;
  color: #312e81;
  font-weight: 600;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  background: #22c55e;
}

.dot.warn {
  background: #f59e0b;
}

@media (min-width: 900px) {
  .content {
    grid-template-columns: 360px 1fr;
    align-items: start;
  }
}
</style>
