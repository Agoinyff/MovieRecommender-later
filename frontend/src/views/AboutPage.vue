<template>
  <div class="about-page">
    <div class="page-container">
      <header class="page-header">
        <div class="header-icon">
          <i class="pi pi-info-circle"></i>
        </div>
        <h1 class="page-title">关于系统</h1>
        <p class="page-description">基于协同过滤算法的智能电影推荐系统</p>
      </header>

      <div class="content-grid">
        <div class="info-card">
          <div class="card-icon">
            <i class="pi pi-code"></i>
          </div>
          <h3>技术栈</h3>
          <ul class="tech-list">
            <li>
              <span class="tech-label">前端框架</span>
              <span class="tech-value">Vue 3 + Vite</span>
            </li>
            <li>
              <span class="tech-label">UI 组件库</span>
              <span class="tech-value">PrimeVue</span>
            </li>
            <li>
              <span class="tech-label">路由管理</span>
              <span class="tech-value">Vue Router</span>
            </li>
            <li>
              <span class="tech-label">状态管理</span>
              <span class="tech-value">Pinia</span>
            </li>
            <li>
              <span class="tech-label">后端框架</span>
              <span class="tech-value">Spring Boot</span>
            </li>
            <li>
              <span class="tech-label">数据库</span>
              <span class="tech-value">MySQL / PostgreSQL</span>
            </li>
          </ul>
        </div>

        <div class="info-card">
          <div class="card-icon">
            <i class="pi pi-chart-line"></i>
          </div>
          <h3>推荐算法</h3>
          <div class="algorithm-grid">
            <div class="algorithm-item">
              <h4>用户协同过滤</h4>
              <p>基于用户相似度的推荐算法，通过找到与目标用户兴趣相似的其他用户，推荐这些用户喜欢的电影。</p>
            </div>
            <div class="algorithm-item">
              <h4>物品协同过滤</h4>
              <p>基于物品相似度的推荐算法，通过计算电影之间的相似性，推荐与用户已喜欢电影相似的其他电影。</p>
            </div>
            <div class="algorithm-item">
              <h4>Slope One</h4>
              <p>基于评分差值的预测算法，通过计算物品之间的平均评分差来预测用户对未评分物品的评分。</p>
            </div>
          </div>
        </div>

        <div class="info-card">
          <div class="card-icon">
            <i class="pi pi-star"></i>
          </div>
          <h3>系统特点</h3>
          <ul class="feature-list">
            <li>
              <i class="pi pi-check-circle"></i>
              <span>多种推荐算法支持</span>
            </li>
            <li>
              <i class="pi pi-check-circle"></i>
              <span>实时推荐结果生成</span>
            </li>
            <li>
              <i class="pi pi-check-circle"></i>
              <span>电影库搜索与浏览</span>
            </li>
            <li>
              <i class="pi pi-check-circle"></i>
              <span>响应式界面设计</span>
            </li>
            <li>
              <i class="pi pi-check-circle"></i>
              <span>分页加载优化</span>
            </li>
            <li>
              <i class="pi pi-check-circle"></i>
              <span>卡片式视觉展示</span>
            </li>
          </ul>
        </div>

        <div class="info-card full-width">
          <div class="card-icon">
            <i class="pi pi-book"></i>
          </div>
          <h3>使用说明</h3>
          <div class="usage-steps">
            <div class="step">
              <div class="step-number">1</div>
              <div class="step-content">
                <h4>浏览电影库</h4>
                <p>在"电影库"页面中，你可以浏览所有电影，使用搜索框查找特定电影，支持分页浏览。</p>
              </div>
            </div>
            <div class="step">
              <div class="step-number">2</div>
              <div class="step-content">
                <h4>获取推荐</h4>
                <p>在"个性化推荐"页面中，输入用户 ID，选择推荐算法，系统将为你生成个性化的电影推荐列表。</p>
              </div>
            </div>
            <div class="step">
              <div class="step-number">3</div>
              <div class="step-content">
                <h4>查看详情</h4>
                <p>点击任意电影卡片，可以查看电影的详细信息，包括年份、类型、推荐指数等。</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <footer class="page-footer">
        <p>© 2024 电影推荐系统 · 毕业设计项目</p>
      </footer>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.about-page {
  min-height: calc(100vh - 64px);
  background: linear-gradient(180deg, rgba(249, 250, 251, 0.5) 0%, rgba(255, 255, 255, 0.8) 100%);
}

