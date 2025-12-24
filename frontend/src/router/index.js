/**
 * Vue Router 配置
 */
import { createRouter, createWebHistory } from 'vue-router';

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/HomePage.vue'),
    meta: { title: '首页' }
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
    meta: { title: '个性化推荐' }
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

// 路由守卫：更新页面标题
router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 电影推荐系统` : '电影推荐系统';
  next();
});

export default router;

