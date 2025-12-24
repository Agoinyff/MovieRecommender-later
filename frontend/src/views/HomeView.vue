<template>
  <section class="hero">
    <div class="hero-text">
      <p class="eyebrow">Movie recommender</p>
      <h1>打造属于你的观影宇宙</h1>
      <p class="lede">
        输入用户 ID，结合多策略协同过滤算法，生成专属推荐，同时支持电影库检索与详情浏览。
      </p>
      <div class="cta">
        <RouterLink to="/recommendations" class="primary">开始推荐</RouterLink>
        <RouterLink to="/library" class="ghost">浏览电影库</RouterLink>
      </div>
    </div>
    <div class="hero-card">
      <h3>系统能力一览</h3>
      <ul>
        <li>用户协同 / 物品协同 / Slope One 三种算法</li>
        <li>支持关键字检索与分页浏览</li>
        <li>玻璃拟态卡片 + 电影详情弹窗</li>
      </ul>
    </div>
  </section>

  <section class="grid">
    <article>
      <h4>个性化推荐</h4>
      <p>从用户历史偏好中挖掘兴趣，提供高匹配度电影。</p>
    </article>
    <article>
      <h4>电影库检索</h4>
      <p>支持关键词搜索与分页展示，快速定位目标影片。</p>
    </article>
    <article>
      <h4>详情弹窗</h4>
      <p>点击卡片即可查看电影详情，减少跳转。</p>
    </article>
  </section>
</template>

<script setup>
import { RouterLink } from 'vue-router';
</script>

<style scoped>
.hero {
  display: grid;
  gap: 24px;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  align-items: center;
  margin-bottom: 32px;
}

.hero-text {
  display: grid;
  gap: 12px;
}

.eyebrow {
  color: rgba(129, 140, 248, 0.95);
  font-weight: 700;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  margin: 0;
}

h1 {
  margin: 0;
  font-size: clamp(28px, 4vw, 40px);
  color: #e2e8f0;
}

.lede {
  margin: 0;
  color: rgba(226, 232, 240, 0.72);
  max-width: 520px;
}

.cta {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.primary,
.ghost {
  text-decoration: none;
  font-weight: 600;
  padding: 12px 20px;
  border-radius: 999px;
}

.primary {
  background: linear-gradient(135deg, #6366f1, #38bdf8);
  color: #fff;
}

.ghost {
  border: 1px solid rgba(148, 163, 184, 0.4);
  color: #e2e8f0;
  background: rgba(15, 23, 42, 0.35);
}

.hero-card {
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 22px;
  padding: 20px;
  color: #e2e8f0;
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.4);
  backdrop-filter: blur(16px);
}

.hero-card h3 {
  margin: 0 0 12px;
}

.hero-card ul {
  margin: 0;
  padding-left: 18px;
  display: grid;
  gap: 8px;
}

.grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
}

.grid article {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 18px;
  padding: 18px;
  color: #e2e8f0;
}

.grid h4 {
  margin: 0 0 6px;
}

.grid p {
  margin: 0;
  color: rgba(226, 232, 240, 0.7);
}
</style>
