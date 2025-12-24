<template>
  <div class="home-page">
    <section class="hero-section">
      <div class="hero-content">
        <div class="hero-badge">
          <i class="pi pi-sparkles"></i>
          <span>AI 驱动的个性化推荐</span>
        </div>
        <h1 class="hero-title">发现你的下一部最爱电影</h1>
        <p class="hero-description">
          基于协同过滤算法的智能推荐系统，为你精准推荐符合口味的电影。
          支持用户协同、物品协同和 Slope One 多种推荐算法。
        </p>
        <div class="hero-actions">
          <router-link to="/movies" class="btn-primary">
            <i class="pi pi-list"></i>
            <span>浏览电影库</span>
          </router-link>
          <router-link to="/recommendations" class="btn-secondary">
            <i class="pi pi-star"></i>
            <span>获取推荐</span>
          </router-link>
        </div>
      </div>

      <div class="hero-illustration">
        <div class="floating-card card-1">
          <i class="pi pi-video"></i>
        </div>
        <div class="floating-card card-2">
          <i class="pi pi-star-fill"></i>
        </div>
        <div class="floating-card card-3">
          <i class="pi pi-heart-fill"></i>
        </div>
      </div>
    </section>

    <section class="features-section">
      <h2 class="section-title">系统特性</h2>
      <div class="features-grid">
        <div class="feature-card">
          <div class="feature-icon">
            <i class="pi pi-users"></i>
          </div>
          <h3>用户协同过滤</h3>
          <p>基于相似用户的观影历史，为你推荐可能感兴趣的电影</p>
        </div>

        <div class="feature-card">
          <div class="feature-icon">
            <i class="pi pi-sitemap"></i>
          </div>
          <h3>物品协同过滤</h3>
          <p>分析电影之间的相似性，推荐与你喜欢的电影类似的作品</p>
        </div>

        <div class="feature-card">
          <div class="feature-icon">
            <i class="pi pi-chart-line"></i>
          </div>
          <h3>Slope One 算法</h3>
          <p>使用评分差值预测，提供快速准确的推荐结果</p>
        </div>

        <div class="feature-card">
          <div class="feature-icon">
            <i class="pi pi-search"></i>
          </div>
          <h3>智能检索</h3>
          <p>支持关键词搜索和分页浏览，快速找到你想要的电影</p>
        </div>
      </div>
    </section>

    <section class="cta-section">
      <div class="cta-card">
        <h2>开始探索</h2>
        <p>立即体验智能电影推荐系统，发现更多精彩内容</p>
        <router-link to="/recommendations" class="btn-primary large">
          <i class="pi pi-play"></i>
          <span>立即开始</span>
        </router-link>
      </div>
    </section>
  </div>
</template>

<script setup>
</script>

<style scoped>
.home-page {
  min-height: calc(100vh - 64px);
}

.hero-section {
  max-width: 1200px;
  margin: 0 auto;
  padding: 80px 24px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 60px;
  align-items: center;
}

.hero-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.1), rgba(139, 92, 246, 0.1));
  border: 1px solid rgba(99, 102, 241, 0.2);
  border-radius: 30px;
  color: #6366f1;
  font-weight: 600;
  font-size: 14px;
  width: fit-content;
}

.hero-title {
  margin: 0;
  font-size: 56px;
  font-weight: 900;
  line-height: 1.1;
  background: linear-gradient(135deg, #1f2937 0%, #4f46e5 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-description {
  margin: 0;
  font-size: 18px;
  line-height: 1.7;
  color: #6b7280;
}

.hero-actions {
  display: flex;
  gap: 16px;
  margin-top: 12px;
}

.btn-primary,
.btn-secondary {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 14px 28px;
  border-radius: 14px;
  font-weight: 700;
  font-size: 16px;
  text-decoration: none;
  transition: all 0.3s ease;
}

.btn-primary {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  box-shadow: 0 8px 24px rgba(99, 102, 241, 0.3);
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 32px rgba(99, 102, 241, 0.4);
}

.btn-secondary {
  background: rgba(255, 255, 255, 0.9);
  color: #4f46e5;
  border: 2px solid rgba(99, 102, 241, 0.2);
  backdrop-filter: blur(10px);
}

.btn-secondary:hover {
  background: rgba(255, 255, 255, 1);
  border-color: rgba(99, 102, 241, 0.4);
  transform: translateY(-2px);
}

.btn-primary.large {
  padding: 18px 36px;
  font-size: 18px;
}

.hero-illustration {
  position: relative;
  height: 500px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.floating-card {
  position: absolute;
  width: 160px;
  height: 220px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(229, 231, 235, 0.5);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 64px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
  animation: float 6s ease-in-out infinite;
}

.card-1 {
  top: 50px;
  left: 50px;
  color: #6366f1;
  animation-delay: 0s;
}

.card-2 {
  top: 150px;
  right: 80px;
  color: #f59e0b;
  animation-delay: 2s;
}

.card-3 {
  bottom: 80px;
  left: 120px;
  color: #ec4899;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(5deg);
  }
}

.features-section {
  max-width: 1200px;
  margin: 0 auto;
  padding: 80px 24px;
  background: rgba(249, 250, 251, 0.5);
}

.section-title {
  text-align: center;
  font-size: 42px;
  font-weight: 800;
  margin: 0 0 60px;
  color: #1f2937;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 24px;
}

.feature-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(229, 231, 235, 0.6);
  border-radius: 20px;
  padding: 32px;
  text-align: center;
  transition: all 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 50px rgba(99, 102, 241, 0.15);
  border-color: rgba(99, 102, 241, 0.3);
}

.feature-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 20px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  color: #fff;
  box-shadow: 0 10px 30px rgba(99, 102, 241, 0.3);
}

.feature-card h3 {
  margin: 0 0 12px;
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
}

.feature-card p {
  margin: 0;
  color: #6b7280;
  line-height: 1.6;
}

.cta-section {
  max-width: 1200px;
  margin: 0 auto;
  padding: 80px 24px;
}

.cta-card {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 30px;
  padding: 80px 40px;
  text-align: center;
  box-shadow: 0 20px 60px rgba(99, 102, 241, 0.3);
}

.cta-card h2 {
  margin: 0 0 16px;
  font-size: 42px;
  font-weight: 800;
  color: #fff;
}

.cta-card p {
  margin: 0 0 32px;
  font-size: 18px;
  color: rgba(255, 255, 255, 0.9);
}

.cta-card .btn-primary {
  background: #fff;
  color: #6366f1;
}

.cta-card .btn-primary:hover {
  background: rgba(255, 255, 255, 0.95);
}

@media (max-width: 768px) {
  .hero-section {
    grid-template-columns: 1fr;
    padding: 40px 24px;
    gap: 40px;
  }

  .hero-title {
    font-size: 36px;
  }

  .hero-illustration {
    height: 300px;
  }

  .floating-card {
    width: 100px;
    height: 140px;
    font-size: 40px;
  }

  .hero-actions {
    flex-direction: column;
  }

  .section-title {
    font-size: 32px;
  }

  .cta-card h2 {
    font-size: 32px;
  }
}
</style>

