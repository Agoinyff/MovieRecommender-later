<template>
  <nav class="navbar">
    <div class="navbar-container">
      <router-link to="/" class="logo">
        <i class="pi pi-sparkles"></i>
        <span>电影推荐系统</span>
      </router-link>

      <div class="nav-links">
        <router-link to="/" class="nav-link">
          <i class="pi pi-home"></i>
          <span>首页</span>
        </router-link>
        <router-link to="/movies" class="nav-link">
          <i class="pi pi-list"></i>
          <span>电影库</span>
        </router-link>
        <router-link to="/recommendations" class="nav-link">
          <i class="pi pi-star"></i>
          <span>个性化推荐</span>
        </router-link>
        <router-link to="/about" class="nav-link">
          <i class="pi pi-info-circle"></i>
          <span>关于</span>
        </router-link>
      </div>
    </div>
  </nav>
</template>

<script setup>
</script>

<style scoped>
.navbar {
  position: sticky;
  top: 0;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(229, 231, 235, 0.5);
  box-shadow: 0 4px 30px rgba(0, 0, 0, 0.03);
}

.navbar-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: 800;
  color: #1f2937;
  text-decoration: none;
  transition: transform 0.2s ease;
}

.logo i {
  font-size: 24px;
  color: #6366f1;
}

.logo:hover {
  transform: scale(1.02);
}

.nav-links {
  display: flex;
  gap: 8px;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  border-radius: 12px;
  color: #4b5563;
  text-decoration: none;
  font-weight: 600;
  font-size: 15px;
  transition: all 0.2s ease;
}

.nav-link i {
  font-size: 16px;
}

.nav-link:hover {
  background: rgba(99, 102, 241, 0.1);
  color: #4f46e5;
}

.nav-link.router-link-active {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

@media (max-width: 768px) {
  .nav-link span {
    display: none;
  }
  
  .nav-links {
    gap: 4px;
  }
  
  .nav-link {
    padding: 10px 12px;
  }
}
</style>

