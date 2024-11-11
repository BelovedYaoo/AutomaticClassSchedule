import { createApp } from 'vue';
import { createPinia } from 'pinia';

import App from './App.vue';
import router from '@/service/router';

import './assets/styles.scss';
import PrimeVueImportAll from '@/service/PrimeVueImportAll';

//全局函数模块
import globalQuotePlugin from '@/service/globalQuotePlugin.ts';

const app = createApp(App);
const pinia = createPinia();

// 导入所有PrimeVue组件
PrimeVueImportAll(app);

app.use(pinia);
app.use(router);
app.use(globalQuotePlugin);

app.mount('#app');