.page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 60px 24px;
}

.page-header {
  text-align: center;
  margin-bottom: 60px;
}

.header-icon {
  width: 100px;
  height: 100px;
  margin: 0 auto 24px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 25px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  color: #fff;
  box-shadow: 0 20px 50px rgba(99, 102, 241, 0.3);
}

.page-title {
  margin: 0 0 16px;
  font-size: 48px;
  font-weight: 900;
  color: #1f2937;
}

.page-description {
  margin: 0;
  font-size: 18px;
  color: #6b7280;
}

.content-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
  margin-bottom: 60px;
}

.info-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(229, 231, 235, 0.6);
  border-radius: 20px;
  padding: 32px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

.info-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 20px 60px rgba(99, 102, 241, 0.1);
  border-color: rgba(99, 102, 241, 0.2);
}

.info-card.full-width {
  grid-column: 1 / -1;
}

.card-icon {
  width: 60px;
  height: 60px;
  margin-bottom: 20px;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.1), rgba(139, 92, 246, 0.1));
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #6366f1;
}

.info-card h3 {
  margin: 0 0 20px;
  font-size: 24px;
  font-weight: 800;
  color: #1f2937;
}

.tech-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  gap: 12px;
}

.tech-list li {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: rgba(249, 250, 251, 0.8);
  border-radius: 12px;
  border: 1px solid rgba(229, 231, 235, 0.6);
}

.tech-label {
  font-weight: 600;
  color: #6b7280;
  font-size: 14px;
}

.tech-value {
  font-weight: 700;
  color: #1f2937;
  font-size: 14px;
}

.algorithm-grid {
  display: grid;
  gap: 16px;
}

.algorithm-item {
  padding: 20px;
  background: rgba(249, 250, 251, 0.8);
  border-radius: 12px;
  border: 1px solid rgba(229, 231, 235, 0.6);
}

.algorithm-item h4 {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 700;
  color: #1f2937;
}

.algorithm-item p {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
  color: #6b7280;
}

.feature-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  gap: 12px;
}

.feature-list li {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: rgba(249, 250, 251, 0.8);
  border-radius: 12px;
  border: 1px solid rgba(229, 231, 235, 0.6);
  font-weight: 500;
  color: #1f2937;
}

.feature-list i {
  color: #22c55e;
  font-size: 18px;
}

.usage-steps {
  display: grid;
  gap: 24px;
}

.step {
  display: flex;
  gap: 20px;
  padding: 24px;
  background: rgba(249, 250, 251, 0.8);
  border-radius: 12px;
  border: 1px solid rgba(229, 231, 235, 0.6);
}

.step-number {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 800;
  color: #fff;
  box-shadow: 0 8px 20px rgba(99, 102, 241, 0.3);
}

.step-content h4 {
  margin: 0 0 8px;
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
}

.step-content p {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
  color: #6b7280;
}

.page-footer {
  text-align: center;
  padding: 32px 0;
  border-top: 1px solid rgba(229, 231, 235, 0.6);
}

.page-footer p {
  margin: 0;
  color: #9ca3af;
  font-size: 14px;
}

@media (max-width: 768px) {
  .content-grid {
    grid-template-columns: 1fr;
  }

  .page-title {
    font-size: 36px;
  }

  .header-icon {
    width: 80px;
    height: 80px;
    font-size: 40px;
  }

  .info-card {
    padding: 24px;
  }

  .step {
    flex-direction: column;
  }
}
</style>

