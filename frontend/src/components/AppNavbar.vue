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
        <router-link v-if="authStore.isAuthenticated" to="/profile" class="nav-link">
          <i class="pi pi-user"></i>
          <span>我的评分</span>
        </router-link>
        <router-link v-if="authStore.isAdmin" to="/metrics" class="nav-link admin-link">
          <i class="pi pi-chart-line"></i>
          <span>系统监控</span>
        </router-link>
        <router-link to="/about" class="nav-link">
          <i class="pi pi-info-circle"></i>
          <span>关于</span>
        </router-link>
      </div>

      <div class="nav-side">
        <HealthStatusBadge :status="healthStatus" />

        <template v-if="authStore.isAuthenticated">
          <div class="user-chip">
            <div class="user-avatar">{{ userInitial }}</div>
            <div class="user-meta">
              <span class="user-name">{{ authStore.user.displayName }}</span>
              <span class="user-role">{{ roleText }}</span>
            </div>
          </div>
          <button class="auth-btn ghost" @click="handleLogout">
            <i class="pi pi-sign-out"></i>
            <span>退出</span>
          </button>
        </template>

        <router-link v-else to="/auth" class="auth-btn solid">
          <i class="pi pi-sign-in"></i>
          <span>登录 / 注册</span>
        </router-link>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { getSystemStatus } from '@/api';
import { useAuthStore } from '@/store/auth';
import HealthStatusBadge from './HealthStatusBadge.vue';

const authStore = useAuthStore();
const router = useRouter();
const healthStatus = ref('unknown');
let healthCheckInterval = null;

const userInitial = computed(() => authStore.user?.displayName?.slice(0, 1)?.toUpperCase() || 'U');
const roleText = computed(() => (authStore.isAdmin ? '管理员' : '普通用户'));

const checkHealth = async () => {
  try {
    const status = await getSystemStatus();
    healthStatus.value = status.status === 'ok' ? 'online' : 'error';
  } catch (error) {
    healthStatus.value = 'offline';
  }
};

const handleLogout = () => {
  authStore.logout();
  router.push('/');
};

onMounted(() => {
  checkHealth();
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
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(229, 231, 235, 0.5);
  box-shadow: 0 4px 30px rgba(0, 0, 0, 0.03);
}

.navbar-container {
  max-width: 1460px;
  margin: 0 auto;
  padding: 0 24px;
  min-height: 64px;
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 18px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: 800;
  color: #1f2937;
  text-decoration: none;
  white-space: nowrap;
}

.logo i {
  font-size: 24px;
  color: #6366f1;
}

.nav-links {
  display: flex;
  justify-content: center;
  gap: 6px;
  flex-wrap: wrap;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 15px;
  border-radius: 12px;
  color: #4b5563;
  text-decoration: none;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.2s ease;
}

.nav-link:hover {
  background: rgba(99, 102, 241, 0.08);
  color: #4f46e5;
}

.nav-link.router-link-active {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.22);
}

.admin-link {
  background: rgba(15, 23, 42, 0.04);
}

.nav-side {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-chip {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 12px 6px 8px;
  border-radius: 18px;
  background: rgba(243, 244, 246, 0.92);
}

.user-avatar {
  width: 34px;
  height: 34px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  font-weight: 800;
}

.user-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-name {
  font-size: 13px;
  font-weight: 700;
  color: #111827;
}

.user-role {
  font-size: 12px;
  color: #6b7280;
}

.auth-btn {
  height: 40px;
  padding: 0 16px;
  border-radius: 12px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border: none;
  text-decoration: none;
  font-weight: 700;
  cursor: pointer;
}

.auth-btn.solid {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  box-shadow: 0 6px 18px rgba(99, 102, 241, 0.24);
}

.auth-btn.ghost {
  background: rgba(255, 255, 255, 0.92);
  color: #374151;
  border: 1px solid rgba(209, 213, 219, 0.8);
}

@media (max-width: 1180px) {
  .navbar-container {
    grid-template-columns: 1fr;
    padding: 12px 16px;
  }

  .nav-links {
    justify-content: flex-start;
  }

  .nav-side {
    justify-content: space-between;
    flex-wrap: wrap;
  }
}

@media (max-width: 640px) {
  .logo span,
  .nav-link span,
  .auth-btn span,
  .user-meta {
    display: none;
  }

  .nav-link,
  .auth-btn {
    padding: 0 12px;
  }
}
</style>
