import { createRouter, createWebHistory } from 'vue-router';
import pinia from '@/store';
import { useAuthStore } from '@/store/auth';

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/HomePage.vue'),
    meta: { title: '首页' }
  },
  {
    path: '/auth',
    name: 'Auth',
    component: () => import('@/views/AuthPage.vue'),
    meta: { title: '登录 / 注册', guestOnly: true }
  },
  {
    path: '/movies',
    name: 'Movies',
    component: () => import('@/views/MoviesPage.vue'),
    meta: { title: '电影库' }
  },
  {
    path: '/recommendations',
    name: 'Recommendations',
    component: () => import('@/views/RecommendationsPage.vue'),
    meta: { title: '个性化推荐', requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/UserProfilePage.vue'),
    meta: { title: '我的评分', requiresAuth: true }
  },
  {
    path: '/metrics',
    name: 'Metrics',
    component: () => import('@/views/MetricsPage.vue'),
    meta: { title: '系统监控', requiresAuth: true, roles: ['ADMIN'] }
  },
  {
    path: '/about',
    name: 'About',
    component: () => import('@/views/AboutPage.vue'),
    meta: { title: '关于系统' }
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition;
    }
    return { top: 0 };
  }
});

router.beforeEach(async (to) => {
  const authStore = useAuthStore(pinia);
  document.title = to.meta.title ? `${to.meta.title} - 电影推荐系统` : '电影推荐系统';

  if (authStore.token && !authStore.user) {
    try {
      await authStore.fetchCurrentUser();
    } catch (error) {
      return {
        name: 'Auth',
        query: { redirect: to.fullPath }
      };
    }
  }

  if (to.meta.guestOnly && authStore.isAuthenticated) {
    return { name: 'Recommendations' };
  }

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return {
      name: 'Auth',
      query: { redirect: to.fullPath }
    };
  }

  if (to.meta.roles?.length && !to.meta.roles.includes(authStore.user?.role)) {
    return { name: 'Home' };
  }

  return true;
});

export default router;
