<template>
  <div class="home-page">
    <section class="hero">
      <div class="hero-copy">
        <p class="eyebrow">正式化电影推荐网站</p>
        <h1>围绕你的评分历史，持续更新专属片单。</h1>
        <p class="subtitle">
          登录后可以维护自己的评分、查看个性化推荐，并在电影详情页直接完成“评分 -> 刷新推荐”的闭环。
        </p>
        <div class="hero-actions">
          <router-link to="/movies" class="secondary-btn">浏览电影库</router-link>
          <router-link v-if="authStore.isAuthenticated" to="/recommendations" class="primary-btn">进入推荐页</router-link>
          <router-link v-else to="/register" class="primary-btn">注册并开始评分</router-link>
        </div>
      </div>
      <div class="hero-panel">
        <div class="panel-card warm">
          <span class="label">推荐策略</span>
          <strong>USER / ITEM / SLOPE ONE</strong>
        </div>
        <div class="panel-card light">
          <span class="label">评分闭环</span>
          <strong>详情页可直接提交评分</strong>
        </div>
        <div class="panel-card accent">
          <span class="label">正式网站化</span>
          <strong>登录、个人中心、管理员权限</strong>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { useAuthStore } from '@/store/auth';

const authStore = useAuthStore();
</script>

<style scoped>
.home-page {
  min-height: calc(100vh - 68px);
  background:
    radial-gradient(circle at top left, rgba(251, 191, 36, 0.18), transparent 24%),
    radial-gradient(circle at bottom right, rgba(234, 88, 12, 0.18), transparent 20%),
    linear-gradient(180deg, #fff7ed, #ffffff 55%);
}

.hero {
  max-width: 1280px;
  margin: 0 auto;
  padding: 72px 24px;
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 40px;
  align-items: center;
}

.eyebrow {
  margin: 0 0 12px;
  color: #c2410c;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

h1 {
  margin: 0;
  font-size: clamp(42px, 6vw, 68px);
  line-height: 1.02;
  color: #111827;
}

.subtitle {
  margin: 24px 0 0;
  max-width: 620px;
  color: #475569;
  font-size: 18px;
  line-height: 1.7;
}

.hero-actions {
  display: flex;
  gap: 14px;
  margin-top: 28px;
}

.primary-btn,
.secondary-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 52px;
  padding: 0 22px;
  border-radius: 16px;
  text-decoration: none;
  font-weight: 800;
}

.primary-btn {
  background: linear-gradient(135deg, #ea580c, #f59e0b);
  color: #fff;
}

.secondary-btn {
  background: #fff;
  border: 1px solid #fed7aa;
  color: #9a3412;
}

.hero-panel {
  display: grid;
  gap: 18px;
}

.panel-card {
  min-height: 148px;
  padding: 24px;
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 18px 60px rgba(15, 23, 42, 0.08);
}

.panel-card.warm {
  background: linear-gradient(135deg, #fff1f2, #fff7ed);
}

.panel-card.light {
  background: linear-gradient(135deg, #ffffff, #fff7ed);
}

.panel-card.accent {
  background: linear-gradient(135deg, #fef3c7, #fdba74);
}

.label {
  display: block;
  margin-bottom: 8px;
  font-size: 13px;
  color: #9a3412;
  font-weight: 700;
}

strong {
  font-size: 28px;
  line-height: 1.2;
  color: #111827;
}

@media (max-width: 900px) {
  .hero {
    grid-template-columns: 1fr;
  }

  .hero-actions {
    flex-direction: column;
  }
}
</style>
