<template>
  <nav class="navbar">
    <div class="navbar-container">
      <router-link to="/" class="logo">
        <i class="pi pi-sparkles"></i>
        <span>电影推荐系统</span>
      </router-link>

      <div class="nav-links">
        <router-link to="/" class="nav-link">首页</router-link>
        <router-link to="/movies" class="nav-link">电影库</router-link>
        <router-link v-if="authStore.isAuthenticated" to="/recommendations" class="nav-link">个性化推荐</router-link>
        <router-link v-if="authStore.isAuthenticated" to="/profile" class="nav-link">个人中心</router-link>
        <router-link v-if="authStore.isAdmin" to="/admin" class="nav-link">后台</router-link>
      </div>

      <div class="nav-actions">
        <template v-if="authStore.isAuthenticated">
          <div class="user-chip">
            <span class="name">{{ authStore.user?.username }}</span>
            <span class="role">{{ authStore.user?.role === 'ADMIN' ? '管理员' : '普通用户' }}</span>
          </div>
          <button class="logout-btn" @click="handleLogout">退出</button>
        </template>
        <template v-else>
          <router-link to="/login" class="text-link">登录</router-link>
          <router-link to="/register" class="primary-link">注册</router-link>
        </template>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/store/auth';

const router = useRouter();
const authStore = useAuthStore();

const handleLogout = async () => {
  await authStore.logout();
  router.push('/login');
};
</script>

<style scoped>
.navbar {
  position: sticky;
  top: 0;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(18px);
  border-bottom: 1px solid rgba(226, 232, 240, 0.8);
}

.navbar-container {
  max-width: 1320px;
  margin: 0 auto;
  height: 68px;
  padding: 0 24px;
  display: flex;
  align-items: center;
  gap: 24px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  color: #0f172a;
  font-size: 20px;
  font-weight: 800;
  white-space: nowrap;
}

.logo i {
  color: #ea580c;
}

.nav-links {
  flex: 1;
  display: flex;
  justify-content: center;
  gap: 10px;
}

.nav-link,
.text-link,
.primary-link,
.logout-btn {
  border-radius: 999px;
  padding: 10px 16px;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.2s ease;
}

.nav-link {
  color: #475569;
}

.nav-link.router-link-active {
  background: #fff7ed;
  color: #c2410c;
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-chip {
  display: grid;
  padding: 8px 14px;
  border-radius: 16px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
}

.name {
  font-size: 14px;
  font-weight: 700;
  color: #0f172a;
}

.role {
  font-size: 12px;
  color: #64748b;
}

.text-link {
  color: #475569;
}

.primary-link,
.logout-btn {
  background: linear-gradient(135deg, #ea580c, #f59e0b);
  color: #fff;
  border: none;
  cursor: pointer;
}

@media (max-width: 900px) {
  .navbar-container {
    height: auto;
    padding: 12px 16px;
    flex-wrap: wrap;
  }

  .nav-links {
    order: 3;
    width: 100%;
    justify-content: flex-start;
    overflow-x: auto;
  }
}
</style>
