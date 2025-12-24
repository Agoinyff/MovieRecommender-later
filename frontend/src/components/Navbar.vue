<template>
  <nav class="navbar">
    <div class="brand">
      <span class="logo">🎬</span>
      <div>
        <p class="title">MovieLens Lab</p>
        <p class="subtitle">毕业设计 · 智能推荐系统</p>
      </div>
    </div>
    <div class="links">
      <RouterLink to="/" class="link">首页</RouterLink>
      <RouterLink to="/recommendations" class="link">个性化推荐</RouterLink>
      <RouterLink to="/library" class="link">电影库</RouterLink>
      <RouterLink to="/about" class="link">关于系统</RouterLink>
    </div>
  </nav>
</template>

<script setup>
import { RouterLink } from 'vue-router';
</script>

<style scoped>
.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 32px;
  gap: 16px;
  position: sticky;
  top: 0;
  z-index: 20;
  backdrop-filter: blur(18px);
  background: rgba(15, 23, 42, 0.55);
  border-bottom: 1px solid rgba(148, 163, 184, 0.2);
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #e2e8f0;
}

.logo {
  font-size: 28px;
}

.title {
  margin: 0;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.subtitle {
  margin: 0;
  font-size: 12px;
  color: rgba(226, 232, 240, 0.75);
}

.links {
  display: flex;
  align-items: center;
  gap: 18px;
  flex-wrap: wrap;
}

.link {
  color: rgba(226, 232, 240, 0.88);
  text-decoration: none;
  padding: 8px 12px;
  border-radius: 999px;
  font-weight: 600;
  transition: background 0.2s ease, color 0.2s ease;
}

.link:hover,
.link.router-link-active {
  background: rgba(148, 163, 184, 0.2);
  color: #fff;
}

@media (max-width: 860px) {
  .navbar {
    flex-direction: column;
    align-items: flex-start;
  }

  .links {
    width: 100%;
  }
}
</style>
