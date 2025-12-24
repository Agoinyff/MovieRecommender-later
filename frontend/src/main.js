import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import pinia from './store';

// PrimeVue 核心
import PrimeVue from 'primevue/config';
import 'primevue/resources/themes/lara-light-blue/theme.css';
import 'primeicons/primeicons.css';

// 全局样式
import './assets/base.css';

const app = createApp(App);

app.use(router);
app.use(pinia);
app.use(PrimeVue);

app.mount('#app');
