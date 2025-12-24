import { createRouter, createWebHistory } from 'vue-router';
import HomeView from '../views/HomeView.vue';
import RecommendationsView from '../views/RecommendationsView.vue';
import MovieLibraryView from '../views/MovieLibraryView.vue';
import AboutView from '../views/AboutView.vue';

const routes = [
  { path: '/', name: 'home', component: HomeView },
  { path: '/recommendations', name: 'recommendations', component: RecommendationsView },
  { path: '/library', name: 'library', component: MovieLibraryView },
  { path: '/about', name: 'about', component: AboutView }
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 };
  }
});

export default router;
