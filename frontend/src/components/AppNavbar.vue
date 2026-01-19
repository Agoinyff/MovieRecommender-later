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
        <router-link to="/profile" class="nav-link">
          <i class="pi pi-user"></i>
          <span>我的评分</span>
        </router-link>
        <router-link to="/metrics" class="nav-link">
          <i class="pi pi-chart-line"></i>
          <span>系统监控</span>
        </router-link>
        <router-link to="/about" class="nav-link">
          <i class="pi pi-info-circle"></i>
          <span>关于</span>
        </router-link>
      </div>

      <div class="nav-status">
        <HealthStatusBadge :status="healthStatus" />
      </div>
    </div>
  </nav>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { getSystemStatus } from '@/api';
import HealthStatusBadge from './HealthStatusBadge.vue';

const healthStatus = ref('unknown');
let healthCheckInterval = null;

const checkHealth = async () => {
  try {
    const status = await getSystemStatus();
    healthStatus.value = status.status === 'ok' ? 'online' : 'error';
  } catch (err) {
    healthStatus.value = 'offline';
  }
};

onMounted(() => {
  checkHealth();
  // Check health every 30 seconds
  healthCheckInterval = setInterval(checkHealth, 30000);
});

onUnmounted(() => {
  if (healthCheckInterval) {
    clearInterval(healthCheckInterval);
  }
});
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
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
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
  white-space: nowrap;
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
  gap: 6px;
  flex: 1;
  justify-content: center;
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
  font-size: 14px;
  transition: all 0.2s ease;
  white-space: nowrap;
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

.nav-status {
  display: flex;
  align-items: center;
}

@media (max-width: 1024px) {
  .nav-link {
    padding: 10px 12px;
    font-size: 13px;
  }
}

@media (max-width: 768px) {
  .navbar-container {
    padding: 0 16px;
  }

  .logo span {
    display: none;
  }

  .nav-links {
    gap: 2px;
    flex-wrap: wrap;
    justify-content: flex-end;
  }
  
  .nav-link span {
    display: none;
  }
  
  .nav-link {
    padding: 10px;
  }

  .nav-status {
    display: none;
  }
}
</style>

